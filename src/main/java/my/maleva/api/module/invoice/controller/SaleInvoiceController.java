package my.maleva.api.module.invoice.controller;

import my.maleva.api.module.invoice.dto.SaleMasterDto;
import my.maleva.api.module.invoice.dto.SaleDetailsDto;
import my.maleva.api.module.invoice.dto.SaleInvoiceRequestDTO;
import my.maleva.api.module.invoice.dto.SaleInvoiceSaveResult;
import my.maleva.api.module.invoice.einvoice.EInvoicePushResponses;
import my.maleva.api.module.invoice.einvoice.SaleInvoiceEInvoiceService;
import my.maleva.api.module.invoice.print.SaleInvoicePdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import my.maleva.api.module.invoice.service.SaleInvoiceQneService;
import my.maleva.api.module.invoice.service.SaleInvoiceTransactionService;
import my.maleva.api.module.invoice.service.SaleMasterService;
import my.maleva.api.module.invoice.service.SaleDetailsService;
import my.maleva.api.module.saleorder.service.SaleOrderMasterService;
import my.maleva.api.module.saleorder.dto.JobNumberDto;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.integration.qne.QnePushResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SaleInvoiceController
 * REST Controller for Sale Invoice API
 * Provides comprehensive endpoints for invoice CRUD operations and advanced search
 *
 * Base URL: /api/v1/sale-invoices
 * Features:
 * - Invoice number generation
 * - Create invoices with line items
 * - Advanced search and filtering
 * - Update and delete operations
 * - QNE integration
 * - E-Invoice support
 */
@RestController
@RequestMapping("/api/v1/sale-invoices")
@PermitAll
public class SaleInvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(SaleInvoiceController.class);

    @Autowired
    private SaleMasterService saleMasterService;

    @Autowired
    private SaleDetailsService saleDetailsService;

    @Autowired
    private SaleOrderMasterService saleOrderMasterService;

    @Autowired
    private SaleInvoiceQneService saleInvoiceQneService;

    @Autowired
    private SaleInvoiceTransactionService saleInvoiceTransactionService;

    @Autowired
    private SaleInvoiceEInvoiceService saleInvoiceEInvoiceService;

    @Autowired
    private SaleInvoicePdfService saleInvoicePdfService;

    @Autowired
    private my.maleva.api.module.invoice.view.SaleInvoiceViewService saleInvoiceViewService;

    @Autowired
    private my.maleva.api.module.invoice.mail.SaleInvoiceMailService saleInvoiceMailService;

    /**
     * The Sale Invoice view grid.
     * POST /api/v1/sale-invoices/view  (body: SaleInvoiceViewFilter)
     *
     * <p>Replaces the legacy {@code /SaleInvoiceApp/SelectSaleInvoice}. Same
     * two result sets (invoice headers, every line of those invoices), same
     * column names; the filter body uses spelled-out names instead of the
     * legacy Id/JId/Statusid abbreviations.
     */
    @PostMapping("/view")
    public ResponseEntity<ApiResponse<my.maleva.api.module.invoice.view.SaleInvoiceViewResult>> view(
            @RequestBody my.maleva.api.module.invoice.view.SaleInvoiceViewFilter filter) {
        try {
            return ResponseEntity.ok(ApiResponse.success(saleInvoiceViewService.view(filter), "Success"));
        } catch (IllegalArgumentException bad) {
            return ResponseEntity.badRequest().body(ApiResponse.error(bad.getMessage(), 400));
        } catch (Exception e) {
            logger.error("Error loading sale invoice view", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error loading sale invoice view: " + e.getMessage(), 500));
        }
    }

    /**
     * Mail the printed invoice and its attachments to the configured recipients.
     * POST /api/v1/sale-invoices/{id}/mail?companyId=1&employeeName=...
     *
     * <p>Replaces the legacy {@code /SaleInvoiceApp/MailInvoice}. A mail that
     * did not go out answers 200 with {@code IsSuccess=false} and the reason.
     */
    @PostMapping("/{id}/mail")
    public ResponseEntity<ApiResponse<Map<String, Object>>> mail(
            @PathVariable Integer id,
            @RequestParam Integer companyId,
            @RequestParam(required = false) String employeeName) {
        try {
            var outcome = saleInvoiceMailService.send(id, companyId, employeeName);
            Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("recipients", outcome.recipients());
            data.put("attachmentCount", outcome.attachmentCount());
            if (!outcome.ok()) {
                ApiResponse<Map<String, Object>> body = ApiResponse.error(outcome.message(), 200);
                body.setData1(data);
                return ResponseEntity.ok(body);
            }
            return ResponseEntity.ok(ApiResponse.success(data, outcome.message()));
        } catch (Exception e) {
            logger.error("Error mailing invoice {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error mailing invoice: " + e.getMessage(), 500));
        }
    }

    /**
     * Submit the invoice to LHDN MyInvois as an e-invoice.
     * POST /api/v1/sale-invoices/{id}/push-einvoice?companyId=1
     *
     * <p>Replaces the legacy {@code /SaleInvoice/EInvoiceConvert}. Same response
     * contract as {@code push-qne}: a local problem is an HTTP error; anything
     * the validator or LHDN said comes back as 200 with {@code IsSuccess=false},
     * the message, and {@code Data1.problems} listing every reason.
     */
    @PostMapping("/{id}/push-einvoice")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pushToEInvoice(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        logger.info("Pushing invoice ID: {} to LHDN MyInvois for company: {}", id, companyId);
        if (id == null || id <= 0 || companyId == null || companyId <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid ID or company ID", 400));
        }
        try {
            return EInvoicePushResponses.toResponse(saleInvoiceEInvoiceService.push(id, companyId));
        } catch (Exception e) {
            logger.error("Error pushing invoice {} to LHDN MyInvois", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error pushing to LHDN MyInvois: " + e.getMessage(), 500));
        }
    }

    /**
     * The printed invoice as a PDF.
     * GET /api/v1/sale-invoices/{id}/print?companyId=1
     *
     * <p>Replaces the Crystal {@code ReportViewer.aspx?ReportName=InvoiceReport}
     * popup. Rendered on demand from the database; carries the LHDN UUID,
     * status and validation QR once the invoice has been e-invoiced.
     */
    @GetMapping(value = "/{id}/print", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> printInvoice(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        if (id == null || id <= 0 || companyId == null || companyId <= 0) {
            return printProblem(HttpStatus.BAD_REQUEST, "Invoice id and company are required");
        }
        try {
            return saleInvoicePdfService.render(id, companyId)
                    .<ResponseEntity<?>>map(rendered -> ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_PDF)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + rendered.fileName() + "\"")
                            .body(rendered.pdf()))
                    .orElseGet(() -> printProblem(HttpStatus.NOT_FOUND,
                            "Invoice " + id + " was not found for company " + companyId));
        } catch (Exception e) {
            // The failure and its cause go to the log in full; the screen gets
            // the innermost message, which is the one that names the problem
            // (a missing template on the classpath, a font, a column).
            logger.error("Invoice {} (company {}) could not be printed", id, companyId, e);
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            String reason = root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
            return printProblem(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Invoice " + id + " could not be printed: " + reason);
        }
    }

    /** A print failure as the standard JSON envelope, so the screen can show the message. */
    private static ResponseEntity<ApiResponse<Object>> printProblem(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(message, status.value()));
    }

    /**
     * Re-read the LHDN validation status of a submitted invoice.
     * GET /api/v1/sale-invoices/{id}/einvoice-status?companyId=1
     *
     * <p>LHDN validates asynchronously, so a push usually answers "Submitted";
     * this call fetches the long id, status and validated time once they exist,
     * saves them, and returns the share link and QR for the printed invoice.
     */
    @GetMapping("/{id}/einvoice-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> eInvoiceStatus(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        if (id == null || id <= 0 || companyId == null || companyId <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid ID or company ID", 400));
        }
        try {
            return EInvoicePushResponses.toResponse(saleInvoiceEInvoiceService.refreshStatus(id, companyId));
        } catch (Exception e) {
            logger.error("Error reading LHDN status for invoice {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error reading LHDN status: " + e.getMessage(), 500));
        }
    }

    /**
     * Get next invoice number
     * GET /api/v1/sale-invoices/next-number?companyId=1
     *
     * @param companyId The company ID for which to generate next invoice number
     * @return Next invoice number in format INV000000001
     */
    @GetMapping("/next-number")
    public ResponseEntity<ApiResponse<String>> getNextInvoiceNumber(
            @RequestParam Integer companyId) {
        logger.info("Generating next invoice number for company: {}", companyId);
        try {
            if (companyId == null || companyId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid company ID", 400));
            }

            // Get max invoice number from database
            String nextNumber = saleMasterService.getNextInvoiceNumber(companyId);

            return ResponseEntity.ok(ApiResponse.success(nextNumber, "Next invoice number generated successfully"));
        } catch (Exception e) {
            logger.error("Error generating next invoice number for company: {}", companyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error generating next invoice number: " + e.getMessage(), 500));
        }
    }

    /**
     * Create new invoice with details
     * POST /api/v1/sale-invoices
     *
     * @param dto Invoice master data with line items
     * @return Created invoice with ID and invoice number
     */
    /**
     * Save a sale invoice - creates when {@code id} is absent or 0, otherwise
     * re-writes that invoice in place.
     * POST /api/v1/sale-invoices/save
     *
     * <p>This is the replacement for the legacy
     * {@code /SaleInvoice/InsertSaleInvoice}. It writes the whole document:
     * header, lines, the SaleMasterReference rows linking the jobs, the
     * {@code SaleOrderMaster.InvoiceNo} stamps, and the invoice number - one
     * transaction, through {@code SP_SaleMaster}, which the .NET screens still
     * in production share.
     *
     * <p>Prefer this over {@code POST /api/v1/sale-invoices}, which persists the
     * header only.
     *
     * @param request the full invoice, lines included
     * @return the saved id and invoice number
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<SaleInvoiceSaveResult>> saveInvoice(
            @Valid @RequestBody SaleInvoiceRequestDTO request) {
        logger.info("Saving invoice {} for company {}",
                request.getId() == null || request.getId() == 0 ? "(new)" : request.getId(),
                request.getCompanyRefId());

        SaleInvoiceSaveResult result = saleInvoiceTransactionService.save(request);

        return ResponseEntity
                .status(result.isCreated() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(ApiResponse.success(result,
                        result.isCreated() ? "Invoice created successfully" : "Invoice updated successfully"));
    }

    /**
     * Persists the invoice header only - no lines, no job links, no invoice
     * number. Kept for callers that already use it; new callers want
     * {@code POST /save}.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createInvoice(@Valid @RequestBody SaleMasterDto dto) {
        logger.info("Creating new invoice for company: {}, customer: {}", dto.getCompanyRefId(), dto.getCustomerRefId());
        try {
            // Validate required fields
            if (dto.getCompanyRefId() == null || dto.getCompanyRefId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid company ID", 400));
            }
            if (dto.getCustomerRefId() == null || dto.getCustomerRefId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid customer ID", 400));
            }
            if (dto.getJobMasterRefId() == null || dto.getJobMasterRefId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid job master ID", 400));
            }

            // Set active status
            dto.setActive(1);

            // Create invoice
            SaleMasterDto created = saleMasterService.create(dto);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", created.getId());
            responseData.put("invoiceNumber", created.getCNumberDisplay());
            responseData.put("amount", created.getAmount());

            logger.info("Invoice created successfully with ID: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(responseData, "Invoice created successfully"));
        } catch (RuntimeException e) {
            logger.error("Business logic error while creating invoice: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Error creating invoice: " + e.getMessage(), 409));
        } catch (Exception e) {
            logger.error("Unexpected error while creating invoice", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Unexpected error: " + e.getMessage(), 500));
        }
    }

    /**
     * Search and filter invoices
     * GET /api/v1/sale-invoices/search?companyId=1&customerId=456&page=0&size=20
     *
     * Supports multiple filter criteria:
     * - companyId (required)
     * - customerId (optional)
     * - employeeId (optional)
     * - jobId (optional)
     * - fromDate, toDate (optional)
     * - billType (optional)
     * - saleType (optional)
     * - search (optional - invoice number search)
     *
     * @return Paginated list of invoices with details
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchInvoices(
            @RequestParam Integer companyId,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) Integer jobId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String billType,
            @RequestParam(required = false) String saleType,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "id,desc") String sort) {

        logger.info("Searching invoices for company: {} with filters - customer: {}, employee: {}, job: {}",
                companyId, customerId, employeeId, jobId);

        try {
            if (companyId == null || companyId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Company ID is required", 400));
            }

            // Create pagination and sorting
            String[] sortParts = sort.split(",");
            // Sort direction and field prepared for potential future pagination use
            // Note: Simple in-memory filtering used for now

            // Build filter list
            List<SaleMasterDto> invoices = saleMasterService.getByCompanyIdAndStatus(companyId, 1);

            // Apply additional filters
            if (customerId != null && customerId > 0) {
                invoices = invoices.stream()
                        .filter(inv -> inv.getCustomerRefId().equals(customerId))
                        .toList();
            }
            if (employeeId != null && employeeId > 0) {
                invoices = invoices.stream()
                        .filter(inv -> inv.getEmployeeRefId() != null && inv.getEmployeeRefId().equals(employeeId))
                        .toList();
            }
            if (jobId != null && jobId > 0) {
                invoices = invoices.stream()
                        .filter(inv -> inv.getJobMasterRefId().equals(jobId))
                        .toList();
            }
            if (fromDate != null && toDate != null) {
                invoices = invoices.stream()
                        .filter(inv -> inv.getSaleDate() != null
                                && inv.getSaleDate().isAfter(fromDate)
                                && inv.getSaleDate().isBefore(toDate))
                        .toList();
            }
            if (billType != null && !billType.isEmpty()) {
                invoices = invoices.stream()
                        .filter(inv -> inv.getBillType() != null && inv.getBillType().equalsIgnoreCase(billType))
                        .toList();
            }
            if (saleType != null && !saleType.isEmpty()) {
                invoices = invoices.stream()
                        .filter(inv -> inv.getSaleType() != null && inv.getSaleType().equalsIgnoreCase(saleType))
                        .toList();
            }
            if (search != null && !search.isEmpty()) {
                invoices = invoices.stream()
                        .filter(inv -> inv.getCNumberDisplay() != null && inv.getCNumberDisplay().contains(search))
                        .toList();
            }

            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, invoices.size());
            List<SaleMasterDto> paginatedInvoices = invoices.subList(start, end);

            // Get details for each invoice
            List<List<SaleDetailsDto>> allDetails = paginatedInvoices.stream()
                    .map(inv -> saleDetailsService.getBySaleMasterRefId(inv.getId()))
                    .toList();

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("invoices", paginatedInvoices);
            responseData.put("details", allDetails);
            responseData.put("totalRecords", invoices.size());
            responseData.put("totalPages", (invoices.size() + size - 1) / size);
            responseData.put("currentPage", page);

            logger.info("Search completed. Found {} invoices", invoices.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "Search completed successfully"));
        } catch (Exception e) {
            logger.error("Error searching invoices", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error during search: " + e.getMessage(), 500));
        }
    }

    /**
     * Get invoice details by ID
     * GET /api/v1/sale-invoices/{id}?companyId=1
     *
     * @param id Invoice ID
     * @param companyId Company ID for validation
     * @return Invoice master and detail records
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getInvoiceById(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        logger.info("Fetching invoice details for ID: {} from company: {}", id, companyId);

        try {
            if (id == null || id <= 0 || companyId == null || companyId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid ID or company ID", 400));
            }

            Optional<SaleMasterDto> invoice = saleMasterService.getById(id);

            if (!invoice.isPresent()) {
                logger.warn("Invoice not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Invoice not found", 404));
            }

            SaleMasterDto invoiceMaster = invoice.get();

            // Verify company match
            if (!invoiceMaster.getCompanyRefId().equals(companyId)) {
                logger.warn("Company ID mismatch for invoice ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Unauthorized access to invoice", 403));
            }

            // Fetch details for this invoice
            List<SaleDetailsDto> details = saleDetailsService.getBySaleMasterRefId(id);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("master", invoiceMaster);
            responseData.put("details", details);

            logger.info("Invoice details fetched successfully");
            return ResponseEntity.ok(ApiResponse.success(responseData, "Invoice retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching invoice details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error fetching invoice: " + e.getMessage(), 500));
        }
    }

    /**
     * Update invoice
     * PUT /api/v1/sale-invoices/{id}?companyId=1
     *
     * @param id Invoice ID to update
     * @param companyId Company ID for validation
     * @param dto Updated invoice data
     * @return Success message
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateInvoice(
            @PathVariable Integer id,
            @RequestParam Integer companyId,
            @Valid @RequestBody SaleMasterDto dto) {
        logger.info("Updating invoice ID: {} for company: {}", id, companyId);

        try {
            if (id == null || id <= 0 || companyId == null || companyId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid ID or company ID", 400));
            }

            Optional<SaleMasterDto> existingInvoice = saleMasterService.getById(id);

            if (!existingInvoice.isPresent()) {
                logger.warn("Invoice not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Invoice not found", 404));
            }

            SaleMasterDto invoice = existingInvoice.get();

            // Verify company match
            if (!invoice.getCompanyRefId().equals(companyId)) {
                logger.warn("Company ID mismatch for invoice ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Unauthorized access to invoice", 403));
            }

            // Check if invoice can be updated (must not be completed or pushed to QNE)
            if (invoice.getJStatus() != null && invoice.getJStatus() == 8) {
                logger.warn("Cannot update completed invoice ID: {}", id);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("Cannot update completed invoice", 409));
            }

            if (invoice.getQneCode() != null && !invoice.getQneCode().isEmpty()) {
                logger.warn("Cannot update invoice already pushed to QNE, ID: {}", id);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("Cannot update invoice already pushed to QNE", 409));
            }

            // Update invoice
            dto.setId(id);
            dto.setCompanyRefId(companyId);
            SaleMasterDto updated = saleMasterService.update(id, dto);

            logger.info("Invoice updated successfully, ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(updated, "Invoice updated successfully"));
        } catch (RuntimeException e) {
            logger.error("Business logic error while updating invoice: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Error updating invoice: " + e.getMessage(), 409));
        } catch (Exception e) {
            logger.error("Error updating invoice", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Unexpected error: " + e.getMessage(), 500));
        }
    }

    /**
     * Delete invoice (soft delete - set Active = 2)
     * DELETE /api/v1/sale-invoices/{id}?companyId=1
     *
     * @param id Invoice ID to delete
     * @param companyId Company ID for validation
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteInvoice(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        logger.info("Deleting invoice ID: {} from company: {}", id, companyId);

        try {
            if (id == null || id <= 0 || companyId == null || companyId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid ID or company ID", 400));
            }

            Optional<SaleMasterDto> invoice = saleMasterService.getById(id);

            if (!invoice.isPresent()) {
                logger.warn("Invoice not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Invoice not found", 404));
            }

            SaleMasterDto invoiceData = invoice.get();

            // Verify company match
            if (!invoiceData.getCompanyRefId().equals(companyId)) {
                logger.warn("Company ID mismatch for invoice ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Unauthorized access to invoice", 403));
            }

            // Check if can be deleted
            if (invoiceData.getJStatus() != null && invoiceData.getJStatus() == 8) {
                logger.warn("Cannot delete completed invoice ID: {}", id);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("Cannot delete completed invoice", 409));
            }

            if (invoiceData.getQneCode() != null && !invoiceData.getQneCode().isEmpty()) {
                logger.warn("Cannot delete invoice already pushed to QNE, ID: {}", id);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("Cannot delete invoice already pushed to QNE", 409));
            }

            // Perform soft delete
            boolean deleted = saleMasterService.delete(id);

            if (deleted) {
                logger.info("Invoice deleted successfully, ID: {}", id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Invoice not found", 404));
            }
        } catch (Exception e) {
            logger.error("Error deleting invoice", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting invoice: " + e.getMessage(), 500));
        }
    }

    /**
     * Push invoice to QNE system
     * POST /api/v1/sale-invoices/{id}/push-qne?companyId=1
     *
     * While the invoice has no QNECode this creates it in QNE and persists the
     * returned id/code (legacy InvoiceConvert); once it has one, the same call
     * re-sends the current data as QNE's live PUT update (legacy
     * InvoiceConvertEdit). A QNE rejection answers 200 with IsSuccess=false
     * and QNE's own message — the local invoice is already committed.
     *
     * @param id Invoice ID to push
     * @param companyId Company ID for validation
     * @return QNE response with code, ID, and file URL (when qne.view is on)
     */
    @PostMapping("/{id}/push-qne")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pushToQne(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        logger.info("Pushing invoice ID: {} to QNE system for company: {}", id, companyId);

        try {
            if (id == null || id <= 0 || companyId == null || companyId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid ID or company ID", 400));
            }
            return QnePushResponses.toResponse(saleInvoiceQneService.push(id, companyId));
        } catch (Exception e) {
            logger.error("Error pushing invoice to QNE", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error pushing to QNE: " + e.getMessage(), 500));
        }
    }

    /**
     * Get customer job numbers for invoice creation
     * Equivalent to ASP.NET GetCustJobNo endpoint
     *
     * POST /api/v1/sale-invoices/jobs
     *
     * Request Body:
     * {
     *   "companyId": 1,
     *   "customerId": 5,
     *   "invoiceNo": 0
     * }
     *
     * Response (Success):
     * {
     *   "IsSuccess": true,
     *   "StatusCode": 200,
     *   "Message": "Success",
     *   "Data1": [
     *     { "id": 1, "billNoDisplay": "JOB-001" },
     *     { "id": 2, "billNoDisplay": "JOB-002" }
     *   ]
     * }
     *
     * Business Logic:
     * 1. Filter by company (multi-tenancy) - required
     * 2. Filter by customer (if customerId != 0) - optional, 0 means all customers
     * 3. Exclude soft-deleted records (Active != 2)
     * 4. Filter by invoice number:
     *    - If invoiceNo = 0: returns jobs NOT YET INVOICED
     *    - If invoiceNo > 0: returns jobs for that specific invoice
     */
    @PostMapping("/jobs")
    public ResponseEntity<ApiResponse<List<JobNumberDto>>> getCustJobNumbers(
            @RequestParam Integer companyId,
            @RequestParam Integer customerId,
            @RequestParam Integer invoiceNo) {

        logger.info("GetCustJobNo Request: companyId={}, customerId={}, invoiceNo={}",
                companyId, customerId, invoiceNo);

        try {
            // Validate required parameters
            if (companyId == null || companyId <= 0) {
                logger.warn("Invalid company ID provided");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Company ID is required and must be greater than 0", 400));
            }

            if (customerId == null) {
                logger.warn("Customer ID is null, using default 0 (all customers)");
                customerId = 0;
            }

            if (invoiceNo == null) {
                logger.warn("Invoice No is null, using default 0 (not yet invoiced)");
                invoiceNo = 0;
            }

            // Call service to get customer job numbers
            List<JobNumberDto> jobs = saleOrderMasterService.getCustJobNumbers(companyId, customerId, invoiceNo);

            // Return success response
            logger.info("GetCustJobNo completed successfully. Retrieved {} jobs for companyId: {}",
                    jobs.size(), companyId);

            return ResponseEntity.ok(ApiResponse.<List<JobNumberDto>>builder()
                    .isSuccess(true)
                    .statusCode(200)
                    .message("Success")
                    .data1(jobs)
                    .build());

        }
        catch (Exception ex) {
            // Extract innermost exception
            Exception innermost = ex;
            while (innermost.getCause() != null && innermost.getCause() instanceof Exception) {
                innermost = (Exception) innermost.getCause();
            }

            logger.error("Error in GetCustJobNo for companyId: {}, customerId: {}. Error: {}",
                    companyId, customerId, innermost.getMessage(), innermost);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<JobNumberDto>>builder()
                            .isSuccess(false)
                            .statusCode(500)
                            .message("Error retrieving job numbers")
                            .errorDetails(innermost.getMessage())
                            .build());
        }
    }

    /**
     * Get invoices by company and customer
     * GET /api/v1/sale-invoices/company/{companyId}/customer/{customerId}
     */
    @GetMapping("/company/{companyId}/customer/{customerId}")
    public ResponseEntity<ApiResponse<?>> getByCompanyAndCustomer(
            @PathVariable Integer companyId,
            @PathVariable Integer customerId) {
        logger.info("Fetching invoices for company: {} and customer: {}", companyId, customerId);

        try {
            List<SaleMasterDto> invoices = saleMasterService.getByCompanyAndCustomer(companyId, customerId);
            return ResponseEntity.ok(ApiResponse.success(invoices, "Invoices retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching invoices", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving invoices: " + e.getMessage(), 500));
        }
    }

    /**
     * Get invoices by company ID
     * GET /api/v1/sale-invoices/company/{companyId}
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<?>> getByCompanyId(@PathVariable Integer companyId) {
        logger.info("Fetching all invoices for company: {}", companyId);

        try {
            List<SaleMasterDto> invoices = saleMasterService.getAllByCompanyId(companyId);
            return ResponseEntity.ok(ApiResponse.success(invoices, "Invoices retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching invoices", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving invoices: " + e.getMessage(), 500));
        }
    }

    /**
     * Get unpushed invoices (not sent to QNE)
     * GET /api/v1/sale-invoices/unpushed?companyId=1
     */
    @GetMapping("/unpushed")
    public ResponseEntity<ApiResponse<?>> getUnpushedInvoices(@RequestParam Integer companyId) {
        logger.info("Fetching unpushed invoices for company: {}", companyId);

        try {
            List<SaleMasterDto> invoices = saleMasterService.getAllByCompanyId(companyId);
            List<SaleMasterDto> unpushed = invoices.stream()
                    .filter(inv -> inv.getQneCode() == null || inv.getQneCode().isEmpty())
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(unpushed, "Unpushed invoices retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching unpushed invoices", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving invoices: " + e.getMessage(), 500));
        }
    }

    /**
     * Get invoice by C Number (invoice number display)
     * GET /api/v1/sale-invoices/by-cnumber?companyId=1&cNumber=INV000000001
     */
    @GetMapping("/by-cnumber")
    public ResponseEntity<ApiResponse<?>> getByCNumber(
            @RequestParam Integer companyId,
            @RequestParam String cNumber) {
        logger.info("Fetching invoice by C Number: {} for company: {}", cNumber, companyId);

        try {
            Optional<SaleMasterDto> invoice = saleMasterService.getByCNumber(companyId, 0); // Parse cNumber if numeric

            if (invoice.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(invoice.get(), "Invoice retrieved successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Invoice not found", 404));
            }
        } catch (Exception e) {
            logger.error("Error fetching invoice by C Number", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving invoice: " + e.getMessage(), 500));
        }
    }
    @PostMapping("/edit-multi-sale-order")
    public ResponseEntity<ApiResponse<List<my.maleva.api.module.saleorder.dto.SaleOrderDTO>>> editMultiSaleOrder(@RequestBody my.maleva.api.module.invoice.dto.MultiInvoiceDto obj) {
        logger.info("EditMultiSaleOrder requested with Comid: {}, Ids: {}", obj.getComid(), obj.getId());
        try {
            List<my.maleva.api.module.saleorder.dto.SaleOrderDTO> result = saleOrderMasterService.editMultiSaleOrder(obj);
            return ResponseEntity.ok(ApiResponse.<List<my.maleva.api.module.saleorder.dto.SaleOrderDTO>>builder()
                    .isSuccess(true)
                    .statusCode(200)
                    .message("Success")
                    .data1(result)
                    .build());
        } catch (my.maleva.api.common.exception.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            logger.error("Error in EditMultiSaleOrder: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Api Details : SaleInvoice_EditMultiSaleOrder - " + e.getMessage(), 500));
        }
    }
}
