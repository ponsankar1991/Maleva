package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.module.fleet.dto.MaintenanceSpendDto;
import my.maleva.api.module.fleet.dto.MaintenanceSpendDto.DailySpend;
import my.maleva.api.module.fleet.dto.MaintenanceSpendDto.NamedSpend;
import my.maleva.api.module.fleet.dto.MaintenanceSpendDto.TruckSpend;
import my.maleva.api.module.fleet.service.MaintenanceSpendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

/**
 * MaintenanceSpendServiceImpl
 *
 * Aggregation queries behind the CFO spending view of the maintenance
 * dashboard. Read-only native SQL, merged in Java so a truck (or a date) that
 * appears in several sources becomes one row.
 *
 * <p><b>Seven statements, not thirty-one.</b> This screen reads seven tables
 * three ways - per truck, per day, and as a single total - which began as one
 * query per table per view. The work SQL Server does is the same either way,
 * but the driver was waiting on thirty-one round trips to build one page. They
 * are now four shapes:
 * <ul>
 *   <li>{@link #TRUCK_SPEND_SQL} - the seven per-truck groups, UNION ALL with a
 *       source tag;</li>
 *   <li>{@link #DAILY_SPEND_SQL} - the same again per calendar day;</li>
 *   <li>{@link #TOTALS_SQL} - every single-row figure as scalar subqueries;</li>
 *   <li>four list queries whose result shapes genuinely differ (job types, bill
 *       descriptions, the purchase order detail, voucher descriptions).</li>
 * </ul>
 * The union queries pad to a common column shape and CAST their money columns:
 * several Amount columns are float, and type precedence would otherwise drag the
 * decimal ones across to float mid-union.
 *
 * <p><b>Cost sources:</b> JobOrderMaster, BillsOrderMaster (purchase orders),
 * FuelEntry, AutoPassEntry, TollEntry, LeviEntry. RTIMaster is counted only -
 * how many orders were delivered - because its amount columns are trip payment
 * figures, not customer revenue, so reporting them as income would mislead.
 *
 * <p><b>What the total means:</b> money leaves through a purchase order or a
 * payment voucher, so those two are the total. The cost sources above are
 * records of what was bought, and that buying is settled by one of those two
 * documents - adding both sides would count the same ringgit twice. They are
 * reported as {@code recordedCostTotal}, a breakdown, not a second total.
 *
 * <p><b>Date semantics:</b> JobOrderMaster.JobDate and SubcdiyEntry.EntryDate
 * are DATE columns and are compared inclusive; every other source carries a
 * DATETIME and is compared half-open, [from, to + 1 day), so an entry stamped
 * late on the last day is not dropped. The day-wise series covers every calendar
 * day in the range, quiet days included, so the chart's time axis stays even.
 */
@Service
public class MaintenanceSpendServiceImpl implements MaintenanceSpendService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceSpendServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;

    /** Named parameters for {@link #TOTALS_SQL}; the positional template for the rest. */
    private final NamedParameterJdbcTemplate namedJdbc;

    public MaintenanceSpendServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    /**
     * The one row {@link #TOTALS_SQL} returns.
     *
     * A record rather than the array-of-one boxes this used to need: a
     * RowCallbackHandler cannot return anything, so every figure was smuggled
     * out through a {@code final BigDecimal[]}, and a reader had to remember
     * that {@code fuelTotals[1]} meant litres.
     */
    private record Totals(
            BigDecimal jobOrderTotal,
            BigDecimal billOrderTotal,
            long billOrderCount,
            BigDecimal autoPassTotal,
            BigDecimal tollTotal,
            BigDecimal leviTotal,
            BigDecimal fuelTotal,
            BigDecimal fuelLiters,
            long fuelEntryCount,
            long rtiOrderCount,
            BigDecimal fuelSubsidyTotal,
            BigDecimal fuelVoucherTotal,
            BigDecimal billTotal,
            long billCount,
            BigDecimal voucherTotal,
            long voucherCount,
            BigDecimal standaloneVoucherTotal,
            long standaloneVoucherCount,
            BigDecimal settledTotal,
            long settledCount,
            BigDecimal outstandingTotal,
            long outstandingCount) {
    }

    /** Reads by column label, so adding a figure to the SELECT cannot shift the others. */
    private static final RowMapper<Totals> TOTALS_MAPPER = (rs, i) -> new Totals(
            nz(rs.getBigDecimal("jobOrderTotal")),
            nz(rs.getBigDecimal("billOrderTotal")),
            rs.getLong("billOrderCount"),
            nz(rs.getBigDecimal("autoPassTotal")),
            nz(rs.getBigDecimal("tollTotal")),
            nz(rs.getBigDecimal("leviTotal")),
            nz(rs.getBigDecimal("fuelTotal")),
            nz(rs.getBigDecimal("fuelLiters")),
            rs.getLong("fuelEntryCount"),
            rs.getLong("rtiOrderCount"),
            nz(rs.getBigDecimal("fuelSubsidyTotal")),
            nz(rs.getBigDecimal("fuelVoucherTotal")),
            nz(rs.getBigDecimal("billTotal")),
            rs.getLong("billCount"),
            nz(rs.getBigDecimal("voucherTotal")),
            rs.getLong("voucherCount"),
            nz(rs.getBigDecimal("standaloneVoucherTotal")),
            rs.getLong("standaloneVoucherCount"),
            nz(rs.getBigDecimal("settledTotal")),
            rs.getLong("settledCount"),
            nz(rs.getBigDecimal("outstandingTotal")),
            rs.getLong("outstandingCount"));

    /** Job order cost: the actual cost once known, the estimate until then. */
    private static final String JOB_COST = "COALESCE(NULLIF(JOM.ActualCost, 0), JOM.EstimatedCost, 0)";

    /**
     * What a fuel entry actually cost: AAmount, the receipt total.
     *
     * The other amount columns are NOT extra spend. PAmount and GAmount are the
     * same purchase measured two other ways - what the patron billed and what
     * the tank sensor saw - and DPAmount / DGAmount are the differences between
     * those readings. Adding them together would count the same litres three
     * times over. SUM(AAmount) is also what the fuel entry list reports as its
     * total, so the two screens agree.
     */
    private static final String FUEL_COST = "COALESCE(X.AAmount,0)";

    /** Litres on the receipt, matching FUEL_COST. */
    private static final String FUEL_LITERS = "COALESCE(X.Aliter,0)";

    // ── Truck-wise ─────────────────────────────────────────────────────────

    /**
     * Every per-truck figure, in one statement.
     *
     * Seven GROUP BY blocks over seven tables, tagged with a source name and
     * stacked with UNION ALL. They were seven separate queries, so producing one
     * table cost seven round trips; the work SQL Server does is the same either
     * way, but the driver now waits once.
     *
     * Every branch INNER JOINs TruckMaster on purpose: a row whose TruckRefid is
     * null, zero, or points at a truck that no longer exists belongs to no line
     * of this table, and is reported as the unassigned remainder rather than
     * being quietly attached to some truck.
     *
     * The columns are padded to a common shape - total / liters / cnt - because
     * a UNION needs one. Only FUEL fills all three and only FUEL and RTI use
     * cnt; the rest carry zeros, which the switch that reads this never looks at.
     *
     * The money columns are CAST to DECIMAL rather than left to type
     * precedence. Some of these Amount columns are float, and in a UNION the
     * float branches would drag the decimal ones - job order cost is
     * DECIMAL(12,2) - across to float, so a truck's job total would come back
     * as a binary approximation of a figure the totals query reports exactly.
     */
    private static final String TRUCK_SPEND_SQL = """
        SELECT 'JOB' AS src, TM.Id AS truckId, TM.TruckName AS truckName,
               CAST(SUM(%s) AS DECIMAL(19,4)) AS total,
               CAST(0 AS DECIMAL(19,4)) AS liters, 0 AS cnt
        FROM JobOrderMaster JOM
        INNER JOIN TruckMaster TM ON JOM.TruckMasterRefId = TM.Id
        WHERE JOM.CompanyRefId = :companyRefId AND JOM.IsActive = 1
          AND JOM.JobDate >= :fromDate AND JOM.JobDate <= :toDate
        GROUP BY TM.Id, TM.TruckName

        UNION ALL
        SELECT 'FUEL', TM.Id, TM.TruckName,
               CAST(SUM(%s) AS DECIMAL(19,4)), CAST(SUM(%s) AS DECIMAL(19,4)), COUNT(*)
        FROM FuelEntry X
        INNER JOIN TruckMaster TM ON X.TruckRefid = TM.Id
        WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
          AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs
        GROUP BY TM.Id, TM.TruckName

        UNION ALL
        SELECT 'BILL', TM.Id, TM.TruckName,
               CAST(SUM(BOM.Amount) AS DECIMAL(19,4)), CAST(0 AS DECIMAL(19,4)), 0
        FROM BillsOrderMaster BOM
        INNER JOIN TruckMaster TM ON BOM.TruckRefid = TM.Id
        WHERE BOM.CompanyRefId = :companyRefId AND BOM.Active = 1
          AND BOM.SaleDate >= :fromTs AND BOM.SaleDate < :toTs
        GROUP BY TM.Id, TM.TruckName

        UNION ALL
        SELECT 'AUTOPASS', TM.Id, TM.TruckName,
               CAST(SUM(X.Amount) AS DECIMAL(19,4)), CAST(0 AS DECIMAL(19,4)), 0
        FROM AutoPassEntry X
        INNER JOIN TruckMaster TM ON X.TruckRefid = TM.Id
        WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
          AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs
        GROUP BY TM.Id, TM.TruckName

        UNION ALL
        SELECT 'TOLL', TM.Id, TM.TruckName,
               CAST(SUM(X.Amount) AS DECIMAL(19,4)), CAST(0 AS DECIMAL(19,4)), 0
        FROM TollEntry X
        INNER JOIN TruckMaster TM ON X.TruckRefid = TM.Id
        WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
          AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs
        GROUP BY TM.Id, TM.TruckName

        UNION ALL
        SELECT 'LEVI', TM.Id, TM.TruckName,
               CAST(SUM(X.Amount) AS DECIMAL(19,4)), CAST(0 AS DECIMAL(19,4)), 0
        FROM LeviEntry X
        INNER JOIN TruckMaster TM ON X.TruckRefid = TM.Id
        WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
          AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs
        GROUP BY TM.Id, TM.TruckName

        UNION ALL
        SELECT 'RTI', TM.Id, TM.TruckName,
               CAST(0 AS DECIMAL(19,4)), CAST(0 AS DECIMAL(19,4)), COUNT(*)
        FROM RTIMaster R
        INNER JOIN TruckMaster TM ON R.TruckRefid = TM.Id
        WHERE R.CompanyRefId = :companyRefId AND R.Active = 1
          AND R.SaleDate >= :fromTs AND R.SaleDate < :toTs
        GROUP BY TM.Id, TM.TruckName
        """.formatted(JOB_COST, FUEL_COST, FUEL_LITERS);

    // ── Breakdowns ─────────────────────────────────────────────────────────

    private static final String JOB_TYPE_SQL = """
        SELECT JT.JobTypeName, COUNT(*) AS cnt, SUM(%s) AS total
        FROM JobOrderMaster JOM
        INNER JOIN JobOrderTypeMaster JT ON JOM.JobTypeRefId = JT.Id
        WHERE JOM.CompanyRefId = ? AND JOM.IsActive = 1
          AND JOM.JobDate >= ? AND JOM.JobDate <= ?
        GROUP BY JT.JobTypeName
        ORDER BY total DESC
        """.formatted(JOB_COST);

    private static final String BILL_DESCRIPTION_SQL = """
        SELECT COALESCE(NULLIF(LTRIM(RTRIM(BOM.Description)), ''), '(No description)') AS descr,
               COUNT(*) AS cnt,
               SUM(BOM.Amount) AS total
        FROM BillsOrderMaster BOM
        WHERE BOM.CompanyRefId = ? AND BOM.Active = 1
          AND BOM.SaleDate >= ? AND BOM.SaleDate < ?
        GROUP BY COALESCE(NULLIF(LTRIM(RTRIM(BOM.Description)), ''), '(No description)')
        ORDER BY total DESC
        """;

    /** The individual purchase orders, newest first; capped so the payload stays light. */
    private static final String BILL_DETAIL_SQL = """
        SELECT TOP 300
               CAST(BOM.SaleDate AS DATE) AS d,
               BOM.CNumberDisplay AS poNumber,
               COALESCE(NULLIF(LTRIM(RTRIM(BOM.Description)), ''), '(No description)') AS descr,
               S.SupplierName,
               BOM.PayTo,
               BOM.InvoiceNo,
               TM.TruckName,
               BOM.Amount
        FROM BillsOrderMaster BOM
        LEFT JOIN Supplier S ON BOM.SupplierRefId = S.Id
        LEFT JOIN TruckMaster TM ON BOM.TruckRefid = TM.Id
        WHERE BOM.CompanyRefId = ? AND BOM.Active = 1
          AND BOM.SaleDate >= ? AND BOM.SaleDate < ?
        ORDER BY BOM.SaleDate DESC
        """;

    // ── Payment side: committed (purchase order) vs released (bill, voucher) ──

    /**
     * Each purchase order in the range, flagged with whether anything settles it.
     *
     * EXISTS rather than a join: an order with two vouchers against it must
     * still count once, and a join would multiply its amount by the number of
     * settling documents. Neither EXISTS is date-bounded - an order raised in
     * March and paid in April is settled, not outstanding.
     */
    private static final String ORDER_SETTLEMENT_SUBQUERY = """
        SELECT BOM.Amount,
               CASE WHEN EXISTS (SELECT 1 FROM BillMaster BM
                                  WHERE BM.BillsOrderMasterRefId = BOM.Id AND BM.Active = 1)
                      OR EXISTS (SELECT 1 FROM PaymentVoucherMaster PVM
                                  WHERE PVM.BillsOrderMasterRefId = BOM.Id AND PVM.Active = 1)
                    THEN 1 ELSE 0 END AS settled
        FROM BillsOrderMaster BOM
        WHERE BOM.CompanyRefId = :companyRefId AND BOM.Active = 1
          AND BOM.SaleDate >= :fromTs AND BOM.SaleDate < :toTs
        """;

    /**
     * Every single-row figure on this screen, in one round trip.
     *
     * These were thirteen separate statements, each fetching one row and each
     * paying a full network round trip to SQL Server. As scalar subqueries in a
     * one-row SELECT they run exactly as often as before - once each - but the
     * driver waits once instead of thirteen times, which is what the request
     * actually spent its time doing. The settlement figures come from a derived
     * table rather than four more scalar subqueries, so that EXISTS scan also
     * runs once instead of four times.
     *
     * Named parameters rather than positional: with this many subqueries a
     * question-mark list would be forty-odd placeholders whose order nothing
     * checks, and one transposed pair would silently mis-date a total.
     *
     * The date parameters are not interchangeable. JobDate and EntryDate are
     * DATE columns and take :fromDate / :toDate inclusive; every other source is
     * a DATETIME and takes the half-open :fromTs / :toTs, so an entry stamped
     * late on the last day is not dropped.
     */
    private static final String TOTALS_SQL = """
        SELECT
          (SELECT COALESCE(SUM(%s), 0) FROM JobOrderMaster JOM
            WHERE JOM.CompanyRefId = :companyRefId AND JOM.IsActive = 1
              AND JOM.JobDate >= :fromDate AND JOM.JobDate <= :toDate)      AS jobOrderTotal,

          (SELECT COALESCE(SUM(BOM.Amount), 0) FROM BillsOrderMaster BOM
            WHERE BOM.CompanyRefId = :companyRefId AND BOM.Active = 1
              AND BOM.SaleDate >= :fromTs AND BOM.SaleDate < :toTs)         AS billOrderTotal,
          (SELECT COUNT(*) FROM BillsOrderMaster BOM
            WHERE BOM.CompanyRefId = :companyRefId AND BOM.Active = 1
              AND BOM.SaleDate >= :fromTs AND BOM.SaleDate < :toTs)         AS billOrderCount,

          (SELECT COALESCE(SUM(X.Amount), 0) FROM AutoPassEntry X
            WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
              AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs)             AS autoPassTotal,
          (SELECT COALESCE(SUM(X.Amount), 0) FROM TollEntry X
            WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
              AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs)             AS tollTotal,
          (SELECT COALESCE(SUM(X.Amount), 0) FROM LeviEntry X
            WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
              AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs)             AS leviTotal,

          (SELECT COALESCE(SUM(%s), 0) FROM FuelEntry X
            WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
              AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs)             AS fuelTotal,
          (SELECT COALESCE(SUM(%s), 0) FROM FuelEntry X
            WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
              AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs)             AS fuelLiters,
          (SELECT COUNT(*) FROM FuelEntry X
            WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
              AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs)             AS fuelEntryCount,

          (SELECT COUNT(*) FROM RTIMaster R
            WHERE R.CompanyRefId = :companyRefId AND R.Active = 1
              AND R.SaleDate >= :fromTs AND R.SaleDate < :toTs)             AS rtiOrderCount,

          -- SubcdiyEntry is not company-scoped and EntryDate is a DATE, so this
          -- is an inclusive comparison. Active is deliberately not filtered:
          -- SubcdiyEntryServiceImpl.create defaults it to 0 when the caller
          -- leaves it null, so "Active = 1" would drop real subsidy money.
          (SELECT COALESCE(SUM(S.Amount), 0) FROM SubcdiyEntry S
            WHERE S.EntryDate >= :fromDate AND S.EntryDate <= :toDate)      AS fuelSubsidyTotal,

          (SELECT COALESCE(SUM(PVM.Amount), 0) FROM PaymentVoucherMaster PVM
            WHERE PVM.CompanyRefId = :companyRefId AND PVM.Active = 1
              AND PVM.Description = 'FUEL'
              AND PVM.PaymentVoucherDate >= :fromTs
              AND PVM.PaymentVoucherDate < :toTs)                           AS fuelVoucherTotal,

          (SELECT COALESCE(SUM(BM.Amount), 0) FROM BillMaster BM
            WHERE BM.CompanyRefId = :companyRefId AND BM.Active = 1
              AND BM.SaleDate >= :fromTs AND BM.SaleDate < :toTs)           AS billTotal,
          (SELECT COUNT(*) FROM BillMaster BM
            WHERE BM.CompanyRefId = :companyRefId AND BM.Active = 1
              AND BM.SaleDate >= :fromTs AND BM.SaleDate < :toTs)           AS billCount,

          (SELECT COALESCE(SUM(PVM.Amount), 0) FROM PaymentVoucherMaster PVM
            WHERE PVM.CompanyRefId = :companyRefId AND PVM.Active = 1
              AND PVM.PaymentVoucherDate >= :fromTs
              AND PVM.PaymentVoucherDate < :toTs)                           AS voucherTotal,
          (SELECT COUNT(*) FROM PaymentVoucherMaster PVM
            WHERE PVM.CompanyRefId = :companyRefId AND PVM.Active = 1
              AND PVM.PaymentVoucherDate >= :fromTs
              AND PVM.PaymentVoucherDate < :toTs)                           AS voucherCount,

          -- Vouchers settling no order: the only ones carrying money the order
          -- side has not already counted, and so the only ones in the total.
          (SELECT COALESCE(SUM(PVM.Amount), 0) FROM PaymentVoucherMaster PVM
            WHERE PVM.CompanyRefId = :companyRefId AND PVM.Active = 1
              AND COALESCE(PVM.BillsOrderMasterRefId, 0) = 0
              AND PVM.PaymentVoucherDate >= :fromTs
              AND PVM.PaymentVoucherDate < :toTs)                           AS standaloneVoucherTotal,
          (SELECT COUNT(*) FROM PaymentVoucherMaster PVM
            WHERE PVM.CompanyRefId = :companyRefId AND PVM.Active = 1
              AND COALESCE(PVM.BillsOrderMasterRefId, 0) = 0
              AND PVM.PaymentVoucherDate >= :fromTs
              AND PVM.PaymentVoucherDate < :toTs)                           AS standaloneVoucherCount,

          st.settledTotal, st.settledCount, st.outstandingTotal, st.outstandingCount
        FROM (
          SELECT COALESCE(SUM(CASE WHEN settled = 1 THEN o.Amount ELSE 0 END), 0) AS settledTotal,
                 COALESCE(SUM(CASE WHEN settled = 1 THEN 1 ELSE 0 END), 0)        AS settledCount,
                 COALESCE(SUM(CASE WHEN settled = 0 THEN o.Amount ELSE 0 END), 0) AS outstandingTotal,
                 COALESCE(SUM(CASE WHEN settled = 0 THEN 1 ELSE 0 END), 0)        AS outstandingCount
          FROM (%s) o
        ) st
        """.formatted(JOB_COST, FUEL_COST, FUEL_LITERS, ORDER_SETTLEMENT_SUBQUERY);

    /** What the vouchers in the range were for. */
    private static final String VOUCHER_DESCRIPTION_SQL = """
        SELECT COALESCE(NULLIF(LTRIM(RTRIM(PVM.Description)), ''), '(No description)') AS descr,
               COUNT(*) AS cnt,
               SUM(PVM.Amount) AS total
        FROM PaymentVoucherMaster PVM
        WHERE PVM.CompanyRefId = ? AND PVM.Active = 1
          AND PVM.PaymentVoucherDate >= ? AND PVM.PaymentVoucherDate < ?
        GROUP BY COALESCE(NULLIF(LTRIM(RTRIM(PVM.Description)), ''), '(No description)')
        ORDER BY total DESC
        """;


    // ── Day-wise ───────────────────────────────────────────────────────────

    /**
     * Every day-wise figure, in one statement — the same UNION shape as
     * {@link #TRUCK_SPEND_SQL}, and for the same reason: this was seven queries
     * to fill one chart.
     *
     * No truck join here, deliberately. The chart is the company's day, so an
     * entry that names no truck still belongs on it; that is also why these
     * totals can exceed what the truck table accounts for.
     *
     * AutoPass, Toll and Levi all report as PASS because the chart stacks them
     * as one band, and the reader adds them per day rather than the database.
     * Money columns are CAST so the float branches cannot drag the decimal ones
     * into a binary approximation across the UNION.
     */
    private static final String DAILY_SPEND_SQL = """
        SELECT 'JOB' AS src, JOM.JobDate AS d,
               CAST(SUM(%s) AS DECIMAL(19,4)) AS total, 0 AS cnt
        FROM JobOrderMaster JOM
        WHERE JOM.CompanyRefId = :companyRefId AND JOM.IsActive = 1
          AND JOM.JobDate >= :fromDate AND JOM.JobDate <= :toDate
        GROUP BY JOM.JobDate

        UNION ALL
        SELECT 'BILL', CAST(BOM.SaleDate AS DATE),
               CAST(SUM(BOM.Amount) AS DECIMAL(19,4)), 0
        FROM BillsOrderMaster BOM
        WHERE BOM.CompanyRefId = :companyRefId AND BOM.Active = 1
          AND BOM.SaleDate >= :fromTs AND BOM.SaleDate < :toTs
        GROUP BY CAST(BOM.SaleDate AS DATE)

        UNION ALL
        SELECT 'FUEL', CAST(X.SaleDate AS DATE),
               CAST(SUM(%s) AS DECIMAL(19,4)), 0
        FROM FuelEntry X
        WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
          AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs
        GROUP BY CAST(X.SaleDate AS DATE)

        UNION ALL
        SELECT 'PASS', CAST(X.SaleDate AS DATE),
               CAST(SUM(X.Amount) AS DECIMAL(19,4)), 0
        FROM AutoPassEntry X
        WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
          AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs
        GROUP BY CAST(X.SaleDate AS DATE)

        UNION ALL
        SELECT 'PASS', CAST(X.SaleDate AS DATE),
               CAST(SUM(X.Amount) AS DECIMAL(19,4)), 0
        FROM TollEntry X
        WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
          AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs
        GROUP BY CAST(X.SaleDate AS DATE)

        UNION ALL
        SELECT 'PASS', CAST(X.SaleDate AS DATE),
               CAST(SUM(X.Amount) AS DECIMAL(19,4)), 0
        FROM LeviEntry X
        WHERE X.CompanyRefId = :companyRefId AND X.Active = 1
          AND X.SaleDate >= :fromTs AND X.SaleDate < :toTs
        GROUP BY CAST(X.SaleDate AS DATE)

        UNION ALL
        SELECT 'RTI', CAST(R.SaleDate AS DATE),
               CAST(0 AS DECIMAL(19,4)), COUNT(*)
        FROM RTIMaster R
        WHERE R.CompanyRefId = :companyRefId AND R.Active = 1
          AND R.SaleDate >= :fromTs AND R.SaleDate < :toTs
        GROUP BY CAST(R.SaleDate AS DATE)
        """.formatted(JOB_COST, FUEL_COST);

    @Override
    @Transactional(readOnly = true)
    public MaintenanceSpendDto getSpend(Integer companyRefId, LocalDate fromDate, LocalDate toDate) {
        logger.info("Building maintenance spend for company {} from {} to {}", companyRefId, fromDate, toDate);

        Timestamp fromTs = Timestamp.valueOf(fromDate.atStartOfDay());
        Timestamp toTsExclusive = Timestamp.valueOf(toDate.plusDays(1).atStartOfDay());

        /*
         * One parameter set for every named query on this screen.
         *
         * The two pairs are not interchangeable: fromDate/toDate are for the DATE
         * columns (JobDate, EntryDate) and compare inclusively, fromTs/toTs are
         * for the DATETIME columns and are half-open, so an entry stamped late on
         * the last day still counts. Sharing one map keeps that distinction in a
         * single place rather than at each call site.
         */
        Map<String, Object> params = Map.of(
                "companyRefId", companyRefId,
                "fromDate", fromDate,
                "toDate", toDate,
                "fromTs", fromTs,
                "toTs", toTsExclusive);

        // ── Truck-wise: merge the six sources on truck id ─────────────────
        Map<Integer, TruckSpend> byTruck = new LinkedHashMap<>();
        BiConsumer<Integer, String> ensureTruck = (id, name) ->
                byTruck.computeIfAbsent(id, key -> TruckSpend.builder()
                        .truckId(id)
                        .truckName(name)
                        .jobOrderAmount(BigDecimal.ZERO)
                        .purchaseAmount(BigDecimal.ZERO)
                        .fuelAmount(BigDecimal.ZERO)
                        .fuelLiters(BigDecimal.ZERO)
                        .fuelEntryCount(0L)
                        .autoPassAmount(BigDecimal.ZERO)
                        .tollAmount(BigDecimal.ZERO)
                        .leviAmount(BigDecimal.ZERO)
                        .totalAmount(BigDecimal.ZERO)
                        .rtiOrderCount(0L)
                        .build());

        /*
         * One statement, seven sources. Each branch of the UNION groups its own
         * table and tags the rows with a source name; the switch below puts each
         * group on the right field. Reading them one query at a time cost seven
         * round trips to produce one table.
         */
        namedJdbc.query(TRUCK_SPEND_SQL, params, rs -> {
            int truckId = rs.getInt("truckId");
            ensureTruck.accept(truckId, rs.getString("truckName"));
            TruckSpend truck = byTruck.get(truckId);
            BigDecimal total = nz(rs.getBigDecimal("total"));
            switch (rs.getString("src")) {
                case "JOB" -> truck.setJobOrderAmount(total);
                case "BILL" -> truck.setPurchaseAmount(total);
                case "AUTOPASS" -> truck.setAutoPassAmount(total);
                case "TOLL" -> truck.setTollAmount(total);
                case "LEVI" -> truck.setLeviAmount(total);
                case "RTI" -> truck.setRtiOrderCount(rs.getLong("cnt"));
                case "FUEL" -> {
                    truck.setFuelAmount(total);
                    truck.setFuelLiters(nz(rs.getBigDecimal("liters")));
                    truck.setFuelEntryCount(rs.getLong("cnt"));
                }
                default -> logger.warn("Unknown truck spend source {}", rs.getString("src"));
            }
        });

        List<TruckSpend> truckSpend = new ArrayList<>(byTruck.values());
        truckSpend.forEach(t -> t.setTotalAmount(t.getJobOrderAmount()
                .add(t.getPurchaseAmount())
                .add(t.getFuelAmount())
                .add(t.getAutoPassAmount())
                .add(t.getTollAmount())
                .add(t.getLeviAmount())));
        truckSpend.sort(Comparator.comparing(TruckSpend::getTotalAmount).reversed());

        // ── Group-wise breakdowns ──────────────────────────────────────────
        List<NamedSpend> jobTypeSpend = jdbcTemplate.query(JOB_TYPE_SQL, (rs, i) -> NamedSpend.builder()
                .name(rs.getString(1))
                .entryCount(rs.getLong(2))
                .totalAmount(nz(rs.getBigDecimal(3)))
                .build(), companyRefId, fromDate, toDate);

        List<NamedSpend> billDescriptionSpend = jdbcTemplate.query(BILL_DESCRIPTION_SQL, (rs, i) -> NamedSpend.builder()
                .name(rs.getString(1))
                .entryCount(rs.getLong(2))
                .totalAmount(nz(rs.getBigDecimal(3)))
                .build(), companyRefId, fromTs, toTsExclusive);

        List<MaintenanceSpendDto.PurchaseOrderDetail> purchaseOrderDetails = jdbcTemplate.query(
                BILL_DETAIL_SQL, (rs, i) -> MaintenanceSpendDto.PurchaseOrderDetail.builder()
                        .date(rs.getDate(1).toLocalDate())
                        .poNumber(rs.getString(2))
                        .description(rs.getString(3))
                        .supplierName(rs.getString(4))
                        .payTo(rs.getString(5))
                        .invoiceNo(rs.getString(6))
                        .truckName(rs.getString(7))
                        .amount(nz(rs.getBigDecimal(8)))
                        .build(), companyRefId, fromTs, toTsExclusive);

        // ── Day-wise: merge seven grouped queries on the calendar date ─────
        Map<LocalDate, DailySpend> byDay = new TreeMap<>();
        java.util.function.Function<LocalDate, DailySpend> day = d ->
                byDay.computeIfAbsent(d, key -> DailySpend.builder()
                        .date(d)
                        .jobOrderAmount(BigDecimal.ZERO)
                        .purchaseAmount(BigDecimal.ZERO)
                        .fuelAmount(BigDecimal.ZERO)
                        .passAmount(BigDecimal.ZERO)
                        .totalSpend(BigDecimal.ZERO)
                        .rtiOrderCount(0L)
                        .build());

        /*
         * Seed every calendar day in the range before the queries run.
         *
         * Without this the series only carries days that had activity, and the
         * chart plots them on a categorical axis - so a quiet week closes up and
         * the line joins two dates a fortnight apart as though they were
         * consecutive. A day-by-day chart has to keep its days, including the
         * empty ones, or the shape it draws is not the shape of the spend.
         */
        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            day.apply(d);
        }

        /*
         * Same shape as the truck query: seven sources, one statement, a source
         * tag per row. PASS accumulates because AutoPass, Toll and Levi all
         * report under it and a day can have all three.
         */
        namedJdbc.query(DAILY_SPEND_SQL, params, rs -> {
            DailySpend row = day.apply(rs.getDate("d").toLocalDate());
            BigDecimal total = nz(rs.getBigDecimal("total"));
            switch (rs.getString("src")) {
                case "JOB" -> row.setJobOrderAmount(total);
                case "BILL" -> row.setPurchaseAmount(total);
                case "FUEL" -> row.setFuelAmount(total);
                case "PASS" -> row.setPassAmount(row.getPassAmount().add(total));
                case "RTI" -> row.setRtiOrderCount(rs.getLong("cnt"));
                default -> logger.warn("Unknown daily spend source {}", rs.getString("src"));
            }
        });

        byDay.values().forEach(d -> d.setTotalSpend(
                d.getJobOrderAmount().add(d.getPurchaseAmount()).add(d.getFuelAmount()).add(d.getPassAmount())));

        // ── Every single-row figure, in one round trip ────────────────────
        Totals totals = namedJdbc.queryForObject(TOTALS_SQL, params, TOTALS_MAPPER);

        BigDecimal jobOrderTotal = totals.jobOrderTotal();
        BigDecimal billOrderTotal = totals.billOrderTotal();
        BigDecimal autoPassTotal = totals.autoPassTotal();
        BigDecimal tollTotal = totals.tollTotal();
        BigDecimal leviTotal = totals.leviTotal();

        /*
         * The remainder row: company total minus whatever the trucks account
         * for, column by column.
         *
         * Derived rather than queried on purpose. A "TruckRefid IS NULL OR = 0"
         * query would only approximate it - it would miss rows pointing at a
         * truck that has since been removed, which the truck-wise INNER JOINs
         * also drop. Subtracting is exact by construction, so the table always
         * adds up to the recorded cost however odd the data is.
         */
        TruckSpend unassignedSpend = TruckSpend.builder()
                .truckId(0)
                .truckName("Not tied to a truck")
                .jobOrderAmount(jobOrderTotal.subtract(sumOf(truckSpend, TruckSpend::getJobOrderAmount)))
                .purchaseAmount(billOrderTotal.subtract(sumOf(truckSpend, TruckSpend::getPurchaseAmount)))
                .fuelAmount(totals.fuelTotal().subtract(sumOf(truckSpend, TruckSpend::getFuelAmount)))
                .fuelLiters(totals.fuelLiters().subtract(sumOf(truckSpend, TruckSpend::getFuelLiters)))
                .fuelEntryCount(totals.fuelEntryCount()
                        - truckSpend.stream().mapToLong(TruckSpend::getFuelEntryCount).sum())
                .autoPassAmount(autoPassTotal.subtract(sumOf(truckSpend, TruckSpend::getAutoPassAmount)))
                .tollAmount(tollTotal.subtract(sumOf(truckSpend, TruckSpend::getTollAmount)))
                .leviAmount(leviTotal.subtract(sumOf(truckSpend, TruckSpend::getLeviAmount)))
                .rtiOrderCount(totals.rtiOrderCount()
                        - truckSpend.stream().mapToLong(TruckSpend::getRtiOrderCount).sum())
                .build();
        unassignedSpend.setTotalAmount(unassignedSpend.getJobOrderAmount()
                .add(unassignedSpend.getPurchaseAmount())
                .add(unassignedSpend.getFuelAmount())
                .add(unassignedSpend.getAutoPassAmount())
                .add(unassignedSpend.getTollAmount())
                .add(unassignedSpend.getLeviAmount()));


        List<NamedSpend> voucherByDescription = jdbcTemplate.query(
                VOUCHER_DESCRIPTION_SQL, (rs, i) -> NamedSpend.builder()
                        .name(rs.getString(1))
                        .entryCount(rs.getLong(2))
                        .totalAmount(nz(rs.getBigDecimal(3)))
                        .build(), companyRefId, fromTs, toTsExclusive);

        MaintenanceSpendDto.PaymentRelease paymentRelease = MaintenanceSpendDto.PaymentRelease.builder()
                .purchaseOrderTotal(totals.billOrderTotal())
                .purchaseOrderCount(totals.billOrderCount())
                .billTotal(totals.billTotal())
                .billCount(totals.billCount())
                .voucherTotal(totals.voucherTotal())
                .voucherCount(totals.voucherCount())
                .standaloneVoucherTotal(totals.standaloneVoucherTotal())
                .standaloneVoucherCount(totals.standaloneVoucherCount())
                .voucherAgainstOrderTotal(totals.voucherTotal().subtract(totals.standaloneVoucherTotal()))
                .settledTotal(totals.settledTotal())
                .settledCount(totals.settledCount())
                .outstandingTotal(totals.outstandingTotal())
                .outstandingCount(totals.outstandingCount())
                .voucherByDescription(voucherByDescription)
                .build();

        /*
         * Money out is the two documents that release it: the purchase order
         * and the payment voucher.
         *
         * The category figures below - fuel, job orders, toll, AutoPass, levi -
         * are records of what was bought, and that buying is settled through an
         * order or a voucher. Adding them to this would count the same ringgit
         * twice, which is why they are reported as a breakdown of the spend
         * rather than as parts of it. Only vouchers with no order behind them
         * are added, for the same reason.
         */
        BigDecimal grandTotal = billOrderTotal.add(totals.standaloneVoucherTotal());

        /** What the money was spent on, across the expense tables. Not the total. */
        BigDecimal recordedCostTotal = jobOrderTotal.add(billOrderTotal).add(totals.fuelTotal())
                .add(autoPassTotal).add(tollTotal).add(leviTotal);

        return MaintenanceSpendDto.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .jobOrderTotal(jobOrderTotal)
                .billOrderTotal(billOrderTotal)
                .fuelTotal(totals.fuelTotal())
                .fuelLiters(totals.fuelLiters())
                .fuelEntryCount(totals.fuelEntryCount())
                .autoPassTotal(autoPassTotal)
                .tollTotal(tollTotal)
                .leviTotal(leviTotal)
                .grandTotal(grandTotal)
                .recordedCostTotal(recordedCostTotal)
                .unassignedSpend(unassignedSpend)
                .fuelSubsidyTotal(totals.fuelSubsidyTotal())
                .fuelPaymentVoucherTotal(totals.fuelVoucherTotal())
                .paymentRelease(paymentRelease)
                .rtiOrderCount(totals.rtiOrderCount())
                .truckSpend(truckSpend)
                .jobTypeSpend(jobTypeSpend)
                .billDescriptionSpend(billDescriptionSpend)
                .dailySpend(new ArrayList<>(byDay.values()))
                .purchaseOrderDetails(purchaseOrderDetails)
                .build();
    }


    /** Column total across the truck rows, for working out the unassigned remainder. */
    private static BigDecimal sumOf(List<TruckSpend> trucks,
                                    java.util.function.Function<TruckSpend, BigDecimal> column) {
        return trucks.stream().map(column).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
