package my.maleva.api.module.dashboard.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.dashboard.dto.VesselPlanningSearchModel;
import my.maleva.api.module.dashboard.dto.F5ViewModel;
import my.maleva.api.module.dashboard.dto.VesselPlanningDashboardModel;
import my.maleva.api.module.dashboard.dto.SaleOrderInvoiceCheckModel;
import my.maleva.api.module.dashboard.dto.DashBoardMonthWiseModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Repository for dashboard-related SQL queries
 * Uses JdbcTemplate for complex queries that can't be expressed with JPA
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DashboardRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    // ========== SALES DATA QUERIES ==========

    /**
     * Get sales summary - Type 0: Invoice, Type 1: SaleOrder, Type 2: Partial, Type 3: Pending
     */
    public List<SalesSummaryRow> getSalesSummary(Integer comId, Integer type) {
        String baseQuery = switch (type) {
            case 0 -> getInvoiceSalesQuery();
            case 1 -> getAllSalesQuery();  // Type 1 = SaleOrder (all)
            case 2 -> getPartialSalesQuery();
            case 3 -> getPendingSalesQuery();
            default -> getAllSalesQuery();
        };

        // For type 0 (Invoice), we need 8 parameters. For types 1-3, we also need 8 parameters.
        return jdbcTemplate.query(baseQuery,
                (rs, rowNum) -> {
                    SalesSummaryRow row = new SalesSummaryRow();
                    row.TodaySales = rs.getInt("TodaySales");
                    row.TodayAmount = rs.getDouble("TodayAmount");
                    row.YesterdaySales = rs.getInt("YesterdaySales");
                    row.YesterdayAmount = rs.getDouble("YesterdayAmount");
                    row.WeekSales = rs.getInt("WeekSales");
                    row.WeekAmount = rs.getDouble("WeekAmount");
                    row.MonthSales = rs.getInt("MonthSales");
                    row.MonthAmount = rs.getDouble("MonthAmount");
                    return row;
                },
                comId, comId, comId, comId, comId, comId, comId, comId);
    }

    /**
     * Type 0: Invoice Sales - Uses SaleMaster table (NOT SaleOrderMaster)
     * This matches the original .NET implementation exactly
     * FIXED: Week calculation corrected to match .NET
     */
    private String getInvoiceSalesQuery() {
        return """
            SELECT
                (SELECT COUNT(Id) FROM SaleMaster WITH (NOLOCK) WHERE SaleDate = CAST(GETDATE() AS DATE) AND Active != 2 AND CompanyRefId = ?) as TodaySales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleMaster A WITH (NOLOCK), SaleDetails B WITH (NOLOCK) WHERE A.id = B.SaleMasterRefId AND A.SaleDate = CAST(GETDATE() AS DATE) AND A.Active != 2 AND A.CompanyRefId = ?) as TodayAmount,
                (SELECT COUNT(Id) FROM SaleMaster WITH (NOLOCK) WHERE SaleDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) AND Active != 2 AND CompanyRefId = ?) as YesterdaySales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleMaster A WITH (NOLOCK), SaleDetails B WITH (NOLOCK) WHERE A.id = B.SaleMasterRefId AND A.SaleDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) AND A.Active != 2 AND A.CompanyRefId = ?) as YesterdayAmount,
                (SELECT COUNT(Id) FROM SaleMaster WITH (NOLOCK) WHERE SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND DATEADD(DAY, 6 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) AND Active != 2 AND CompanyRefId = ?) as WeekSales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleMaster A WITH (NOLOCK), SaleDetails B WITH (NOLOCK) WHERE A.id = B.SaleMasterRefId AND A.SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND DATEADD(DAY, 6 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) AND A.Active != 2 AND A.CompanyRefId = ?) as WeekAmount,
                (SELECT COUNT(Id) FROM SaleMaster WITH (NOLOCK) WHERE SaleDate BETWEEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE()) AND Active != 2 AND CompanyRefId = ?) as MonthSales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleMaster A WITH (NOLOCK), SaleDetails B WITH (NOLOCK) WHERE A.id = B.SaleMasterRefId AND A.SaleDate BETWEEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE()) AND A.Active != 2 AND A.CompanyRefId = ?) as MonthAmount
            """;
    }

    private String getAllSalesQuery() {
        return """
            SELECT
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate = CAST(GETDATE() AS DATE) AND Active != 2 AND CompanyRefId = ?) as TodaySales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate = CAST(GETDATE() AS DATE) AND A.Active != 2 AND A.CompanyRefId = ?) as TodayAmount,
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) AND Active != 2 AND CompanyRefId = ?) as YesterdaySales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) AND A.Active != 2 AND A.CompanyRefId = ?) as YesterdayAmount,
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND DATEADD(DAY, 6 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) AND Active != 2 AND CompanyRefId = ?) as WeekSales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND DATEADD(DAY, 6 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) AND A.Active != 2 AND A.CompanyRefId = ?) as WeekAmount,
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate BETWEEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE()) AND Active != 2 AND CompanyRefId = ?) as MonthSales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate BETWEEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE()) AND A.Active != 2 AND A.CompanyRefId = ?) as MonthAmount
            """;
    }

    private String getPartialSalesQuery() {
        return """
            SELECT
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate = CAST(GETDATE() AS DATE) AND Active != 2 AND CompanyRefId = ? AND ((SaleDate < '2024-10-01' AND ISNULL(Remarks,'') != '') OR (SaleDate >= '2024-10-01' AND InvoiceNo != 0))) as TodaySales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate = CAST(GETDATE() AS DATE) AND A.Active != 2 AND A.CompanyRefId = ? AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') != '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo != 0))) as TodayAmount,
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) AND Active != 2 AND CompanyRefId = ? AND ((SaleDate < '2024-10-01' AND ISNULL(Remarks,'') != '') OR (SaleDate >= '2024-10-01' AND InvoiceNo != 0))) as YesterdaySales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) AND A.Active != 2 AND A.CompanyRefId = ? AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') != '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo != 0))) as YesterdayAmount,
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND DATEADD(DAY, 6 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) AND Active != 2 AND CompanyRefId = ? AND ((SaleDate < '2024-10-01' AND ISNULL(Remarks,'') != '') OR (SaleDate >= '2024-10-01' AND InvoiceNo != 0))) as WeekSales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND DATEADD(DAY, 6 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) AND A.Active != 2 AND A.CompanyRefId = ? AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') != '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo != 0))) as WeekAmount,
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate BETWEEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE()) AND Active != 2 AND CompanyRefId = ? AND ((SaleDate < '2024-10-01' AND ISNULL(Remarks,'') != '') OR (SaleDate >= '2024-10-01' AND InvoiceNo != 0))) as MonthSales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate BETWEEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE()) AND A.Active != 2 AND A.CompanyRefId = ? AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') != '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo != 0))) as MonthAmount
            """;
    }

    private String getPendingSalesQuery() {
        return """
            SELECT
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate = CAST(GETDATE() AS DATE) AND Active != 2 AND CompanyRefId = ? AND Jstatus NOT IN (8, 12) AND ((SaleDate < '2024-10-01' AND ISNULL(Remarks,'') = '') OR (SaleDate >= '2024-10-01' AND InvoiceNo = 0))) as TodaySales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate = CAST(GETDATE() AS DATE) AND A.Active != 2 AND A.CompanyRefId = ? AND A.Jstatus NOT IN (8, 12) AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') = '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo = 0))) as TodayAmount,
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) AND Active != 2 AND CompanyRefId = ? AND Jstatus NOT IN (8, 12) AND ((SaleDate < '2024-10-01' AND ISNULL(Remarks,'') = '') OR (SaleDate >= '2024-10-01' AND InvoiceNo = 0))) as YesterdaySales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleMasterRefId AND A.SaleDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) AND A.Active != 2 AND A.CompanyRefId = ? AND A.Jstatus NOT IN (8, 12) AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') = '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo = 0))) as YesterdayAmount,
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND DATEADD(DAY, 6 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) AND Active != 2 AND CompanyRefId = ? AND Jstatus NOT IN (8, 12) AND ((SaleDate < '2024-10-01' AND ISNULL(Remarks,'') = '') OR (SaleDate >= '2024-10-01' AND InvoiceNo = 0))) as WeekSales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleMasterRefId AND A.SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND DATEADD(DAY, 6 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) AND A.Active != 2 AND A.CompanyRefId = ? AND A.Jstatus NOT IN (8, 12) AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') = '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo = 0))) as WeekAmount,
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate BETWEEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE()) AND Active != 2 AND CompanyRefId = ? AND Jstatus NOT IN (8, 12) AND ((SaleDate < '2024-10-01' AND ISNULL(Remarks,'') = '') OR (SaleDate >= '2024-10-01' AND InvoiceNo = 0))) as MonthSales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleMasterRefId AND A.SaleDate BETWEEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE()) AND A.Active != 2 AND A.CompanyRefId = ? AND A.Jstatus NOT IN (8, 12) AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') = '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo != 0))) as MonthAmount
            """;
    }

    /**
     * Get monthly sales for chart (last 12 months)
     * Type 0: SaleMaster + SaleDetails
     * Type 1-3: SaleOrderMaster + SaleOrderDetails
     */
    public List<MonthlySalesRow> getMonthlySales(Integer comId, Integer type) {
        return switch (type) {
            case 0 -> getMonthlySalesType0(comId);  // SaleMaster
            case 1 -> getMonthlySalesType1(comId);  // SaleOrderMaster (all)
            case 2 -> getMonthlySalesType2(comId);  // SaleOrderMaster (partial)
            case 3 -> getMonthlySalesType3(comId);  // SaleOrderMaster (pending)
            default -> getMonthlySalesType1(comId);
        };
    }

    /**
     * Type 0: Invoice Sales - SaleMaster table (no filters)
     * Matches .NET query exactly: returns 12 rows (current month first, then previous 11 months)
     * FIXED: MonthOffset now included in SELECT statement to properly identify each month
     */
    private List<MonthlySalesRow> getMonthlySalesType0(Integer comId) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (int monthOffset = 0; monthOffset <= 11; monthOffset++) {
            int sqlMonthOffset = -monthOffset;
            if (monthOffset > 0) sql.append(" UNION ALL ");
            sql.append(
                "SELECT ? as MonthOffset, COUNT(DISTINCT A.Id) as SalesCount, ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) as SalesAmount " +
                "FROM SaleMaster A WITH (NOLOCK), SaleDetails B WITH (NOLOCK) " +
                "WHERE A.id = B.SaleMasterRefId " +
                "AND YEAR(A.SaleDate) = YEAR(DATEADD(mm, ?, GETDATE())) " +
                "AND MONTH(A.SaleDate) = MONTH(DATEADD(mm, ?, GETDATE())) " +
                "AND A.Active != 2 AND A.CompanyRefId = ?");
            params.add(monthOffset);
            params.add(sqlMonthOffset);
            params.add(sqlMonthOffset);
            params.add(comId);
        }

        List<MonthlySalesRow> results = jdbcTemplate.query(sql.toString(),
            (rs, rowNum) -> {
                MonthlySalesRow row = new MonthlySalesRow();
                row.MonthOffset = rs.getInt("MonthOffset");
                row.SalesCount = rs.getInt("SalesCount");
                row.SalesAmount = rs.getDouble("SalesAmount");
                return row;
            }, params.toArray());

        return ensureCompleteMonthlyData(results);
    }

    private List<MonthlySalesRow> getMonthlySalesType1(Integer comId) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (int monthOffset = 0; monthOffset <= 11; monthOffset++) {
            int sqlMonthOffset = -monthOffset;
            if (monthOffset > 0) sql.append(" UNION ALL ");
            sql.append(
                "SELECT ? as MonthOffset, COUNT(DISTINCT A.Id) as SalesCount, ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) as SalesAmount " +
                "FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) " +
                "WHERE A.id = B.SaleOrderMasterRefId " +
                "AND YEAR(A.SaleDate) = YEAR(DATEADD(mm, ?, GETDATE())) " +
                "AND MONTH(A.SaleDate) = MONTH(DATEADD(mm, ?, GETDATE())) " +
                "AND A.Active != 2 AND A.CompanyRefId = ?");
            params.add(monthOffset);
            params.add(sqlMonthOffset);
            params.add(sqlMonthOffset);
            params.add(comId);
        }

        List<MonthlySalesRow> results = jdbcTemplate.query(sql.toString(),
            (rs, rowNum) -> {
                MonthlySalesRow row = new MonthlySalesRow();
                row.MonthOffset = rs.getInt("MonthOffset");
                row.SalesCount = rs.getInt("SalesCount");
                row.SalesAmount = rs.getDouble("SalesAmount");
                return row;
            }, params.toArray());

        return ensureCompleteMonthlyData(results);
    }

    private List<MonthlySalesRow> getMonthlySalesType2(Integer comId) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (int monthOffset = 0; monthOffset <= 11; monthOffset++) {
            int sqlMonthOffset = -monthOffset;
            if (monthOffset > 0) sql.append(" UNION ALL ");
            sql.append(
                "SELECT ? as MonthOffset, COUNT(DISTINCT A.Id) as SalesCount, ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) as SalesAmount " +
                "FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) " +
                "WHERE A.id = B.SaleOrderMasterRefId " +
                "AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') != '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo != 0)) " +
                "AND YEAR(A.SaleDate) = YEAR(DATEADD(mm, ?, GETDATE())) " +
                "AND MONTH(A.SaleDate) = MONTH(DATEADD(mm, ?, GETDATE())) " +
                "AND A.Active != 2 AND A.CompanyRefId = ?");
            params.add(monthOffset);
            params.add(sqlMonthOffset);
            params.add(sqlMonthOffset);
            params.add(comId);
        }

        List<MonthlySalesRow> results = jdbcTemplate.query(sql.toString(),
            (rs, rowNum) -> {
                MonthlySalesRow row = new MonthlySalesRow();
                row.MonthOffset = rs.getInt("MonthOffset");
                row.SalesCount = rs.getInt("SalesCount");
                row.SalesAmount = rs.getDouble("SalesAmount");
                return row;
            }, params.toArray());

        return ensureCompleteMonthlyData(results);
    }

    private List<MonthlySalesRow> getMonthlySalesType3(Integer comId) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (int monthOffset = 0; monthOffset <= 11; monthOffset++) {
            int sqlMonthOffset = -monthOffset;
            if (monthOffset > 0) sql.append(" UNION ALL ");
            sql.append(
                "SELECT ? as MonthOffset, COUNT(DISTINCT A.Id) as SalesCount, ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) as SalesAmount " +
                "FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) " +
                "WHERE A.id = B.SaleOrderMasterRefId " +
                "AND A.Jstatus NOT IN (8, 12) " +
                "AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') = '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo = 0)) " +
                "AND YEAR(A.SaleDate) = YEAR(DATEADD(mm, ?, GETDATE())) " +
                "AND MONTH(A.SaleDate) = MONTH(DATEADD(mm, ?, GETDATE())) " +
                "AND A.Active != 2 AND A.CompanyRefId = ?");
            params.add(monthOffset);
            params.add(sqlMonthOffset);
            params.add(sqlMonthOffset);
            params.add(comId);
        }

        List<MonthlySalesRow> results = jdbcTemplate.query(sql.toString(),
            (rs, rowNum) -> {
                MonthlySalesRow row = new MonthlySalesRow();
                row.MonthOffset = rs.getInt("MonthOffset");
                row.SalesCount = rs.getInt("SalesCount");
                row.SalesAmount = rs.getDouble("SalesAmount");
                return row;
            }, params.toArray());

        return ensureCompleteMonthlyData(results);
    }

    // ========== EXPENSE DATA QUERIES ==========

    /**
     * Get expense summary - mirrors legacy GetExpData result query.
     * Uses UNION ALL with 8 sub-queries (today/yesterday/week/month for PaymentVoucherMaster + Payment).
     * Week boundary: DATEADD(DAY, 6 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE))
     */
    public List<ExpenseSummaryRow> getExpenseSummary(Integer comId) {
        String sql = """
            SELECT
                SUM(TodaySales) as TodaySales, SUM(TodayAmount) as TodayAmount,
                SUM(YesterdaySales) as YesterdaySales, SUM(YesterdayAmount) as YesterdayAmount,
                SUM(WeekSales) as WeekSales, SUM(WeekAmount) as WeekAmount,
                SUM(MonthSales) as MonthSales, SUM(MonthAmount) as MonthAmount
            FROM (
                -- PaymentVoucherMaster: Today
                SELECT COUNT(Id) as TodaySales, ISNULL(ROUND(SUM(Amount), 2), 0) as TodayAmount,
                    0 as YesterdaySales, 0.0 as YesterdayAmount, 0 as WeekSales, 0.0 as WeekAmount, 0 as MonthSales, 0.0 as MonthAmount
                FROM PaymentVoucherMaster WITH (NOLOCK)
                WHERE Active = 1 AND CompanyRefId = ? AND PaymentVoucherDate = CAST(GETDATE() AS DATE)
                UNION ALL
                -- PaymentVoucherMaster: Yesterday
                SELECT 0, 0.0, COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0), 0, 0.0, 0, 0.0
                FROM PaymentVoucherMaster WITH (NOLOCK)
                WHERE Active = 1 AND CompanyRefId = ? AND PaymentVoucherDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)
                UNION ALL
                -- PaymentVoucherMaster: Week (Mon-Sun week, same as legacy)
                SELECT 0, 0.0, 0, 0.0, COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0), 0, 0.0
                FROM PaymentVoucherMaster WITH (NOLOCK)
                WHERE Active = 1 AND CompanyRefId = ? AND PaymentVoucherDate BETWEEN
                    CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND
                    DATEADD(DAY, 6 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE))
                UNION ALL
                -- PaymentVoucherMaster: Month
                SELECT 0, 0.0, 0, 0.0, 0, 0.0, COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0)
                FROM PaymentVoucherMaster WITH (NOLOCK)
                WHERE Active = 1 AND CompanyRefId = ? AND PaymentVoucherDate BETWEEN
                    DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE())
                UNION ALL
                -- Payment: Today
                SELECT COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0), 0, 0.0, 0, 0.0, 0, 0.0
                FROM Payment WITH (NOLOCK)
                WHERE CompanyRefId = ? AND PaymentDate = CAST(GETDATE() AS DATE)
                UNION ALL
                -- Payment: Yesterday
                SELECT 0, 0.0, COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0), 0, 0.0, 0, 0.0
                FROM Payment WITH (NOLOCK)
                WHERE CompanyRefId = ? AND PaymentDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)
                UNION ALL
                -- Payment: Week (Mon-Sun week, same as legacy)
                SELECT 0, 0.0, 0, 0.0, COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0), 0, 0.0
                FROM Payment WITH (NOLOCK)
                WHERE CompanyRefId = ? AND PaymentDate BETWEEN
                    CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND
                    DATEADD(DAY, 6 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE))
                UNION ALL
                -- Payment: Month
                SELECT 0, 0.0, 0, 0.0, 0, 0.0, COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0)
                FROM Payment WITH (NOLOCK)
                WHERE CompanyRefId = ? AND PaymentDate BETWEEN
                    DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE())
            ) t
            """;
        try {
            List<ExpenseSummaryRow> results = jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    ExpenseSummaryRow row = new ExpenseSummaryRow();
                    row.TodaySales = rs.getInt("TodaySales");
                    row.TodayAmount = rs.getDouble("TodayAmount");
                    row.YesterdaySales = rs.getInt("YesterdaySales");
                    row.YesterdayAmount = rs.getDouble("YesterdayAmount");
                    row.WeekSales = rs.getInt("WeekSales");
                    row.WeekAmount = rs.getDouble("WeekAmount");
                    row.MonthSales = rs.getInt("MonthSales");
                    row.MonthAmount = rs.getDouble("MonthAmount");
                    return row;
                },
                comId, comId, comId, comId, comId, comId, comId, comId);
            log.debug("getExpenseSummary for comId={} returned {} rows", comId, results.size());
            return results;
        } catch (Exception e) {
            log.error("Error in getExpenseSummary for comId={}: {}", comId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get expense breakdown grouped by expense name.
     * Mirrors legacy GetExpData result2 query.
     * Both PaymentVoucherMaster and Payment use Description field (FIXED: was using Remarks incorrectly).
     * IMPORTANT: Matches C# implementation exactly
     */
    public List<ExpenseBreakdownRow> getExpenseBreakdown(Integer comId, String fromDate, String toDate) {
        String sql = """
            SELECT SUM(ExpCount) as ExpCount, SUM(ExpAmount) as ExpAmount, ExpenseName
            FROM (
                SELECT COUNT(Id) as ExpCount, ISNULL(ROUND(SUM(Amount), 2), 0) as ExpAmount, Description as ExpenseName
                FROM PaymentVoucherMaster WITH (NOLOCK)
                WHERE Active = 1 AND CompanyRefId = ? AND PaymentVoucherDate BETWEEN ? AND ?
                GROUP BY Description
                UNION ALL
                SELECT COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0), ISNULL(Description, '') as ExpenseName
                FROM Payment WITH (NOLOCK)
                WHERE CompanyRefId = ? AND PaymentDate BETWEEN ? AND ?
                GROUP BY ISNULL(Description, '')
            ) t
            GROUP BY ExpenseName
            """;
        try {
            List<ExpenseBreakdownRow> results = jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    ExpenseBreakdownRow row = new ExpenseBreakdownRow();
                    row.ExpCount = rs.getInt("ExpCount");
                    row.ExpAmount = rs.getDouble("ExpAmount");
                    row.ExpenseName = rs.getString("ExpenseName");
                    return row;
                },
                comId, fromDate, toDate, comId, fromDate, toDate);
            log.debug("getExpenseBreakdown for comId={}, fromDate={}, toDate={} returned {} rows",
                comId, fromDate, toDate, results.size());
            return results;
        } catch (Exception e) {
            log.error("Error in getExpenseBreakdown: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get expense details filtered by expense name.
     * Used for drill-down from expense summary to individual expense entries.
     * Both PaymentVoucherMaster and Payment use Description field (FIXED: was using Remarks incorrectly).
     * IMPORTANT: Matches C# implementation exactly
     */
    public List<ExpenseBreakdownRow> getExpenseByName(Integer comId, String fromDate, String toDate, String expenseName) {
        String sql;
        List<Object> params;

        if (expenseName == null || expenseName.trim().isEmpty() || "%".equals(expenseName.trim())) {
            // Return all expenses (same as breakdown)
            return getExpenseBreakdown(comId, fromDate, toDate);
        }

        sql = """
            SELECT SUM(ExpCount) as ExpCount, SUM(ExpAmount) as ExpAmount, ExpenseName
            FROM (
                SELECT COUNT(Id) as ExpCount, ISNULL(ROUND(SUM(Amount), 2), 0) as ExpAmount, Description as ExpenseName
                FROM PaymentVoucherMaster WITH (NOLOCK)
                WHERE Active = 1 AND CompanyRefId = ? AND PaymentVoucherDate BETWEEN ? AND ?
                AND Description = ?
                GROUP BY Description
                UNION ALL
                SELECT COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0), ISNULL(Description, '') as ExpenseName
                FROM Payment WITH (NOLOCK)
                WHERE CompanyRefId = ? AND PaymentDate BETWEEN ? AND ?
                AND ISNULL(Description, '') = ?
                GROUP BY ISNULL(Description, '')
            ) t
            GROUP BY ExpenseName
            """;
        params = List.of(comId, fromDate, toDate, expenseName, comId, fromDate, toDate, expenseName);
        try {
            List<ExpenseBreakdownRow> results = jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    ExpenseBreakdownRow row = new ExpenseBreakdownRow();
                    row.ExpCount = rs.getInt("ExpCount");
                    row.ExpAmount = rs.getDouble("ExpAmount");
                    row.ExpenseName = rs.getString("ExpenseName");
                    return row;
                }, params.toArray());
            log.debug("getExpenseByName for comId={}, expenseName={} returned {} rows",
                comId, expenseName, results.size());
            return results;
        } catch (Exception e) {
            log.error("Error in getExpenseByName: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // ========== FORWARDING DATA QUERIES ==========

    /**
     * Get forwarding summary for K1/K2/K3/K8 with Release/WithRelease counts.
     * Mirrors the legacy RT_ForwardingReport stored procedure logic.
     * Handles Forwarding, Forwarding2, Forwarding3 columns.
     */
    public ForwardingSummaryRow getForwardingSummary(Integer comId, String fromDate, String toDate) {
        String sql = """
            SELECT
                ISNULL(SUM(K1Count), 0) as K1Count,
                ISNULL(SUM(K1Release), 0) as K1Release,
                ISNULL(SUM(K1WithRelease), 0) as K1WithRelease,
                ISNULL(SUM(K2Count), 0) as K2Count,
                ISNULL(SUM(K2Release), 0) as K2Release,
                ISNULL(SUM(K2WithRelease), 0) as K2WithRelease,
                ISNULL(SUM(K3Count), 0) as K3Count,
                ISNULL(SUM(K3Release), 0) as K3Release,
                ISNULL(SUM(K3WithRelease), 0) as K3WithRelease,
                ISNULL(SUM(K8Count), 0) as K8Count,
                ISNULL(SUM(K8Release), 0) as K8Release,
                ISNULL(SUM(K8WithRelease), 0) as K8WithRelease
            FROM (
                -- K1 Forwarding counts
                SELECT COUNT(Id) as K1Count, 0 as K1Release, 0 as K1WithRelease,
                       0 as K2Count, 0 as K2Release, 0 as K2WithRelease,
                       0 as K3Count, 0 as K3Release, 0 as K3WithRelease,
                       0 as K8Count, 0 as K8Release, 0 as K8WithRelease
                FROM SaleOrderMaster WITH (NOLOCK)
                WHERE ForwardingDate BETWEEN ? AND ? AND Forwarding = 'K1' AND Active != 2 AND CompanyRefId = ?
                UNION ALL
                SELECT 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.ForwardingDate BETWEEN ? AND ? AND A.Forwarding = 'K1'
                  AND ISNULL(A.ForwardingExitRef, '') = '' AND A.Active != 2 AND A.CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.ForwardingDate BETWEEN ? AND ? AND A.Forwarding = 'K1'
                  AND ISNULL(A.ForwardingExitRef, '') != '' AND A.Active != 2 AND A.CompanyRefId = ?
                -- K2 Forwarding counts
                UNION ALL
                SELECT 0, 0, 0, COUNT(Id), 0, 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster WITH (NOLOCK)
                WHERE ForwardingDate BETWEEN ? AND ? AND Forwarding = 'K2' AND Active != 2 AND CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.ForwardingDate BETWEEN ? AND ? AND A.Forwarding = 'K2'
                  AND ISNULL(A.ForwardingExitRef, '') = '' AND A.Active != 2 AND A.CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.ForwardingDate BETWEEN ? AND ? AND A.Forwarding = 'K2'
                  AND ISNULL(A.ForwardingExitRef, '') != '' AND A.Active != 2 AND A.CompanyRefId = ?
                -- K3 Forwarding counts
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, COUNT(Id), 0, 0, 0, 0, 0
                FROM SaleOrderMaster WITH (NOLOCK)
                WHERE ForwardingDate BETWEEN ? AND ? AND Forwarding = 'K3' AND Active != 2 AND CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, COUNT(A.Id), 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.ForwardingDate BETWEEN ? AND ? AND A.Forwarding = 'K3'
                  AND ISNULL(A.ForwardingExitRef, '') = '' AND A.Active != 2 AND A.CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, 0, COUNT(A.Id), 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.ForwardingDate BETWEEN ? AND ? AND A.Forwarding = 'K3'
                  AND ISNULL(A.ForwardingExitRef, '') != '' AND A.Active != 2 AND A.CompanyRefId = ?
                -- K8 Forwarding counts
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, 0, 0, COUNT(Id), 0, 0
                FROM SaleOrderMaster WITH (NOLOCK)
                WHERE ForwardingDate BETWEEN ? AND ? AND Forwarding = 'K8' AND Active != 2 AND CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, COUNT(A.Id), 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.ForwardingDate BETWEEN ? AND ? AND A.Forwarding = 'K8'
                  AND ISNULL(A.ForwardingExitRef, '') = '' AND A.Active != 2 AND A.CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, COUNT(A.Id)
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.ForwardingDate BETWEEN ? AND ? AND A.Forwarding = 'K8'
                  AND ISNULL(A.ForwardingExitRef, '') != '' AND A.Active != 2 AND A.CompanyRefId = ?
                -- K1 Forwarding2 counts
                UNION ALL
                SELECT COUNT(Id), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster WITH (NOLOCK)
                WHERE Forwarding2Date BETWEEN ? AND ? AND Forwarding2 = 'K1' AND Active != 2 AND CompanyRefId = ?
                UNION ALL
                SELECT 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding2Date BETWEEN ? AND ? AND A.Forwarding2 = 'K1'
                  AND ISNULL(A.ForwardingExitRef2, '') = '' AND A.Active != 2 AND A.CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding2Date BETWEEN ? AND ? AND A.Forwarding2 = 'K1'
                  AND ISNULL(A.ForwardingExitRef2, '') != '' AND A.Active != 2 AND A.CompanyRefId = ?
                -- K2 Forwarding2 counts
                UNION ALL
                SELECT 0, 0, 0, COUNT(Id), 0, 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster WITH (NOLOCK)
                WHERE Forwarding2Date BETWEEN ? AND ? AND Forwarding2 = 'K2' AND Active != 2 AND CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding2Date BETWEEN ? AND ? AND A.Forwarding2 = 'K2'
                  AND ISNULL(A.ForwardingExitRef2, '') = '' AND A.Active != 2 AND A.CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding2Date BETWEEN ? AND ? AND A.Forwarding2 = 'K2'
                  AND ISNULL(A.ForwardingExitRef2, '') != '' AND A.Active != 2 AND A.CompanyRefId = ?
                -- K3 Forwarding2 counts
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, COUNT(Id), 0, 0, 0, 0, 0
                FROM SaleOrderMaster WITH (NOLOCK)
                WHERE Forwarding2Date BETWEEN ? AND ? AND Forwarding2 = 'K3' AND Active != 2 AND CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, COUNT(A.Id), 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding2Date BETWEEN ? AND ? AND A.Forwarding2 = 'K3'
                  AND ISNULL(A.ForwardingExitRef2, '') = '' AND A.Active != 2 AND A.CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, 0, COUNT(A.Id), 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding2Date BETWEEN ? AND ? AND A.Forwarding2 = 'K3'
                  AND ISNULL(A.ForwardingExitRef2, '') != '' AND A.Active != 2 AND A.CompanyRefId = ?
                -- K8 Forwarding2 counts
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, 0, 0, COUNT(Id), 0, 0
                FROM SaleOrderMaster WITH (NOLOCK)
                WHERE Forwarding2Date BETWEEN ? AND ? AND Forwarding2 = 'K8' AND Active != 2 AND CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, COUNT(A.Id), 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding2Date BETWEEN ? AND ? AND A.Forwarding2 = 'K8'
                  AND ISNULL(A.ForwardingExitRef2, '') = '' AND A.Active != 2 AND A.CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, COUNT(A.Id)
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding2Date BETWEEN ? AND ? AND A.Forwarding2 = 'K8'
                  AND ISNULL(A.ForwardingExitRef2, '') != '' AND A.Active != 2 AND A.CompanyRefId = ?
                -- K1 Forwarding3 counts
                UNION ALL
                SELECT COUNT(Id), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster WITH (NOLOCK)
                WHERE Forwarding3Date BETWEEN ? AND ? AND Forwarding3 = 'K1' AND Active != 2 AND CompanyRefId = ?
                UNION ALL
                SELECT 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding3Date BETWEEN ? AND ? AND A.Forwarding3 = 'K1'
                  AND ISNULL(A.ForwardingExitRef3, '') = '' AND A.Active != 2 AND A.CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding3Date BETWEEN ? AND ? AND A.Forwarding3 = 'K1'
                  AND ISNULL(A.ForwardingExitRef3, '') != '' AND A.Active != 2 AND A.CompanyRefId = ?
                -- K2 Forwarding3 counts
                UNION ALL
                SELECT 0, 0, 0, COUNT(Id), 0, 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster WITH (NOLOCK)
                WHERE Forwarding3Date BETWEEN ? AND ? AND Forwarding3 = 'K2' AND Active != 2 AND CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding3Date BETWEEN ? AND ? AND A.Forwarding3 = 'K2'
                  AND ISNULL(A.ForwardingExitRef3, '') = '' AND A.Active != 2 AND A.CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding3Date BETWEEN ? AND ? AND A.Forwarding3 = 'K2'
                  AND ISNULL(A.ForwardingExitRef3, '') != '' AND A.Active != 2 AND A.CompanyRefId = ?
                -- K3 Forwarding3 counts
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, COUNT(Id), 0, 0, 0, 0, 0
                FROM SaleOrderMaster WITH (NOLOCK)
                WHERE Forwarding3Date BETWEEN ? AND ? AND Forwarding3 = 'K3' AND Active != 2 AND CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, COUNT(A.Id), 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding3Date BETWEEN ? AND ? AND A.Forwarding3 = 'K3'
                  AND ISNULL(A.ForwardingExitRef3, '') = '' AND A.Active != 2 AND A.CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, 0, COUNT(A.Id), 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding3Date BETWEEN ? AND ? AND A.Forwarding3 = 'K3'
                  AND ISNULL(A.ForwardingExitRef3, '') != '' AND A.Active != 2 AND A.CompanyRefId = ?
                -- K8 Forwarding3 counts
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, 0, 0, COUNT(Id), 0, 0
                FROM SaleOrderMaster WITH (NOLOCK)
                WHERE Forwarding3Date BETWEEN ? AND ? AND Forwarding3 = 'K8' AND Active != 2 AND CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, COUNT(A.Id), 0
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding3Date BETWEEN ? AND ? AND A.Forwarding3 = 'K8'
                  AND ISNULL(A.ForwardingExitRef3, '') = '' AND A.Active != 2 AND A.CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, COUNT(A.Id)
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.Forwarding3Date BETWEEN ? AND ? AND A.Forwarding3 = 'K8'
                  AND ISNULL(A.ForwardingExitRef3, '') != '' AND A.Active != 2 AND A.CompanyRefId = ?
            ) t
            """;
        try {
            Object[] params = new Object[108];
            int idx = 0;
            for (int i = 0; i < 36; i++) {
                params[idx++] = fromDate;
                params[idx++] = toDate;
                params[idx++] = comId;
            }
            List<ForwardingSummaryRow> results = jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    ForwardingSummaryRow row = new ForwardingSummaryRow();
                    row.K1Count = rs.getInt("K1Count");
                    row.K1Release = rs.getInt("K1Release");
                    row.K1WithRelease = rs.getInt("K1WithRelease");
                    row.K2Count = rs.getInt("K2Count");
                    row.K2Release = rs.getInt("K2Release");
                    row.K2WithRelease = rs.getInt("K2WithRelease");
                    row.K3Count = rs.getInt("K3Count");
                    row.K3Release = rs.getInt("K3Release");
                    row.K3WithRelease = rs.getInt("K3WithRelease");
                    row.K8Count = rs.getInt("K8Count");
                    row.K8Release = rs.getInt("K8Release");
                    row.K8WithRelease = rs.getInt("K8WithRelease");
                    return row;
                }, params);
            if (results.isEmpty()) {
                log.warn("getForwardingSummary returned no rows for comId={}, fromDate={}, toDate={}",
                    comId, fromDate, toDate);
                return new ForwardingSummaryRow();
            }
            log.debug("getForwardingSummary for comId={} returned row with K1Count={}, K8Count={}",
                comId, results.get(0).K1Count, results.get(0).K8Count);
            return results.get(0);
        } catch (Exception e) {
            log.error("Error fetching forwarding summary for comId={}: {}", comId, e.getMessage(), e);
            return new ForwardingSummaryRow();
        }
    }

    // ========== EMPLOYEE SALES QUERY ==========

    public List<EmployeeSalesRow> getEmployeeSales(Integer comId, String whereClause) {
        String sql = String.format("""
            SELECT EM.EmployeeName, COUNT(A.Id) as SalesCount,
                ISNULL(ROUND(SUM(A.ActualNetAmount), 2), 0) as Amount
            FROM SaleOrderMaster A WITH (NOLOCK)
            LEFT JOIN EmployeeMaster EM WITH (NOLOCK) ON EM.Id = A.EmployeeRefId
            WHERE A.Active != 2 AND A.CompanyRefId = ? %s
            GROUP BY EM.EmployeeName
            """, whereClause);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EmployeeSalesRow.class), comId);
    }

    /**
     * Get top performers for current and previous month
     * @param comId Company ID
     * @param baseDate Reference date for month calculation
     * @return List of top performers with CURRENT and PREVIOUS month types
     */
    public List<TopPerformerRow> getTopPerformers(Integer comId, String baseDate) {
        String sql = """
            DECLARE @BaseDate DATE = ?;
            DECLARE @CompanyId INT = ?;
            DECLARE @CurrentMonthStart DATE = DATEFROMPARTS(YEAR(@BaseDate), MONTH(@BaseDate), 1);
            DECLARE @CurrentMonthEnd DATE = EOMONTH(@BaseDate);
            DECLARE @PreviousMonthStart DATE = DATEFROMPARTS(YEAR(DATEADD(MONTH, -1, @BaseDate)), MONTH(DATEADD(MONTH, -1, @BaseDate)), 1);
            
            WITH CurrentMonthData AS (
                SELECT 
                    'CURRENT' as MonthType,
                    em.EmployeeName,
                    SUM(sm.ActualNetAmount) AS TotalSales,
                    COUNT(DISTINCT sm.Id) AS SalesCount,
                    ROW_NUMBER() OVER (ORDER BY SUM(sm.ActualNetAmount) DESC) as Rank
                FROM SaleMaster sm WITH (NOLOCK)
                INNER JOIN SaleOrderMaster som WITH (NOLOCK) ON som.Id = sm.SaleOrderMasterNo
                INNER JOIN EmployeeMaster em WITH (NOLOCK) ON som.EmployeeRefId = em.Id
                WHERE 
                    som.Active = 1
                    AND som.CompanyRefId = @CompanyId
                    AND sm.SaleDate >= @CurrentMonthStart
                    AND sm.SaleDate <= @CurrentMonthEnd
                GROUP BY em.EmployeeName
            ),
            PreviousMonthData AS (
                SELECT 
                    'PREVIOUS' as MonthType,
                    em.EmployeeName,
                    SUM(sm.ActualNetAmount) AS TotalSales,
                    COUNT(DISTINCT sm.Id) AS SalesCount,
                    ROW_NUMBER() OVER (ORDER BY SUM(sm.ActualNetAmount) DESC) as Rank
                FROM SaleMaster sm WITH (NOLOCK)
                INNER JOIN SaleOrderMaster som WITH (NOLOCK) ON som.Id = sm.SaleOrderMasterNo
                INNER JOIN EmployeeMaster em WITH (NOLOCK) ON som.EmployeeRefId = em.Id
                WHERE 
                    som.Active = 1
                    AND som.CompanyRefId = @CompanyId
                    AND sm.SaleDate >= @PreviousMonthStart
                    AND sm.SaleDate < @CurrentMonthStart
                GROUP BY em.EmployeeName
            )
            SELECT * FROM CurrentMonthData WHERE Rank = 1
            UNION ALL
            SELECT * FROM PreviousMonthData WHERE Rank = 1
            ORDER BY MonthType DESC
            """;

        try {
            List<TopPerformerRow> results = jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    TopPerformerRow row = new TopPerformerRow();
                    row.MonthType = rs.getString("MonthType");
                    row.EmployeeName = rs.getString("EmployeeName");
                    row.TotalSales = rs.getDouble("TotalSales");
                    row.SalesCount = rs.getInt("SalesCount");
                    row.Rank = rs.getInt("Rank");
                    return row;
                },
                baseDate, comId);

            log.debug("getTopPerformers for comId={}, baseDate={} returned {} rows",
                comId, baseDate, results.size());
            return results;
        } catch (Exception e) {
            log.error("Error in getTopPerformers: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get current month sales breakdown by employee
     * Shows which employees generated the most sales in current month
     * @param comId Company ID
     * @param baseDate Reference date for current month calculation
     * @return List of employees with their current month sales totals
     */
    public List<EmployeeWiseSalesRow> getEmployeeWiseSales(Integer comId, String baseDate) {
        String sql = """
            DECLARE @BaseDate DATE = ?;
            
               SELECT 
            em.EmployeeName,
            SUM(SI.ActualNetAmount) AS CurrentMonthSales,
            COUNT(DISTINCT SI.Id) AS SalesCount
        FROM SaleMaster SI WITH (NOLOCK)
        INNER JOIN SaleOrderMaster sm WITH (NOLOCK)
            ON sm.Id = SI.SaleOrderMasterNo
        INNER JOIN EmployeeMaster em WITH (NOLOCK)
            ON sm.EmployeeRefId = em.Id
        WHERE 
            sm.Active = 1
            AND sm.CompanyRefId = ?
            AND SI.SaleDate >= DATEFROMPARTS(YEAR(@BaseDate), MONTH(@BaseDate), 1)
            AND SI.SaleDate < DATEADD(MONTH, 1, DATEFROMPARTS(YEAR(@BaseDate), MONTH(@BaseDate), 1))
        GROUP BY 
            em.EmployeeName
        ORDER BY 
            CurrentMonthSales DESC
        """;

        try {
            List<EmployeeWiseSalesRow> results = jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    EmployeeWiseSalesRow row = new EmployeeWiseSalesRow();
                    row.EmployeeName = rs.getString("EmployeeName");
                    row.CurrentMonthSales = rs.getDouble("CurrentMonthSales");
                    row.SalesCount = rs.getInt("SalesCount");
                    return row;
                },
                baseDate, comId);

            log.debug("getEmployeeWiseSales for comId={}, baseDate={} returned {} rows",
                comId, baseDate, results.size());
            return results;
        } catch (Exception e) {
            log.error("Error in getEmployeeWiseSales: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // ========== SALES ORDER STATUS QUERY ==========

    public List<SalesOrderStatusRow> getSalesOrderStatus(Integer comId, Integer employeeId) {
        String employeeFilter = employeeId != null && employeeId > 0
                ? " AND A.EmployeeRefId IN (SELECT SubEmployeeId FROM RulesTypeMaster WITH (NOLOCK) WHERE MasterEmployeeId = " + employeeId + " AND Active = 1 AND CompanyRefId = " + comId + " UNION ALL SELECT " + employeeId + ")"
                : "";

        String sql = """
            SELECT J.MId as Id, (SELECT Name FROM JobStatusMaster WHERE Id = J.Mid) as JobStatus, COUNT(A.Id) as DayCount
            FROM SaleOrderMaster A WITH (NOLOCK)
            INNER JOIN JobStatusMaster J WITH (NOLOCK) ON J.Id = A.JStatus
            WHERE A.Active = 1 AND A.CompanyRefId = ? %s AND J.Name != 'JOB COMPLET'
            GROUP BY J.MId
            UNION ALL
            SELECT 0 as Id, 'ENQUIRY' as JobStatus, COUNT(Id) as DayCount
            FROM EnquiryMaster A
            WHERE CompanyRefId = ? AND Active = 1 AND ISNULL(JobStatus, '') NOT IN ('CANCEL', 'CONFIRMED') %s
            """.formatted(employeeFilter, employeeFilter);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SalesOrderStatusRow.class), comId, comId);
    }

    // ========== PENDING PAYMENT QUERY ==========

    public List<PendingPaymentRow> getPendingPayments(Integer comId, String dueDate) {
        String sql = """
            SELECT 0 as Id, ExpenseName, SubExpenseName, Amount - paidamount as Amount,
                CASE WHEN DueDate < GETDATE() THEN 2
                     WHEN DueDate > DATEADD(DAY, -5, GETDATE()) AND DueDate <= GETDATE() THEN 1
                     ELSE 0 END as DueReportId,
                0 as DetailedId, '' as BankName, '' as AccountNo
            FROM (
                SELECT B.Name as ExpenseName, A.Description as SubExpenseName, A.DueAmount as Amount,
                    PP.DueDate,
                    ISNULL((SELECT SUM(ISNULL(PD.Amount, 0)) FROM PaymentVoucherMaster PM WITH (NOLOCK)
                        INNER JOIN PaymentVoucherDetails PD WITH (NOLOCK) ON PD.PaymentVoucherMasterRefId = PM.Id
                        WHERE PD.PendingPaymentRefId = PP.id AND PM.Active = 1 AND PD.SubExpenseRefid = A.Id), 0) as paidamount
                FROM SubExpenseMaster A WITH (NOLOCK)
                INNER JOIN ExpenseMaster B WITH (NOLOCK) ON A.ExpenseMasterRefId = B.Id
                INNER JOIN PendingPayment PP WITH (NOLOCK) ON PP.SubExpenseRefId = A.Id
                WHERE PP.DueDate < ? AND A.Active = 1 AND A.CompanyRefId = ?
            ) t
            WHERE Amount != paidamount
            """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PendingPaymentRow.class), dueDate, comId);
    }

    // ========== VESSEL PLANNING & AIR FREIGHT QUERIES ==========

    /**
     * Get vessel planning data with clean, parameterized queries
     */
    public List<VesselPlanningDashboardModel> getVesselPlanningData(VesselPlanningSearchModel search) {
        log.info("Fetching vessel planning data for company: {}", search.getComId());
        
        // Validate required fields
        if (search == null || search.getComId() == null) {
            log.warn("Invalid search model: company ID is required");
            return Collections.emptyList();
        }
        
        // Provide default toDate if not provided (use today's date)
        String toDate = search.getToDate();
        if (toDate == null || toDate.trim().isEmpty()) {
            toDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            log.debug("Using default toDate: {}", toDate);
        }

        QueryBuilder queryBuilder = new QueryBuilder()
                .addBaseParams(toDate, search.getComId(), search.getComId())
                .addEmployeeFilter(search.getEmployeeId())
                .addEtaTypeFilter(search.getEtaType(), search.getFromDate(), search.getToDate())
                .addPortSearchFilter(search.getSearch());

        return executeVesselPlanningQuery(queryBuilder, search.getEtaType());
    }

    /**
     * Get air freight data with clean, parameterized queries
     */
    public List<VesselPlanningDashboardModel> getAirFreightData(VesselPlanningSearchModel search) {
        log.info("Fetching air freight data for company: {}", search.getComId());

        // Validate required fields
        if (search == null || search.getComId() == null) {
            log.warn("Invalid search model: company ID is required");
            return Collections.emptyList();
        }

        // Provide default toDate if not provided (use today's date)
        String toDate = search.getToDate();
        if (toDate == null || toDate.trim().isEmpty()) {
            toDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            log.debug("Using default toDate: {}", toDate);
        }

        QueryBuilder queryBuilder = new QueryBuilder()
                .addBaseParams(toDate, search.getComId(), search.getComId())
                .addEmployeeFilter(search.getEmployeeId())
                .addFlightTimeFilter(search.getEtaType(), search.getFromDate(), search.getToDate())
                .addStatusFilter(search.getStatusId())
                .addPortSearchFilter(search.getSearch());

        return executeAirFreightQuery(queryBuilder);
    }

    /**
     * Check sale invoice count with clean, parameterized queries
     */
    public List<SaleOrderInvoiceCheckModel> checkSaleInvoiceCount(F5ViewModel search) {
        log.info("Checking sale invoice count for company: {}", search.getComId());

        QueryBuilder queryBuilder = new QueryBuilder()
                .addBaseParams(search.getComId())
                .addInvoiceFilter(search.getInvoice(), search.getFromDate(), search.getToDate(),
                                search.getRemarks(), search.getStatusId(), search.getCompleteStatusNotShow(),
                                search.getEmployeeId());

        return executeInvoiceCheckQuery(queryBuilder);
    }

    // ========== IMPROVED QUERY BUILDER HELPER CLASS ==========

    /**
     * Improved QueryBuilder with better type safety, validation, and maintainability
     */
    private static class QueryBuilder {
        private final StringBuilder baseWhereClause = new StringBuilder();
        private final StringBuilder unionWhereClause = new StringBuilder();
        private final List<Object> baseParameters = new ArrayList<>();
        private final List<Object> unionParameters = new ArrayList<>();
        private boolean hasBaseConditions = false;
        private boolean hasUnionConditions = false;

        // ========== FLUENT API METHODS ==========

        public QueryBuilder addBaseParams(Object... params) {
            if (params != null) {
                // Filter out null values before adding to list
                for (Object param : params) {
                    if (param != null) {
                        baseParameters.add(param);
                        unionParameters.add(param); // Also add to union for common params
                    }
                }
            }
            return this;
        }

        public QueryBuilder addEmployeeFilter(Integer employeeId) {
            return addBaseCondition(employeeId != null && employeeId > 0,
                "S.EmployeeRefId = ?", employeeId);
        }

        public QueryBuilder addEtaTypeFilter(Integer etaType, String fromDate, String toDate) {
            if (etaType == null || fromDate == null || toDate == null) {
                return this;
            }

            return switch (etaType) {
                case 1 -> addBaseCondition(true, "CAST(S.OETA AS DATE) BETWEEN ? AND ?", fromDate, toDate);
                case 2 -> addBaseCondition(true, "CAST(S.ETA AS DATE) BETWEEN ? AND ?", fromDate, toDate);
                case 5 -> addBaseCondition(true, "S.JobMasterRefId IN (1,2) AND CAST(S.FlighTime AS DATE) BETWEEN ? AND ?", fromDate, toDate);
                default -> {
                    // For etaType 3 or others: base uses ETA, union uses OETA
                    addBaseCondition(true, "CAST(S.ETA AS DATE) BETWEEN ? AND ?", fromDate, toDate);
                    addUnionCondition(true, "CAST(S.OETA AS DATE) BETWEEN ? AND ?", fromDate, toDate);
                    yield this;
                }
            };
        }

        public QueryBuilder addFlightTimeFilter(Integer etaType, String fromDate, String toDate) {
            return addBaseCondition(etaType != null && etaType == 5 && fromDate != null && toDate != null,
                "S.JobMasterRefId IN (1,2) AND CAST(S.FlighTime AS DATE) BETWEEN ? AND ?", fromDate, toDate);
        }

        public QueryBuilder addStatusFilter(Integer statusId) {
            return addBaseCondition(statusId != null && statusId > 0, "S.JStatus = ?", statusId);
        }

        public QueryBuilder addPortSearchFilter(String search) {
            if (search == null || search.trim().isEmpty()) {
                return this;
            }

            String[] ports = search.split(",");
            if (ports.length == 0) {
                return this;
            }

            // Clean and validate ports
            List<String> validPorts = Arrays.stream(ports)
                .map(String::trim)
                .filter(port -> !port.isEmpty())
                .distinct()
                .toList();

            if (validPorts.isEmpty()) {
                return this;
            }

            // Build IN clause with proper number of placeholders
            String inClause = buildInClause(validPorts.size());

            String condition = " AND (S.SPort IN (" + inClause + ") OR S.OPort IN (" + inClause + ") OR TRIM(S.AWBNo) IN (" + inClause + "))";
            baseWhereClause.append(condition);
            unionWhereClause.append(condition);

            // Add parameters for each field (SPort, OPort, AWBNo) to both base and union
            for (int i = 0; i < 3; i++) {
                baseParameters.addAll(validPorts);
                unionParameters.addAll(validPorts);
            }

            hasBaseConditions = true;
            hasUnionConditions = true;
            return this;
        }

        public QueryBuilder addInvoiceFilter(Boolean invoice, String fromDate, String toDate,
                                           Integer remarks, Integer statusId, Boolean completeStatusNotShow,
                                           Integer employeeId) {
            if (invoice != null && invoice) {
                return addBaseCondition(true, "A.InvoiceNo = 0 AND A.SaleDate >= '2024-10-01' AND A.JStatus IN (6, 15)");
            }

            // Date range filter
            if (fromDate != null && toDate != null) {
                addBaseCondition(true, "A.SaleDate BETWEEN ? AND ?", fromDate, toDate);
            }

            // Remarks filter
            if (remarks != null) {
                switch (remarks) {
                    case 1 -> addBaseCondition(true, "A.InvoiceNo != 0");
                    case 2 -> addBaseCondition(true, "A.InvoiceNo = 0");
                }
            }

            // Status filter
            if (statusId != null && statusId > 0 && Boolean.TRUE.equals(completeStatusNotShow)) {
                addBaseCondition(true, "A.JStatus = ?", statusId);
            } else if (remarks != null && remarks == 2) {
                addBaseCondition(true, "A.JStatus != 8");
            }

            // Employee hierarchy filter
            if (employeeId != null && employeeId > 0) {
                addBaseCondition(true, "A.EmployeeRefId IN (SELECT SubEmployeeId FROM RulesTypeMaster WHERE MasterEmployeeId = ? UNION ALL SELECT ?)",
                    employeeId, employeeId);
            }

            return this;
        }

        // ========== UTILITY METHODS ==========

        /**
         * Generic method to add a condition to base where clause
         */
        private QueryBuilder addBaseCondition(boolean condition, String clause, Object... params) {
            if (!condition || clause == null || clause.trim().isEmpty()) {
                return this;
            }

            baseWhereClause.append(hasBaseConditions ? " AND " : " AND ").append(clause);
            if (params != null) {
                baseParameters.addAll(List.of(params));
            }
            hasBaseConditions = true;
            return this;
        }

        /**
         * Generic method to add a condition to union where clause
         */
        private QueryBuilder addUnionCondition(boolean condition, String clause, Object... params) {
            if (!condition || clause == null || clause.trim().isEmpty()) {
                return this;
            }

            unionWhereClause.append(hasUnionConditions ? " AND " : " AND ").append(clause);
            if (params != null) {
                unionParameters.addAll(List.of(params));
            }
            hasUnionConditions = true;
            return this;
        }

        /**
         * Build IN clause with proper number of placeholders
         */
        private String buildInClause(int count) {
            if (count <= 0) {
                return "()";
            }
            return String.join(",", Collections.nCopies(count, "?"));
        }

        /**
         * Validate that all required parameters are present
         */
        public boolean isValid() {
            return !baseParameters.isEmpty() || hasBaseConditions;
        }

        /**
         * Get the complete base WHERE clause
         */
        public String getBaseWhereClause() {
            return baseWhereClause.toString();
        }

        /**
         * Get the complete union WHERE clause
         */
        public String getUnionWhereClause() {
            return unionWhereClause.toString();
        }

        /**
         * Get base parameters as type-safe array
         */
        public Object[] getBaseParameters() {
            return baseParameters.toArray(new Object[0]);
        }

        /**
         * Get union parameters as type-safe array
         */
        public Object[] getUnionParameters() {
            return unionParameters.toArray(new Object[0]);
        }

        /**
         * Get combined parameters for when no union
         */
        public Object[] getParameters() {
            return getBaseParameters();
        }

        /**
         * Get parameter count for validation
         */
        public int getParameterCount() {
            return baseParameters.size();
        }

        /**
         * Clear all conditions and parameters
         */
        public QueryBuilder clear() {
            baseWhereClause.setLength(0);
            unionWhereClause.setLength(0);
            baseParameters.clear();
            unionParameters.clear();
            hasBaseConditions = false;
            hasUnionConditions = false;
            return this;
        }

        /**
         * Create a copy of this builder
         */
        public QueryBuilder copy() {
            QueryBuilder copy = new QueryBuilder();
            copy.baseWhereClause.append(this.baseWhereClause);
            copy.unionWhereClause.append(this.unionWhereClause);
            copy.baseParameters.addAll(this.baseParameters);
            copy.unionParameters.addAll(this.unionParameters);
            copy.hasBaseConditions = this.hasBaseConditions;
            copy.hasUnionConditions = this.hasUnionConditions;
            return copy;
        }

        @Override
        public String toString() {
            return String.format("QueryBuilder{baseConditions='%s', unionConditions='%s', baseParamCount=%d, unionParamCount=%d}",
                baseWhereClause, unionWhereClause, baseParameters.size(), unionParameters.size());
        }
    }

    // ========== QUERY EXECUTION METHODS ==========

    private List<VesselPlanningDashboardModel> executeVesselPlanningQuery(QueryBuilder queryBuilder, Integer etaType) {
        validateQueryBuilder(queryBuilder);

        String sql = getVesselPlanningBaseSql() + queryBuilder.getBaseWhereClause();

        Object[] params = queryBuilder.getBaseParameters();

        // Add UNION query for additional records when etaType != 5 (same logic as C#)
        if (etaType != null && etaType != 5) {
            String unionSql = getVesselPlanningUnionSql() + queryBuilder.getUnionWhereClause();
            sql = sql + " UNION " + unionSql;
            // Duplicate parameters for union query
            Object[] unionParams = queryBuilder.getUnionParameters();
            Object[] newParams = new Object[params.length + unionParams.length];
            System.arraycopy(params, 0, newParams, 0, params.length);
            System.arraycopy(unionParams, 0, newParams, params.length, unionParams.length);
            params = newParams;
        }

        sql = sql + " ORDER BY SDId DESC";

        try {
            log.debug("Executing vessel planning query with {} parameters", params.length);
            log.debug("SQL Query: {}", sql);
            log.debug("Query Parameters: {}", Arrays.toString(params));

            return jdbcTemplate.query(sql,
                    new BeanPropertyRowMapper<>(VesselPlanningDashboardModel.class), params);
        } catch (Exception e) {
            log.error("Error executing vessel planning query: {}", e.getMessage(), e);
            log.error("SQL Query: {}", sql);
            log.error("Parameters: {}", Arrays.toString(params));
            return Collections.emptyList();
        }
    }

    private List<VesselPlanningDashboardModel> executeAirFreightQuery(QueryBuilder queryBuilder) {
        validateQueryBuilder(queryBuilder);

        String sql = getAirFreightBaseSql() + queryBuilder.getBaseWhereClause() + " ORDER BY SDId DESC";

        try {
            log.debug("Executing air freight query with {} parameters", queryBuilder.getParameterCount());
            log.debug("SQL Query: {}", sql);
            log.debug("Query Parameters: {}", Arrays.toString(queryBuilder.getParameters()));

            return jdbcTemplate.query(sql,
                    new BeanPropertyRowMapper<>(VesselPlanningDashboardModel.class), queryBuilder.getParameters());
        } catch (Exception e) {
            log.error("Error executing air freight query: {}", e.getMessage(), e);
            log.error("SQL Query: {}", sql);
            log.error("Parameters: {}", Arrays.toString(queryBuilder.getParameters()));
            return Collections.emptyList();
        }
    }

    private List<SaleOrderInvoiceCheckModel> executeInvoiceCheckQuery(QueryBuilder queryBuilder) {
        validateQueryBuilder(queryBuilder);

        String sql = getInvoiceCheckBaseSql() + queryBuilder.getBaseWhereClause() + " ORDER BY DayCount DESC";

        try {
            log.debug("Executing invoice check query with {} parameters", queryBuilder.getParameterCount());
            log.debug("SQL Query: {}", sql);
            log.debug("Query Parameters: {}", Arrays.toString(queryBuilder.getParameters()));

            return jdbcTemplate.query(sql,
                    new BeanPropertyRowMapper<>(SaleOrderInvoiceCheckModel.class), queryBuilder.getParameters());
        } catch (Exception e) {
            log.error("Error executing invoice check query: {}", e.getMessage(), e);
            log.error("SQL Query: {}", sql);
            log.error("Parameters: {}", Arrays.toString(queryBuilder.getParameters()));
            return Collections.emptyList();
        }
    }

    /**
     * Validate QueryBuilder before execution
     */
    private void validateQueryBuilder(QueryBuilder queryBuilder) {
        if (queryBuilder == null) {
            throw new IllegalArgumentException("QueryBuilder cannot be null");
        }
        if (!queryBuilder.isValid()) {
            log.warn("QueryBuilder is not valid: {}", queryBuilder);
        }
    }

    // ========== BASE SQL QUERIES ==========

    private String getVesselPlanningBaseSql() {
        return """
            SELECT DISTINCT S.Id, S.Id as SaleOrderMasterRefId, S.Origin, S.Destination, S.CNumberDisplay as JobNo,
                S.BoatCPop, S.PermitCPop, S.ForwardingCPop, S.PortCPop, S.LiveCPop, S.MMHECPop, S.AFpoCPop,
                S.PPFpoCPop, S.SFEWpoCPop, S.SFWpoCPop, S.BoatCPop1, S.PFPPCPop1, S.SCN as OSCN, S.LSCN,
                S.Vessel as VesselType, FORMAT(ISNULL(S.SaleDate,'1900-01-01'),'dd/MM/yyyy') as JobDate,
                ISNULL(J.Name,'') as JobStatus, ISNULL(S.ETA, S.OETA) as DETA,
                ISNULL(CONVERT(VARCHAR(26), S.ETA, 20), '') as SETA, S.ETA,
                ISNULL(CONVERT(VARCHAR(26), S.ETB, 20), '') as SETB, S.ETB,
                ISNULL(CONVERT(VARCHAR(26), S.ETD, 20), '') as SETD, S.ETD,
                ISNULL(CONVERT(VARCHAR(26), S.OETA, 20), '') as SOETA, S.OETA,
                ISNULL(CONVERT(VARCHAR(26), S.OETB, 20), '') as SOETB, S.OETB,
                ISNULL(CONVERT(VARCHAR(26), S.OETD, 20), '') as SOETD, S.OETD,
                ISNULL(CONVERT(VARCHAR(26), S.PickupDate, 20), '') as SPickupDate, S.PickupDate,
                ISNULL(CONVERT(VARCHAR(26), S.DeliveryDate, 20), '') as SDeliveryDate,
                ISNULL(CONVERT(VARCHAR(26), S.WareHouseEnterDate, 20), '') as SWareHouseEnterDate, S.WareHouseEnterDate,
                ISNULL(CONVERT(VARCHAR(26), S.WareHouseExitDate, 20), '') as SWareHouseExitDate, S.WareHouseExitDate,
                S.WareHouseAddress, (S.Quantity + '/' + S.TotalWeight) as Pkg, S.LoadingVesselName, S.BLCopy,
                S.TruckSize, S.SCN, S.LSCN, S.OffVesselName, S.Commodity, S.Vessel, S.OVessel, S.SPort,
                S.SPort as Port, S.OPort, AC.Name as AgentCompany, OC.Name as OAgentCompany, JT.Name as JobName,
                S.AWBNo, S.Remarks as Remarks1, S.Cargo,
                ISNULL(S.PTW, '') as PTW, ISNULL(S.ZB, '') as ZB, ISNULL(S.ZB2, '') as ZB2,
                ISNULL(S.ZBRef, '') as ZBRef, ISNULL(S.ZBRef2, '') as ZBRef2,
                ISNULL(S.PortCharges, '') as PortCharges, ISNULL(S.PortChargesRef, '') as PortChargesRef,
                ISNULL(Ag.AgentName, '') as AgentName, ISNULL(Ag.MobileNo, '') as AgentPhone,
                ISNULL(OAg.AgentName, '') as OAgentName, ISNULL(OAg.MobileNo, '') as OAgentPhone,
                ISNULL(S.BoardingOfficerRefId, 0) as BoardingOfficerRefId, ISNULL(EB.EmployeeName, '') as BoardingOfficerName,
                ISNULL(S.BoardingOfficer1RefId, 0) as BoardingOfficer1RefId, ISNULL(EB1.EmployeeName, '') as BoardingOfficerName1,
                S.BoardingAmount, S.BoardingAmount1, C.CustomerName, ISNULL(E.EmployeeName, '') as EmployeeName,
                '' as Remarks, CASE WHEN CAST(S.ETA AS DATE) < ? THEN 1 ELSE 0 END AS SDId
            FROM SaleOrderMaster S WITH (NOLOCK)
            INNER JOIN Customer C WITH (NOLOCK) ON C.Id = S.CustomerRefId
            INNER JOIN JobTypeMaster JT WITH (NOLOCK) ON JT.Id = S.JobMasterRefId
            LEFT JOIN JobStatusMaster J WITH (NOLOCK) ON J.Id = S.JStatus
            LEFT JOIN EmployeeMaster E WITH (NOLOCK) ON E.Id = S.EmployeeRefId
            LEFT JOIN Agent Ag WITH (NOLOCK) ON Ag.Id = S.AgentMasterRefId
            LEFT JOIN Agent OAg WITH (NOLOCK) ON OAg.Id = S.OAgentMasterRefId
            LEFT JOIN AgentCompanyMaster AC WITH (NOLOCK) ON AC.Id = S.AgentCompanyRefId
            LEFT JOIN AgentCompanyMaster OC WITH (NOLOCK) ON OC.Id = S.OAgentCompanyRefId
            LEFT JOIN EmployeeMaster EB WITH (NOLOCK) ON EB.Id = S.BoardingOfficerRefId
            LEFT JOIN EmployeeMaster EB1 WITH (NOLOCK) ON EB1.Id = S.BoardingOfficer1RefId
            WHERE S.CompanyRefId = ? AND S.Active != 2
            AND S.JStatus NOT IN (6, 5, 20, 8, 15, 12, 16, 19)
            AND S.JStatus NOT IN (SELECT Id FROM JobStatusMaster WHERE MId = 5 AND Active = 1 AND CompanyRefId = ?)
            """;
    }

    private String getVesselPlanningUnionSql() {
        return """
            SELECT DISTINCT S.Id, S.Id as SaleOrderMasterRefId, S.Origin, S.Destination, S.CNumberDisplay as JobNo,
                S.BoatCPop, S.PermitCPop, S.ForwardingCPop, S.PortCPop, S.LiveCPop, S.MMHECPop, S.AFpoCPop,
                S.PPFpoCPop, S.SFEWpoCPop, S.SFWpoCPop, S.BoatCPop1, S.PFPPCPop1, S.SCN as OSCN, S.LSCN,
                S.OVessel as VesselType, FORMAT(ISNULL(S.SaleDate,'1900-01-01'),'dd/MM/yyyy') as JobDate,
                ISNULL(J.Name,'') as JobStatus, ISNULL(S.ETA, S.OETA) as DETA,
                ISNULL(CONVERT(VARCHAR(26), S.ETA, 20), '') as SETA, S.ETA,
                ISNULL(CONVERT(VARCHAR(26), S.ETB, 20), '') as SETB, S.ETB,
                ISNULL(CONVERT(VARCHAR(26), S.ETD, 20), '') as SETD, S.ETD,
                ISNULL(CONVERT(VARCHAR(26), S.OETA, 20), '') as SOETA, S.OETA,
                ISNULL(CONVERT(VARCHAR(26), S.OETB, 20), '') as SOETB, S.OETB,
                ISNULL(CONVERT(VARCHAR(26), S.OETD, 20), '') as SOETD, S.OETD,
                ISNULL(CONVERT(VARCHAR(26), S.PickupDate, 20), '') as SPickupDate, S.PickupDate,
                ISNULL(CONVERT(VARCHAR(26), S.DeliveryDate, 20), '') as SDeliveryDate,
                ISNULL(CONVERT(VARCHAR(26), S.WareHouseEnterDate, 20), '') as SWareHouseEnterDate, S.WareHouseEnterDate,
                ISNULL(CONVERT(VARCHAR(26), S.WareHouseExitDate, 20), '') as SWareHouseExitDate, S.WareHouseExitDate,
                S.WareHouseAddress, (S.Quantity + '/' + S.TotalWeight) as Pkg, S.OffVesselName as LoadingVesselName, S.BLCopy,
                S.TruckSize, S.SCN, S.LSCN, '' as OffVesselName, S.Commodity, S.Vessel, S.OVessel, S.SPort,
                S.OPort as Port, S.OPort, AC.Name as AgentCompany, OC.Name as OAgentCompany, JT.Name as JobName,
                S.AWBNo, S.Remarks as Remarks1, S.Cargo,
                ISNULL(S.PTW, '') as PTW, ISNULL(S.ZB, '') as ZB, ISNULL(S.ZB2, '') as ZB2,
                ISNULL(S.ZBRef, '') as ZBRef, ISNULL(S.ZBRef2, '') as ZBRef2,
                ISNULL(S.PortCharges, '') as PortCharges, ISNULL(S.PortChargesRef, '') as PortChargesRef,
                ISNULL(Ag.AgentName, '') as AgentName, ISNULL(Ag.MobileNo, '') as AgentPhone,
                ISNULL(OAg.AgentName, '') as OAgentName, ISNULL(OAg.MobileNo, '') as OAgentPhone,
                ISNULL(S.BoardingOfficerRefId, 0) as BoardingOfficerRefId, ISNULL(EB.EmployeeName, '') as BoardingOfficerName,
                ISNULL(S.BoardingOfficer1RefId, 0) as BoardingOfficer1RefId, ISNULL(EB1.EmployeeName, '') as BoardingOfficerName1,
                S.BoardingAmount, S.BoardingAmount1, C.CustomerName, ISNULL(E.EmployeeName, '') as EmployeeName,
                '' as Remarks, CASE WHEN CAST(S.OETA AS DATE) < ? THEN 1 ELSE 0 END AS SDId
            FROM SaleOrderMaster S WITH (NOLOCK)
            INNER JOIN Customer C WITH (NOLOCK) ON C.Id = S.CustomerRefId
            INNER JOIN JobTypeMaster JT WITH (NOLOCK) ON JT.Id = S.JobMasterRefId
            LEFT JOIN JobStatusMaster J WITH (NOLOCK) ON J.Id = S.JStatus
            LEFT JOIN EmployeeMaster E WITH (NOLOCK) ON E.Id = S.EmployeeRefId
            LEFT JOIN Agent Ag WITH (NOLOCK) ON Ag.Id = S.AgentMasterRefId
            LEFT JOIN Agent OAg WITH (NOLOCK) ON OAg.Id = S.OAgentMasterRefId
            LEFT JOIN AgentCompanyMaster AC WITH (NOLOCK) ON AC.Id = S.AgentCompanyRefId
            LEFT JOIN AgentCompanyMaster OC WITH (NOLOCK) ON OC.Id = S.OAgentCompanyRefId
            LEFT JOIN EmployeeMaster EB WITH (NOLOCK) ON EB.Id = S.BoardingOfficerRefId
            LEFT JOIN EmployeeMaster EB1 WITH (NOLOCK) ON EB1.Id = S.BoardingOfficer1RefId
            WHERE S.CompanyRefId = ? AND S.Active != 2
            AND S.JStatus NOT IN (6, 5, 20, 8, 15, 12, 16, 9, 4, 19, 22)
            AND S.JStatus NOT IN (SELECT Id FROM JobStatusMaster WHERE MId = 5 AND Active = 1 AND CompanyRefId = ?)
            """;
    }

    private String getInvoiceCheckBaseSql() {
        return """
            SELECT A.Id, A.Remarks, A.JobMasterRefId, ISNULL(E.EmployeeName,'') as EmployeeName,
                A.OffVesselName, A.LoadingVesselName, A.SPort, A.OPort,
                FORMAT(ISNULL(A.SaleDate,'1900-01-01'),'dd/MM/yyyy') as BillDate, A.ETA,
                ISNULL(FORMAT(A.ETA, 'dd/MM/yyyy HH:mm:ss'), '') as SETA,
                ISNULL(FORMAT(A.ETB, 'dd/MM/yyyy HH:mm:ss'), '') as SETB,
                ISNULL(FORMAT(A.OETA, 'dd/MM/yyyy HH:mm:ss'), '') as SOETA,
                ISNULL(FORMAT(A.OETB, 'dd/MM/yyyy HH:mm:ss'), '') as SOETB,
                ISNULL(CONVERT(VARCHAR(26), A.PickupDate, 20),'') as SPickupDate,
                A.CNumberDisplay as BillNoDisplay,
                FORMAT(ISNULL(A.Created_Date,'1900-01-01'),'dd/MM/yyyy hh:mm:ss') as BillTime,
                B.CustomerName as CustomerName, A.Amount as NetAmt, A.SaleType as SaleType,
                A.CNumber as BillNo, ISNULL(J.Name,'') as JobStatus,
                ISNULL(SM.CNumberDisplay,'') as InvoiceNo, ISNULL(SM.QNECode,'') as QNECode,
                ISNULL(SM.QNEId,'') as QNEId,
                DATEDIFF(DAY, A.CompletedDate, GETDATE()) AS DayCount
            FROM SaleOrderMaster A WITH (NOLOCK)
            INNER JOIN Customer B WITH (NOLOCK) ON A.CustomerRefId = B.Id
            LEFT JOIN EmployeeMaster E WITH (NOLOCK) ON E.Id = A.EmployeeRefId
            LEFT JOIN JobStatusMaster J WITH (NOLOCK) ON J.Id = A.JStatus
            LEFT JOIN SaleMaster SM WITH (NOLOCK) ON SM.Id = A.InvoiceNo
            WHERE A.CompanyRefId = ? AND A.Active = 1
            """;
    }

    private String getAirFreightBaseSql() {
        return """
            SELECT DISTINCT S.Id, S.Id as SaleOrderMasterRefId, S.Origin, S.Destination, S.CNumberDisplay as JobNo,
                S.BoatCPop, S.PermitCPop, S.ForwardingCPop, S.PortCPop, S.LiveCPop, S.MMHECPop, S.AFpoCPop,
                S.PPFpoCPop, S.SFEWpoCPop, S.SFWpoCPop, S.BoatCPop1, S.PFPPCPop1, S.SCN as OSCN, S.LSCN,
                S.Vessel as VesselType, FORMAT(ISNULL(S.SaleDate,'1900-01-01'),'dd/MM/yyyy') as JobDate,
                ISNULL(J.Name,'') as JobStatus, ISNULL(S.FlighTime, S.OETA) as DETA,
                ISNULL(CONVERT(VARCHAR(26), S.FlighTime, 20), '') as SETA, S.FlighTime as ETA,
                ISNULL(CONVERT(VARCHAR(26), S.ETB, 20), '') as SETB, S.ETB,
                ISNULL(CONVERT(VARCHAR(26), S.ETD, 20), '') as SETD, S.ETD,
                ISNULL(CONVERT(VARCHAR(26), S.OETA, 20), '') as SOETA, S.OETA,
                ISNULL(CONVERT(VARCHAR(26), S.OETB, 20), '') as SOETB, S.OETB,
                ISNULL(CONVERT(VARCHAR(26), S.OETD, 20), '') as SOETD, S.OETD,
                ISNULL(CONVERT(VARCHAR(26), S.PickupDate, 20), '') as SPickupDate, S.PickupDate,
                ISNULL(CONVERT(VARCHAR(26), S.DeliveryDate, 20), '') as SDeliveryDate,
                ISNULL(CONVERT(VARCHAR(26), S.WareHouseEnterDate, 20), '') as SWareHouseEnterDate, S.WareHouseEnterDate,
                ISNULL(CONVERT(VARCHAR(26), S.WareHouseExitDate, 20), '') as SWareHouseExitDate, S.WareHouseExitDate,
                S.WareHouseAddress, (S.Quantity + '/' + S.TotalWeight) as Pkg, S.LoadingVesselName, S.BLCopy,
                S.TruckSize, S.SCN, S.LSCN, S.OffVesselName, S.Commodity, S.Vessel, S.OVessel, S.SPort,
                S.SPort as Port, S.OPort, AC.Name as AgentCompany, OC.Name as OAgentCompany, JT.Name as JobName,
                S.AWBNo, S.Remarks as Remarks1, S.Cargo,
                ISNULL(S.PTW, '') as PTW, ISNULL(S.ZB, '') as ZB, ISNULL(S.ZB2, '') as ZB2,
                ISNULL(S.ZBRef, '') as ZBRef, ISNULL(S.ZBRef2, '') as ZBRef2,
                ISNULL(S.PortCharges, '') as PortCharges, ISNULL(S.PortChargesRef, '') as PortChargesRef,
                ISNULL(Ag.AgentName, '') as AgentName, ISNULL(Ag.MobileNo, '') as AgentPhone,
                ISNULL(OAg.AgentName, '') as OAgentName, ISNULL(OAg.MobileNo, '') as OAgentPhone,
                ISNULL(S.BoardingOfficerRefId, 0) as BoardingOfficerRefId, ISNULL(EB.EmployeeName, '') as BoardingOfficerName,
                ISNULL(S.BoardingOfficer1RefId, 0) as BoardingOfficer1RefId, ISNULL(EB1.EmployeeName, '') as BoardingOfficerName1,
                S.BoardingAmount, S.BoardingAmount1, C.CustomerName, ISNULL(E.EmployeeName, '') as EmployeeName,
                '' as Remarks, CASE WHEN CAST(S.FlighTime AS DATE) < ? THEN 1 ELSE 0 END AS SDId
            FROM SaleOrderMaster S WITH (NOLOCK)
            INNER JOIN Customer C WITH (NOLOCK) ON C.Id = S.CustomerRefId
            INNER JOIN JobTypeMaster JT WITH (NOLOCK) ON JT.Id = S.JobMasterRefId
            LEFT JOIN JobStatusMaster J WITH (NOLOCK) ON J.Id = S.JStatus
            LEFT JOIN EmployeeMaster E WITH (NOLOCK) ON E.Id = S.EmployeeRefId
            LEFT JOIN Agent Ag WITH (NOLOCK) ON Ag.Id = S.AgentMasterRefId
            LEFT JOIN Agent OAg WITH (NOLOCK) ON OAg.Id = S.OAgentMasterRefId
            LEFT JOIN AgentCompanyMaster AC WITH (NOLOCK) ON AC.Id = S.AgentCompanyRefId
            LEFT JOIN AgentCompanyMaster OC WITH (NOLOCK) ON OC.Id = S.OAgentCompanyRefId
            LEFT JOIN EmployeeMaster EB WITH (NOLOCK) ON EB.Id = S.BoardingOfficerRefId
            LEFT JOIN EmployeeMaster EB1 WITH (NOLOCK) ON EB1.Id = S.BoardingOfficer1RefId
            WHERE S.CompanyRefId = ? AND S.Active != 2
            AND S.JobMasterRefId IN (1, 2)
            AND S.JStatus NOT IN (6, 5, 20, 8, 15, 12, 16, 19)
            AND S.JStatus NOT IN (SELECT Id FROM JobStatusMaster WHERE MId = 5 AND Active = 1 AND CompanyRefId = ?)
            AND S.JStatus != ''
            """;
    }

    // ========== HELPER METHODS ==========

    /**
     * Ensure complete monthly data for all 12 months (0-11)
     * Fills missing months with 0 sales and 0 amount
     */
    private List<MonthlySalesRow> ensureCompleteMonthlyData(List<MonthlySalesRow> results) {
        // Create a map for quick lookup
        Map<Integer, MonthlySalesRow> monthMap = new HashMap<>();
        for (MonthlySalesRow row : results) {
            if (row.MonthOffset != null) {
                monthMap.put(row.MonthOffset, row);
            }
        }

        // Build complete list with all 12 months (0-11)
        List<MonthlySalesRow> completeData = new ArrayList<>();
        for (int monthOffset = 0; monthOffset <= 11; monthOffset++) {
            if (monthMap.containsKey(monthOffset)) {
                completeData.add(monthMap.get(monthOffset));
            } else {
                // Add zero-filled row for missing month
                MonthlySalesRow zeroRow = new MonthlySalesRow();
                zeroRow.MonthOffset = monthOffset;
                zeroRow.SalesCount = 0;
                zeroRow.SalesAmount = 0.0;
                completeData.add(zeroRow);
            }
        }

        return completeData;
    }

    // ========== HELPER CLASSES ==========

    public static class SalesSummaryRow {
        public Integer TodaySales;
        public Double TodayAmount;
        public Integer YesterdaySales;
        public Double YesterdayAmount;
        public Integer WeekSales;
        public Double WeekAmount;
        public Integer MonthSales;
        public Double MonthAmount;
    }

    public static class MonthlySalesRow {
        public Integer MonthOffset;
        public Integer SalesCount;
        public Double SalesAmount;
    }

    public static class ExpenseSummaryRow {
        public Integer TodaySales;
        public Double TodayAmount;
        public Integer YesterdaySales;
        public Double YesterdayAmount;
        public Integer WeekSales;
        public Double WeekAmount;
        public Integer MonthSales;
        public Double MonthAmount;
    }

    public static class ExpenseBreakdownRow {
        public Integer ExpCount;
        public Double ExpAmount;
        public String ExpenseName;
    }

    public static class ForwardingSummaryRow {
        public Integer K1Count = 0;
        public Integer K1Release = 0;
        public Integer K1WithRelease = 0;
        public Integer K2Count = 0;
        public Integer K2Release = 0;
        public Integer K2WithRelease = 0;
        public Integer K3Count = 0;
        public Integer K3Release = 0;
        public Integer K3WithRelease = 0;
        public Integer K8Count = 0;
        public Integer K8Release = 0;
        public Integer K8WithRelease = 0;
    }

    public static class EmployeeSalesRow {
        public String EmployeeName;
        public Integer SalesCount;
        public Double Amount;
    }

    public static class SalesOrderStatusRow {
        public Integer Id;
        public String JobStatus;
        public Integer DayCount;
    }

    public static class PendingPaymentRow {
        public Integer Id;
        public String ExpenseName;
        public String SubExpenseName;
        public Double Amount;
        public String DueDate;
        public Integer DueReportId;
        public Integer DetailedId;
        public String BankName;
        public String AccountNo;
    }

    public static class EmployeeWiseSalesRow {
        public String EmployeeName;
        public Double CurrentMonthSales;
        public Integer SalesCount;
    }

    public static class TopPerformerRow {
        public String MonthType;      // "CURRENT" or "PREVIOUS"
        public String EmployeeName;
        public Double TotalSales;
        public Integer SalesCount;
        public Integer Rank;          // ROW_NUMBER for each month
    }

    /**
     * Search planning details with dynamic filters.
     * Matches C# GetPlaningSearchDbDetails with GroupBy/OrderByDescending in service layer.
     *
     * @param request search filters (company, dates, ports, employee)
     * @return list of planning detail rows
     */
    public List<my.maleva.api.module.planning.dto.PlanningDetailsModel> getPlaningSearchDbDetails(
            my.maleva.api.module.planning.dto.request.PLANINGSearchRequestDto request) {

        // Base query using Java 17 text block for readability
        String baseQuery = """
                SELECT S.Id,
                       ISNULL(E.EmployeeName, '') AS EmployeeName,
                       S.PickupDate,
                       S.CNumberDisplay AS JobNo,
                       ISNULL(CONVERT(VARCHAR(26), S.PickupDate, 20), '')   AS SPickupDate,
                       ISNULL(CONVERT(VARCHAR(26), S.DeliveryDate, 20), '') AS SDeliveryDate,
                       S.WareHouseEnterDate,
                       S.WareHouseExitDate,
                       ISNULL(CONVERT(VARCHAR(26), S.WareHouseEnterDate, 20), '') AS SWareHouseEnterDate,
                       ISNULL(CONVERT(VARCHAR(26), S.WareHouseExitDate, 20), '')  AS SWareHouseExitDate,
                       S.WareHouseAddress,
                       S.Origin,
                       S.Destination,
                       (S.Quantity + '/' + S.TotalWeight) AS Pkg,
                       S.LoadingVesselName AS VesselName,
                       FORMAT(ISNULL(S.SaleDate, '1900-01-01'), 'dd/MM/yyyy') AS JobDate,
                       C.CustomerName,
                       (0)  AS TruckRefid,
                       ('')  AS Remarks,
                       ISNULL(J.Name, '') AS JobStatus,
                       S.PickupAddress,
                       S.DeliveryAddress,
                       ISNULL(CONVERT(VARCHAR(26), S.ETA, 20), '')  AS LETA,
                       ISNULL(CONVERT(VARCHAR(26), S.OETA, 20), '') AS OETA,
                       JT.Name AS JobName,
                       S.AWBNo,
                       S.BLCopy,
                       S.SPort,
                       S.OPort,
                       S.TruckSize,
                       S.DODescription,
                       CASE WHEN CAST(S.PickupDate AS DATE) = :fromDate THEN 0 ELSE 1 END AS SDId,
                       TM.TruckName
                FROM SaleOrderMaster S WITH (NOLOCK)
                INNER JOIN Customer C WITH (NOLOCK) ON C.Id = S.CustomerRefId
                INNER JOIN JobTypeMaster JT WITH (NOLOCK) ON JT.Id = S.JobMasterRefId
                LEFT JOIN JobStatusMaster J WITH (NOLOCK) ON J.Id = S.JStatus
                LEFT JOIN EmployeeMaster E WITH (NOLOCK) ON E.Id = S.EmployeeRefId
                LEFT JOIN PlaningDetails PD WITH (NOLOCK) ON PD.SaleOrderMasterRefId = S.Id
                LEFT JOIN TruckMaster TM WITH (NOLOCK) ON PD.TruckRefid = TM.Id
                WHERE S.CompanyRefId = :comId
                  AND S.Active != 2
                  AND CAST(S.PickupDate AS DATE) BETWEEN :fromDate AND :toDate
                  AND S.JStatus NOT IN (5, 6, 8, 12, 15,19)
                """;

        // Build dynamic WHERE clauses
        StringBuilder query = new StringBuilder(baseQuery);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("comId", request.getComid());

        // Defensive checks to prevent SQL Server conversion errors for empty dates
        String fromDateStr = request.getFromdate();
        if (fromDateStr == null || fromDateStr.trim().isEmpty()) {
            fromDateStr = "1900-01-01"; // SQL Server safe min date
        }
        
        String toDateStr = request.getTodate();
        if (toDateStr == null || toDateStr.trim().isEmpty()) {
            toDateStr = "2099-12-31"; // SQL Server safe max date
        }
        
        params.addValue("fromDate", fromDateStr);
        params.addValue("toDate", toDateStr);

        // Optional: filter by ports (SPort / OPort)
        if (request.getSearch() != null && !request.getSearch().isEmpty()) {
            List<String> portsList = Arrays.asList(request.getSearch().split(","));
            query.append("AND (S.SPort IN (:ports) OR S.OPort IN (:ports)) ");
            params.addValue("ports", portsList);
        }

        // Optional: filter by employee
        if (request.getEmployeeid() != null && !request.getEmployeeid().isEmpty()
                && !"0".equals(request.getEmployeeid())) {
            try {
                params.addValue("empId", Integer.parseInt(request.getEmployeeid()));
                query.append("AND S.EmployeeRefId = :empId ");
            } catch (NumberFormatException e) {
                log.warn("Invalid employeeId '{}', skipping employee filter", request.getEmployeeid());
            }
        }

        return namedJdbcTemplate.query(
                query.toString(),
                params,
                new BeanPropertyRowMapper<>(my.maleva.api.module.planning.dto.PlanningDetailsModel.class)
        );
    }
}
