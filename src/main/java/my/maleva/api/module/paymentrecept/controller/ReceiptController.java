package my.maleva.api.module.paymentrecept.controller;

import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.integration.qne.QnePushResponses;
import my.maleva.api.module.paymentrecept.dto.ReceiptDto;
import my.maleva.api.module.paymentrecept.service.ReceiptQneService;
import my.maleva.api.module.paymentrecept.service.ReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Receipt REST Controller
 * Handles all RESTful API endpoints for Receipt operations
 * Base URL: /api/receipts
 */
@RestController
@RequestMapping("/api/receipts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReceiptController {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptController.class);

    @Autowired
    private ReceiptService receiptService;

    @Autowired
    private ReceiptQneService receiptQneService;

    /**
     * Push receipt to QNE (create + invoice knockoff)
     * POST /api/receipts/{id}/push-qne?companyId=1
     *
     * Legacy synced the receipt as a side effect of viewing it (ReceiptVIEW);
     * here it is this explicit call, still create-once via the empty-QNECode
     * guard. Unlike legacy, a failed knockoff is reported (IsSuccess=false)
     * instead of silently swallowed — the receipt's QNE ids are still
     * persisted, because the receipt does exist in QNE at that point.
     */
    @PostMapping("/{id}/push-qne")
    @PermitAll
    public ResponseEntity<ApiResponse<Map<String, Object>>> pushToQne(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        logger.info("Pushing receipt ID: {} to QNE for company: {}", id, companyId);
        try {
            if (id == null || id <= 0 || companyId == null || companyId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid ID or company ID", 400));
            }
            return QnePushResponses.toResponse(receiptQneService.push(id, companyId));
        } catch (Exception e) {
            logger.error("Error pushing receipt to QNE", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error pushing to QNE: " + e.getMessage(), 500));
        }
    }

    /**
     * Get all Receipt records by company ID
     * GET /api/receipts/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    @PermitAll
    public ResponseEntity<List<ReceiptDto>> getAllByCompanyId(
            @PathVariable Integer companyRefId) {
        logger.info("Fetching all Receipt records for company: {}", companyRefId);
        List<ReceiptDto> records = receiptService.getAllByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get Receipt by ID
     * GET /api/receipts/{id}
     */
    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching Receipt by ID: {}", id);
        Optional<ReceiptDto> record = receiptService.getById(id);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Receipt not found with ID: " + id);
        }
    }

    /**
     * Create new Receipt record
     * POST /api/receipts
     */
    @PostMapping
    @PermitAll
    public ResponseEntity<?> create(@Valid @RequestBody ReceiptDto dto) {
        logger.info("Creating new Receipt for company: {}", dto.getCompanyRefId());

        try {
            ReceiptDto created = receiptService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating Receipt", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating Receipt: " + e.getMessage());
        }
    }

    /**
     * Update Receipt record
     * PUT /api/receipts/{id}
     */
    @PutMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody ReceiptDto dto) {
        logger.info("Updating Receipt with ID: {}", id);

        try {
            ReceiptDto updated = receiptService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("Receipt not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Receipt not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating Receipt", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating Receipt: " + e.getMessage());
        }
    }

    /**
     * Delete Receipt record
     * DELETE /api/receipts/{id}
     */
    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting Receipt with ID: {}", id);

        try {
            boolean deleted = receiptService.delete(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Receipt not found with ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Error deleting Receipt", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting Receipt: " + e.getMessage());
        }
    }

    /**
     * Get Receipt by customer
     * GET /api/receipts/company/{companyRefId}/customer/{customerRefId}
     */
    @GetMapping("/company/{companyRefId}/customer/{customerRefId}")
    @PermitAll
    public ResponseEntity<List<ReceiptDto>> getByCustomer(
            @PathVariable Integer companyRefId,
            @PathVariable Integer customerRefId) {
        logger.info("Fetching Receipt for customer: {}", customerRefId);
        List<ReceiptDto> records = receiptService.getByCustomer(companyRefId, customerRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get Receipt by bank
     * GET /api/receipts/company/{companyRefId}/bank/{bankRefId}
     */
    @GetMapping("/company/{companyRefId}/bank/{bankRefId}")
    @PermitAll
    public ResponseEntity<List<ReceiptDto>> getByBank(
            @PathVariable Integer companyRefId,
            @PathVariable Integer bankRefId) {
        logger.info("Fetching Receipt for bank: {}", bankRefId);
        List<ReceiptDto> records = receiptService.getByBank(companyRefId, bankRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get Receipt by CNumber
     * GET /api/receipts/company/{companyRefId}/cnumber/{cNumber}
     */
    @GetMapping("/company/{companyRefId}/cnumber/{cNumber}")
    @PermitAll
    public ResponseEntity<?> getByCNumber(
            @PathVariable Integer companyRefId,
            @PathVariable Integer cNumber) {
        logger.info("Fetching Receipt by CNumber: {} for company: {}", cNumber, companyRefId);
        if (companyRefId == null || cNumber == null) {
            logger.warn("Invalid request: companyRefId or cNumber is null");
            return ResponseEntity.badRequest().body("companyRefId and cNumber must be provided");
        }
        try {
            Optional<ReceiptDto> record = receiptService.getByCNumber(companyRefId, cNumber);
            if (record.isPresent()) {
                return ResponseEntity.ok(record.get());
            } else {
                logger.warn("Receipt not found with CNumber: {} for company: {}", cNumber, companyRefId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(String.format("Receipt not found with CNumber: %d for company: %d", cNumber, companyRefId));
            }
        } catch (Exception e) {
            logger.error("Error fetching Receipt by CNumber: {} for company: {}", cNumber, companyRefId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching Receipt: " + e.getMessage());
        }
    }

    /**
     * Get Receipt by date range
     * GET /api/receipts/company/{companyRefId}/date-range?startDate=2026-02-01T00:00:00&endDate=2026-02-28T23:59:59
     */
    @GetMapping("/company/{companyRefId}/date-range")
    @PermitAll
    public ResponseEntity<List<ReceiptDto>> getByDateRange(
            @PathVariable Integer companyRefId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        logger.info("Fetching Receipt between dates: {} to {}", startDate, endDate);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        List<ReceiptDto> records = receiptService.getByDateRange(companyRefId, start, end);
        return ResponseEntity.ok(records);
    }

    /**
     * Get Receipt by reference number
     * GET /api/receipts/company/{companyRefId}/ref-number/{refNumber}
     */
    @GetMapping("/company/{companyRefId}/ref-number/{refNumber}")
    @PermitAll
    public ResponseEntity<?> getByRefNumber(
            @PathVariable Integer companyRefId,
            @PathVariable String refNumber) {
        logger.info("Fetching Receipt by reference number: {}", refNumber);
        Optional<ReceiptDto> record = receiptService.getByRefNumber(companyRefId, refNumber);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Receipt not found with reference number: " + refNumber);
        }
    }

    /**
     * Get Receipt by CNumberDisplay
     * GET /api/receipts/cnumber-display/{cNumberDisplay}
     */
    @GetMapping("/cnumber-display/{cNumberDisplay}")
    @PermitAll
    public ResponseEntity<?> getByCNumberDisplay(@PathVariable String cNumberDisplay) {
        logger.info("Fetching Receipt by CNumberDisplay: {}", cNumberDisplay);
        Optional<ReceiptDto> record = receiptService.getByCNumberDisplay(cNumberDisplay);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Receipt not found with CNumberDisplay: " + cNumberDisplay);
        }
    }

    /**
     * Get Receipt by PV Status
     * GET /api/receipts/company/{companyRefId}/pv-status/{pvStatus}
     */
    @GetMapping("/company/{companyRefId}/pv-status/{pvStatus}")
    @PermitAll
    public ResponseEntity<List<ReceiptDto>> getByPvStatus(
            @PathVariable Integer companyRefId,
            @PathVariable Integer pvStatus) {
        logger.info("Fetching Receipt by PV Status: {}", pvStatus);
        List<ReceiptDto> records = receiptService.getByPvStatus(companyRefId, pvStatus);
        return ResponseEntity.ok(records);
    }

    /**
     * Count Receipt by company
     * GET /api/receipts/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    @PermitAll
    public ResponseEntity<?> countByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting Receipt records for company: {}", companyRefId);
        long count = receiptService.countByCompanyId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count Receipt by PV Status
     * GET /api/receipts/company/{companyRefId}/count/pv-status/{pvStatus}
     */
    @GetMapping("/company/{companyRefId}/count/pv-status/{pvStatus}")
    @PermitAll
    public ResponseEntity<?> countByPvStatus(
            @PathVariable Integer companyRefId,
            @PathVariable Integer pvStatus) {
        logger.info("Counting Receipt by PV Status for company: {}", companyRefId);
        long count = receiptService.countByPvStatus(companyRefId, pvStatus);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Change Receipt status
     * POST /api/receipts/{id}/change-status
     */
    @PostMapping("/{id}/change-status")
    @PermitAll
    public ResponseEntity<?> changeStatus(
            @PathVariable Integer id,
            @RequestParam Integer pvStatus) {
        logger.info("Changing Receipt status to: {}", pvStatus);

        try {
            ReceiptDto updated = receiptService.changeStatus(id, pvStatus);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("Receipt not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Receipt not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error changing Receipt status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error changing status: " + e.getMessage());
        }
    }
}


