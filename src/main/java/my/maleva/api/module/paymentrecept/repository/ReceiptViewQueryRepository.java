package my.maleva.api.module.paymentrecept.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.paymentrecept.dto.ReceiptSearchRequest;
import my.maleva.api.module.paymentrecept.dto.ReceiptViewDetailDto;
import my.maleva.api.module.paymentrecept.dto.ReceiptViewRowDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * The RECEIPT ENTRY VIEW queries — the port of legacy
 * {@code ReceiptServices.SelectReceipt}, parameterised.
 *
 * <p>Kept from legacy: an exact receipt number search drops every other
 * filter, including the dates; rows are ordered by customer name. Dropped:
 * three correlated sub-selects (TotalAmount, TotalBalance, Balance) that ran
 * the whole-company balance function per row against dates hard-coded in
 * 2025 and were never shown. Fixed: "Received By" printed blank in legacy
 * because the SELECT aliased the bank as {@code BankName1} while the model
 * only had {@code BankName}; this returns {@code BankMaster.Name}, which is
 * what the Payment By dropdown shows (legacy GetBank: {@code Name as AccountName}).
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ReceiptViewQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private static final String FROM_WHERE =
            " FROM Receipt A WITH (NOLOCK)"
            + " INNER JOIN Customer B WITH (NOLOCK) ON A.CustomerRefId = B.Id"
            + " LEFT JOIN EmployeeMaster E WITH (NOLOCK) ON E.Id = A.EmployeeRefId"
            + " INNER JOIN BankMaster BA WITH (NOLOCK) ON BA.Id = A.BankRefId"
            + " WHERE A.CompanyRefId = :companyId ";

    public List<ReceiptViewRowDto> selectReceipts(ReceiptSearchRequest request) {
        Filter filter = Filter.of(request);
        String sql = "SELECT A.Id, A.CNumber, A.CNumberDisplay, ISNULL(A.QNECode, '') AS QNECode, ISNULL(A.QNEId, '') AS QNEId,"
                + " ISNULL(E.EmployeeName, '') AS EmployeeName,"
                + " FORMAT(ISNULL(A.ReceiptDate, '1900-01-01'), 'dd/MM/yyyy') AS BillDate,"
                + " FORMAT(ISNULL(A.Created_Date, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS BillTime,"
                + " A.CustomerRefId, B.CustomerName, BA.Name AS BankName,"
                + " CAST(A.Amount AS NUMERIC(18,2)) AS Amount, ISNULL(A.Remarks, '') AS Remarks,"
                + " ISNULL(A.RefNumber, '') AS RefNumber, ISNULL(A.PVStatus, 0) AS PVStatus, ISNULL(A.Fileupload, 0) AS Fileupload"
                + FROM_WHERE + filter.where
                + " ORDER BY B.CustomerName, A.Id";
        return jdbc.query(sql, filter.params, (rs, i) -> ReceiptViewRowDto.builder()
                .id(rs.getInt("Id"))
                .billNo(rs.getInt("CNumber"))
                .billNoDisplay(rs.getString("CNumberDisplay"))
                .billDate(rs.getString("BillDate"))
                .billTime(rs.getString("BillTime"))
                .employeeName(rs.getString("EmployeeName"))
                .customerRefId(rs.getInt("CustomerRefId"))
                .customerName(rs.getString("CustomerName"))
                .bankName(rs.getString("BankName"))
                .amount(rs.getBigDecimal("Amount"))
                .remarks(rs.getString("Remarks"))
                .refNumber(rs.getString("RefNumber"))
                .qneCode(rs.getString("QNECode"))
                .qneId(rs.getString("QNEId"))
                .pvStatus(rs.getInt("PVStatus"))
                .fileUpload(rs.getInt("Fileupload"))
                .build());
    }

    public List<ReceiptViewDetailDto> selectReceiptDetails(ReceiptSearchRequest request) {
        Filter filter = Filter.of(request);
        String sql = "SELECT D.Id AS DetailId, D.ReceiptRefId, D.SaleMasterRefId, D.CustomeropenRefId,"
                + " CAST(D.ReceiptAmount AS NUMERIC(18,2)) AS ReceiptAmount,"
                + " ISNULL(P.CNumberDisplay, '') AS SaleNo,"
                + " CASE WHEN P.SaleDate IS NULL THEN '' ELSE FORMAT(P.SaleDate, 'dd/MM/yyyy') END AS SSaleDate,"
                + " ISNULL(S.CustomerName, '') AS DCustomerName"
                + " FROM ReceiptDetails D WITH (NOLOCK)"
                + " INNER JOIN Receipt A WITH (NOLOCK) ON D.ReceiptRefId = A.Id"
                + " INNER JOIN Customer B WITH (NOLOCK) ON A.CustomerRefId = B.Id"
                + " LEFT JOIN EmployeeMaster E WITH (NOLOCK) ON E.Id = A.EmployeeRefId"
                + " INNER JOIN BankMaster BA WITH (NOLOCK) ON BA.Id = A.BankRefId"
                + " LEFT JOIN SaleMaster P WITH (NOLOCK) ON P.Id = D.SaleMasterRefId"
                + " LEFT JOIN Customer S WITH (NOLOCK) ON S.Id = D.CustomeropenRefId"
                + " WHERE A.CompanyRefId = :companyId " + filter.where
                + " ORDER BY D.ReceiptRefId, D.Id";
        return jdbc.query(sql, filter.params, (rs, i) -> ReceiptViewDetailDto.builder()
                .detailId(rs.getInt("DetailId"))
                .saleRefId(rs.getInt("ReceiptRefId"))
                .saleMasterRefId(rs.getObject("SaleMasterRefId") == null ? null : rs.getInt("SaleMasterRefId"))
                .customerOpenRefId(rs.getObject("CustomeropenRefId") == null ? null : rs.getInt("CustomeropenRefId"))
                .receiptAmount(rs.getBigDecimal("ReceiptAmount"))
                .saleNo(rs.getString("SaleNo"))
                .sSaleDate(rs.getString("SSaleDate"))
                .dCustomerName(rs.getString("DCustomerName"))
                .build());
    }

    /** Exact total over the same rows the grid shows, summed by the database. */
    public BigDecimal sumAmount(ReceiptSearchRequest request) {
        Filter filter = Filter.of(request);
        String sql = "SELECT ISNULL(SUM(CAST(A.Amount AS NUMERIC(18,2))), 0)" + FROM_WHERE + filter.where;
        BigDecimal total = jdbc.queryForObject(sql, filter.params, BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
    }

    /** The shared WHERE clause and its parameters. */
    private record Filter(String where, MapSqlParameterSource params) {
        static Filter of(ReceiptSearchRequest request) {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("companyId", request.getCompanyId() == null ? 0 : request.getCompanyId());
            StringBuilder where = new StringBuilder();

            String search = request.getSearch() == null ? "" : request.getSearch().trim();
            if (!search.isEmpty()) {
                // legacy: a receipt number search replaces every other filter
                where.append(" AND A.CNumberDisplay = :search ");
                params.addValue("search", search);
                return new Filter(where.toString(), params);
            }

            if (request.getCustomerId() != null && request.getCustomerId() != 0) {
                where.append(" AND A.CustomerRefId = :customerId ");
                params.addValue("customerId", request.getCustomerId());
            }
            if (request.getEmployeeId() != null && request.getEmployeeId() != 0) {
                where.append(" AND A.EmployeeRefId = :employeeId ");
                params.addValue("employeeId", request.getEmployeeId());
            }
            LocalDate from = parse(request.getFromDate(), LocalDate.now());
            LocalDate to = parse(request.getToDate(), LocalDate.now());
            // half-open range so a receipt dated at any time on the last day is included
            where.append(" AND A.ReceiptDate >= :fromDate AND A.ReceiptDate < :toDateExclusive ");
            params.addValue("fromDate", java.sql.Timestamp.valueOf(from.atStartOfDay()));
            params.addValue("toDateExclusive", java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            return new Filter(where.toString(), params);
        }

        private static LocalDate parse(String value, LocalDate fallback) {
            if (value == null || value.isBlank()) {
                return fallback;
            }
            String text = value.trim();
            try {
                if (text.length() >= 10 && text.charAt(4) == '-') {
                    return LocalDate.parse(text.substring(0, 10));
                }
                if (text.length() == 10 && text.charAt(2) == '/') {
                    return LocalDate.parse(text, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
            } catch (DateTimeParseException ignored) {
                // fall through
            }
            throw new my.maleva.api.common.exception.InvalidRequestException(
                    "Date '" + value + "' must be yyyy-MM-dd or dd/MM/yyyy");
        }
    }
}
