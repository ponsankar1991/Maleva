package my.maleva.api.module.saleorder.service;

import my.maleva.api.module.saleorder.dto.*;
import my.maleva.api.module.invoice.dto.SaleF5View;

import java.util.List;


public interface SaleOrderMasterService {


    SaleOrderMasterDto save(SaleOrderDTO dto);


    SaleOrderEditDto getById(Integer id);


    SaleOrderEditDto getEditSaleOrder(Integer id, Integer saleOrderNo, Integer companyId);


    SaleOrderMasterDto update(Integer id, SaleOrderDTO dto);


    SaleOrderMasterDto updateMaster(Integer id, SaleOrderMasterDto dto);


    SaleOrderStatusUpdateDto updateStatus(Integer id, Integer companyId, Integer jobStatusId);


    List<VesselActivityReportProjection> getVesselActivityReport(Integer companyId, String fromDate, String toDate, String portName);


    SaleOrderQuickUpdateDto updateQuickFields(Integer id, SaleOrderQuickUpdateDto dto);


    boolean delete(Integer id);


    SaleF5View selectSaleOrder(SaleOrderFilterDTO filter);


    SaleOrderStatusUpdateDto updateJobStatus(Integer id, Integer jobStatusId);

    /**
     * Get customer job numbers for a given company and customer
     *
     * Equivalent to ASP.NET GetCustJobNo endpoint
     *
     * Business Logic:
     * 1. Filter by company (multi-tenancy) - required
     * 2. Filter by customer (if custId != 0) - optional, 0 means all customers
     * 3. Exclude soft-deleted records (Active != 2)
     * 4. Filter by invoice number:
     *    - If invoiceNo = 0: returns jobs NOT YET INVOICED
     *    - If invoiceNo > 0: returns jobs for that specific invoice
     *
     * @param companyId Company ID (tenant identifier)
     * @param customerId Customer ID (0 means all customers)
     * @param invoiceNo Invoice number (0 means not yet invoiced)
     * @return List of job records with Id and billNoDisplay (CNumberDisplay)
     */
    List<JobNumberDto> getCustJobNumbers(Integer companyId, Integer customerId, Integer invoiceNo);
    
    List<my.maleva.api.module.saleorder.dto.SaleOrderDTO> editMultiSaleOrder(my.maleva.api.module.invoice.dto.MultiInvoiceDto dto);

    Integer selectPortChargeCount(int companyId, int jobId);

    /**
     * Equivalent to .NET SaleJobView
     */
    List<SaleJobViewAggregateDto> getSaleJobView(SaleOrderFilterDTO filter);

    List<SaleJobViewAggregateDto> getSaleCurrencyView(SaleOrderFilterDTO filter);

    List<SaleJobViewAggregateDto> getSaleEmployeeView(SaleOrderFilterDTO filter);

    java.util.List<java.util.Map<String, Object>> getSalePortView(SaleOrderFilterDTO filter);

    List<SaleJobViewAggregateDto> getSaleCustomerView(SaleOrderFilterDTO filter);


    List<VesselScheduleResponseDto> getVesselSchedules(Integer companyId, String fromDate, String toDate);

    /**
     * Update DODescription for a SaleOrder
     *
     * @param dto SaleOrderDoDescriptionUpdateDto containing id and new doDescription
     * @return The updated SaleOrderMasterDto with the new DODescription
     * @throws EntityNotFoundException if SaleOrderMaster not found with given id
     */
    @org.springframework.transaction.annotation.Transactional
    SaleOrderMasterDto updateDoDescription(SaleOrderDoDescriptionUpdateDto dto);

    /**
     * Check sale order invoices dynamically based on multiple parameters.
     * Equivalent to ASP.NET CheckSaleOrderInvoice endpoint.
     */
    java.util.List<my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCheckDto> checkSaleOrderInvoice(my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCheckRequest request);
}
