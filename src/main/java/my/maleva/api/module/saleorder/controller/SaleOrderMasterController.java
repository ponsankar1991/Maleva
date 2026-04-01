package my.maleva.api.module.saleorder.controller;

import my.maleva.api.module.invoice.dto.SaleF5View;
import my.maleva.api.module.saleorder.dto.SaleOrderFilterDTO;
import my.maleva.api.module.saleorder.dto.SaleOrderDTO;
import my.maleva.api.module.saleorder.dto.SaleOrderEditDto;
import my.maleva.api.module.saleorder.dto.SaleOrderMasterDto;
import my.maleva.api.module.saleorder.dto.SaleOrderQuickUpdateDto;
import my.maleva.api.module.saleorder.dto.SaleOrderStatusUpdateDto;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.saleorder.service.SaleOrderMasterService;
import my.maleva.api.module.saleorder.util.SaleOrderApiConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * SaleOrderMasterController - REST Controller for SaleOrderMaster API
 */
@RestController
@Validated
@RequestMapping("/api/sale-orders")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
public class SaleOrderMasterController {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderMasterController.class);

    private final SaleOrderMasterService service;

    public SaleOrderMasterController(SaleOrderMasterService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleOrderEditDto> getById(@PathVariable @Positive Integer id) {
        logger.info("Get SaleOrder edit payload request received - id: {}", id);
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/edit")
    public ResponseEntity<ApiResponse<SaleOrderEditDto>> getEditSaleOrder(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer saleOrderNo,
            @RequestParam Integer companyId) {
        logger.info("Edit SaleOrder request received - id: {}, saleOrderNo: {}, companyId: {}",
                id, saleOrderNo, companyId);
        return ResponseEntity.ok(ApiResponse.success(
                service.getEditSaleOrder(id, saleOrderNo, companyId),
                SaleOrderApiConstants.MESSAGE_SELECT_SUCCESS
        ));
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<SaleOrderMasterDto>> save(@Valid @RequestBody SaleOrderDTO dto) {
        boolean isCreate = dto.getId() == null || dto.getId() == 0;

        logger.info("Save SaleOrder request received - operation: {}, company: {}, customer: {}, id: {}, cNumber: {}",
                isCreate ? "CREATE" : "UPDATE",
                dto.getCompanyRefId(),
                dto.getCustomerRefId(),
                dto.getId(),
                dto.getCNumber());

        SaleOrderMasterDto savedOrder = service.save(dto);
        HttpStatus status = isCreate ? HttpStatus.CREATED : HttpStatus.OK;
        String message = isCreate
                ? SaleOrderApiConstants.MESSAGE_CREATE_SUCCESS
                : SaleOrderApiConstants.MESSAGE_UPDATE_SUCCESS;

        logger.info("Save SaleOrder completed - operation: {}, savedId: {}, company: {}",
                isCreate ? "CREATE" : "UPDATE",
                savedOrder.getId(),
                savedOrder.getCompanyRefId());

        return ResponseEntity.status(status).body(ApiResponse.success(savedOrder, message));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleOrderMasterDto>> update(@PathVariable @Positive Integer id,
                                                                  @Valid @RequestBody SaleOrderDTO dto) {
        logger.info("Update SaleOrder aggregate request received - id: {}, company: {}, customer: {}",
                id, dto.getCompanyRefId(), dto.getCustomerRefId());

        return ResponseEntity.ok(ApiResponse.success(
                service.update(id, dto),
                SaleOrderApiConstants.MESSAGE_UPDATE_SUCCESS
        ));
    }

    @PutMapping("/{id}/master")
    public ResponseEntity<SaleOrderMasterDto> updateMaster(@PathVariable @Positive Integer id,
                                                           @Valid @RequestBody SaleOrderMasterDto dto) {
        logger.info("Update SaleOrder master request received - id: {}, company: {}", id, dto.getCompanyRefId());
        return ResponseEntity.ok(service.updateMaster(id, dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SaleOrderStatusUpdateDto>> updateStatus(
            @PathVariable @Positive Integer id,
            @RequestParam @Positive Integer companyId,
            @RequestParam(name = "jStatus") @Positive Integer jStatus) {
        logger.info("Update SaleOrder status request received - id: {}, company: {}, jStatus: {}",
                id, companyId, jStatus);

        return ResponseEntity.ok(ApiResponse.success(
                service.updateStatus(id, companyId, jStatus),
                SaleOrderApiConstants.MESSAGE_UPDATE_SUCCESS
        ));
    }

    @PutMapping("/{id}/quick-update")
    public ResponseEntity<ApiResponse<SaleOrderQuickUpdateDto>> updateQuickFields(
            @PathVariable @Positive Integer id,
            @RequestBody SaleOrderQuickUpdateDto dto) {
        logger.info("Quick update SaleOrder request received - id: {}, company: {}, jStatus: {}",
                id, dto != null ? dto.getCompanyRefId() : null, dto != null ? dto.getJStatus() : null);

        return ResponseEntity.ok(ApiResponse.success(
                service.updateQuickFields(id, dto),
                SaleOrderApiConstants.MESSAGE_UPDATE_SUCCESS
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        logger.info("Delete SaleOrder request received - id: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<SaleF5View>> selectSaleOrder(@Valid @RequestBody SaleOrderFilterDTO filter) {
        logger.info("SelectSaleOrder endpoint called - Company: {}, Customer: {}, Employee: {}",
                filter.getComid(), filter.getId(), filter.getEmployeeid());

        long startTime = System.currentTimeMillis();
        SaleF5View result = service.selectSaleOrder(filter);
        long duration = System.currentTimeMillis() - startTime;

        logger.info("SelectSaleOrder completed in {} ms - Returned {} master records and {} detail records",
                duration,
                result.getSalemaster() != null ? result.getSalemaster().size() : 0,
                result.getSaledetails() != null ? result.getSaledetails().size() : 0);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                SaleOrderApiConstants.DEFAULT_STATUS_CODE,
                SaleOrderApiConstants.MESSAGE_SELECT_SUCCESS,
                result,
                SaleOrderApiConstants.RESPONSE_DATA3_SALE_F5_VIEW,
                null
        ));
    }
}
