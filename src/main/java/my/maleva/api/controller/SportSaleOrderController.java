package my.maleva.api.controller;

import my.maleva.api.dto.SportSaleOrderDto;
import my.maleva.api.service.SportSaleOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SportSaleOrderController - REST Controller for SportSaleOrder API
 */
@RestController
@RequestMapping("/api/sport-sale-orders")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
public class SportSaleOrderController {

    private static final Logger logger = LoggerFactory.getLogger(SportSaleOrderController.class);

    @Autowired
    private SportSaleOrderService service;

    /**
     * Get all SportSaleOrder records by company ID
     * GET /api/sport-sale-orders/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<SportSaleOrderDto>> getByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching SportSaleOrder for company: {}", companyRefId);
        return ResponseEntity.ok(service.getByCompanyRefId(companyRefId));
    }

    /**
     * Get active SportSaleOrder records by company ID
     * GET /api/sport-sale-orders/company/{companyRefId}/active
     */
    @GetMapping("/company/{companyRefId}/active")
    public ResponseEntity<List<SportSaleOrderDto>> getActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching active SportSaleOrder for company: {}", companyRefId);
        return ResponseEntity.ok(service.getActiveByCompanyRefId(companyRefId));
    }

    /**
     * Get all SportSaleOrder records by customer ID
     * GET /api/sport-sale-orders/customer/{customerRefId}
     */
    @GetMapping("/customer/{customerRefId}")
    public ResponseEntity<List<SportSaleOrderDto>> getByCustomerRefId(@PathVariable Integer customerRefId) {
        logger.info("Fetching SportSaleOrder for customer: {}", customerRefId);
        return ResponseEntity.ok(service.getByCustomerRefId(customerRefId));
    }

    /**
     * Get SportSaleOrder records by company and customer
     * GET /api/sport-sale-orders/company/{companyRefId}/customer/{customerRefId}
     */
    @GetMapping("/company/{companyRefId}/customer/{customerRefId}")
    public ResponseEntity<List<SportSaleOrderDto>> getByCompanyAndCustomer(
            @PathVariable Integer companyRefId,
            @PathVariable Integer customerRefId) {
        logger.info("Fetching SportSaleOrder for company: {} and customer: {}", companyRefId, customerRefId);
        return ResponseEntity.ok(service.getByCompanyAndCustomer(companyRefId, customerRefId));
    }

    /**
     * Get all SportSaleOrder records by job master ID
     * GET /api/sport-sale-orders/job/{jobMasterRefId}
     */
    @GetMapping("/job/{jobMasterRefId}")
    public ResponseEntity<List<SportSaleOrderDto>> getByJobMasterRefId(@PathVariable Integer jobMasterRefId) {
        logger.info("Fetching SportSaleOrder for job master: {}", jobMasterRefId);
        return ResponseEntity.ok(service.getByJobMasterRefId(jobMasterRefId));
    }

    /**
     * Get all SportSaleOrder records by employee ID
     * GET /api/sport-sale-orders/employee/{employeeRefId}
     */
    @GetMapping("/employee/{employeeRefId}")
    public ResponseEntity<List<SportSaleOrderDto>> getByEmployeeRefId(@PathVariable Integer employeeRefId) {
        logger.info("Fetching SportSaleOrder for employee: {}", employeeRefId);
        return ResponseEntity.ok(service.getByEmployeeRefId(employeeRefId));
    }

    /**
     * Get all SportSaleOrder records by status
     * GET /api/sport-sale-orders/status/{jStatus}
     */
    @GetMapping("/status/{jStatus}")
    public ResponseEntity<List<SportSaleOrderDto>> getByStatus(@PathVariable Integer jStatus) {
        logger.info("Fetching SportSaleOrder for status: {}", jStatus);
        return ResponseEntity.ok(service.getByStatus(jStatus));
    }

    /**
     * Get SportSaleOrder by ID
     * GET /api/sport-sale-orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SportSaleOrder by ID: {}", id);
        Optional<SportSaleOrderDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get SportSaleOrder by AWB Number
     * GET /api/sport-sale-orders/awb/{awbNo}
     */
    @GetMapping("/awb/{awbNo}")
    public ResponseEntity<?> getByAwbNo(@PathVariable String awbNo) {
        logger.info("Fetching SportSaleOrder by AWB No: {}", awbNo);
        Optional<SportSaleOrderDto> record = service.getByAwbNo(awbNo);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new SportSaleOrder
     * POST /api/sport-sale-orders
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SportSaleOrderDto dto) {
        logger.info("Creating new SportSaleOrder");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Process SportSaleOrder (SP_SoptSaleorder logic - INSERT or UPDATE)
     * POST /api/sport-sale-orders/process
     */
    @PostMapping("/process")
    public ResponseEntity<?> processSportSaleOrder(@Valid @RequestBody SportSaleOrderDto dto) {
        logger.info("Processing SportSaleOrder with SP_SoptSaleorder logic");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.processSportSaleOrder(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update SportSaleOrder
     * PUT /api/sport-sale-orders/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SportSaleOrderDto dto) {
        logger.info("Updating SportSaleOrder with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete SportSaleOrder
     * DELETE /api/sport-sale-orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SportSaleOrder with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Count SportSaleOrder records by company ID
     * GET /api/sport-sale-orders/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    public ResponseEntity<?> countByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting SportSaleOrder for company: {}", companyRefId);
        long count = service.countByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count active SportSaleOrder records by company ID
     * GET /api/sport-sale-orders/company/{companyRefId}/active/count
     */
    @GetMapping("/company/{companyRefId}/active/count")
    public ResponseEntity<?> countActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting active SportSaleOrder for company: {}", companyRefId);
        long count = service.countActiveByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }
}

