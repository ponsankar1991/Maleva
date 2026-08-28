package my.maleva.api.module.transactionreport.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.transactionreport.dto.PaymentDoneRowDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Report queries behind the legacy {@code TransactionReport} controller.
 *
 * <p>Native SQL rather than JPA: these are cross-table unions over tables this
 * module owns no entities for, and the shapes are report rows, not aggregates.
 * Same reasoning (and same style) as {@code dashboard/repository/DashboardRepository}.
 *
 * <p>Everything is bound through {@link MapSqlParameterSource}. The .NET
 * original concatenated {@code obj.Comid}, {@code obj.PayTo} and every category
 * name straight into the SQL string, so a PayTo carrying an apostrophe broke
 * the screen and a hostile one did rather more.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TransactionReportRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Filters shared by the row query and the total, resolved once.
     *
     * <p>{@code voucher} constrains the {@code PaymentVoucherMaster} arm and
     * {@code payment} the {@code Payment} arm; they are not the same clause
     * because the two arms filter on different columns.
     */
    private record Filters(String voucher, String payment, MapSqlParameterSource params) {}

    /**
     * The Payment Completed rows, ordered by document number.
     *
     * @param comid        company scope
     * @param fromDateTime inclusive lower bound, {@code yyyy-MM-dd HH:mm:ss}
     * @param toDateTime   inclusive upper bound, {@code yyyy-MM-dd HH:mm:ss}
     * @param supplierId   non-zero narrows to that supplier's payments only
     * @param payTo        non-blank narrows to vouchers paid to that name only
     * @param descriptions expense categories to keep; empty means all
     */
    public List<PaymentDoneRowDto> findCompletedPayments(Integer comid,
                                                         String fromDateTime,
                                                         String toDateTime,
                                                         Integer supplierId,
                                                         String payTo,
                                                         List<String> descriptions) {
        Filters filters = buildFilters(comid, fromDateTime, toDateTime, supplierId, payTo, descriptions);
        String sql = union(filters) + " ORDER BY CNumberDisplay";

        return jdbc.query(sql, filters.params(), (rs, rowNum) -> PaymentDoneRowDto.builder()
                .id(rs.getInt("Id"))
                .sSaleDate(rs.getString("SSaleDate"))
                .cNumberDisplay(rs.getString("CNumberDisplay"))
                .expenseName(rs.getString("ExpenseName"))
                .refNumber(rs.getString("RefNumber"))
                .amount(rs.getBigDecimal("Amount"))
                .remarks(rs.getString("Remarks"))
                .detailedId(rs.getInt("DetailedId"))
                .filePath(rs.getString("FilePath"))
                .totalAmount(rs.getBigDecimal("TotalAmount"))
                .build());
    }

    /**
     * The total of exactly the rows {@link #findCompletedPayments} returns.
     *
     * <p>Summed in SQL over the same union so the figure keeps the precision
     * the {@code numeric} columns actually have. The legacy screen added the
     * rows up in JavaScript instead.
     */
    public BigDecimal sumCompletedPayments(Integer comid,
                                           String fromDateTime,
                                           String toDateTime,
                                           Integer supplierId,
                                           String payTo,
                                           List<String> descriptions) {
        Filters filters = buildFilters(comid, fromDateTime, toDateTime, supplierId, payTo, descriptions);
        String sql = "SELECT ISNULL(SUM(t.Amount), 0) FROM (" + union(filters) + ") t";

        BigDecimal total = jdbc.queryForObject(sql, filters.params(), BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
    }

    /**
     * The two-armed union, without an ORDER BY so it can also be wrapped.
     *
     * <p>{@code UNION}, not {@code UNION ALL}, is the legacy behaviour and is
     * kept: the arms cannot collide (they select different {@code DetailedId}
     * literals) so it changes nothing here, but a future third arm would
     * inherit the original's de-duplication rather than silently double-count.
     */
    private String union(Filters filters) {
        return VOUCHER_ARM
                + filters.voucher()
                + PAYMENT_ARM
                + filters.payment();
    }

    private static final String VOUCHER_ARM = """
            SELECT PM.Id,
                   FORMAT(ISNULL(PM.PaymentVoucherDate, '1900-01-01'), 'dd/MM/yyyy') AS SSaleDate,
                   PM.CNumberDisplay,
                   PM.PayTo AS ExpenseName,
                   PM.RefNo AS RefNumber,
                   PM.Amount,
                   PM.QNECode AS Remarks,
                   0 AS DetailedId,
                   PM.Description AS FilePath,
                   (SELECT SUM(x.Amount) FROM PaymentVoucherMaster x WITH (NOLOCK)
                     WHERE x.Description = PM.Description) AS TotalAmount
              FROM PaymentVoucherMaster PM WITH (NOLOCK)
             WHERE PM.Active = 1
               AND PM.CompanyRefId = :comid
               AND PM.PaymentVoucherDate BETWEEN :fromDate AND :toDate
            """;

    private static final String PAYMENT_ARM = """
            UNION
            SELECT P.Id,
                   FORMAT(ISNULL(P.PaymentDate, '1900-01-01'), 'dd/MM/yyyy') AS SSaleDate,
                   P.CNumberDisplay,
                   S.SupplierName AS ExpenseName,
                   P.RefNumber,
                   P.Amount,
                   P.QNECode AS Remarks,
                   1 AS DetailedId,
                   P.Description AS FilePath,
                   CAST(NULL AS DECIMAL(19, 4)) AS TotalAmount
              FROM Payment P WITH (NOLOCK)
             INNER JOIN Supplier S WITH (NOLOCK) ON P.SupplierRefId = S.Id
             WHERE P.CompanyRefId = :comid
               AND P.PaymentDate BETWEEN :fromDate AND :toDate
            """;

    private Filters buildFilters(Integer comid,
                                 String fromDateTime,
                                 String toDateTime,
                                 Integer supplierId,
                                 String payTo,
                                 List<String> descriptions) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("comid", comid)
                .addValue("fromDate", fromDateTime)
                .addValue("toDate", toDateTime);

        StringBuilder voucher = new StringBuilder();
        StringBuilder payment = new StringBuilder();

        // Supplier and PayTo each suppress the arm they cannot describe, rather
        // than narrowing it. That is deliberate in the original and is what
        // makes the two filters mutually exclusive: a voucher has no supplier,
        // and a supplier payment has no PayTo, so leaving the other arm
        // unfiltered would answer a supplier question with voucher rows.
        boolean bySupplier = supplierId != null && supplierId != 0;
        boolean byPayTo = !bySupplier && payTo != null && !payTo.isBlank();

        if (bySupplier) {
            payment.append(" AND P.SupplierRefId = :supplierId ");
            voucher.append(" AND PM.PayTo = '' ");
            params.addValue("supplierId", supplierId);
        } else if (byPayTo) {
            voucher.append(" AND PM.PayTo = :payTo ");
            payment.append(" AND P.SupplierRefId = 0 ");
            params.addValue("payTo", payTo.trim());
        }

        if (descriptions != null && !descriptions.isEmpty()) {
            // PM./P. qualified on both arms. The .NET original emitted
            // `(P.Description='A' OR Description='B')` — every category after
            // the first was unqualified, and only survived because Supplier
            // happens to have no Description column of its own.
            voucher.append(" AND PM.Description IN (:descriptions) ");
            payment.append(" AND P.Description IN (:descriptions) ");
            params.addValue("descriptions", descriptions);
        }

        return new Filters(voucher.toString(), payment.toString(), params);
    }
}
