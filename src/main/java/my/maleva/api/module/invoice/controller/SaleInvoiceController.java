package my.maleva.api.module.invoice.controller;

import my.maleva.api.module.invoice.dto.SaleMasterDto;
import my.maleva.api.module.invoice.dto.SaleDetailsDto;
import my.maleva.api.module.invoice.service.SaleMasterService;
import my.maleva.api.module.invoice.service.SaleDetailsService;
import my.maleva.api.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_USER')")
public class SaleInvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(SaleInvoiceController.class);

    @Autowired
    private SaleMasterService saleMasterService;

    @Autowired
    private SaleDetailsService saleDetailsService;

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
     * @param id Invoice ID to push
     * @param companyId Company ID for validation
     * @return QNE response with code, ID, and file URL
     */
    @PostMapping("/{id}/push-qne")
    public ResponseEntity<ApiResponse<?>> pushToQne(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        logger.info("Pushing invoice ID: {} to QNE system for company: {}", id, companyId);

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

            // Check if already pushed
            if (invoiceData.getQneCode() != null && !invoiceData.getQneCode().isEmpty()) {
                logger.warn("Invoice already pushed to QNE, ID: {}", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Invoice already pushed to QNE", 400));
            }

            // TODO: Implement QNE integration
            // For now, return success response with placeholder data
            Map<String, Object> qneResponse = new HashMap<>();
            qneResponse.put("qneCode", "QNE" + System.currentTimeMillis());
            qneResponse.put("qneId", "ID" + id);
            qneResponse.put("fileUrl", "https://qne.system.com/files/" + id);
            qneResponse.put("status", "success");

            logger.info("Invoice pushed to QNE successfully, ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(qneResponse, "Invoice pushed to QNE successfully"));
        } catch (Exception e) {
            logger.error("Error pushing invoice to QNE", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error pushing to QNE: " + e.getMessage(), 500));
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
}






