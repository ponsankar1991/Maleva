package my.maleva.api.module.salecreditmaster.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.invoice.einvoice.EInvoicePushResponses;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditBillDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditEditDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditInvoiceLookupDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditSaveRequest;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditSaveResponse;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditSearchRequest;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditViewDto;
import my.maleva.api.module.salecreditmaster.einvoice.SaleCreditEInvoiceService;
import my.maleva.api.module.salecreditmaster.print.SaleCreditPdfService;
import my.maleva.api.module.salecreditmaster.service.SaleCreditEntryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * The Sale Credit entry screen's API — what the legacy
 * {@code SaleCreditController} MVC actions did, as REST.
 *
 * <p>Sits alongside {@link SaleCreditMasterController}, which keeps the
 * generic CRUD; these are the screen's own endpoints:
 *
 * <table>
 *   <tr><th>Legacy action</th><th>Here</th></tr>
 *   <tr><td>{@code MaxSaleCreditNo}</td><td>{@code GET /company/{id}/next-no}</td></tr>
 *   <tr><td>{@code /Receipt/SelectCustomerBills}</td><td>{@code GET /customer-bills}</td></tr>
 *   <tr><td>{@code InsertSaleCredit}</td><td>{@code POST /insert}</td></tr>
 *   <tr><td>{@code EditSaleCredit}</td><td>{@code GET /edit}</td></tr>
 *   <tr><td>{@code SelectSaleCredit}</td><td>{@code POST /search}</td></tr>
 *   <tr><td>{@code DeleteSaleCredit}</td><td>{@code DELETE /{id}?companyId=}</td></tr>
 *   <tr><td>{@code /SaleOrder/SelectSaleInvoice}</td><td>{@code GET /invoice-lookup}</td></tr>
 *   <tr><td>{@code SaleCreditVIEW} (print half)</td><td>{@code GET /{id}/print}</td></tr>
 *   <tr><td>{@code SaleCreditVIEW} (QNE half)</td><td>{@code POST /{id}/push-qne} (existing)</td></tr>
 *   <tr><td>{@code EInvoiceCreditConvert}</td><td>{@code POST /{id}/push-einvoice}</td></tr>
 * </table>
 *
 * <p>The legacy screen's single VIEW button did three things at once: pushed
 * to QNE, submitted to LHDN and opened a Crystal report whose rows it had just
 * parked in the ASP.NET session. Each is its own call here, so a failure in
 * one no longer decides whether the operator gets paper.
 */
@Slf4j
@RestController
@RequestMapping("/api/sale-credits")
@RequiredArgsConstructor
@PermitAll
public class SaleCreditEntryController {

    private final SaleCreditEntryService entryService;
    private final SaleCreditEInvoiceService eInvoiceService;
    private final SaleCreditPdfService pdfService;
    private final ObjectMapper objectMapper;

    /**
     * The next credit note number, for display only.
     * {@code GET /api/sale-credits/company/6/next-no}
     */
    @GetMapping("/company/{companyId}/next-no")
    public ResponseEntity<ApiResponse<String>> nextNumber(@PathVariable Integer companyId) {
        return ResponseEntity.ok(ApiResponse.success(entryService.nextCreditNoteNo(companyId), "Success"));
    }

    /**
     * The customer's outstanding documents for the knock-off grid.
     * {@code GET /api/sale-credits/customer-bills?companyId=6&customerId=10&excludeCreditNoteId=0}
     */
    @GetMapping("/customer-bills")
    public ResponseEntity<ApiResponse<List<SaleCreditBillDto>>> customerBills(
            @RequestParam Integer companyId,
            @RequestParam Integer customerId,
            @RequestParam(required = false, defaultValue = "0") Integer excludeCreditNoteId) {
        List<SaleCreditBillDto> bills = entryService.customerBills(companyId, customerId, excludeCreditNoteId);
        return ResponseEntity.ok(ApiResponse.success(bills, "Success"));
    }

    /**
     * Save (insert or replace) a credit note.
     * {@code POST /api/sale-credits/insert}
     *
     * <p>Accepts the object, and the single-element array the legacy screen
     * posted. The company comes from the body, the {@code Comid} header or the
     * {@code companyId} parameter, in that order.
     *
     * <p>The body is bound as a plain {@code Object} — a {@code Map} for
     * {@code &#123;...&#125;}, a {@code List} for {@code [...]} — rather than a
     * {@code JsonNode}: the HTTP layer here is Jackson 3 and cannot construct
     * Jackson 2's abstract {@code JsonNode}.
     */
    @PostMapping({"/insert", "/save"})
    public ResponseEntity<SaleCreditSaveResponse> insert(
            @RequestBody Object payload,
            @RequestHeader(value = "Comid", required = false) Integer headerComid,
            @RequestParam(value = "companyId", required = false) Integer paramCompanyId) {
        SaleCreditSaveRequest request;
        try {
            request = payload instanceof List<?>
                    ? objectMapper.convertValue(payload, new TypeReference<List<SaleCreditSaveRequest>>() {})
                            .stream().findFirst().orElse(null)
                    : objectMapper.convertValue(payload, SaleCreditSaveRequest.class);
        } catch (IllegalArgumentException malformed) {
            log.warn("Unreadable SaleCredit save payload", malformed);
            return ResponseEntity.badRequest().body(SaleCreditSaveResponse.failure("The credit note could not be read"));
        }
        if (request == null) {
            return ResponseEntity.badRequest().body(SaleCreditSaveResponse.failure("Empty credit note: nothing to save"));
        }

        Integer companyId = headerComid != null ? headerComid : paramCompanyId;
        // A refusal the operator can act on ("Total Not Matching", a missing
        // reference) is a 200 whose body says ok=false, as every screen in this
        // API expects; only an unreadable body above is a 400.
        return ResponseEntity.ok(entryService.save(request, companyId));
    }

    /**
     * A saved credit note, for the entry screen.
     * {@code GET /api/sale-credits/edit?companyId=6&id=123} or {@code &creditNoteNumber=12}
     */
    @GetMapping("/edit")
    public ResponseEntity<ApiResponse<SaleCreditEditDto>> edit(
            @RequestParam Integer companyId,
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer creditNoteNumber) {
        return entryService.edit(companyId, id, creditNoteNumber)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Success")))
                // Legacy answered "Invaild SaleCredit No !!!." with ok=false and HTTP 200.
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Invalid SaleCredit No", 404)));
    }

    /**
     * The SALECREDIT ENTRY VIEW grid.
     * {@code POST /api/sale-credits/search}
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<SaleCreditViewDto>> search(
            @RequestBody SaleCreditSearchRequest request,
            @RequestParam(value = "companyId", required = false) Integer paramCompanyId,
            @RequestHeader(value = "Comid", required = false) Integer headerComid) {
        if (request.getCompanyId() == null || request.getCompanyId() <= 0) {
            request.setCompanyId(paramCompanyId != null ? paramCompanyId : headerComid);
        }
        return ResponseEntity.ok(ApiResponse.success(entryService.search(request), "Success"));
    }

    /**
     * Delete a credit note with its lines and knock-offs.
     * {@code DELETE /api/sale-credits/{id}/entry?companyId=6}
     *
     * <p>A distinct path from the generic {@code DELETE /{id}}, which deletes
     * the master row alone and is what other callers already use.
     */
    @DeleteMapping("/{id}/entry")
    public ResponseEntity<ApiResponse<Integer>> delete(
            @PathVariable Integer id,
            @RequestParam(value = "companyId", required = false) Integer companyId,
            @RequestHeader(value = "Comid", required = false) Integer headerComid) {
        Integer company = companyId != null ? companyId : headerComid;
        return ResponseEntity.ok(ApiResponse.success(id, entryService.delete(id, company)));
    }

    /**
     * The invoice behind the "Invoice No" box.
     * {@code GET /api/sale-credits/invoice-lookup?companyId=6&invoiceNo=INV000000123}
     * or {@code ...&invoiceId=500} for the {@code ?Id=} link from the Sale
     * Invoice screen, which carries the row id rather than the number.
     */
    @GetMapping("/invoice-lookup")
    public ResponseEntity<ApiResponse<SaleCreditInvoiceLookupDto>> invoiceLookup(
            @RequestParam Integer companyId,
            @RequestParam(required = false) String invoiceNo,
            @RequestParam(required = false) Integer invoiceId) {
        return entryService.lookupInvoice(companyId, invoiceNo, invoiceId)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Success")))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Job No Not Found", 404)));
    }

    /**
     * Submit the credit note to LHDN MyInvois.
     * {@code POST /api/sale-credits/{id}/push-einvoice?companyId=6}
     *
     * <p>Replaces the legacy {@code EInvoiceCreditConvert}. A note LHDN or the
     * validator refused answers 200 with {@code IsSuccess=false} and the
     * reasons in {@code Data1.problems}; only a local precondition (not found,
     * wrong company, feature off) is an HTTP error.
     */
    @PostMapping("/{id}/push-einvoice")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pushEInvoice(
            @PathVariable Integer id, @RequestParam Integer companyId) {
        return EInvoicePushResponses.toResponse(eInvoiceService.push(id, companyId));
    }

    /**
     * Re-read LHDN's verdict on a submitted credit note.
     * {@code POST /api/sale-credits/{id}/einvoice-status?companyId=6}
     *
     * <p>The legacy screen only had one button, which re-read the status as a
     * side effect of a second push; this is that read on its own.
     */
    @PostMapping("/{id}/einvoice-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> eInvoiceStatus(
            @PathVariable Integer id, @RequestParam Integer companyId) {
        return EInvoicePushResponses.toResponse(eInvoiceService.refreshStatus(id, companyId));
    }

    /**
     * The credit note as a PDF.
     * {@code GET /api/sale-credits/{id}/print?companyId=6}
     *
     * <p>Replaces the Crystal {@code ReportViewer.aspx?ReportName=CreditNote}
     * popup behind the EXPORT icon. Unlike legacy, printing no longer pushes
     * the note to QNE or to LHDN as a side effect.
     */
    @GetMapping(value = "/{id}/print", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> print(@PathVariable Integer id, @RequestParam Integer companyId) {
        if (id == null || id <= 0 || companyId == null || companyId <= 0) {
            return printProblem(HttpStatus.BAD_REQUEST, "Credit note id and company are required");
        }
        try {
            return pdfService.render(id, companyId)
                    .<ResponseEntity<?>>map(rendered -> ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_PDF)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + rendered.fileName() + "\"")
                            .body(rendered.pdf()))
                    .orElseGet(() -> printProblem(HttpStatus.NOT_FOUND,
                            "Credit note " + id + " was not found for company " + companyId));
        } catch (Exception e) {
            log.error("Credit note {} (company {}) could not be printed", id, companyId, e);
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            String reason = root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
            return printProblem(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Credit note " + id + " could not be printed: " + reason);
        }
    }

    /**
     * A refusal the operator can act on is a 200 with {@code IsSuccess=false},
     * as every screen in this API expects; the front end shows the message.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<Object>> refused(InvalidRequestException refused) {
        return ResponseEntity.ok(ApiResponse.error(refused.getMessage(), 400));
    }

    private static ResponseEntity<ApiResponse<Object>> printProblem(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(message, status.value()));
    }
}
