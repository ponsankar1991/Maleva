package my.maleva.api.module.salecreditmaster.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditSearchRequest;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditViewDetailDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditViewKnockOffDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditViewRowDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * The SALECREDIT ENTRY VIEW queries — the port of legacy
 * {@code SaleCreditServices.SelectSaleCredit}, parameterised.
 *
 * <p>Kept from legacy: an exact number search drops the other filters and
 * matches either the credit note's own number or the number of the invoice it
 * was raised against; the grid shows the note, its product lines and its
 * knock-off lines.
 *
 * <p>Fixed here:
 * <ul>
 *   <li>The search no longer escapes the company. Legacy built
 *       {@code ... and A.CompanyRefId=6 and A.CNumberDisplay='x' or A.SaleMasterRefId=(...)};
 *       {@code or} binds looser than {@code and}, so the second branch matched
 *       across every company in the database.</li>
 *   <li>Line tax is the LINE's tax. Legacy selected {@code A.TaxAmount} — the
 *       master's — under the alias {@code TaxAmt}, so every line of a note
 *       showed the note's whole tax.</li>
 *   <li>Rows are ordered by the real date. Legacy sorted the already-formatted
 *       {@code dd/MM/yyyy} string, which orders by day of month.</li>
 *   <li>{@code BillTime} uses {@code HH} — legacy's {@code hh} printed 13:05
 *       as 01:05 with no AM/PM.</li>
 *   <li>Knock-offs against a customer's opening balance are returned and
 *       named, not only invoice knock-offs.</li>
 * </ul>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SaleCreditViewQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private static final String CREDIT_NOTES_SQL =
            "SELECT A.Id, A.CNumber, A.CNumberDisplay,"
            + " ISNULL(E.EmployeeName, '') AS EmployeeName,"
            + " FORMAT(ISNULL(A.SaleDate, '1900-01-01'), 'dd/MM/yyyy') AS BillDate,"
            + " FORMAT(ISNULL(A.Created_Date, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS BillTime,"
            + " A.CustomerRefId, B.CustomerName,"
            + " ISNULL(P.CNumberDisplay, '') AS InvoiceNo,"
            + " CAST(A.Amount AS NUMERIC(18,2)) AS Amount,"
            + " ISNULL(A.Remarks, '') AS Remarks,"
            + " ISNULL(A.QNECode, '') AS QNECode,"
            + " ISNULL(A.QNEId, '') AS QNEId,"
            + " ISNULL(A.EInvoiceUid, '') AS EInvoiceUid,"
            + " ISNULL(A.EInvoiceStatus, '') AS EInvoiceStatus,"
            // the grid total in the same round trip, summed exactly by the database
            + " SUM(CAST(A.Amount AS NUMERIC(18,2))) OVER () AS TotalAmount"
            + " FROM SaleCreditMaster A WITH (NOLOCK)"
            + " INNER JOIN Customer B WITH (NOLOCK) ON B.Id = A.CustomerRefId"
            + " LEFT JOIN EmployeeMaster E WITH (NOLOCK) ON E.Id = A.EmployeeRefId"
            + " LEFT JOIN SaleMaster P WITH (NOLOCK) ON P.Id = A.SaleMasterRefId"
            + " WHERE A.CompanyRefId = :companyId ";

    private static final String DETAILS_SQL =
            "SELECT D.Id AS DetailId, D.SaleCreditMasterRefId AS SaleRefId,"
            + " ISNULL(I.Prod_Code, '') AS ProductCode, ISNULL(I.PName, '') AS ProductName,"
            + " D.ItemQty, D.SalesRate, D.TaxPercent,"
            + " D.TaxAmount AS LineTaxAmount, D.Amount AS LineAmount"
            + " FROM SaleCreditDetails D WITH (NOLOCK)"
            + " INNER JOIN SaleCreditMaster A WITH (NOLOCK) ON A.Id = D.SaleCreditMasterRefId"
            + " LEFT JOIN ItemMaster I WITH (NOLOCK) ON I.Id = D.ItemMasterRefId"
            + " WHERE A.CompanyRefId = :companyId ";

    private static final String KNOCK_OFFS_SQL =
            "SELECT K.Id AS KnockOffId, K.SaleCreditMasterRefId AS SaleRefId,"
            + " ISNULL(S.CNumberDisplay, '') AS SaleNo,"
            + " CASE WHEN S.SaleDate IS NULL THEN '' ELSE FORMAT(S.SaleDate, 'dd/MM/yyyy') END AS SSaleDate,"
            + " ISNULL(C.CustomerName, '') AS OpeningBalanceCustomer,"
            + " CAST(K.SaleCreditAmount AS NUMERIC(18,2)) AS SaleCreditAmount"
            + " FROM SaleCreditKnockOff K WITH (NOLOCK)"
            + " INNER JOIN SaleCreditMaster A WITH (NOLOCK) ON A.Id = K.SaleCreditMasterRefId"
            + " LEFT JOIN SaleMaster S WITH (NOLOCK) ON S.Id = K.SaleMasterRefId"
            + " LEFT JOIN Customer C WITH (NOLOCK) ON C.Id = K.CustomeropenRefId"
            + " WHERE A.CompanyRefId = :companyId ";

    public List<SaleCreditViewRowDto> selectCreditNotes(SaleCreditSearchRequest request) {
        Filter filter = Filter.of(request);
        String sql = CREDIT_NOTES_SQL + filter.where() + " ORDER BY A.SaleDate, A.Id";
        return jdbc.query(sql, filter.params(), (rs, row) -> SaleCreditViewRowDto.builder()
                .id(rs.getInt("Id"))
                .billNo(rs.getInt("CNumber"))
                .billNoDisplay(rs.getString("CNumberDisplay"))
                .billDate(rs.getString("BillDate"))
                .billTime(rs.getString("BillTime"))
                .employeeName(rs.getString("EmployeeName"))
                .customerRefId(rs.getInt("CustomerRefId"))
                .customerName(rs.getString("CustomerName"))
                .invoiceNo(rs.getString("InvoiceNo"))
                .amount(rs.getBigDecimal("Amount"))
                .remarks(rs.getString("Remarks"))
                .qneCode(rs.getString("QNECode"))
                .qneId(rs.getString("QNEId"))
                .eInvoiceUid(rs.getString("EInvoiceUid"))
                .eInvoiceStatus(rs.getString("EInvoiceStatus"))
                .totalAmount(rs.getBigDecimal("TotalAmount"))
                .build());
    }

    public List<SaleCreditViewDetailDto> selectDetails(SaleCreditSearchRequest request) {
        Filter filter = Filter.of(request);
        String sql = DETAILS_SQL + filter.where() + " ORDER BY D.SaleCreditMasterRefId, D.Id";
        return jdbc.query(sql, filter.params(), (rs, row) -> SaleCreditViewDetailDto.builder()
                .detailId(rs.getInt("DetailId"))
                .saleRefId(rs.getInt("SaleRefId"))
                .productCode(rs.getString("ProductCode"))
                .productName(rs.getString("ProductName"))
                .itemQty(rs.getBigDecimal("ItemQty"))
                .saleRate(rs.getBigDecimal("SalesRate"))
                .taxPercent(rs.getBigDecimal("TaxPercent"))
                .taxAmount(rs.getBigDecimal("LineTaxAmount"))
                .amount(rs.getBigDecimal("LineAmount"))
                .build());
    }

    public List<SaleCreditViewKnockOffDto> selectKnockOffs(SaleCreditSearchRequest request) {
        Filter filter = Filter.of(request);
        String sql = KNOCK_OFFS_SQL + filter.where() + " ORDER BY K.SaleCreditMasterRefId, K.Id";
        return jdbc.query(sql, filter.params(), (rs, row) -> SaleCreditViewKnockOffDto.builder()
                .knockOffId(rs.getInt("KnockOffId"))
                .saleRefId(rs.getInt("SaleRefId"))
                .saleNo(rs.getString("SaleNo"))
                .sSaleDate(rs.getString("SSaleDate"))
                .openingBalanceCustomer(rs.getString("OpeningBalanceCustomer"))
                .saleCreditAmount(rs.getBigDecimal("SaleCreditAmount"))
                .build());
    }

    /**
     * The WHERE fragment shared by the three queries and its parameters. All
     * three alias the credit note master as {@code A}, so one fragment fits.
     */
    private record Filter(String where, MapSqlParameterSource params) {

        static Filter of(SaleCreditSearchRequest request) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("companyId", request.getCompanyId() == null ? 0 : request.getCompanyId());

            String search = request.getSearch() == null ? "" : request.getSearch().trim();
            if (!search.isEmpty()) {
                // Legacy meaning kept: the box matches a credit note number or
                // the number of the invoice it was raised against. The
                // parentheses are the fix — without them the company filter
                // applied only to the first branch.
                params.addValue("search", search);
                return new Filter(
                        " AND (A.CNumberDisplay = :search"
                        + " OR A.SaleMasterRefId IN (SELECT SM.Id FROM SaleMaster SM WITH (NOLOCK)"
                        + " WHERE SM.CompanyRefId = :companyId AND SM.CNumberDisplay = :search)) ",
                        params);
            }

            StringBuilder where = new StringBuilder();
            if (request.getCustomerId() != null && request.getCustomerId() != 0) {
                where.append(" AND A.CustomerRefId = :customerId ");
                params.addValue("customerId", request.getCustomerId());
            }
            if (request.getEmployeeId() != null && request.getEmployeeId() != 0) {
                where.append(" AND A.EmployeeRefId = :employeeId ");
                params.addValue("employeeId", request.getEmployeeId());
            }
            LocalDate from = parse(request.getFromDate());
            LocalDate to = parse(request.getToDate());
            if (from.isAfter(to)) {
                throw new InvalidRequestException("From Date Is Greater Than To Date");
            }
            // Half-open, so a note dated at any time on the last day is included.
            where.append(" AND A.SaleDate >= :fromDate AND A.SaleDate < :toDateExclusive ");
            params.addValue("fromDate", java.sql.Timestamp.valueOf(from.atStartOfDay()));
            params.addValue("toDateExclusive", java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            return new Filter(where.toString(), params);
        }

        private static LocalDate parse(String value) {
            if (value == null || value.isBlank()) {
                return LocalDate.now();
            }
            String text = value.trim();
            try {
                if (text.length() >= 10 && text.charAt(4) == '-') {
                    return LocalDate.parse(text.substring(0, 10));
                }
                if (text.length() == 10 && text.charAt(2) == '/') {
                    return LocalDate.parse(text, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
            } catch (DateTimeParseException ignored) {
                // fall through to the refusal below
            }
            throw new InvalidRequestException("Date '" + value + "' must be yyyy-MM-dd or dd/MM/yyyy");
        }
    }
}
