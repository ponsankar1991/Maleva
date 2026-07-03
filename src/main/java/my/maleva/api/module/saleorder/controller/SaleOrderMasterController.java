package my.maleva.api.module.saleorder.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import my.maleva.api.module.saleorder.dto.UpdateJobStatusDto;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.common.exception.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;


/**
 * SaleOrderMasterController - REST Controller for SaleOrderMaster API
 */
@RestController
@Validated
@RequestMapping("/api/sale-orders")
@PermitAll
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
        return ResponseEntity.ok(ApiResponse.success(service.getEditSaleOrder(id, saleOrderNo, companyId), SaleOrderApiConstants.MESSAGE_SELECT_SUCCESS
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

        return ResponseEntity.ok(ApiResponse.success(service.update(id, dto), SaleOrderApiConstants.MESSAGE_UPDATE_SUCCESS
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

        return ResponseEntity.ok(ApiResponse.success(
                result,
                SaleOrderApiConstants.MESSAGE_SELECT_SUCCESS
        ));
    }

    /**
     * Update Job Status for a Sale Order
     * PUT /api/sale-orders/{id}/job-status
     * POST /api/sale-orders/update-job-status?id={id}
     *
     * Updates the JStatus field of a SaleOrderMaster record and sets Modified_Date to current timestamp.
     * This is equivalent to the .NET UpdateJobStatus method.
     *
     * @param id Sale Order Master ID (path variable)
     * @param updateDto UpdateJobStatusDto containing new jobStatusId
     * @return ApiResponse with success status and message
     */
    @PutMapping("/{id}/job-status")
    public ResponseEntity<ApiResponse<Void>> updateJobStatus(
            @PathVariable @Positive Integer id,
            @Valid @RequestBody UpdateJobStatusDto updateDto) {
        logger.info("Update job status request received - Sale Order ID: {}, New Job Status ID: {}",
                id, updateDto.getJobStatusId());

        try {
            service.updateJobStatus(id, updateDto.getJobStatusId());

            logger.info("Job status updated successfully - Sale Order ID: {}, New Job Status ID: {}",
                    id, updateDto.getJobStatusId());

            return ResponseEntity.ok(ApiResponse.success(
                    null,
                    "Update JobStatus Successfully!"
            ));

        } catch (EntityNotFoundException ex) {
            logger.warn("Sale Order not found - ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value())
            );

        } catch (InvalidRequestException ex) {
            logger.warn("Invalid request - {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value())
            );

        } catch (Exception ex) {
            logger.error("Error updating job status for Sale Order ID: {}", id, ex);
            Throwable realError = ex;
            while (realError.getCause() != null) {
                realError = realError.getCause();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Error updating job status: " + realError.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value())
            );
        }
    }

    /**
     * Alternative endpoint for Update Job Status (POST variant)
     * POST /api/sale-orders/update-job-status?id={id}
     *
     * Same functionality as PUT variant, provided for backward compatibility.
     *
     * @param id Sale Order Master ID (query parameter)
     * @param updateDto UpdateJobStatusDto containing new jobStatusId
     * @return ApiResponse with success status and message
     */
    @PostMapping("/update-job-status")
    public ResponseEntity<ApiResponse<Void>> updateJobStatusPost(
            @RequestParam @Positive Integer id,
            @Valid @RequestBody UpdateJobStatusDto updateDto) {
        logger.info("Update job status request (POST) received - Sale Order ID: {}, New Job Status ID: {}",
                id, updateDto.getJobStatusId());

        try {
            service.updateJobStatus(id, updateDto.getJobStatusId());

            logger.info("Job status updated successfully - Sale Order ID: {}, New Job Status ID: {}",
                    id, updateDto.getJobStatusId());

            return ResponseEntity.ok(ApiResponse.success(
                    null,
                    "Update JobStatus Successfully!"
            ));

        } catch (EntityNotFoundException ex) {
            logger.warn("Sale Order not found - ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value())
            );

        } catch (InvalidRequestException ex) {
            logger.warn("Invalid request - {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value())
            );

        } catch (Exception ex) {
            logger.error("Error updating job status for Sale Order ID: {}", id, ex);
            Throwable realError = ex;
            while (realError.getCause() != null) {
                realError = realError.getCause();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Error updating job status: " + realError.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value())
            );
        }
    }


    @GetMapping("/count")
    @Operation(
            summary     = "Check pending port-charge pops for a Sale Order job",
            description = "Returns a count of SaleOrderMaster rows where at least one "
                    + "port-charge pop flag is active and its waiver flag is not set. "
                    + "A count > 0 means the job still has pending charges."
    )

    public ResponseEntity<ApiResponse<Integer>> selectPortChargeCount(
            @Parameter(description = "Company reference ID", required = true, example = "1")
            @RequestParam @Positive int companyId,

            @Parameter(description = "Sale Order Master ID (job ID)", required = true, example = "1001")
            @RequestParam @Positive int jobId
    ) {
        logger.info("[PortCharge] selectPortChargeCount called | companyId={} jobId={}", companyId, jobId);

        if (companyId <= 0 || jobId <= 0) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("companyId and jobId must be positive integers.", HttpStatus.BAD_REQUEST.value())
            );
        }

        Integer count = service.selectPortChargeCount(companyId, jobId);
        return ResponseEntity.ok(ApiResponse.success(count, "Pending port-charge count retrieved."));
    }


    @PostMapping("/job-view")
    @Operation(summary = "Get aggregated Sale Job View", description = "Mirrors .NET SaleJobView endpoint")
    public ResponseEntity<ApiResponse<List<my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto>>> getSaleJobView(
            @RequestBody my.maleva.api.module.saleorder.dto.SaleOrderFilterDTO filter) {
        try {
            List<my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto> result = service.getSaleJobView(filter);
            return ResponseEntity.ok(ApiResponse.success(result, "Success"));
        } catch (Exception ex) {
            logger.error("Error retrieving sale job view", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving sale job view: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping("/currency-view")
    @Operation(summary = "Get aggregated Sale Currency View", description = "Mirrors .NET SaleCurrencyView endpoint")
    public ResponseEntity<ApiResponse<List<my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto>>> getSaleCurrencyView(
            @RequestBody my.maleva.api.module.saleorder.dto.SaleOrderFilterDTO filter) {
        try {
            List<my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto> result = service.getSaleCurrencyView(filter);
            return ResponseEntity.ok(ApiResponse.success(result, "Success"));
        } catch (Exception ex) {
            logger.error("Error retrieving sale currency view", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving sale currency view: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
    @PostMapping("/employee-view")
    @Operation(summary = "Get aggregated Sale Employee View", description = "Mirrors .NET SaleEmployeeView endpoint")
    public ResponseEntity<ApiResponse<List<my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto>>> getSaleEmployeeView(
            @RequestBody my.maleva.api.module.saleorder.dto.SaleOrderFilterDTO filter) {
        try {
            List<my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto> result = service.getSaleEmployeeView(filter);
            return ResponseEntity.ok(ApiResponse.success(result, "Success"));
        } catch (Exception ex) {
            logger.error("Error retrieving sale employee view", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving sale employee view: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
    @PostMapping("/port-view")
    @Operation(summary = "Get aggregated Sale Port View", description = "Mirrors .NET SalePortView endpoint with dynamic pivoting")
    public ResponseEntity<ApiResponse<java.util.List<java.util.Map<String, Object>>>> getSalePortView(
            @RequestBody my.maleva.api.module.saleorder.dto.SaleOrderFilterDTO filter) {
        try {
            java.util.List<java.util.Map<String, Object>> result = service.getSalePortView(filter);
            return ResponseEntity.ok(ApiResponse.success(result, "Success"));
        } catch (my.maleva.api.common.exception.InvalidRequestException ex) {
            logger.error("Invalid request for sale port view", ex);


            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception ex) {
            logger.error("Error retrieving sale port view", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving sale port view: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping("/customer-view")
    @Operation(summary = "Get aggregated Sale Customer View", description = "Mirrors .NET SaleCustomerView endpoint")
    public ResponseEntity<ApiResponse<List<my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto>>> getSaleCustomerView(
            @RequestBody my.maleva.api.module.saleorder.dto.SaleOrderFilterDTO filter) {
        try {
            List<my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto> result = service.getSaleCustomerView(filter);
            return ResponseEntity.ok(ApiResponse.success(result, "Success"));
        } catch (Exception ex) {
            logger.error("Error retrieving sale customer view", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving sale customer view: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
