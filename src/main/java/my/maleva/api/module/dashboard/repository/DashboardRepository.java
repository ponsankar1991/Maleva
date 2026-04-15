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
                new BeanPropertyRowMapper<>(SalesSummaryRow.class),
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
                (SELECT COUNT(Id) FROM SaleMaster WITH (NOLOCK) WHERE SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND CAST(GETDATE() AS DATE) AND Active != 2 AND CompanyRefId = ?) as WeekSales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleMaster A WITH (NOLOCK), SaleDetails B WITH (NOLOCK) WHERE A.id = B.SaleMasterRefId AND A.SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND CAST(GETDATE() AS DATE) AND A.Active != 2 AND A.CompanyRefId = ?) as WeekAmount,
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
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND CAST(GETDATE() AS DATE) AND Active != 2 AND CompanyRefId = ?) as WeekSales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND CAST(GETDATE() AS DATE) AND A.Active != 2 AND A.CompanyRefId = ?) as WeekAmount,
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
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND CAST(GETDATE() AS DATE) AND Active != 2 AND CompanyRefId = ? AND ((SaleDate < '2024-10-01' AND ISNULL(Remarks,'') != '') OR (SaleDate >= '2024-10-01' AND InvoiceNo != 0))) as WeekSales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND CAST(GETDATE() AS DATE) AND A.Active != 2 AND A.CompanyRefId = ? AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') != '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo != 0))) as WeekAmount,
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
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) AND A.Active != 2 AND A.CompanyRefId = ? AND A.Jstatus NOT IN (8, 12) AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') = '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo = 0))) as YesterdayAmount,
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND CAST(GETDATE() AS DATE) AND Active != 2 AND CompanyRefId = ? AND Jstatus NOT IN (8, 12) AND ((SaleDate < '2024-10-01' AND ISNULL(Remarks,'') = '') OR (SaleDate >= '2024-10-01' AND InvoiceNo = 0))) as WeekSales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND CAST(GETDATE() AS DATE) AND A.Active != 2 AND A.CompanyRefId = ? AND A.Jstatus NOT IN (8, 12) AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') = '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo = 0))) as WeekAmount,
                (SELECT COUNT(Id) FROM SaleOrderMaster WITH (NOLOCK) WHERE SaleDate BETWEEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE()) AND Active != 2 AND CompanyRefId = ? AND Jstatus NOT IN (8, 12) AND ((SaleDate < '2024-10-01' AND ISNULL(Remarks,'') = '') OR (SaleDate >= '2024-10-01' AND InvoiceNo = 0))) as MonthSales,
                (SELECT ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) WHERE A.id = B.SaleOrderMasterRefId AND A.SaleDate BETWEEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE()) AND A.Active != 2 AND A.CompanyRefId = ? AND A.Jstatus NOT IN (8, 12) AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') = '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo = 0))) as MonthAmount
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

        for (int i = 0; i >= -11; i--) {
            if (i < 0) sql.append(" UNION ALL ");
            sql.append(
                "SELECT COUNT(DISTINCT A.Id) as SalesCount, ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) as SalesAmount " +
                "FROM SaleMaster A WITH (NOLOCK), SaleDetails B WITH (NOLOCK) " +
                "WHERE A.id = B.SaleMasterRefId " +
                "AND YEAR(A.SaleDate) = YEAR(DATEADD(mm, ?, GETDATE())) " +
                "AND MONTH(A.SaleDate) = MONTH(DATEADD(mm, ?, GETDATE())) " +
                "AND A.Active != 2 AND A.CompanyRefId = ?");
            params.add(i);
            params.add(i);
            params.add(comId);
        }

        List<MonthlySalesRow> results = jdbcTemplate.query(sql.toString(),
            (rs, rowNum) -> {
                MonthlySalesRow row = new MonthlySalesRow();
                row.MonthOffset = 11 - rowNum;
                row.SalesCount = rs.getInt("SalesCount");
                row.SalesAmount = rs.getDouble("SalesAmount");
                return row;
            }, params.toArray());

        return results;
    }

    private List<MonthlySalesRow> getMonthlySalesType1(Integer comId) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (int i = 0; i >= -11; i--) {
            if (i < 0) sql.append(" UNION ALL ");
            sql.append(
                "SELECT COUNT(DISTINCT A.Id) as SalesCount, ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) as SalesAmount " +
                "FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) " +
                "WHERE A.id = B.SaleOrderMasterRefId " +
                "AND YEAR(A.SaleDate) = YEAR(DATEADD(mm, ?, GETDATE())) " +
                "AND MONTH(A.SaleDate) = MONTH(DATEADD(mm, ?, GETDATE())) " +
                "AND A.Active != 2 AND A.CompanyRefId = ?");
            params.add(i);
            params.add(i);
            params.add(comId);
        }

        List<MonthlySalesRow> results = jdbcTemplate.query(sql.toString(),
            (rs, rowNum) -> {
                MonthlySalesRow row = new MonthlySalesRow();
                row.MonthOffset = 11 - rowNum;
                row.SalesCount = rs.getInt("SalesCount");
                row.SalesAmount = rs.getDouble("SalesAmount");
                return row;
            }, params.toArray());

        return results;
    }

    private List<MonthlySalesRow> getMonthlySalesType2(Integer comId) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (int i = 0; i >= -11; i--) {
            if (i < 0) sql.append(" UNION ALL ");
            sql.append(
                "SELECT COUNT(DISTINCT A.Id) as SalesCount, ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) as SalesAmount " +
                "FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) " +
                "WHERE A.id = B.SaleOrderMasterRefId " +
                "AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') != '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo != 0)) " +
                "AND YEAR(A.SaleDate) = YEAR(DATEADD(mm, ?, GETDATE())) " +
                "AND MONTH(A.SaleDate) = MONTH(DATEADD(mm, ?, GETDATE())) " +
                "AND A.Active != 2 AND A.CompanyRefId = ?");
            params.add(i);
            params.add(i);
            params.add(comId);
        }

        List<MonthlySalesRow> results = jdbcTemplate.query(sql.toString(),
            (rs, rowNum) -> {
                MonthlySalesRow row = new MonthlySalesRow();
                row.MonthOffset = 11 - rowNum;
                row.SalesCount = rs.getInt("SalesCount");
                row.SalesAmount = rs.getDouble("SalesAmount");
                return row;
            }, params.toArray());

        return results;
    }

    private List<MonthlySalesRow> getMonthlySalesType3(Integer comId) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (int i = 0; i >= -11; i--) {
            if (i < 0) sql.append(" UNION ALL ");
            sql.append(
                "SELECT COUNT(DISTINCT A.Id) as SalesCount, ISNULL(ROUND(SUM(B.ActualAmount), 2), 0) as SalesAmount " +
                "FROM SaleOrderMaster A WITH (NOLOCK), SaleOrderDetails B WITH (NOLOCK) " +
                "WHERE A.id = B.SaleOrderMasterRefId " +
                "AND A.Jstatus NOT IN (8, 12) " +
                "AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks,'') = '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo = 0)) " +
                "AND YEAR(A.SaleDate) = YEAR(DATEADD(mm, ?, GETDATE())) " +
                "AND MONTH(A.SaleDate) = MONTH(DATEADD(mm, ?, GETDATE())) " +
                "AND A.Active != 2 AND A.CompanyRefId = ?");
            params.add(i);
            params.add(i);
            params.add(comId);
        }

        List<MonthlySalesRow> results = jdbcTemplate.query(sql.toString(),
            (rs, rowNum) -> {
                MonthlySalesRow row = new MonthlySalesRow();
                row.MonthOffset = 11 - rowNum;
                row.SalesCount = rs.getInt("SalesCount");
                row.SalesAmount = rs.getDouble("SalesAmount");
                return row;
            }, params.toArray());

        return results;
    }

    // ========== EXPENSE DATA QUERIES ==========

    public List<ExpenseSummaryRow> getExpenseSummary(Integer comId) {
        String sql = """
            SELECT
                SUM(TodaySales) as TodaySales, SUM(TodayAmount) as TodayAmount,
                SUM(YesterdaySales) as YesterdaySales, SUM(YesterdayAmount) as YesterdayAmount,
                SUM(WeekSales) as WeekSales, SUM(WeekAmount) as WeekAmount,
                SUM(MonthSales) as MonthSales, SUM(MonthAmount) as MonthAmount
            FROM (
                SELECT COUNT(Id) as TodaySales, ISNULL(ROUND(SUM(Amount), 2), 0) as TodayAmount, 0 as YesterdaySales, 0.0 as YesterdayAmount, 0 as WeekSales, 0.0 as WeekAmount, 0 as MonthSales, 0.0 as MonthAmount
                FROM PaymentVoucherMaster WITH (NOLOCK) WHERE Active = 1 AND CompanyRefId = ? AND PaymentVoucherDate = CAST(GETDATE() AS DATE)
                UNION ALL
                SELECT 0, 0.0, COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0), 0, 0.0, 0, 0.0 FROM PaymentVoucherMaster WITH (NOLOCK) WHERE Active = 1 AND CompanyRefId = ? AND PaymentVoucherDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)
                UNION ALL
                SELECT 0, 0.0, 0, 0.0, COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0), 0, 0.0 FROM PaymentVoucherMaster WITH (NOLOCK) WHERE Active = 1 AND CompanyRefId = ? AND PaymentVoucherDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND CAST(GETDATE() AS DATE)
                UNION ALL
                SELECT 0, 0.0, 0, 0.0, 0, 0.0, COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0) FROM PaymentVoucherMaster WITH (NOLOCK) WHERE Active = 1 AND CompanyRefId = ? AND PaymentVoucherDate BETWEEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE())
                UNION ALL
                SELECT COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0), 0, 0.0, 0, 0.0, 0, 0.0 FROM Payment WITH (NOLOCK) WHERE CompanyRefId = ? AND PaymentDate = CAST(GETDATE() AS DATE)
                UNION ALL
                SELECT 0, 0.0, COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0), 0, 0.0, 0, 0.0 FROM Payment WITH (NOLOCK) WHERE CompanyRefId = ? AND PaymentDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)
                UNION ALL
                SELECT 0, 0.0, 0, 0.0, COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0), 0, 0.0 FROM Payment WITH (NOLOCK) WHERE CompanyRefId = ? AND PaymentDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND CAST(GETDATE() AS DATE)
                UNION ALL
                SELECT 0, 0.0, 0, 0.0, 0, 0.0, COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0) FROM Payment WITH (NOLOCK) WHERE CompanyRefId = ? AND PaymentDate BETWEEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE())
            ) t
            """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ExpenseSummaryRow.class),
                comId, comId, comId, comId, comId, comId, comId, comId);
    }

    public List<ExpenseBreakdownRow> getExpenseBreakdown(Integer comId, String fromDate, String toDate) {
        String sql = """
            SELECT SUM(ExpCount) as ExpCount, SUM(ExpAmount) as ExpAmount, ExpenseName
            FROM (
                SELECT COUNT(Id) as ExpCount, ISNULL(ROUND(SUM(Amount), 2), 0) as ExpAmount, Description as ExpenseName
                FROM PaymentVoucherMaster WITH (NOLOCK)
                WHERE Active = 1 AND CompanyRefId = ? AND PaymentVoucherDate BETWEEN ? AND ?
                GROUP BY Description
                UNION ALL
                SELECT COUNT(Id), ISNULL(ROUND(SUM(Amount), 2), 0), ISNULL(Description, '')
                FROM Payment WITH (NOLOCK)
                WHERE CompanyRefId = ? AND PaymentDate BETWEEN ? AND ?
                GROUP BY ISNULL(Description, '')
            ) t
            GROUP BY ExpenseName
            """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ExpenseBreakdownRow.class),
                comId, fromDate, toDate, comId, fromDate, toDate);
    }

    // ========== FORWARDING DATA QUERIES ==========

    public ForwardingSummaryRow getForwardingSummary(Integer comId, String fromDate, String toDate) {
        String sql = """
            SELECT
                ISNULL(SUM(K1Count), 0) as K1Count, ISNULL(SUM(K1Release), 0) as K1Release, ISNULL(SUM(K1WithRelease), 0) as K1WithRelease,
                ISNULL(SUM(K2Count), 0) as K2Count, ISNULL(SUM(K2Release), 0) as K2Release, ISNULL(SUM(K2WithRelease), 0) as K2WithRelease,
                ISNULL(SUM(K3Count), 0) as K3Count, ISNULL(SUM(K3Release), 0) as K3Release, ISNULL(SUM(K3WithRelease), 0) as K3WithRelease,
                ISNULL(SUM(K8Count), 0) as K8Count, ISNULL(SUM(K8Release), 0) as K8Release, ISNULL(SUM(K8WithRelease), 0) as K8WithRelease
            FROM (
                SELECT COUNT(Id) as K1Count, 0 as K1Release, 0 as K1WithRelease, 0 as K2Count, 0 as K2Release, 0 as K2WithRelease, 0 as K3Count, 0 as K3Release, 0 as K3WithRelease, 0 as K8Count, 0 as K8Release, 0 as K8WithRelease
                FROM SaleOrderMaster WITH (NOLOCK) WHERE ForwardingDate BETWEEN ? AND ? AND Forwarding = 'K1' AND Active != 2 AND CompanyRefId = ?
                UNION ALL
                SELECT 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK) WHERE A.ForwardingDate BETWEEN ? AND ? AND A.Forwarding = 'K1' AND ISNULL(A.ForwardingExitRef, '') = '' AND A.Active != 2 AND A.CompanyRefId = ?
                UNION ALL
                SELECT 0, 0, COUNT(A.Id), 0, 0, 0, 0, 0, 0, 0, 0, 0
                FROM SaleOrderMaster A WITH (NOLOCK) WHERE A.ForwardingDate BETWEEN ? AND ? AND A.Forwarding = 'K1' AND ISNULL(A.ForwardingExitRef, '') != '' AND A.Active != 2 AND A.CompanyRefId = ?
            ) t
            """;
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(ForwardingSummaryRow.class),
                    fromDate, toDate, comId, fromDate, toDate, comId, fromDate, toDate, comId);
        } catch (Exception e) {
            log.error("Error fetching forwarding summary: {}", e.getMessage());
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
                case 5 -> addBaseCondition(true, "S.JobMasterRefId IN (1,2) AND CAST(S.FlightTime AS DATE) BETWEEN ? AND ?", fromDate, toDate);
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
                "S.JobMasterRefId IN (1,2) AND CAST(S.FlightTime AS DATE) BETWEEN ? AND ?", fromDate, toDate);
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
                ISNULL(J.Name,'') as JobStatus, ISNULL(S.FlightTime, S.OETA) as DETA,
                ISNULL(CONVERT(VARCHAR(26), S.FlightTime, 20), '') as SETA, S.FlightTime as ETA,
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
                '' as Remarks, CASE WHEN CAST(S.FlightTime AS DATE) < ? THEN 1 ELSE 0 END AS SDId
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
}
