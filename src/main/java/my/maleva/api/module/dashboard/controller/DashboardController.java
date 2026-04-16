package my.maleva.api.module.dashboard.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.agentcompany.common.ApiResponse;
import my.maleva.api.module.dashboard.dto.*;
import my.maleva.api.module.dashboard.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for SuperAdmin Dashboard operations.
 * Provides KPIs, Sales, Expenses, Forwarding, Payments data.
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // ========== SALES DATA ENDPOINTS ==========

    /**
     * Get sales data (Invoice/SaleOrder/Partial/Pending)
     *
     * @param comId Company ID
     * @param type  0=Invoice, 1=SaleOrder, 2=Partial, 3=Pending
     */
    @GetMapping("/sales/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<SalesDataDto>> getSalesData(
            @PathVariable Integer comId,
            @RequestParam(defaultValue = "0") Integer type) {

        // Validate input
        if (comId == null || comId <= 0) {
            log.warn("Invalid comId provided: {}", comId);
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid company ID. Must be a positive number"));
        }
        if (type < 0 || type > 3) {
            log.warn("Invalid sales type: {}", type);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid sales type. Expected 0-3 (0=Invoice, 1=SaleOrder, 2=Partial, 3=Pending)"));
        }
        log.info("GET /api/dashboard/sales/{} type={}", comId, type);
        try {
            SalesDataDto data = dashboardService.getSalesData(comId, type);
            return ResponseEntity.ok(ApiResponse.success("Sales data fetched successfully", data));
        } catch (Exception e) {
            log.error("Error fetching sales data for comId={}, type={}: {}", comId, type, e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to fetch sales data. Please try again later."));
        }
    }

    /**
     * Get employee sales data
     *
     * @param comId Company ID
     * @param type  0-44 (date range × filter combinations)
     */
    @GetMapping("/employee-sales/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<EmployeeSalesDto.EmployeeSalesItemDto>>> getEmployeeSales(
            @PathVariable Integer comId,
            @RequestParam(defaultValue = "0") Integer type) {

        log.info("GET /api/dashboard/employee-sales/{} type={}", comId, type);
        List<EmployeeSalesDto.EmployeeSalesItemDto> data = dashboardService.getEmployeeSales(comId, type);
        return ResponseEntity.ok(ApiResponse.success("Employee sales data fetched successfully", data));
    }

    /**
     * Get employee invoice data
     */
    @GetMapping("/employee-invoice/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<EmployeeSalesDto.EmployeeSalesItemDto>>> getEmployeeInvoiceData(
            @PathVariable Integer comId,
            @RequestParam(defaultValue = "0") Integer type) {

        log.info("GET /api/dashboard/employee-invoice/{} type={}", comId, type);
        List<EmployeeSalesDto.EmployeeSalesItemDto> data = dashboardService.getEmployeeInvoiceData(comId, type);
        return ResponseEntity.ok(ApiResponse.success("Employee invoice data fetched successfully", data));
    }

    // ========== EXPENSE DATA ENDPOINTS ==========

    /**
     * Get expense data with summary and breakdown
     *
     * @param comId    Company ID
     * @param fromDate Start date (yyyy-MM-dd)
     * @param toDate   End date (yyyy-MM-dd)
     */
    @GetMapping("/expense/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<ExpenseDataDto>> getExpenseData(
            @PathVariable Integer comId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        log.info("GET /api/dashboard/expense/{} from={} to={}", comId, fromDate, toDate);
        ExpenseDataDto data = dashboardService.getExpenseData(comId, fromDate.toString(), toDate.toString());
        return ResponseEntity.ok(ApiResponse.success("Expense data fetched successfully", data));
    }

    /**
     * Get expense breakdown by name
     */
    @GetMapping("/expense-name/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<ExpenseNameDto.ExpenseNameItemDto>>> getExpenseByName(
            @PathVariable Integer comId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "%") String expenseName) {

        log.info("GET /api/dashboard/expense-name/{}", comId);
        List<ExpenseNameDto.ExpenseNameItemDto> data =
                dashboardService.getExpenseByName(comId, fromDate.toString(), toDate.toString(), expenseName);
        return ResponseEntity.ok(ApiResponse.success("Expense by name fetched successfully", data));
    }

    /**
     * Get supplier expenses
     */
    @GetMapping("/supplier-expense/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<SupplierExpenseDto.SupplierExpenseItemDto>>> getSupplierExpense(
            @PathVariable Integer comId) {

        log.info("GET /api/dashboard/supplier-expense/{}", comId);
        List<SupplierExpenseDto.SupplierExpenseItemDto> data = dashboardService.getSupplierExpense(comId);
        return ResponseEntity.ok(ApiResponse.success("Supplier expense fetched successfully", data));
    }

    // ========== FORWARDING DATA ENDPOINT ==========

    /**
     * Get forwarding data (K1/K2/K3/K8 counts and release status)
     */
    @GetMapping("/forwarding/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<ForwardingDataDto>> getForwardingData(
            @PathVariable Integer comId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        log.info("GET /api/dashboard/forwarding/{} from={} to={}", comId, fromDate, toDate);
        ForwardingDataDto data = dashboardService.getForwardingData(comId, fromDate.toString(), toDate.toString());
        return ResponseEntity.ok(ApiResponse.success("Forwarding data fetched successfully", data));
    }

    // ========== STATUS & EMPLOYEE ENDPOINTS ==========

    /**
     * Get sales order status breakdown
     */
    @GetMapping("/sales-order-status/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<SalesOrderStatusDto.SalesOrderStatusItemDto>>> getSalesOrderStatus(
            @PathVariable Integer comId,
            @RequestParam(required = false) Integer employeeId) {

        log.info("GET /api/dashboard/sales-order-status/{}", comId);
        List<SalesOrderStatusDto.SalesOrderStatusItemDto> data =
                dashboardService.getSalesOrderStatus(comId, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Sales order status fetched successfully", data));
    }

    /**
     * Get employee rules (subordinates)
     */
    @GetMapping("/employee-rules/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<Object>>> getEmployeeRules(
            @PathVariable Integer comId,
            @RequestParam(required = false) Integer employeeId) {

        log.info("GET /api/dashboard/employee-rules/{}", comId);
        List<Object> data = dashboardService.getEmployeeRules(comId, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee rules fetched successfully", data));
    }

    /**
     * Get employee performance (pivot table)
     */
    @GetMapping("/employee-performance/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<EmployeePerformanceDto.EmployeePerformanceItemDto>>> getEmployeePerformance(
            @PathVariable Integer comId,
            @RequestParam(required = false) Integer employeeId) {

        log.info("GET /api/dashboard/employee-performance/{}", comId);
        List<EmployeePerformanceDto.EmployeePerformanceItemDto> data =
                dashboardService.getEmployeePerformance(comId, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee performance fetched successfully", data));
    }

    /**
     * Get weekly report
     */
    @GetMapping("/weekly-report/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<WeeklyReportDto.WeeklyReportItemDto>>> getWeeklyReport(
            @PathVariable Integer comId,
            @RequestParam(required = false) Integer employeeId) {

        log.info("GET /api/dashboard/weekly-report/{}", comId);
        List<WeeklyReportDto.WeeklyReportItemDto> data = dashboardService.getWeeklyReport(comId, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Weekly report fetched successfully", data));
    }

    /**
     * Get monthly sale by employee
     */
    @GetMapping("/monthly-sale/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<MonthlySaleDto.MonthlySaleItemDto>>> getMonthlySale(
            @PathVariable Integer comId,
            @RequestParam(required = false) Integer employeeId) {

        log.info("GET /api/dashboard/monthly-sale/{}", comId);
        List<MonthlySaleDto.MonthlySaleItemDto> data = dashboardService.getMonthlySale(comId, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Monthly sale fetched successfully", data));
    }

    // ========== PAYMENT ENDPOINTS ==========

    /**
     * Get pending payments
     */
    @GetMapping("/pending-payment/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<PendingPaymentDto.PendingPaymentItemDto>>> getPendingPayments(
            @PathVariable Integer comId,
            @RequestParam(required = false, defaultValue = "") String dueDate) {

        log.info("GET /api/dashboard/pending-payment/{}", comId);
        String due = dueDate.isEmpty() ? LocalDate.now().toString() : dueDate;
        List<PendingPaymentDto.PendingPaymentItemDto> data = dashboardService.getPendingPayments(comId, due);
        return ResponseEntity.ok(ApiResponse.success("Pending payments fetched successfully", data));
    }

    /**
     * Get completed payments
     */
    @GetMapping("/completed-payment/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<PendingPaymentDto.CompletedPaymentDto>>> getCompletedPayments(
            @PathVariable Integer comId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        log.info("GET /api/dashboard/completed-payment/{}", comId);
        List<PendingPaymentDto.CompletedPaymentDto> data =
                dashboardService.getCompletedPayments(comId, fromDate.toString(), toDate.toString());
        return ResponseEntity.ok(ApiResponse.success("Completed payments fetched successfully", data));
    }

    /**
     * Get unreleased forwarding numbers
     */
    @GetMapping("/unreleased/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<PendingPaymentDto.UnreleasedNumberDto>>> getUnreleasedNumbers(
            @PathVariable Integer comId) {

        log.info("GET /api/dashboard/unreleased/{}", comId);
        List<PendingPaymentDto.UnreleasedNumberDto> data = dashboardService.getUnreleasedNumbers(comId);
        return ResponseEntity.ok(ApiResponse.success("Unreleased numbers fetched successfully", data));
    }

    /**
     * Get K8 unreleased forwarding numbers
     */
    @GetMapping("/k8-unreleased/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<PendingPaymentDto.UnreleasedNumberDto>>> getK8UnreleasedNumbers(
            @PathVariable Integer comId) {

        log.info("GET /api/dashboard/k8-unreleased/{}", comId);
        List<PendingPaymentDto.UnreleasedNumberDto> data = dashboardService.getK8UnreleasedNumbers(comId);
        return ResponseEntity.ok(ApiResponse.success("K8 unreleased numbers fetched successfully", data));
    }

    // ========== INVOICE CHECK ENDPOINT ==========

    /**
     * Check sale invoices
     */
    @PostMapping("/invoice-check/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<InvoiceCheckDto.InvoiceCheckItemDto>>> checkSaleInvoices(
            @PathVariable Integer comId,
            @RequestParam(required = false, defaultValue = "0") Integer invoiceType,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Integer employeeId) {

        log.info("POST /api/dashboard/invoice-check/{}", comId);
        List<InvoiceCheckDto.InvoiceCheckItemDto> data =
                dashboardService.checkSaleInvoices(comId, invoiceType, fromDate, toDate, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Invoice check completed successfully", data));
    }

    // ========== VESSEL PLANNING & AIR FREIGHT ENDPOINTS ==========

    /**
     * Get vessel planning data
     * etaType: 1=OETA, 2=ETA, 3=Both, 5=FlightTime
     */
    @PostMapping("/vessel-planning/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<VesselPlanningDashboardModel>>> getVesselPlanningData(
            @PathVariable Integer comId,
            @RequestBody VesselPlanningSearchModel searchModel) {

        log.info("POST /api/dashboard/vessel-planning/{}", comId);
        searchModel.setComId(comId);
        List<VesselPlanningDashboardModel> data = dashboardService.getVesselPlanningData(searchModel);
        return ResponseEntity.ok(ApiResponse.success("Vessel planning data fetched successfully", data));
    }

    /**
     * Get air freight data
     */
    @PostMapping("/air-freight/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<VesselPlanningDashboardModel>>> getAirFreightData(
            @PathVariable Integer comId,
            @RequestBody VesselPlanningSearchModel searchModel) {

        log.info("POST /api/dashboard/air-freight/{}", comId);
        searchModel.setComId(comId);
        List<VesselPlanningDashboardModel> data = dashboardService.getAirFreightData(searchModel);
        return ResponseEntity.ok(ApiResponse.success("Air freight data fetched successfully", data));
    }

    /**
     * Check sale invoice count
     */
    @PostMapping("/check-invoice-count/{comId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<ApiResponse<List<SaleOrderInvoiceCheckModel>>> checkSaleInvoiceCount(
            @PathVariable Integer comId,
            @RequestBody F5ViewModel searchModel) {

        log.info("POST /api/dashboard/check-invoice-count/{}", comId);
        searchModel.setComId(comId);
        List<SaleOrderInvoiceCheckModel> data = dashboardService.checkSaleInvoiceCount(searchModel);
        return ResponseEntity.ok(ApiResponse.success("Sale invoice count checked successfully", data));
    }

    // ========== ADMIN DASHBOARD (EXISTING) ==========

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<DashboardDataDto> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboardData());
    }

    @GetMapping("/super-admin")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<DashboardDataDto> getSuperAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getSuperAdminDashboardData());
    }

    // Placeholder methods for existing interface
    private DashboardDataDto getAdminDashboardData() {
        return DashboardDataDto.builder()
                .kpiStats(DashboardDataDto.KPIStats.builder()
                        .totalRevenue(0)
                        .activeOperations(0)
                        .teamEfficiency(0)
                        .pendingApprovals(0)
                        .revenueChangePercent(0)
                        .operationsChange(0)
                        .todaySalesCount(0)
                        .todaySalesAmount(0)
                        .yesterdaySalesCount(0)
                        .yesterdaySalesAmount(0)
                        .weekSalesCount(0)
                        .weekSalesAmount(0)
                        .monthSalesCount(0)
                        .monthSalesAmount(0)
                        .build())
                .build();
    }

    private DashboardDataDto getSuperAdminDashboardData() {
        return getAdminDashboardData();
    }
}
