package my.maleva.api.module.customerstatement.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The three reads behind a Statement of Account, the port of the SQL in
 * legacy {@code CustomerReportServices.SelectCustomerStatementAllReport}.
 *
 * <p>Same rows, different shape. Legacy computed "what is still owed on this
 * invoice" with two correlated subqueries per invoice row, ran the ageing
 * with no customer filter at all, then loaded <em>every</em> customer in the
 * table (no company filter) and scanned that list nineteen times per customer
 * in C#. Here receipts and knockoffs are pre-aggregated once
 * ({@code GROUP BY SaleMasterRefId}) and joined, every query is scoped, and
 * every value is a bound parameter — legacy concatenated dates and ids into
 * the SQL text.
 *
 * <p>Money is read as {@code BigDecimal} rounded to two places, because the
 * amount columns are floats (see the money-precision notes): the double
 * 431.35 comes back as 431.35000000000002, and a running balance built from
 * unrounded floats drifts by sen over a page. The same floats are why
 * "outstanding" is tested against half a sen rather than zero.
 */
@Repository
@RequiredArgsConstructor
public class CustomerStatementQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /** An invoice with something still owed on it. */
    public record OutstandingInvoice(
            int saleMasterId, int customerId, String customerName, LocalDate saleDate,
            String documentNo, String reference, String description,
            BigDecimal amount, BigDecimal received, BigDecimal knockedOff) {

        /** What the customer still owes: the legacy {@code (BillAmount - Receipt)}. */
        public BigDecimal outstanding() {
            return amount.subtract(received).subtract(knockedOff);
        }
    }

    /** One credit note applied against one invoice. */
    public record CreditNoteKnockoff(
            int saleMasterId, int customerId, LocalDate creditDate,
            String creditNoteNo, String invoiceNo, BigDecimal knockedAmount) {
    }

    /** Outstanding invoices dated in one month, summed per customer. */
    public record AgeingRow(int customerId, int year, int month, BigDecimal balance) {
    }

    public record OpeningBalance(int customerId, BigDecimal balance) {
    }

    /** The customer block printed at the top of each statement. */
    public record CustomerHeader(
            int customerId, String customerName,
            String address1, String address2, String address3, String phone,
            String attn, String accountCode, String terms, String currency,
            String aEmail, String aEmail1, String oEmail, String oEmail1) {
    }

    /**
     * Every CREDIT invoice in the window on which the customer still owes
     * something — {@code Amount − receipts − credit-note knockoffs ≠ 0}.
     *
     * @param toExclusive the day after the last day wanted, or null for no
     *                    upper bound (the "since cutoff" mode)
     */
    public List<OutstandingInvoice> findOutstandingInvoices(
            int companyId, Integer customerId, LocalDate from, LocalDate toExclusive) {

        StringBuilder sql = new StringBuilder("""
                SELECT A.Id, A.CustomerRefId, C.CustomerName, A.SaleDate,
                       A.CNumberDisplay, A.Remarks1,
                       CASE WHEN ISNULL(A.Offvesselname, '') <> '' THEN A.Offvesselname
                            ELSE A.Loadingvesselname END AS Description,
                       A.Amount,
                       ISNULL(R.Received, 0) AS Received,
                       ISNULL(K.Knocked, 0)  AS Knocked
                FROM SaleMaster A WITH (NOLOCK)
                JOIN Customer C WITH (NOLOCK) ON C.Id = A.CustomerRefId
                LEFT JOIN (SELECT pd.SaleMasterRefId, SUM(ISNULL(pd.ReceiptAmount, 0)) AS Received
                           FROM ReceiptDetails pd WITH (NOLOCK)
                           JOIN Receipt py WITH (NOLOCK) ON py.Id = pd.ReceiptRefId
                           GROUP BY pd.SaleMasterRefId) R ON R.SaleMasterRefId = A.Id
                LEFT JOIN (SELECT B.SaleMasterRefId, SUM(ISNULL(B.SaleCreditAmount, 0)) AS Knocked
                           FROM SaleCreditKnockOff B WITH (NOLOCK)
                           JOIN SaleCreditMaster SC WITH (NOLOCK) ON SC.Id = B.SaleCreditMasterRefId
                           GROUP BY B.SaleMasterRefId) K ON K.SaleMasterRefId = A.Id
                WHERE A.CompanyRefId = :companyId
                  AND A.Active = 1 AND A.SaleType = 'CREDIT' AND C.Active = 1
                  AND A.SaleDate >= :from
                """);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyId", companyId)
                .addValue("from", Date.valueOf(from));

        if (toExclusive != null) {
            sql.append("  AND A.SaleDate < :toExclusive\n");
            params.addValue("toExclusive", Date.valueOf(toExclusive));
        }
        if (customerId != null) {
            sql.append("  AND C.Id = :customerId\n");
            params.addValue("customerId", customerId);
        }
        // Half a sen, not zero. Amount and ReceiptAmount are float columns, so a
        // fully paid invoice is left with ±0.00003 and would print as a 0.00 line:
        // on LiveMaleva2, ACS FREIGHT had 17 such ghosts among 27 rows. Crystal
        // hid them in the template; here they never leave the database.
        sql.append("""
                  AND ABS(A.Amount - ISNULL(R.Received, 0) - ISNULL(K.Knocked, 0)) >= 0.005
                ORDER BY C.CustomerName, A.SaleDate, A.Id
                """);

        return jdbc.query(sql.toString(), params, (rs, i) -> new OutstandingInvoice(
                rs.getInt("Id"), rs.getInt("CustomerRefId"), rs.getString("CustomerName"),
                localDate(rs, "SaleDate"),
                rs.getString("CNumberDisplay"), rs.getString("Remarks1"), rs.getString("Description"),
                money(rs, "Amount"), money(rs, "Received"), money(rs, "Knocked")));
    }

    /**
     * Credit notes since the cutoff, one row per invoice they were applied to.
     *
     * <p>Legacy listed each credit note once with its whole amount and only
     * the <em>first</em> invoice it touched, then kept it only if that invoice
     * was on the statement. Per-knockoff rows say what was actually applied to
     * which invoice, which is what lets the caller avoid counting a credit
     * note twice — see {@code CustomerStatementService}.
     */
    public List<CreditNoteKnockoff> findCreditNoteKnockoffs(int companyId, Integer customerId, LocalDate since) {
        StringBuilder sql = new StringBuilder("""
                SELECT B.SaleMasterRefId, SC.CustomerRefId, SC.SaleDate,
                       SC.CNumberDisplay AS CreditNoteNo, SA.CNumberDisplay AS InvoiceNo,
                       B.SaleCreditAmount
                FROM SaleCreditKnockOff B WITH (NOLOCK)
                JOIN SaleCreditMaster SC WITH (NOLOCK) ON SC.Id = B.SaleCreditMasterRefId
                JOIN SaleMaster SA WITH (NOLOCK) ON SA.Id = B.SaleMasterRefId
                JOIN Customer C WITH (NOLOCK) ON C.Id = SC.CustomerRefId
                WHERE SC.CompanyRefId = :companyId AND C.Active = 1
                  AND SC.SaleDate >= :since
                """);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyId", companyId)
                .addValue("since", Date.valueOf(since));
        if (customerId != null) {
            sql.append("  AND C.Id = :customerId\n");
            params.addValue("customerId", customerId);
        }
        sql.append("ORDER BY SC.SaleDate, SC.Id\n");

        return jdbc.query(sql.toString(), params, (rs, i) -> new CreditNoteKnockoff(
                rs.getInt("SaleMasterRefId"), rs.getInt("CustomerRefId"), localDate(rs, "SaleDate"),
                rs.getString("CreditNoteNo"), rs.getString("InvoiceNo"), money(rs, "SaleCreditAmount")));
    }

    /**
     * Outstanding per customer per month over the ageing window.
     *
     * <p>The window is the same for every customer, so the grid on every
     * statement lines up. One legacy rule is deliberately NOT copied: legacy
     * subtracted a receipt or knockoff only if it was itself dated inside the
     * window, so an invoice paid by a receipt dated before the window still
     * showed as outstanding in its month while the statement above it did not
     * list it. On MalevanewDemo that put 3,519.00 of paid invoices into ACS
     * FREIGHT's September cell and made the grid sum to 22,355.45 against a
     * closing balance of 18,836.45 - the same page contradicting itself. Every
     * payment counts here, whenever it was dated, so the twelve cells always
     * add up to what the lines say is owed. On data where no payment predates
     * its invoice the two rules give identical numbers.
     */
    public List<AgeingRow> findAgeing(int companyId, Integer customerId, LocalDate from, LocalDate toExclusive) {
        StringBuilder sql = new StringBuilder("""
                SELECT A.CustomerRefId, YEAR(A.SaleDate) AS Y, MONTH(A.SaleDate) AS M,
                       SUM(A.Amount - ISNULL(R.Received, 0) - ISNULL(K.Knocked, 0)) AS Balance
                FROM SaleMaster A WITH (NOLOCK)
                LEFT JOIN (SELECT pd.SaleMasterRefId, SUM(ISNULL(pd.ReceiptAmount, 0)) AS Received
                           FROM ReceiptDetails pd WITH (NOLOCK)
                           JOIN Receipt py WITH (NOLOCK) ON py.Id = pd.ReceiptRefId
                           GROUP BY pd.SaleMasterRefId) R ON R.SaleMasterRefId = A.Id
                LEFT JOIN (SELECT B.SaleMasterRefId, SUM(ISNULL(B.SaleCreditAmount, 0)) AS Knocked
                           FROM SaleCreditKnockOff B WITH (NOLOCK)
                           JOIN SaleCreditMaster SC WITH (NOLOCK) ON SC.Id = B.SaleCreditMasterRefId
                           GROUP BY B.SaleMasterRefId) K ON K.SaleMasterRefId = A.Id
                WHERE A.CompanyRefId = :companyId
                  AND A.Active = 1 AND A.SaleType = 'CREDIT'
                  AND A.SaleDate >= :from AND A.SaleDate < :toExclusive
                """);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyId", companyId)
                .addValue("from", Date.valueOf(from))
                .addValue("toExclusive", Date.valueOf(toExclusive));
        if (customerId != null) {
            sql.append("  AND A.CustomerRefId = :customerId\n");
            params.addValue("customerId", customerId);
        }
        sql.append("""
                GROUP BY A.CustomerRefId, YEAR(A.SaleDate), MONTH(A.SaleDate)
                HAVING ABS(SUM(A.Amount - ISNULL(R.Received, 0) - ISNULL(K.Knocked, 0))) >= 0.005
                """);

        return jdbc.query(sql.toString(), params, (rs, i) -> new AgeingRow(
                rs.getInt("CustomerRefId"), rs.getInt("Y"), rs.getInt("M"), money(rs, "Balance")));
    }

    /**
     * {@code CustomerBalance(company, asOf)} — the database's own balance
     * function, kept as the single definition of "balance on a date".
     */
    public List<OpeningBalance> findOpeningBalances(int companyId, Integer customerId, LocalDate asOf) {
        StringBuilder sql = new StringBuilder(
                "SELECT Id, Balance FROM CustomerBalance(:companyId, :asOf) A\n");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyId", companyId)
                .addValue("asOf", Date.valueOf(asOf));
        if (customerId != null) {
            sql.append("WHERE A.Id = :customerId\n");
            params.addValue("customerId", customerId);
        }
        return jdbc.query(sql.toString(), params, (rs, i) ->
                new OpeningBalance(rs.getInt("Id"), money(rs, "Balance")));
    }

    /**
     * The header block for the customers on the statement.
     *
     * <p>LEFT joins on terms and symbol where legacy used INNER: a customer
     * whose payment term or currency was never set still owes money and still
     * gets a statement, with those two cells blank. Chunked so an
     * every-customer run cannot run into SQL Server's 2,100-parameter limit.
     */
    public List<CustomerHeader> findHeaders(int companyId, Collection<Integer> customerIds) {
        List<CustomerHeader> all = new ArrayList<>();
        List<Integer> ids = new ArrayList<>(customerIds);
        for (int start = 0; start < ids.size(); start += 1000) {
            List<Integer> chunk = ids.subList(start, Math.min(start + 1000, ids.size()));
            all.addAll(jdbc.query("""
                    SELECT C.Id, C.CustomerName, C.Address1, C.Address2, C.Address3,
                           C.OPhone, C.City, C.CompanyCode,
                           C.AEmail, C.AEmail1, C.OEmail, C.OEmail1,
                           P.TermsName, SM.SName
                    FROM Customer C WITH (NOLOCK)
                    LEFT JOIN PaymentTermsMaster P WITH (NOLOCK) ON P.Id = C.PaymentTermsRefid
                    LEFT JOIN SymbolMaster SM WITH (NOLOCK) ON SM.Id = C.SymbolRefid
                    WHERE C.CompanyRefId = :companyId AND C.Id IN (:ids)
                    """,
                    new MapSqlParameterSource()
                            .addValue("companyId", companyId)
                            .addValue("ids", chunk),
                    HEADER_MAPPER));
        }
        return all;
    }

    private static final RowMapper<CustomerHeader> HEADER_MAPPER = (rs, i) -> new CustomerHeader(
            rs.getInt("Id"), rs.getString("CustomerName"),
            rs.getString("Address1"), rs.getString("Address2"), rs.getString("Address3"), rs.getString("OPhone"),
            rs.getString("City"), rs.getString("CompanyCode"), rs.getString("TermsName"), rs.getString("SName"),
            rs.getString("AEmail"), rs.getString("AEmail1"), rs.getString("OEmail"), rs.getString("OEmail1"));

    private static BigDecimal money(ResultSet rs, String column) throws SQLException {
        BigDecimal value = rs.getBigDecimal(column);
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static LocalDate localDate(ResultSet rs, String column) throws SQLException {
        Date date = rs.getDate(column);
        return date == null ? null : date.toLocalDate();
    }
}
