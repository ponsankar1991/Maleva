package my.maleva.api.module.invoice.view;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * The Sale Invoice view grid — the port of the legacy
 * {@code SaleInvoiceServices.SelectSaleInvoice}.
 *
 * <p>The legacy method glued the filter values straight into SQL. The
 * conditions are the same here, but every value is a bound parameter, so an
 * apostrophe in a vessel name no longer breaks the query and a search box
 * can no longer inject SQL.
 *
 * <p>Two legacy behaviours are kept on purpose because operators rely on
 * them: an exact-number search ignores every other filter including the
 * date range, and the "not pushed to QNE" tick shows every unpushed invoice
 * regardless of date or customer. One legacy bug is not kept: in that
 * unpushed mode the rows were sorted by the dd/MM/yyyy display string, which
 * put 01/12/2025 before 02/01/2025; they are sorted by the real date now.
 */
@Service
@RequiredArgsConstructor
public class SaleInvoiceViewService {

    private final NamedParameterJdbcTemplate jdbc;

    @Transactional(readOnly = true)
    public SaleInvoiceViewResult view(SaleInvoiceViewFilter filter) {
        if (filter == null || filter.getCompanyId() == null || filter.getCompanyId() <= 0) {
            throw new IllegalArgumentException("Company is required");
        }
        Query where = where(filter);

        String detaSelect = filter.isEta() && isBlank(filter.getSearch()) && !filter.isUnpushedOnly()
                ? switch (filter.etaTypeOrDefault()) {
                    case 1 -> ", ISNULL(FORMAT(A.OETA, 'dd/MM/yyyy HH:mm:ss'), '') AS DETA";
                    case 2 -> ", ISNULL(FORMAT(A.ETA, 'dd/MM/yyyy HH:mm:ss'), '') AS DETA";
                    default -> ", ISNULL(FORMAT(ISNULL(A.ETA, A.OETA), 'dd/MM/yyyy HH:mm:ss'), '') AS DETA";
                }
                : ", NULL AS DETA";

        String masterSql = """
                SELECT A.Id, A.CNumber AS BillNo, A.CNumberDisplay AS BillNoDisplay,
                       FORMAT(ISNULL(A.SaleDate, '1900-01-01'), 'dd/MM/yyyy') AS BillDate,
                       FORMAT(ISNULL(A.Created_Date, '1900-01-01'), 'dd/MM/yyyy hh:mm:ss') AS BillTime,
                       B.CustomerName, ISNULL(E.EmployeeName, '') AS EmployeeName,
                       ISNULL(J.Name, '') AS JobStatus, A.JobMasterRefId,
                       ISNULL(SO.CNumberDisplay, '') AS JobNo, A.SaleOrderMasterNo, A.SaleType,
                       A.Amount AS NetAmt, A.ActualNetAmount AS Amount,
                       A.QNECode, A.QNEId, A.EInvoiceUid,
                       A.Loadingvesselname, A.Offvesselname, A.SPort, A.OPort,
                       ISNULL(FORMAT(A.ETA, 'dd/MM/yyyy HH:mm:ss'), '') AS SETA,
                       ISNULL(FORMAT(A.ETB, 'dd/MM/yyyy HH:mm:ss'), '') AS SETB,
                       ISNULL(FORMAT(A.OETA, 'dd/MM/yyyy HH:mm:ss'), '') AS SOETA,
                       ISNULL(FORMAT(A.OETB, 'dd/MM/yyyy HH:mm:ss'), '') AS SOETB,
                       ISNULL(CONVERT(VARCHAR(26), A.PickupDate, 20), '') AS SPickupDate
                """ + detaSelect + """

                FROM SaleMaster A WITH (NOLOCK)
                INNER JOIN Customer B WITH (NOLOCK) ON A.CustomerRefId = B.Id
                LEFT JOIN EmployeeMaster E WITH (NOLOCK) ON E.Id = A.EmployeeRefId
                LEFT JOIN JobStatusMaster J WITH (NOLOCK) ON J.Id = A.JStatus
                LEFT JOIN SaleOrderMaster SO WITH (NOLOCK) ON SO.Id = A.SaleOrderMasterNo
                WHERE A.CompanyRefId = :companyId AND A.Active = 1
                """ + where.sql()
                + (filter.isUnpushedOnly() ? " ORDER BY A.SaleDate, A.Id" : " ORDER BY A.Id DESC");

        String detailSql = """
                SELECT B.SaleMasterRefId AS SaleRefId, I.Prod_Code AS ProductCode, I.PName AS ProductName,
                       B.SDRemarks, B.SalesRate AS SaleRate, B.ItemQty, B.MRP, B.TaxPercent,
                       A.TaxAmount AS TaxAmt, B.DiscPer AS DiscountPercent, B.DiscAmount AS DiscountAmt,
                       B.Amount AS SAmount, ISNULL(B.CurrencyValue, 0) AS CurrencyValue,
                       ISNULL(B.ActualAmount, 0) AS ActualAmount,
                       ISNULL(B.SaleOrderMasterRefId, 0) AS SaleOrderMasterRefId,
                       ISNULL(SO.CNumberDisplay, '') AS SaleOrderMasterNoDisplay
                FROM SaleDetails B WITH (NOLOCK)
                INNER JOIN SaleMaster A WITH (NOLOCK) ON B.SaleMasterRefId = A.Id
                INNER JOIN ItemMaster I WITH (NOLOCK) ON B.ItemMasterRefId = I.Id
                LEFT JOIN SaleOrderMaster SO WITH (NOLOCK) ON SO.Id = B.SaleOrderMasterRefId
                WHERE A.CompanyRefId = :companyId AND A.Active = 1
                """ + where.sql() + " ORDER BY B.Id";

        List<SaleInvoiceViewRow> master = jdbc.query(masterSql, where.params(), (rs, i) -> masterRow(rs));
        List<SaleInvoiceViewDetailRow> details = jdbc.query(detailSql, where.params(), (rs, i) -> detailRow(rs));
        return new SaleInvoiceViewResult(master, details);
    }

    /** The WHERE fragment (starting with AND) and its bound values. Package-private for the tests. */
    static Query where(SaleInvoiceViewFilter f) {
        MapSqlParameterSource p = new MapSqlParameterSource("companyId", f.getCompanyId());

        // Legacy: "not pushed to QNE" replaced every other condition.
        if (f.isUnpushedOnly()) {
            return new Query(" AND ISNULL(A.QNECode, '') = ''", p);
        }
        // Legacy: an exact-number search replaced every other condition.
        if (!isBlank(f.getSearch())) {
            p.addValue("search", f.getSearch().trim());
            return new Query(f.isSearchByJobNo()
                    ? " AND SO.CNumberDisplay = :search"
                    : " AND A.CNumberDisplay = :search", p);
        }

        StringBuilder sql = new StringBuilder();
        if (positive(f.getCustomerId())) {
            sql.append(" AND A.CustomerRefId = :customerId");
            p.addValue("customerId", f.getCustomerId());
        }
        if (positive(f.getJobTypeId())) {
            sql.append(" AND A.JobMasterRefId = :jobTypeId");
            p.addValue("jobTypeId", f.getJobTypeId());
        }
        if (positive(f.getEmployeeId())) {
            sql.append(" AND A.EmployeeRefId = :employeeId");
            p.addValue("employeeId", f.getEmployeeId());
        }
        if (positive(f.getStatusId())) {
            sql.append(" AND A.JStatus = :statusId");
            p.addValue("statusId", f.getStatusId());
        }
        if (f.isHideCompleted()) {
            sql.append(" AND A.JStatus <> 8");
        }
        if (f.remarksFilterOrDefault() == 1) {
            sql.append(" AND A.Remarks <> ''");
        } else if (f.remarksFilterOrDefault() == 2) {
            sql.append(" AND A.Remarks = ''");
        }
        if (!isBlank(f.getOffVesselName())) {
            sql.append(" AND A.Offvesselname LIKE :offVessel");
            p.addValue("offVessel", "%" + f.getOffVesselName().trim() + "%");
        }
        if (!isBlank(f.getLoadingVesselName())) {
            sql.append(" AND A.Loadingvesselname LIKE :loadingVessel");
            p.addValue("loadingVessel", "%" + f.getLoadingVesselName().trim() + "%");
        }

        LocalDate from = f.getFromDate();
        LocalDate to = f.getToDate();
        if (from == null || to == null) {
            throw new IllegalArgumentException("From date and To date are required");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("To date must not be before From date");
        }
        p.addValue("fromDate", from);
        p.addValue("toDate", to);
        if (f.isEta()) {
            sql.append(switch (f.etaTypeOrDefault()) {
                case 1 -> " AND CAST(A.OETA AS date) BETWEEN :fromDate AND :toDate";
                case 2 -> " AND CAST(A.ETA AS date) BETWEEN :fromDate AND :toDate";
                default -> " AND (CAST(A.ETA AS date) BETWEEN :fromDate AND :toDate"
                        + " OR CAST(A.OETA AS date) BETWEEN :fromDate AND :toDate)";
            });
        } else if (f.isPickup()) {
            sql.append(" AND CAST(A.PickupDate AS date) BETWEEN :fromDate AND :toDate");
        } else {
            sql.append(" AND A.SaleDate BETWEEN :fromDate AND :toDate");
        }
        return new Query(sql.toString(), p);
    }

    private static SaleInvoiceViewRow masterRow(ResultSet rs) throws SQLException {
        return SaleInvoiceViewRow.builder()
                .id(rs.getInt("Id"))
                .billNo(nullableInt(rs, "BillNo"))
                .billNoDisplay(rs.getString("BillNoDisplay"))
                .billDate(rs.getString("BillDate"))
                .billTime(rs.getString("BillTime"))
                .customerName(rs.getString("CustomerName"))
                .employeeName(rs.getString("EmployeeName"))
                .jobStatus(rs.getString("JobStatus"))
                .jobMasterRefId(nullableInt(rs, "JobMasterRefId"))
                .jobNo(rs.getString("JobNo"))
                .saleOrderMasterNo(nullableInt(rs, "SaleOrderMasterNo"))
                .saleType(rs.getString("SaleType"))
                .netAmt(nullableDouble(rs, "NetAmt"))
                .amount(nullableDouble(rs, "Amount"))
                .qneCode(rs.getString("QNECode"))
                .qneId(rs.getString("QNEId"))
                .eInvoiceUid(rs.getString("EInvoiceUid"))
                .loadingVesselName(rs.getString("Loadingvesselname"))
                .offVesselName(rs.getString("Offvesselname"))
                .sPort(rs.getString("SPort"))
                .oPort(rs.getString("OPort"))
                .seta(rs.getString("SETA"))
                .setb(rs.getString("SETB"))
                .soeta(rs.getString("SOETA"))
                .soetb(rs.getString("SOETB"))
                .sPickupDate(rs.getString("SPickupDate"))
                .deta(rs.getString("DETA"))
                .build();
    }

    private static SaleInvoiceViewDetailRow detailRow(ResultSet rs) throws SQLException {
        return SaleInvoiceViewDetailRow.builder()
                .saleRefId(nullableInt(rs, "SaleRefId"))
                .productCode(rs.getString("ProductCode"))
                .productName(rs.getString("ProductName"))
                .sdRemarks(rs.getString("SDRemarks"))
                .saleRate(nullableDouble(rs, "SaleRate"))
                .itemQty(nullableDouble(rs, "ItemQty"))
                .mrp(nullableDouble(rs, "MRP"))
                .taxPercent(nullableDouble(rs, "TaxPercent"))
                .taxAmt(nullableDouble(rs, "TaxAmt"))
                .discountPercent(nullableDouble(rs, "DiscountPercent"))
                .discountAmt(nullableDouble(rs, "DiscountAmt"))
                .sAmount(nullableDouble(rs, "SAmount"))
                .currencyValue(nullableDouble(rs, "CurrencyValue"))
                .actualAmount(nullableDouble(rs, "ActualAmount"))
                .saleOrderMasterRefId(nullableInt(rs, "SaleOrderMasterRefId"))
                .saleOrderMasterNoDisplay(rs.getString("SaleOrderMasterNoDisplay"))
                .build();
    }

    private static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static Double nullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private static boolean positive(Integer value) {
        return value != null && value > 0;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** A WHERE fragment and its parameters. */
    record Query(String sql, MapSqlParameterSource params) {
    }
}
