package my.maleva.api.module.stockin.controller;

import my.maleva.api.module.stockin.dto.StockInDto;
import my.maleva.api.module.stockin.service.StockInService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

/**
 * StockInController - REST Controller for StockIn API
 */
@RestController
@RequestMapping("/api/stock-ins")
@PermitAll
public class StockInController {

    private static final Logger logger = LoggerFactory.getLogger(StockInController.class);

    @Autowired
    private StockInService service;

    /**
     * Get all StockIn records by company ID
     * GET /api/stock-ins/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<StockInDto>> getByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching StockIn for company: {}", companyRefId);
        return ResponseEntity.ok(service.getByCompanyRefId(companyRefId));
    }

    /**
     * Get all StockIn records by user ID
     * GET /api/stock-ins/user/{userRefId}
     */
    @GetMapping("/user/{userRefId}")
    public ResponseEntity<List<StockInDto>> getByUserRefId(@PathVariable Integer userRefId) {
        logger.info("Fetching StockIn for user: {}", userRefId);
        return ResponseEntity.ok(service.getByUserRefId(userRefId));
    }

    /**
     * Get all StockIn records by employee ID
     * GET /api/stock-ins/employee/{employeeRefId}
     */
    @GetMapping("/employee/{employeeRefId}")
    public ResponseEntity<List<StockInDto>> getByEmployeeRefId(@PathVariable Integer employeeRefId) {
        logger.info("Fetching StockIn for employee: {}", employeeRefId);
        return ResponseEntity.ok(service.getByEmployeeRefId(employeeRefId));
    }

    /**
     * Get all StockIn records by sale order master ID
     * GET /api/stock-ins/sale-order/{saleOrderMasterRefId}
     */
    @GetMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<List<StockInDto>> getBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Fetching StockIn for sale order master: {}", saleOrderMasterRefId);
        return ResponseEntity.ok(service.getBySaleOrderMasterRefId(saleOrderMasterRefId));
    }

    /**
     * Get all StockIn records by port master ID
     * GET /api/stock-ins/port/{portMasterRefId}
     */
    @GetMapping("/port/{portMasterRefId}")
    public ResponseEntity<List<StockInDto>> getByPortMasterRefId(@PathVariable Integer portMasterRefId) {
        logger.info("Fetching StockIn for port master: {}", portMasterRefId);
        return ResponseEntity.ok(service.getByPortMasterRefId(portMasterRefId));
    }

    /**
     * Get all StockIn records by status
     * GET /api/stock-ins/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<StockInDto>> getByStatus(@PathVariable Integer status) {
        logger.info("Fetching StockIn for status: {}", status);
        return ResponseEntity.ok(service.getByStatus(status));
    }

    /**
     * Get StockIn records by company and status
     * GET /api/stock-ins/company/{companyRefId}/status/{status}
     */
    @GetMapping("/company/{companyRefId}/status/{status}")
    public ResponseEntity<List<StockInDto>> getByCompanyAndStatus(
            @PathVariable Integer companyRefId,
            @PathVariable Integer status) {
        logger.info("Fetching StockIn for company: {} and status: {}", companyRefId, status);
        return ResponseEntity.ok(service.getByCompanyAndStatus(companyRefId, status));
    }

    /**
     * Get StockIn by ID
     * GET /api/stock-ins/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching StockIn by ID: {}", id);
        Optional<StockInDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get StockIn by C Number
     * GET /api/stock-ins/c-number/{cNumber}/company/{companyRefId}
     */
    @GetMapping("/c-number/{cNumber}/company/{companyRefId}")
    public ResponseEntity<?> getByCNumber(@PathVariable Integer cNumber, @PathVariable Integer companyRefId) {
        logger.info("Fetching StockIn by C Number: {} for company: {}", cNumber, companyRefId);
        Optional<StockInDto> record = service.getByCNumber(cNumber, companyRefId);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get StockIn by Barcode
     * GET /api/stock-ins/barcode/{barcode}
     */
    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<?> getByBarcode(@PathVariable String barcode) {
        logger.info("Fetching StockIn by barcode: {}", barcode);
        Optional<StockInDto> record = service.getByBarcode(barcode);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new StockIn
     * POST /api/stock-ins
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody StockInDto dto) {
        logger.info("Creating new StockIn");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Process StockIn (SP_StockIn logic - DELETE existing + INSERT or UPDATE)
     * POST /api/stock-ins/process
     */
    @PostMapping("/process")
    public ResponseEntity<?> processStockIn(@Valid @RequestBody StockInDto dto) {
        logger.info("Processing StockIn with SP_StockIn logic");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.processStockIn(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update StockIn
     * PUT /api/stock-ins/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody StockInDto dto) {
        logger.info("Updating StockIn with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete StockIn
     * DELETE /api/stock-ins/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting StockIn with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Count StockIn records by company ID
     * GET /api/stock-ins/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    public ResponseEntity<?> countByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting StockIn for company: {}", companyRefId);
        long count = service.countByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count StockIn records by company and status
     * GET /api/stock-ins/company/{companyRefId}/status/{status}/count
     */
    @GetMapping("/company/{companyRefId}/status/{status}/count")
    public ResponseEntity<?> countByCompanyAndStatus(
            @PathVariable Integer companyRefId,
            @PathVariable Integer status) {
        logger.info("Counting StockIn for company: {} and status: {}", companyRefId, status);
        long count = service.countByCompanyAndStatus(companyRefId, status);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Delete all StockIn records by sale order master ID
     * DELETE /api/stock-ins/sale-order/{saleOrderMasterRefId}
     */
    @DeleteMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<?> deleteAllBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Deleting all StockIn records for SaleOrderMasterRefId: {}", saleOrderMasterRefId);
        try {
            service.deleteAllBySaleOrderMasterRefId(saleOrderMasterRefId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}


