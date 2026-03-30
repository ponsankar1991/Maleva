package my.maleva.api.module.saleorder.controller;

import my.maleva.api.module.invoice.dto.SaleF5View;
import my.maleva.api.module.saleorder.dto.SaleOrderDTO;
import my.maleva.api.module.saleorder.dto.SaleOrderMasterDto;
import my.maleva.api.module.saleorder.dto.SaleOrderFilterDTO;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.saleorder.service.SaleOrderMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * SaleOrderMasterController - REST Controller for SaleOrderMaster API
 */
@RestController
@RequestMapping("/api/sale-orders")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
public class SaleOrderMasterController {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderMasterController.class);

    @Autowired
    private SaleOrderMasterService service;


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
        String message = isCreate ? "Sale order created successfully" : "Sale order updated successfully";

        logger.info("Save SaleOrder completed - operation: {}, savedId: {}, company: {}",
                isCreate ? "CREATE" : "UPDATE",
                savedOrder.getId(),
                savedOrder.getCompanyRefId());

        return ResponseEntity.status(status)
                .body(ApiResponse.success(savedOrder, message));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleOrderMasterDto dto) {
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @PostMapping("/search")
    public ResponseEntity<?> selectSaleOrder(@Valid @RequestBody SaleOrderFilterDTO filter) {
        logger.info("SelectSaleOrder endpoint called - Company: {}, Customer: {}, Employee: {}",
                filter.getComid(), filter.getId(), filter.getEmployeeid());
        logger.info("Full Search Filter Payload: {}", filter);

        try {
            if (filter.getComid() == null || filter.getComid() == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Company ID is required", null, null, null));
            }

            logger.info("Starting execution of service.selectSaleOrder for Company: {}", filter.getComid());
            long startTime = System.currentTimeMillis();
            SaleF5View result = service.selectSaleOrder(filter);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Finished execution of service.selectSaleOrder. Time taken: {} ms", duration);

            logger.info("SelectSaleOrder completed - Returned {} master records and {} detail records",
                    result.getSalemaster() != null ? result.getSalemaster().size() : 0,
                    result.getSaledetails() != null ? result.getSaledetails().size() : 0);

            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    200,
                    "Success",
                    result,
                    "SaleF5View",
                    null
            ));

        } catch (RuntimeException e) {
            logger.error("RuntimeException in SelectSaleOrder - Company: {}, Error: {}",
                    filter.getComid(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, null,
                            "Api Details: SaleOrder_SelectSaleOrder"));
        } catch (Exception e) {
            logger.error("Unexpected error in SelectSaleOrder - Company: {}, Error: {}",
                    filter.getComid(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Internal server error", null, null,
                            e.getMessage()));
        }
    }
}
