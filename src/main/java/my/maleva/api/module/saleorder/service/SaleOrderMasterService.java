package my.maleva.api.module.saleorder.service;

import my.maleva.api.module.saleorder.dto.SaleOrderDTO;
import my.maleva.api.module.saleorder.dto.SaleOrderEditDto;
import my.maleva.api.module.saleorder.dto.SaleOrderFilterDTO;
import my.maleva.api.module.saleorder.dto.SaleOrderMasterDto;
import my.maleva.api.module.saleorder.dto.SaleOrderQuickUpdateDto;
import my.maleva.api.module.saleorder.dto.SaleOrderStatusUpdateDto;
import my.maleva.api.module.invoice.dto.SaleF5View;

/**
 * SaleOrderMasterService - Business logic for SaleOrderMaster
 * Incorporates SP_SaleOrderMaster stored procedure logic
 */
public interface SaleOrderMasterService {

    /**
     * Persists a full sale-order aggregate because the current page submits the
     * master row together with child detail collections in one request.
     *
     * @param dto complete sale-order request payload
     * @return saved master DTO for the caller
     */
    SaleOrderMasterDto save(SaleOrderDTO dto);

    /**
     * Loads a full sale-order aggregate for edit screens so the client can
     * re-submit the existing master and child data without losing omitted
     * sections.
     *
     * @param id sale-order identifier
     * @return master row together with details, pickup, delivery, and forwarding
     */
    SaleOrderEditDto getById(Integer id);

    /**
     * Equivalent to the legacy EditSaleOrder flow. Supports lookup either by
     * primary key or by sale-order number within a company and enriches detail
     * rows with item, tax, and UOM data needed by edit screens.
     *
     * @param id sale-order identifier, optional when saleOrderNo is provided
     * @param saleOrderNo document number, optional when id is provided
     * @param companyId company identifier used for secure lookup
     * @return full edit payload
     */
    SaleOrderEditDto getEditSaleOrder(Integer id, Integer saleOrderNo, Integer companyId);

    /**
     * Updates a full sale-order aggregate from the edit page. The caller sends
     * the master row together with nested details so the service can keep all
     * child collections in sync.
     *
     * @param id sale-order identifier
     * @param dto complete sale-order request payload
     * @return updated master DTO
     */
    SaleOrderMasterDto update(Integer id, SaleOrderDTO dto);

    /**
     * Updates only the master row because some smaller workflows edit the
     * header without resubmitting nested collections.
     *
     * @param id sale-order identifier
     * @param dto master DTO with updated values
     * @return updated master DTO
     */
    SaleOrderMasterDto updateMaster(Integer id, SaleOrderMasterDto dto);

    /**
     * Updates only the job status from the sale-order view page so users can
     * quickly move a job between statuses without reopening the full edit form.
     *
     * @param id sale-order identifier
     * @param companyId company identifier for validation
     * @param jobStatusId selected job status identifier
     * @return lightweight response with the persisted status selection
     */
    SaleOrderStatusUpdateDto updateStatus(Integer id, Integer companyId, Integer jobStatusId);

    /**
     * Updates the fields that the list view edits inline: status plus loading
     * and off ETA/ETB values.
     *
     * @param id sale-order identifier
     * @param dto quick-update request payload
     * @return lightweight response for list-view refresh
     */
    SaleOrderQuickUpdateDto updateQuickFields(Integer id, SaleOrderQuickUpdateDto dto);

    /**
     * Performs a logical delete because the module already uses the Active flag
     * in reads and should preserve audit history.
     *
     * @param id sale-order identifier
     * @return true when the record was marked inactive
     */
    boolean delete(Integer id);

    /**
     * SelectSaleOrder - Complex filtered search equivalent to .NET SelectSaleOrder method
     * Returns combined SaleMaster and SaleDetails data with dynamic filtering
     */
    SaleF5View selectSaleOrder(SaleOrderFilterDTO filter);
}

