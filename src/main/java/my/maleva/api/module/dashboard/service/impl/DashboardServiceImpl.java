package my.maleva.api.module.dashboard.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.dashboard.dto.*;
import my.maleva.api.module.dashboard.repository.DashboardRepository;
import my.maleva.api.module.dashboard.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard Service Implementation
 * Maps legacy DashBoardServices.cs to Spring Boot with proper architecture
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ========== LEGACY DASHBOARD ==========

    /**
     * Get admin dashboard data - mirrors legacy GetExpData result.
     * Returns KPI stats from ExpenseSummary (Today/Yesterday/Week/Month counts & amounts).
     * Uses today's date as the date range.
     */
    @Override
    public DashboardDataDto getAdminDashboardData() {
        try {
            // Use today's date as default for admin dashboard (mirrors legacy behavior)
            String today = LocalDate.now().format(DATE_FORMAT);

            // Get expense summary (Today/Yesterday/Week/Month)
            List<DashboardRepository.ExpenseSummaryRow> summary =
                    dashboardRepository.getExpenseSummary(6); // comId=6 is hardcoded in legacy

            // Also get pending approvals count from sales order status
            List<DashboardRepository.SalesOrderStatusRow> statusRows =
                    dashboardRepository.getSalesOrderStatus(6, null);

            DashboardRepository.ExpenseSummaryRow row = summary.isEmpty()
                    ? new DashboardRepository.ExpenseSummaryRow()
                    : summary.get(0);

            int pendingApprovals = statusRows.stream()
                    .filter(r -> r.JobStatus != null && !"JOB COMPLET".equals(r.JobStatus))
                    .mapToInt(r -> r.DayCount != null ? r.DayCount : 0)
                    .sum();

            return DashboardDataDto.builder()
                    .kpiStats(DashboardDataDto.KPIStats.builder()
                            .totalRevenue(row.TodayAmount != null ? row.TodayAmount : 0.0)
                            .activeOperations(pendingApprovals)
                            .teamEfficiency(0.0)
                            .pendingApprovals(pendingApprovals)
                            .revenueChangePercent(0.0)
                            .operationsChange(0)
                            .todaySalesCount(row.TodaySales != null ? row.TodaySales : 0)
                            .todaySalesAmount(row.TodayAmount != null ? row.TodayAmount : 0.0)
                            .yesterdaySalesCount(row.YesterdaySales != null ? row.YesterdaySales : 0)
                            .yesterdaySalesAmount(row.YesterdayAmount != null ? row.YesterdayAmount : 0.0)
                            .weekSalesCount(row.WeekSales != null ? row.WeekSales : 0)
                            .weekSalesAmount(row.WeekAmount != null ? row.WeekAmount : 0.0)
                            .monthSalesCount(row.MonthSales != null ? row.MonthSales : 0)
                            .monthSalesAmount(row.MonthAmount != null ? row.MonthAmount : 0.0)
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Error fetching admin dashboard data: {}", e.getMessage(), e);
            return DashboardDataDto.builder()
                    .kpiStats(DashboardDataDto.KPIStats.builder()
                            .totalRevenue(0.0)
                            .activeOperations(0)
                            .teamEfficiency(0.0)
                            .pendingApprovals(0)
                            .revenueChangePercent(0.0)
                            .operationsChange(0)
                            .todaySalesCount(0)
                            .todaySalesAmount(0.0)
                            .yesterdaySalesCount(0)
                            .yesterdaySalesAmount(0.0)
                            .weekSalesCount(0)
                            .weekSalesAmount(0.0)
                            .monthSalesCount(0)
                            .monthSalesAmount(0.0)
                            .build())
                    .build();
        }
    }

    @Override
    public DashboardDataDto getSuperAdminDashboardData() {
        DashboardDataDto dto = getAdminDashboardData();
        dto.setSystemHealth(DashboardDataDto.SystemHealthDto.builder()
                .usersOnline(0)
                .uptimePercent(100.0)
                .totalRegisteredUsers(0)
                .build());
        return dto;
    }

    // ========== SALES DATA ==========

    @Override
    public SalesDataDto getSalesData(Integer comId, Integer type) {
        log.info("=== SALES DATA REQUEST: comId={}, type={} ===", comId, type);

        try {
            List<DashboardRepository.SalesSummaryRow> summary =
                    dashboardRepository.getSalesSummary(comId, type);

            log.debug("Sales summary result: {} rows", summary == null ? 0 : summary.size());

            if (summary == null || summary.isEmpty()) {
                log.warn("No sales summary data found for comId={}, type={}", comId, type);
                return buildEmptySalesData();
            }

            List<DashboardRepository.MonthlySalesRow> monthlyRows =
                    dashboardRepository.getMonthlySales(comId, type);

            log.debug("Monthly sales result: {} rows", monthlyRows == null ? 0 : monthlyRows.size());

            if (monthlyRows == null) {
                log.warn("No monthly sales data found for comId={}, type={}", comId, type);
                monthlyRows = Collections.emptyList();
            }

            DashboardRepository.SalesSummaryRow row = summary.get(0);

            List<SalesDataDto.MonthlySalesDto> monthlySales = monthlyRows.stream()
                    .filter(m -> m.MonthOffset != null)
                    .map(m -> SalesDataDto.MonthlySalesDto.builder()
                            .salesCount(m.SalesCount != null ? m.SalesCount : 0)
                            .salesAmount(m.SalesAmount != null ? m.SalesAmount : 0.0)
                            .monthName(getMonthName(m.MonthOffset))
                            .build())
                    .collect(Collectors.toList());

            log.info("Built {} monthly sales records for comId={}", monthlySales.size(), comId);

            return SalesDataDto.builder()
                    .todaySales(row.TodaySales != null ? row.TodaySales : 0)
                    .todayAmount(row.TodayAmount != null ? row.TodayAmount : 0.0)
                    .yesterdaySales(row.YesterdaySales != null ? row.YesterdaySales : 0)
                    .yesterdayAmount(row.YesterdayAmount != null ? row.YesterdayAmount : 0.0)
                    .weekSales(row.WeekSales != null ? row.WeekSales : 0)
                    .weekAmount(row.WeekAmount != null ? row.WeekAmount : 0.0)
                    .monthSales(row.MonthSales != null ? row.MonthSales : 0)
                    .monthAmount(row.MonthAmount != null ? row.MonthAmount : 0.0)
                    .monthlySales(monthlySales)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching sales data for comId={}, type={}: {}", comId, type, e.getMessage(), e);
            return buildEmptySalesData();
        }
    }

    @Override
    public List<EmployeeSalesDto.EmployeeSalesItemDto> getEmployeeSales(Integer comId, Integer type) {
        String whereClause = buildEmployeeSalesWhere(type);
        return dashboardRepository.getEmployeeSales(comId, whereClause).stream()
                .map(r -> EmployeeSalesDto.EmployeeSalesItemDto.builder()
                        .employeeName(r.EmployeeName)
                        .salesCount(r.SalesCount)
                        .amount(r.Amount)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeSalesDto.EmployeeSalesItemDto> getEmployeeInvoiceData(Integer comId, Integer type) {
        // Similar to employee sales but uses invoice tables
        return getEmployeeSales(comId, type);
    }

    private String buildEmployeeSalesWhere(Integer type) {
        if (type == null) type = 0;

        String dateFilter = switch (type % 15) {
            case 0 -> "AND A.SaleDate = CAST(GETDATE() AS DATE)";
            case 1 -> "AND A.SaleDate = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)";
            case 2 -> "AND A.SaleDate BETWEEN CAST(DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), -1) AS DATE) AND CAST(GETDATE() AS DATE)";
            case 3 -> "AND A.SaleDate BETWEEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AND EOMONTH(GETDATE())";
            default -> "";
        };

        String invoiceFilter = "";
        if (type >= 15 && type < 30) {
            invoiceFilter = "AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks, '') != '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo != 0))";
        } else if (type >= 30) {
            invoiceFilter = "AND A.Jstatus NOT IN (8, 12) AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks, '') = '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo = 0))";
        }

        return dateFilter + " " + invoiceFilter;
    }

    // ========== EXPENSE DATA ==========

    @Override
    public ExpenseDataDto getExpenseData(Integer comId, String fromDate, String toDate) {
        log.info("=== EXPENSE DATA REQUEST: comId={}, fromDate={}, toDate={} ===", comId, fromDate, toDate);

        // Get expense summary - uses GETDATE() internally for fixed periods (today/yesterday/week/month)
        // The fromDate/toDate are used for the breakdown query
        List<DashboardRepository.ExpenseSummaryRow> summary =
                dashboardRepository.getExpenseSummary(comId);
        log.debug("Expense summary returned {} rows", summary.size());

        // Get expense breakdown filtered by fromDate/toDate (mirrors legacy GetExpData result2 query)
        List<DashboardRepository.ExpenseBreakdownRow> breakdown =
                dashboardRepository.getExpenseBreakdown(comId, fromDate, toDate);
        log.debug("Expense breakdown returned {} rows", breakdown.size());

        DashboardRepository.ExpenseSummaryRow row = summary.isEmpty()
                ? new DashboardRepository.ExpenseSummaryRow()
                : summary.get(0);

        List<ExpenseDataDto.ExpenseBreakdownDto> expenses = breakdown.stream()
                .map(r -> ExpenseDataDto.ExpenseBreakdownDto.builder()
                        .expenseName(r.ExpenseName)
                        .expCount(r.ExpCount)
                        .expAmount(r.ExpAmount)
                        .build())
                .collect(Collectors.toList());

        log.info("Built expense data with {} breakdown items, TodaySales={}, MonthAmount={}",
            expenses.size(), row.TodaySales, row.MonthAmount);

        return ExpenseDataDto.builder()
                .todaySales(row.TodaySales != null ? row.TodaySales : 0)
                .todayAmount(row.TodayAmount != null ? row.TodayAmount : 0.0)
                .yesterdaySales(row.YesterdaySales != null ? row.YesterdaySales : 0)
                .yesterdayAmount(row.YesterdayAmount != null ? row.YesterdayAmount : 0.0)
                .weekSales(row.WeekSales != null ? row.WeekSales : 0)
                .weekAmount(row.WeekAmount != null ? row.WeekAmount : 0.0)
                .monthSales(row.MonthSales != null ? row.MonthSales : 0)
                .monthAmount(row.MonthAmount != null ? row.MonthAmount : 0.0)
                .expenses(expenses)
                .build();
    }

    @Override
    public List<ExpenseNameDto.ExpenseNameItemDto> getExpenseByName(Integer comId, String fromDate, String toDate, String expenseName) {
        List<DashboardRepository.ExpenseBreakdownRow> breakdown =
                dashboardRepository.getExpenseByName(comId, fromDate, toDate, expenseName);

        return breakdown.stream()
                .map(r -> ExpenseNameDto.ExpenseNameItemDto.builder()
                        .id(0) // Id not available in this query
                        .expenseName(r.ExpenseName)
                        .expCount(r.ExpCount)
                        .expAmount(r.ExpAmount)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierExpenseDto.SupplierExpenseItemDto> getSupplierExpense(Integer comId) {
        // This would need a separate query - simplified for now
        return Collections.emptyList();
    }

    // ========== FORWARDING DATA ==========

    @Override
    public ForwardingDataDto getForwardingData(Integer comId, String fromDate, String toDate) {
        log.info("=== FORWARDING DATA REQUEST: comId={}, fromDate={}, toDate={} ===", comId, fromDate, toDate);
        DashboardRepository.ForwardingSummaryRow row =
                dashboardRepository.getForwardingSummary(comId, fromDate, toDate);

        if (row == null) {
            row = new DashboardRepository.ForwardingSummaryRow();
        }

        log.info("Forwarding data - K1: {}/{}/{}, K2: {}/{}/{}, K3: {}/{}/{}, K8: {}/{}/{}",
            row.K1Count, row.K1Release, row.K1WithRelease,
            row.K2Count, row.K2Release, row.K2WithRelease,
            row.K3Count, row.K3Release, row.K3WithRelease,
            row.K8Count, row.K8Release, row.K8WithRelease);

        return ForwardingDataDto.builder()
                .k1Count(row.K1Count)
                .k1Release(row.K1Release)
                .k1WithRelease(row.K1WithRelease)
                .k2Count(row.K2Count)
                .k2Release(row.K2Release)
                .k2WithRelease(row.K2WithRelease)
                .k3Count(row.K3Count)
                .k3Release(row.K3Release)
                .k3WithRelease(row.K3WithRelease)
                .k8Count(row.K8Count)
                .k8Release(row.K8Release)
                .k8WithRelease(row.K8WithRelease)
                .report(Collections.emptyList()) // Report data needs separate query
                .build();
    }

    // ========== STATUS & EMPLOYEE ==========

    @Override
    public List<SalesOrderStatusDto.SalesOrderStatusItemDto> getSalesOrderStatus(Integer comId, Integer employeeId) {
        return dashboardRepository.getSalesOrderStatus(comId, employeeId).stream()
                .map(r -> SalesOrderStatusDto.SalesOrderStatusItemDto.builder()
                        .id(r.Id != null ? r.Id : 0)
                        .jobStatus(r.JobStatus)
                        .dayCount(r.DayCount != null ? r.DayCount : 0)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Object> getEmployeeRules(Integer comId, Integer employeeId) {
        // This needs RulesTypeMaster query
        return Collections.emptyList();
    }

    @Override
    public List<EmployeePerformanceDto.EmployeePerformanceItemDto> getEmployeePerformance(Integer comId, Integer employeeId) {
        // This needs PIVOT query - simplified for now
        return Collections.emptyList();
    }

    @Override
    public List<WeeklyReportDto.WeeklyReportItemDto> getWeeklyReport(Integer comId, Integer employeeId) {
        // This needs CTE query - simplified for now
        return Collections.emptyList();
    }

    @Override
    public List<MonthlySaleDto.MonthlySaleItemDto> getMonthlySale(Integer comId, Integer employeeId) {
        // This needs symbol master join - simplified for now
        return Collections.emptyList();
    }

    // ========== PAYMENT DATA ==========

    @Override
    public List<PendingPaymentDto.PendingPaymentItemDto> getPendingPayments(Integer comId, String dueDate) {
        return dashboardRepository.getPendingPayments(comId, dueDate).stream()
                .map(r -> PendingPaymentDto.PendingPaymentItemDto.builder()
                        .id(r.Id)
                        .expenseName(r.ExpenseName)
                        .subExpenseName(r.SubExpenseName)
                        .amount(r.Amount)
                        .dueDate(r.DueDate)
                        .dueReportId(r.DueReportId)
                        .detailedId(r.DetailedId)
                        .bankName(r.BankName)
                        .accountNo(r.AccountNo)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<PendingPaymentDto.CompletedPaymentDto> getCompletedPayments(Integer comId, String fromDate, String toDate) {
        // This needs separate query
        return Collections.emptyList();
    }

    @Override
    public List<PendingPaymentDto.UnreleasedNumberDto> getUnreleasedNumbers(Integer comId) {
        // This needs separate query
        return Collections.emptyList();
    }

    @Override
    public List<PendingPaymentDto.UnreleasedNumberDto> getK8UnreleasedNumbers(Integer comId) {
        // This needs separate query
        return Collections.emptyList();
    }

    // ========== INVOICE CHECK ==========

    @Override
    public List<InvoiceCheckDto.InvoiceCheckItemDto> checkSaleInvoices(Integer comId, Integer invoiceType, String fromDate, String toDate, Integer employeeId) {
        // This needs complex query with subqueries - simplified for now
        return Collections.emptyList();
    }

    // ========== VESSEL PLANNING & AIR FREIGHT ==========

    @Override
    public List<VesselPlanningDashboardModel> getVesselPlanningData(VesselPlanningSearchModel searchModel) {
        log.info("Fetching Vessel Planning data for comId={}, etaType={}", searchModel.getComId(), searchModel.getEtaType());
        try {
            return dashboardRepository.getVesselPlanningData(searchModel);
        } catch (Exception e) {
            log.error("Error fetching vessel planning data: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<VesselPlanningDashboardModel> getAirFreightData(VesselPlanningSearchModel searchModel) {
        log.info("Fetching Air Freight data for comId={}", searchModel.getComId());
        try {
            return dashboardRepository.getAirFreightData(searchModel);
        } catch (Exception e) {
            log.error("Error fetching air freight data: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<SaleOrderInvoiceCheckModel> checkSaleInvoiceCount(F5ViewModel searchModel) {
        log.info("Checking sale invoice count for comId={}", searchModel.getComId());
        try {
            return dashboardRepository.checkSaleInvoiceCount(searchModel);
        } catch (Exception e) {
            log.error("Error checking sale invoice count: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // ========== HELPER METHODS ==========

    private String getMonthName(int monthOffset) {
        LocalDate date = LocalDate.now().minusMonths(monthOffset);
        return date.format(DateTimeFormatter.ofPattern("MMM yyyy"));
    }

    /**
     * Build empty sales data for error cases or no data scenarios
     */
    private SalesDataDto buildEmptySalesData() {
        return SalesDataDto.builder()
                .todaySales(0)
                .todayAmount(0.0)
                .yesterdaySales(0)
                .yesterdayAmount(0.0)
                .weekSales(0)
                .weekAmount(0.0)
                .monthSales(0)
                .monthAmount(0.0)
                .monthlySales(Collections.emptyList())
                .build();
    }

    @Override
    public List<EmployeeWiseSalesDto> getEmployeeWiseSales(Integer comId, String baseDate) {
        log.info("=== EMPLOYEE WISE SALES REQUEST: comId={}, baseDate={} ===", comId, baseDate);

        try {
            List<DashboardRepository.EmployeeWiseSalesRow> rows =
                    dashboardRepository.getEmployeeWiseSales(comId, baseDate);

            log.debug("Employee-wise sales returned {} rows", rows.size());

            if (rows == null || rows.isEmpty()) {
                log.warn("No employee-wise sales data found for comId={}, baseDate={}", comId, baseDate);
                return Collections.emptyList();
            }

            List<EmployeeWiseSalesDto> result = rows.stream()
                    .map(r -> EmployeeWiseSalesDto.builder()
                            .employeeName(r.EmployeeName != null ? r.EmployeeName : "Unknown")
                            .currentMonthSales(r.CurrentMonthSales != null ? r.CurrentMonthSales : 0.0)
                            .salesCount(r.SalesCount != null ? r.SalesCount : 0)
                            .build())
                    .sorted((a, b) -> Double.compare(b.getCurrentMonthSales(), a.getCurrentMonthSales())) // Sort by sales desc
                    .collect(Collectors.toList());

            log.info("Built {} employee-wise sales records for comId={}", result.size(), comId);
            return result;
        } catch (Exception e) {
            log.error("Error fetching employee-wise sales for comId={}, baseDate={}: {}", comId, baseDate, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

}
