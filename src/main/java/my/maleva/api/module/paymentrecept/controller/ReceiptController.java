package my.maleva.api.module.paymentrecept.controller;

import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.integration.qne.QnePushResponses;
import my.maleva.api.module.paymentrecept.dto.ReceiptDto;
import my.maleva.api.module.paymentrecept.service.ReceiptQneService;
import my.maleva.api.module.paymentrecept.service.ReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.paymentrecept.dto.ReceiptSaveRequest;
import my.maleva.api.module.paymentrecept.dto.ReceiptSaveResponseDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Receipt REST Controller
 * Handles all RESTful API endpoints for Receipt operations
 * Base URLs: /api/receipts and /Receipt (legacy compatibility)
 */
@RestController
@RequestMapping(value = {"/api/receipts", "/Receipt"})
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReceiptController {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptController.class);

    @Autowired
    private ReceiptService receiptService;

    @Autowired
    private ReceiptQneService receiptQneService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private my.maleva.api.module.paymentrecept.print.ReceiptPdfService receiptPdfService;

    @Autowired
    private my.maleva.api.module.paymentrecept.mail.ReceiptMailService receiptMailService;

    /**
     * The RECEIPT ENTRY VIEW grid.
     * POST /api/receipts/search  {companyId, fromDate, toDate, customerId, employeeId, search}
     *
     * <p>Replaces the legacy {@code /Receipt/SelectReceipt}. Data1 carries
     * {@code receiptMaster}, {@code receiptDetails}, {@code totalAmount} and
     * {@code count}. A non-blank {@code search} is an exact receipt number and,
     * as in legacy, drops every other filter.
     */
    @PostMapping("/search")
    @PermitAll
    public ResponseEntity<ApiResponse<my.maleva.api.module.paymentrecept.dto.ReceiptViewDto>> search(
            @RequestBody my.maleva.api.module.paymentrecept.dto.ReceiptSearchRequest request,
            @RequestParam(value = "companyId", required = false) Integer paramCompanyId,
            @RequestHeader(value = "Comid", required = false) Integer headerComid) {
        if (request.getCompanyId() == null || request.getCompanyId() <= 0) {
            request.setCompanyId(paramCompanyId != null ? paramCompanyId : headerComid);
        }
        try {
            my.maleva.api.module.paymentrecept.dto.ReceiptViewDto view = receiptService.search(request);
            return ResponseEntity.ok(ApiResponse.success(view, "Success"));
        } catch (my.maleva.api.common.exception.InvalidRequestException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            logger.error("Error searching receipts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error searching receipts: " + e.getMessage(), 500));
        }
    }

    /**
     * A saved receipt loaded back into the entry screen.
     * GET /api/receipts/edit?companyId=6&id=123   or   &receiptNumber=2284
     *
     * <p>Replaces the legacy {@code /Receipt/EditReceipt}. Data1 is the header
     * plus {@code receiptDetails}: the customer's whole outstanding list with
     * this receipt's amounts merged in.
     */
    @GetMapping("/edit")
    @PermitAll
    public ResponseEntity<ApiResponse<my.maleva.api.module.paymentrecept.dto.ReceiptEditDto>> edit(
            @RequestParam Integer companyId,
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer receiptNumber) {
        try {
            return receiptService.edit(companyId, id, receiptNumber)
                    .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Success")))
                    .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Invalid Receipt No", 404)));
        } catch (my.maleva.api.common.exception.InvalidRequestException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            logger.error("Error loading receipt for edit", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error loading receipt: " + e.getMessage(), 500));
        }
    }

    /**
     * The receipt voucher as a PDF.
     * GET /api/receipts/{id}/print?companyId=6
     *
     * <p>Replaces the Crystal {@code ReportViewer.aspx?ReportName=ReceiptReport}
     * popup behind the EXPORT icon and the file export the mail used. Unlike
     * legacy {@code ReceiptVIEW}, printing no longer pushes the receipt to
     * QNE as a side effect — that is {@code POST /{id}/push-qne}.
     */
    @GetMapping(value = "/{id}/print", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @PermitAll
    public ResponseEntity<?> print(@PathVariable Integer id, @RequestParam Integer companyId) {
        if (id == null || id <= 0 || companyId == null || companyId <= 0) {
            return printProblem(HttpStatus.BAD_REQUEST, "Receipt id and company are required");
        }
        try {
            return receiptPdfService.render(id, companyId)
                    .<ResponseEntity<?>>map(rendered -> ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_PDF)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + rendered.fileName() + "\"")
                            .body(rendered.pdf()))
                    .orElseGet(() -> printProblem(HttpStatus.NOT_FOUND,
                            "Receipt " + id + " was not found for company " + companyId));
        } catch (Exception e) {
            logger.error("Receipt {} (company {}) could not be printed", id, companyId, e);
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            String reason = root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
            return printProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Receipt " + id + " could not be printed: " + reason);
        }
    }

    private static ResponseEntity<ApiResponse<Object>> printProblem(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(message, status.value()));
    }

    /**
     * What the SEND RECEIPT window is prefilled with.
     * GET /api/receipts/{id}/mail-info?companyId=6
     *
     * <p>Replaces the legacy {@code /Receipt/ReceiptMailInfo}. The PDF preview
     * is {@code GET /{id}/print}; nothing is written to disk any more.
     */
    @GetMapping("/{id}/mail-info")
    @PermitAll
    public ResponseEntity<ApiResponse<my.maleva.api.module.paymentrecept.dto.ReceiptMailInfoDto>> mailInfo(
            @PathVariable Integer id, @RequestParam Integer companyId) {
        try {
            return receiptMailService.info(id, companyId)
                    .map(info -> ResponseEntity.ok(ApiResponse.success(info, "Success")))
                    .orElseGet(() -> ResponseEntity.ok(ApiResponse.error(
                            "Receipt " + id + " was not found for this company", 404)));
        } catch (Exception e) {
            logger.error("Error reading mail info for receipt {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error reading receipt mail info: " + e.getMessage(), 500));
        }
    }

    /**
     * Mail the receipt voucher (and optionally its attachments) to the
     * addresses the operator confirmed.
     * POST /api/receipts/{id}/mail?companyId=6  {to:[..], cc:[..], subject, remarks, includeAttachments}
     *
     * <p>Replaces the legacy {@code /Receipt/SendReceiptMail}. A mail that did
     * not go out answers 200 with {@code IsSuccess=false} and the reason.
     */
    @PostMapping("/{id}/mail")
    @PermitAll
    public ResponseEntity<ApiResponse<Map<String, Object>>> mail(
            @PathVariable Integer id, @RequestParam Integer companyId,
            @RequestBody my.maleva.api.module.paymentrecept.dto.ReceiptMailRequest request) {
        try {
            var outcome = receiptMailService.send(id, companyId, request);
            Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("to", outcome.to());
            data.put("cc", outcome.cc());
            data.put("attachmentCount", outcome.attachmentCount());
            data.put("sentCopyWarning", outcome.sentCopyWarning());
            if (!outcome.ok()) {
                ApiResponse<Map<String, Object>> body = ApiResponse.error(outcome.message(), 200);
                body.setData1(data);
                return ResponseEntity.ok(body);
            }
            return ResponseEntity.ok(ApiResponse.success(data, outcome.message()));
        } catch (Exception e) {
            logger.error("Error mailing receipt {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error mailing receipt: " + e.getMessage(), 500));
        }
    }

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
     * Delete a receipt and its lines.
     * DELETE /api/receipts/{id}?companyId=6
     *
     * <p>Replaces the legacy {@code /Receipt/DeleteReceipt}. With a company the
     * delete is checked against it and refused for a receipt already in QNE;
     * the answer is the JSON envelope with the message the screen shows.
     * Without a company the old bare delete is kept for callers that never sent one.
     */
    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestParam(value = "companyId", required = false) Integer companyId,
                                    @RequestHeader(value = "Comid", required = false) Integer headerComid) {
        logger.info("Deleting Receipt with ID: {}", id);
        Integer company = companyId != null ? companyId : headerComid;
        try {
            if (company != null && company > 0) {
                String message = receiptService.deleteReceipt(id, company);
                return ResponseEntity.ok(ApiResponse.success(id, message));
            }
            boolean deleted = receiptService.delete(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Receipt not found with ID: " + id);
            }
        } catch (my.maleva.api.common.exception.InvalidRequestException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            logger.error("Error deleting Receipt", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting Receipt: " + e.getMessage(), 500));
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

    /**
     * Get max receipt number
     */
    @GetMapping("/company/{companyRefId}/max-receipt-no")
    public ResponseEntity<ApiResponse<String>> getMaxReceiptNo(
            @PathVariable Integer companyRefId,
            @RequestParam(required = false) String billType) {
        logger.info("Getting max receipt no for company: {}", companyRefId);
        String maxReceiptNo = receiptService.getMaxReceiptNo(companyRefId, billType);
        return ResponseEntity.ok(ApiResponse.success(maxReceiptNo, "Max receipt number fetched successfully"));
    }

    /**
     * Get customer balance (from CustomerBalance or CustomerBalance_Single)
     * POST /api/receipts/customer-balance
     */
    @PostMapping("/customer-balance")
    @PermitAll
    public ResponseEntity<ApiResponse<List<my.maleva.api.module.paymentrecept.dto.ReceiptBillDto>>> selectCustomerBalance(
            @RequestBody my.maleva.api.module.paymentrecept.dto.ReceiptViewBillRequest request) {
        logger.info("Fetching customer balance for company: {}, tilldate: {}", request.getCompanyRefId(), request.getTilldate());
        try {
            List<my.maleva.api.module.paymentrecept.dto.ReceiptBillDto> data = receiptService.selectCustomerBalance(request);
            ApiResponse<List<my.maleva.api.module.paymentrecept.dto.ReceiptBillDto>> response = ApiResponse.<List<my.maleva.api.module.paymentrecept.dto.ReceiptBillDto>>builder()
                    .isSuccess(true)
                    .statusCode(200)
                    .message("Success")
                    .data1(data)
                    .data(data)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching customer balance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error fetching customer balance: " + e.getMessage(), 500));
        }
    }

    /**
     * Select customer bills for receipt entry (from RT_CustomerBills)
     * POST /api/receipts/customer-bills
     * POST /api/receipts/SelectCustomerBills
     */
    @PostMapping(value = {"/customer-bills", "/SelectCustomerBills"})
    @PermitAll
    public ResponseEntity<ApiResponse<List<my.maleva.api.module.paymentrecept.dto.ReceiptBillDto>>> selectCustomerBills(
            @RequestBody my.maleva.api.module.paymentrecept.dto.ReceiptViewBillRequest request) {
        logger.info("Select customer bills called - customerId: {}, companyRefId: {}, excludeReceiptId: {}",
                request.getId(), request.getCompanyRefId(), request.getId2());
        try {
            List<my.maleva.api.module.paymentrecept.dto.ReceiptBillDto> list = receiptService.selectCustomerBills(request);
            ApiResponse<List<my.maleva.api.module.paymentrecept.dto.ReceiptBillDto>> response = ApiResponse.<List<my.maleva.api.module.paymentrecept.dto.ReceiptBillDto>>builder()
                    .isSuccess(true)
                    .statusCode(200)
                    .message("Success")
                    .data1(list)
                    .data(list)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error executing selectCustomerBills", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error executing selectCustomerBills: " + e.getMessage(), 500));
        }
    }

    /**
     * GET alternative for select customer bills
     * GET /api/receipts/customer-bills?id=10&companyRefId=6&id2=0
     */
    @GetMapping("/customer-bills")
    @PermitAll
    public ResponseEntity<ApiResponse<List<my.maleva.api.module.paymentrecept.dto.ReceiptBillDto>>> selectCustomerBillsGet(
            @RequestParam(name = "id", required = false) Integer id,
            @RequestParam(name = "companyRefId", required = false) Integer companyRefId,
            @RequestParam(name = "id2", required = false) Integer id2) {
        my.maleva.api.module.paymentrecept.dto.ReceiptViewBillRequest request = new my.maleva.api.module.paymentrecept.dto.ReceiptViewBillRequest();
        request.setId(id);
        request.setCompanyRefId(companyRefId);
        request.setId2(id2);
        return selectCustomerBills(request);
    }

    /**
     * Insert or update Receipt (migrated from legacy SP_Receipt / InsertReceipt)
     * Supports both JSON array [ { ... } ] and single object { ... }
     * Resolves company ID from Comid header, query parameter, or payload body
     *
     * POST /api/receipts/insert
     * POST /api/receipts/InsertReceipt
     * POST /Receipt/InsertReceipt
     */
    @PostMapping(value = {"/insert", "/InsertReceipt", "/save"})
    @PermitAll
    public ResponseEntity<ReceiptSaveResponseDto> insertReceipt(
            @RequestBody JsonNode payload,
            @RequestHeader(value = "Comid", required = false) Integer headerComid,
            @RequestHeader(value = "comid", required = false) Integer headerComidLower,
            @RequestParam(value = "companyId", required = false) Integer paramCompanyId) {
        logger.info("Received insertReceipt request");
        try {
            List<ReceiptSaveRequest> requestList = new ArrayList<>();
            if (payload != null) {
                if (payload.isArray()) {
                    requestList = objectMapper.convertValue(payload, new TypeReference<List<ReceiptSaveRequest>>() {});
                } else if (payload.isObject()) {
                    ReceiptSaveRequest single = objectMapper.treeToValue(payload, ReceiptSaveRequest.class);
                    if (single != null) {
                        requestList.add(single);
                    }
                }
            }

            Integer companyId = headerComid != null ? headerComid : headerComidLower;
            if (companyId == null) {
                companyId = paramCompanyId;
            }
            if (companyId == null && !requestList.isEmpty() && requestList.get(0).getCompanyRefId() != null) {
                companyId = requestList.get(0).getCompanyRefId();
            }

            if (companyId == null || companyId <= 0) {
                logger.warn("Validation failed: Company ID (Comid) is missing or invalid");
                return ResponseEntity.badRequest().body(ReceiptSaveResponseDto.builder()
                        .ok(false)
                        .isSuccess(false)
                        .message("Company ID (Comid) is required")
                        .build());
            }

            ReceiptSaveResponseDto result = receiptService.insertReceipt(requestList, companyId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error executing insertReceipt", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ReceiptSaveResponseDto.builder()
                            .ok(false)
                            .isSuccess(false)
                            .message("Error processing receipt: " + e.getMessage())
                            .build());
        }
    }

}


