package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.module.fleet.dto.MaintenanceSpendDto;
import my.maleva.api.module.fleet.dto.MaintenanceSpendDto.DailySpend;
import my.maleva.api.module.fleet.dto.MaintenanceSpendDto.NamedSpend;
import my.maleva.api.module.fleet.dto.MaintenanceSpendDto.TruckSpend;
import my.maleva.api.module.fleet.service.MaintenanceSpendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
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
 * dashboard. Read-only native SQL through JdbcTemplate: every block is a
 * GROUP BY over one source table, merged in Java so a truck (or a date) that
 * appears in several sources becomes one row.
 *
 * Cost sources: JobOrderMaster, BillsOrderMaster (purchases), FuelEntry,
 * AutoPassEntry, TollEntry, LeviEntry. Earnings source: RTIMaster — its
 * Amount plus the surcharge columns the Driver RTI report also lists
 * (Sleeping / Pickup / Drop / Exit / EmptyDelivery / Manpower).
 *
 * Date semantics: JobOrderMaster.JobDate is a DATE and is compared inclusive;
 * every other table carries DATETIME SaleDate, compared as [from, to + 1 day).
 */
@Service
public class MaintenanceSpendServiceImpl implements MaintenanceSpendService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceSpendServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public MaintenanceSpendServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Job order cost: the actual cost once known, the estimate until then. */
    private static final String JOB_COST = "COALESCE(NULLIF(JOM.ActualCost, 0), JOM.EstimatedCost, 0)";

    /** All five FuelEntry amount buckets together. */
    private static final String FUEL_COST =
            "(COALESCE(X.AAmount,0) + COALESCE(X.PAmount,0) + COALESCE(X.GAmount,0) + COALESCE(X.DPAmount,0) + COALESCE(X.DGAmount,0))";

    private static final String FUEL_LITERS =
            "(COALESCE(X.Aliter,0) + COALESCE(X.Pliter,0) + COALESCE(X.Gliter,0) + COALESCE(X.DPliter,0) + COALESCE(X.DGliter,0))";

    // ── Truck-wise ─────────────────────────────────────────────────────────

    private static final String TRUCK_JOB_ORDER_SQL = """
        SELECT TM.Id, TM.TruckName, SUM(%s) AS total
        FROM JobOrderMaster JOM
        INNER JOIN TruckMaster TM ON JOM.TruckMasterRefId = TM.Id
        WHERE JOM.CompanyRefId = ? AND JOM.IsActive = 1
          AND JOM.JobDate >= ? AND JOM.JobDate <= ?
        GROUP BY TM.Id, TM.TruckName
        """.formatted(JOB_COST);

    /** %s is the entry table name — only ever one of the three fixed pass tables. */
    private static final String TRUCK_ENTRY_SQL_TEMPLATE = """
        SELECT TM.Id, TM.TruckName, SUM(X.Amount) AS total
        FROM %s X
        INNER JOIN TruckMaster TM ON X.TruckRefid = TM.Id
        WHERE X.CompanyRefId = ? AND X.Active = 1
          AND X.SaleDate >= ? AND X.SaleDate < ?
        GROUP BY TM.Id, TM.TruckName
        """;

    private static final String TRUCK_FUEL_SQL = """
        SELECT TM.Id, TM.TruckName, SUM(%s) AS total, SUM(%s) AS liters, COUNT(*) AS cnt
        FROM FuelEntry X
        INNER JOIN TruckMaster TM ON X.TruckRefid = TM.Id
        WHERE X.CompanyRefId = ? AND X.Active = 1
          AND X.SaleDate >= ? AND X.SaleDate < ?
        GROUP BY TM.Id, TM.TruckName
        """.formatted(FUEL_COST, FUEL_LITERS);

    private static final String TRUCK_RTI_SQL = """
        SELECT TM.Id, TM.TruckName, COUNT(*) AS cnt
        FROM RTIMaster R
        INNER JOIN TruckMaster TM ON R.TruckRefid = TM.Id
        WHERE R.CompanyRefId = ? AND R.Active = 1
          AND R.SaleDate >= ? AND R.SaleDate < ?
        GROUP BY TM.Id, TM.TruckName
        """;

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

    // ── Totals ─────────────────────────────────────────────────────────────

    private static final String JOB_ORDER_TOTAL_SQL = """
        SELECT COALESCE(SUM(%s), 0)
        FROM JobOrderMaster JOM
        WHERE JOM.CompanyRefId = ? AND JOM.IsActive = 1
          AND JOM.JobDate >= ? AND JOM.JobDate <= ?
        """.formatted(JOB_COST);

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

    private static final String BILL_TOTAL_SQL = """
        SELECT COALESCE(SUM(BOM.Amount), 0)
        FROM BillsOrderMaster BOM
        WHERE BOM.CompanyRefId = ? AND BOM.Active = 1
          AND BOM.SaleDate >= ? AND BOM.SaleDate < ?
        """;

    private static final String ENTRY_TOTAL_SQL_TEMPLATE = """
        SELECT COALESCE(SUM(X.Amount), 0)
        FROM %s X
        WHERE X.CompanyRefId = ? AND X.Active = 1
          AND X.SaleDate >= ? AND X.SaleDate < ?
        """;

    private static final String FUEL_TOTAL_SQL = """
        SELECT COALESCE(SUM(%s), 0), COALESCE(SUM(%s), 0), COUNT(*)
        FROM FuelEntry X
        WHERE X.CompanyRefId = ? AND X.Active = 1
          AND X.SaleDate >= ? AND X.SaleDate < ?
        """.formatted(FUEL_COST, FUEL_LITERS);

    private static final String RTI_TOTAL_SQL = """
        SELECT COUNT(*)
        FROM RTIMaster R
        WHERE R.CompanyRefId = ? AND R.Active = 1
          AND R.SaleDate >= ? AND R.SaleDate < ?
        """;

    // ── Day-wise ───────────────────────────────────────────────────────────

    private static final String DAILY_JOB_SQL = """
        SELECT JOM.JobDate AS d, SUM(%s) AS total
        FROM JobOrderMaster JOM
        WHERE JOM.CompanyRefId = ? AND JOM.IsActive = 1
          AND JOM.JobDate >= ? AND JOM.JobDate <= ?
        GROUP BY JOM.JobDate
        """.formatted(JOB_COST);

    private static final String DAILY_BILL_SQL = """
        SELECT CAST(BOM.SaleDate AS DATE) AS d, SUM(BOM.Amount) AS total
        FROM BillsOrderMaster BOM
        WHERE BOM.CompanyRefId = ? AND BOM.Active = 1
          AND BOM.SaleDate >= ? AND BOM.SaleDate < ?
        GROUP BY CAST(BOM.SaleDate AS DATE)
        """;

    private static final String DAILY_FUEL_SQL = """
        SELECT CAST(X.SaleDate AS DATE) AS d, SUM(%s) AS total
        FROM FuelEntry X
        WHERE X.CompanyRefId = ? AND X.Active = 1
          AND X.SaleDate >= ? AND X.SaleDate < ?
        GROUP BY CAST(X.SaleDate AS DATE)
        """.formatted(FUEL_COST);

    private static final String DAILY_ENTRY_SQL_TEMPLATE = """
        SELECT CAST(X.SaleDate AS DATE) AS d, SUM(X.Amount) AS total
        FROM %s X
        WHERE X.CompanyRefId = ? AND X.Active = 1
          AND X.SaleDate >= ? AND X.SaleDate < ?
        GROUP BY CAST(X.SaleDate AS DATE)
        """;

    private static final String DAILY_RTI_SQL = """
        SELECT CAST(R.SaleDate AS DATE) AS d, COUNT(*) AS cnt
        FROM RTIMaster R
        WHERE R.CompanyRefId = ? AND R.Active = 1
          AND R.SaleDate >= ? AND R.SaleDate < ?
        GROUP BY CAST(R.SaleDate AS DATE)
        """;

    @Override
    @Transactional(readOnly = true)
    public MaintenanceSpendDto getSpend(Integer companyRefId, LocalDate fromDate, LocalDate toDate) {
        logger.info("Building maintenance spend for company {} from {} to {}", companyRefId, fromDate, toDate);

        Timestamp fromTs = Timestamp.valueOf(fromDate.atStartOfDay());
        Timestamp toTsExclusive = Timestamp.valueOf(toDate.plusDays(1).atStartOfDay());

        // ── Truck-wise: merge the six sources on truck id ─────────────────
        Map<Integer, TruckSpend> byTruck = new LinkedHashMap<>();
        BiConsumer<Integer, String> ensureTruck = (id, name) ->
                byTruck.computeIfAbsent(id, key -> TruckSpend.builder()
                        .truckId(id)
                        .truckName(name)
                        .jobOrderAmount(BigDecimal.ZERO)
                        .fuelAmount(BigDecimal.ZERO)
                        .fuelLiters(BigDecimal.ZERO)
                        .fuelEntryCount(0L)
                        .autoPassAmount(BigDecimal.ZERO)
                        .tollAmount(BigDecimal.ZERO)
                        .leviAmount(BigDecimal.ZERO)
                        .totalAmount(BigDecimal.ZERO)
                        .rtiOrderCount(0L)
                        .build());

        jdbcTemplate.query(TRUCK_JOB_ORDER_SQL, rs -> {
            ensureTruck.accept(rs.getInt(1), rs.getString(2));
            byTruck.get(rs.getInt(1)).setJobOrderAmount(nz(rs.getBigDecimal(3)));
        }, companyRefId, fromDate, toDate);

        jdbcTemplate.query(TRUCK_FUEL_SQL, rs -> {
            ensureTruck.accept(rs.getInt(1), rs.getString(2));
            TruckSpend truck = byTruck.get(rs.getInt(1));
            truck.setFuelAmount(nz(rs.getBigDecimal(3)));
            truck.setFuelLiters(nz(rs.getBigDecimal(4)));
            truck.setFuelEntryCount(rs.getLong(5));
        }, companyRefId, fromTs, toTsExclusive);

        jdbcTemplate.query(TRUCK_ENTRY_SQL_TEMPLATE.formatted("AutoPassEntry"), rs -> {
            ensureTruck.accept(rs.getInt(1), rs.getString(2));
            byTruck.get(rs.getInt(1)).setAutoPassAmount(nz(rs.getBigDecimal(3)));
        }, companyRefId, fromTs, toTsExclusive);

        jdbcTemplate.query(TRUCK_ENTRY_SQL_TEMPLATE.formatted("TollEntry"), rs -> {
            ensureTruck.accept(rs.getInt(1), rs.getString(2));
            byTruck.get(rs.getInt(1)).setTollAmount(nz(rs.getBigDecimal(3)));
        }, companyRefId, fromTs, toTsExclusive);

        jdbcTemplate.query(TRUCK_ENTRY_SQL_TEMPLATE.formatted("LeviEntry"), rs -> {
            ensureTruck.accept(rs.getInt(1), rs.getString(2));
            byTruck.get(rs.getInt(1)).setLeviAmount(nz(rs.getBigDecimal(3)));
        }, companyRefId, fromTs, toTsExclusive);

        jdbcTemplate.query(TRUCK_RTI_SQL, rs -> {
            ensureTruck.accept(rs.getInt(1), rs.getString(2));
            byTruck.get(rs.getInt(1)).setRtiOrderCount(rs.getLong(3));
        }, companyRefId, fromTs, toTsExclusive);

        List<TruckSpend> truckSpend = new ArrayList<>(byTruck.values());
        truckSpend.forEach(t -> t.setTotalAmount(t.getJobOrderAmount()
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

        jdbcTemplate.query(DAILY_JOB_SQL, rs -> {
            day.apply(rs.getDate(1).toLocalDate()).setJobOrderAmount(nz(rs.getBigDecimal(2)));
        }, companyRefId, fromDate, toDate);

        jdbcTemplate.query(DAILY_BILL_SQL, rs -> {
            day.apply(rs.getDate(1).toLocalDate()).setPurchaseAmount(nz(rs.getBigDecimal(2)));
        }, companyRefId, fromTs, toTsExclusive);

        jdbcTemplate.query(DAILY_FUEL_SQL, rs -> {
            day.apply(rs.getDate(1).toLocalDate()).setFuelAmount(nz(rs.getBigDecimal(2)));
        }, companyRefId, fromTs, toTsExclusive);

        for (String table : new String[] {"AutoPassEntry", "TollEntry", "LeviEntry"}) {
            jdbcTemplate.query(DAILY_ENTRY_SQL_TEMPLATE.formatted(table), rs -> {
                DailySpend row = day.apply(rs.getDate(1).toLocalDate());
                row.setPassAmount(row.getPassAmount().add(nz(rs.getBigDecimal(2))));
            }, companyRefId, fromTs, toTsExclusive);
        }

        jdbcTemplate.query(DAILY_RTI_SQL, rs -> {
            day.apply(rs.getDate(1).toLocalDate()).setRtiOrderCount(rs.getLong(2));
        }, companyRefId, fromTs, toTsExclusive);

        byDay.values().forEach(d -> d.setTotalSpend(
                d.getJobOrderAmount().add(d.getPurchaseAmount()).add(d.getFuelAmount()).add(d.getPassAmount())));

        // ── Overall totals (no truck join, so entries without a truck count too) ──
        BigDecimal jobOrderTotal = scalar(JOB_ORDER_TOTAL_SQL, companyRefId, fromDate, toDate);
        BigDecimal billOrderTotal = scalar(BILL_TOTAL_SQL, companyRefId, fromTs, toTsExclusive);
        BigDecimal autoPassTotal = scalar(ENTRY_TOTAL_SQL_TEMPLATE.formatted("AutoPassEntry"), companyRefId, fromTs, toTsExclusive);
        BigDecimal tollTotal = scalar(ENTRY_TOTAL_SQL_TEMPLATE.formatted("TollEntry"), companyRefId, fromTs, toTsExclusive);
        BigDecimal leviTotal = scalar(ENTRY_TOTAL_SQL_TEMPLATE.formatted("LeviEntry"), companyRefId, fromTs, toTsExclusive);

        final BigDecimal[] fuelTotals = {BigDecimal.ZERO, BigDecimal.ZERO};
        final long[] fuelCount = {0};
        jdbcTemplate.query(FUEL_TOTAL_SQL, rs -> {
            fuelTotals[0] = nz(rs.getBigDecimal(1));
            fuelTotals[1] = nz(rs.getBigDecimal(2));
            fuelCount[0] = rs.getLong(3);
        }, companyRefId, fromTs, toTsExclusive);

        final long[] rtiCount = {0};
        jdbcTemplate.query(RTI_TOTAL_SQL, rs -> {
            rtiCount[0] = rs.getLong(1);
        }, companyRefId, fromTs, toTsExclusive);

        BigDecimal grandTotal = jobOrderTotal.add(billOrderTotal).add(fuelTotals[0])
                .add(autoPassTotal).add(tollTotal).add(leviTotal);

        return MaintenanceSpendDto.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .jobOrderTotal(jobOrderTotal)
                .billOrderTotal(billOrderTotal)
                .fuelTotal(fuelTotals[0])
                .fuelLiters(fuelTotals[1])
                .fuelEntryCount(fuelCount[0])
                .autoPassTotal(autoPassTotal)
                .tollTotal(tollTotal)
                .leviTotal(leviTotal)
                .grandTotal(grandTotal)
                .rtiOrderCount(rtiCount[0])
                .truckSpend(truckSpend)
                .jobTypeSpend(jobTypeSpend)
                .billDescriptionSpend(billDescriptionSpend)
                .dailySpend(new ArrayList<>(byDay.values()))
                .purchaseOrderDetails(purchaseOrderDetails)
                .build();
    }

    private BigDecimal scalar(String sql, Object... args) {
        BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
        return nz(value);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
