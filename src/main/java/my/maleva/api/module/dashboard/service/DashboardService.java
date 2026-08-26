package my.maleva.api.module.dashboard.service;

import my.maleva.api.module.dashboard.dto.*;
import my.maleva.api.module.dashboard.dto.request.*;

import java.util.List;

/**
 * Service interface for dashboard operations
 */
public interface DashboardService {

    // ========== LEGACY DASHBOARD ==========
    DashboardDataDto getAdminDashboardData();
    DashboardDataDto getSuperAdminDashboardData();

    // ========== SALES DATA ==========

    /**
     * Get sales data with type filter
     * type=0: Invoice, type=1: SaleOrder, type=2: Partial, type=3: Pending
     */
    SalesDataDto getSalesData(Integer comId, Integer type);

    /**
     * Get employee sales data
     * type: 0-14 for date ranges, 15-29 for with invoice, 30-44 for without invoice
     */
    List<EmployeeSalesDto.EmployeeSalesItemDto> getEmployeeSales(Integer comId, Integer type);

    /**
     * Get employee invoice data
     */
    List<EmployeeSalesDto.EmployeeSalesItemDto> getEmployeeInvoiceData(Integer comId, Integer type);

    /**
     * Get current month sales breakdown by employee
     * Shows which employees generated the most sales in current month
     * @param comId Company ID
     * @param baseDate Reference date for current month calculation
     * @return List of employees with their current month sales totals
     */
    List<EmployeeWiseSalesDto> getEmployeeWiseSales(Integer comId, String baseDate);

    // ========== EXPENSE DATA ==========

    /**
     * Get expense summary and breakdown
     */
    ExpenseDataDto getExpenseData(Integer comId, String fromDate, String toDate);

    /**
     * Get expense by name
     */
    List<ExpenseNameDto.ExpenseNameItemDto> getExpenseByName(Integer comId, String fromDate, String toDate, String expenseName);

    /**
     * Get supplier expense data
     */
    List<SupplierExpenseDto.SupplierExpenseItemDto> getSupplierExpense(Integer comId);

    // ========== FORWARDING DATA ==========

    /**
     * Get forwarding summary and report
     */
    ForwardingDataDto getForwardingData(Integer comId, String fromDate, String toDate);

    // ========== STATUS & EMPLOYEE ==========

    /**
     * Get sales order status breakdown
     */
    List<SalesOrderStatusDto.SalesOrderStatusItemDto> getSalesOrderStatus(Integer comId, Integer employeeId);

    /**
     * Get employee rules/subordinates
     */
    List<Object> getEmployeeRules(Integer comId, Integer employeeId);

    /**
     * Get employee performance (pivot)
     */
    List<EmployeePerformanceDto.EmployeePerformanceItemDto> getEmployeePerformance(Integer comId, Integer employeeId);

    /**
     * Get weekly report
     */
    List<WeeklyReportDto.WeeklyReportItemDto> getWeeklyReport(Integer comId, Integer employeeId);

    /**
     * Get monthly sale by employee
     */
    List<MonthlySaleDto.MonthlySaleItemDto> getMonthlySale(Integer comId, Integer employeeId);

    // ========== PAYMENT DATA ==========

    /**
     * Get pending payments
     */
    List<PendingPaymentDto.PendingPaymentItemDto> getPendingPayments(Integer comId, String dueDate, String toDate);

    /**
     * Get completed payments
     */
    List<PendingPaymentDto.CompletedPaymentDto> getCompletedPayments(Integer comId, String fromDate, String toDate);

    /**
     * Get unreleased forwarding numbers
     */
    List<PendingPaymentDto.UnreleasedNumberDto> getUnreleasedNumbers(Integer comId);

    /**
     * Get K8 unreleased forwarding numbers
     */
    List<PendingPaymentDto.UnreleasedNumberDto> getK8UnreleasedNumbers(Integer comId);

    // ========== INVOICE CHECK ==========

    /**
     * Check sale invoices
     */
    List<InvoiceCheckDto.InvoiceCheckItemDto> checkSaleInvoices(Integer comId, Integer invoiceType, String fromDate, String toDate, Integer employeeId);

    // ========== VESSEL PLANNING & AIR FREIGHT ==========

    /**
     * Get vessel planning data with ETA/OETA filtering
     * etaType: 1=OETA, 2=ETA, 3=Both, 5=FlightTime
     */
    List<VesselPlanningDashboardModel> getVesselPlanningData(VesselPlanningSearchModel searchModel);

    /**
     * Get air freight data with ETA filtering
     */
    List<VesselPlanningDashboardModel> getAirFreightData(VesselPlanningSearchModel searchModel);

    /**
     * Check sale invoice count and status
     */
    List<SaleOrderInvoiceCheckModel> checkSaleInvoiceCount(F5ViewModel searchModel);

    /**
     * Get top employee performers (monthly comparison)
     * Shows which employee generated the most sales in current and previous month
     * @param comId Company ID
     * @param baseDate Reference date for month calculation (YYYY-MM-DD)
     * @return Response wrapper with top performers and metrics for both months
     */
    TopPerformerDto.TopPerformersResponseDto getTopPerformers(Integer comId, String baseDate);

    /**
     * Get planning search DB details for the dashboard
     */
    List<my.maleva.api.module.planning.dto.PlanningDetailsModel> getPlaningSearchDbDetails(my.maleva.api.module.planning.dto.request.PLANINGSearchRequestDto searchModel);
}
