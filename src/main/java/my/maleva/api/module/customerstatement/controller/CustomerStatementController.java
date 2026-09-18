package my.maleva.api.module.customerstatement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.StatementLastSent;
import my.maleva.api.module.customerstatement.dto.StatementMailPreview;
import my.maleva.api.module.customerstatement.dto.StatementMailJobRequest;
import my.maleva.api.module.customerstatement.dto.StatementMailJobView;
import my.maleva.api.module.customerstatement.dto.StatementRequest;
import my.maleva.api.module.customerstatement.dto.StatementResult;
import my.maleva.api.module.customerstatement.dto.StatementSendRequest;
import my.maleva.api.module.customerstatement.dto.StatementSendResult;
import my.maleva.api.module.customerstatement.mail.CustomerStatementMailService;
import my.maleva.api.module.customerstatement.mail.StatementMailJobService;
import my.maleva.api.module.customerstatement.mail.StatementMailLogRepository;
import my.maleva.api.module.customerstatement.print.CustomerStatementExcelService;
import my.maleva.api.module.customerstatement.print.CustomerStatementPdfService;
import my.maleva.api.module.common.service.EmailService.EmailAttachment;
import my.maleva.api.module.customerstatement.print.CustomerStatementPdfService.RenderedStatement;
import my.maleva.api.module.customerstatement.service.CustomerStatementService;
import my.maleva.api.module.invoice.print.PrintStash;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Customer Statement of Account — the migrated /Report/CustomerStatement
 * screen, and the Send Statements workspace built on it.
 *
 * <p>Legacy's {@code SelectCustomerStatementAllReport} answered the screen
 * with a bare {@code {ok: true}} and parked the rows in the HTTP session for
 * a Crystal viewer to pick up. This returns the statements themselves, fully
 * computed, so the screen can show them and a PDF or mail step can render the
 * same object rather than re-query.
 */
@RestController
@RequestMapping("/api/customer-statements")
@RequiredArgsConstructor
public class CustomerStatementController {

    private final CustomerStatementService service;
    private final CustomerStatementPdfService pdf;
    private final CustomerStatementExcelService excel;
    private final PrintStash stash;
    private final CustomerStatementMailService mail;
    private final StatementMailJobService jobs;
    private final StatementMailLogRepository sendLog;

    /**
     * Every customer with something outstanding under the filters, or one.
     * A statement with nothing on it is not an error: the result simply lists
     * no customers, and the screen says so — legacy's {@code ok: false} with
     * "No Record !!!." was the same thing dressed as a failure.
     */
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<StatementResult>> query(@Valid @RequestBody StatementRequest request) {
        StatementResult result = service.build(request);
        String message = result.getCustomerCount() == 0
                ? "No outstanding invoices for these filters"
                : result.getCustomerCount() + " customer(s), " + result.getLineCount() + " line(s)";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }

    /**
     * The statement as an Excel workbook - the report's Excel option, and what
     * the send window lets the operator check before attaching it. One sheet
     * per customer; every customer with something outstanding when none is
     * chosen. Downloaded by the page with its token (an XHR blob), so no ticket.
     */
    @PostMapping("/excel")
    public ResponseEntity<byte[]> excel(@Valid @RequestBody StatementRequest request) {
        StatementResult result = service.build(request);
        if (result.getCustomerCount() == 0) {
            throw new InvalidRequestException("No outstanding invoices for these filters; nothing to export.");
        }
        CustomerStatementExcelService.RenderedWorkbook workbook = excel.render(result);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(CustomerStatementExcelService.CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + workbook.fileName() + "\"")
                .body(workbook.content());
    }

    /**
     * Render the statement and mint a ticket for the report window.
     *
     * <p>Legacy's View stashed the rows in the session and opened
     * {@code ReportViewer.aspx} in a popup, which shared the session cookie.
     * A React popup carries no bearer token, so the PDF is rendered here,
     * under the caller's authentication, and parked in {@link PrintStash}
     * behind a random ticket that {@link #print} serves for a few minutes.
     * Same mechanism as the sale invoice; see its controller.
     *
     * <p>Answers 404 rather than an empty PDF when nothing is outstanding.
     */
    @PostMapping("/print-ticket")
    public ResponseEntity<ApiResponse<Map<String, String>>> printTicket(@Valid @RequestBody StatementRequest request) {
        StatementResult result = service.build(request);
        if (result.getCustomerCount() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No outstanding invoices for these filters", 404));
        }
        RenderedStatement rendered = pdf.render(result);
        String ticket = stash.put(rendered.fileName(), rendered.pdf());
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("Url", "/api/customer-statements/print/" + ticket + "/" + rendered.fileName()),
                "Statement ready"));
    }

    /**
     * Collect a statement prepared by {@link #printTicket}. Permitted without a
     * token (see SecurityConfig): the ticket is the credential, and it returns
     * only bytes already rendered for the caller who minted it.
     */
    @GetMapping(value = "/print/{ticket}/{fileName}",
            produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> print(@PathVariable String ticket, @PathVariable String fileName) {
        return stash.get(ticket)
                .<ResponseEntity<?>>map(entry -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + entry.fileName() + "\"")
                        .body(entry.pdf()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(ApiResponse.error("This statement link has expired; open it again from the screen", 404)));
    }

    /**
     * Mail one customer's statement — the Send button, legacy
     * {@code SelectCustomerStatementEMAIL} with {@code SendId = 1}.
     *
     * <p>One customer only. Legacy accepted an all-customers run here, attached
     * a PDF of everybody and addressed the mail to whatever was in the box,
     * quoting the last customer's balance. Many customers is a run — see
     * {@link #startMailJob}.
     *
     * <p>The statement is rebuilt from the filters at send time, so the PDF
     * attached and the figures quoted are the same object — not a stale
     * render and not a browser-side number.
     *
     * <p>{@code cc}, {@code subject} and {@code body} carry what the operator
     * changed on the preview; left out, the wording's own template is sent.
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<StatementSendResult>> send(@Valid @RequestBody StatementSendRequest request) {
        return sendStatement(request);
    }

    /**
     * The statement mail as Send would build it - subject, filled HTML body,
     * To and CC - for one customer and wording, without rendering the PDF or
     * sending anything. The screen shows it, lets the operator change it, and
     * posts the edited mail to {@code /send}.
     */
    @PostMapping("/mail-preview")
    public ResponseEntity<ApiResponse<StatementMailPreview>> mailPreview(@Valid @RequestBody StatementSendRequest request) {
        if (!request.isSingleCustomer()) {
            throw new InvalidRequestException("Select one customer to preview the statement mail for.");
        }
        StatementResult result = service.build(request);
        if (result.getCustomerCount() == 0) {
            throw new InvalidRequestException("This customer has no outstanding invoices under these filters; nothing to send.");
        }
        CustomerStatement statement = result.getStatements().get(0);
        // The addresses asked for, else the customer's own statement addresses.
        List<String> to = request.getEmails() == null
                ? statement.getEmails()
                : CustomerStatementMailService.splitAddresses(request.getEmails());
        return ResponseEntity.ok(ApiResponse.success(
                mail.preview(statement, to, request.getReminder(), CustomerStatementPdfService.fileNameFor(result),
                        CustomerStatementExcelService.fileNameFor(result)),
                "Statement mail preview"));
    }

    private ResponseEntity<ApiResponse<StatementSendResult>> sendStatement(StatementSendRequest request) {
        if (!request.isSingleCustomer()) {
            throw new InvalidRequestException("Select one customer to send a statement to.");
        }
        StatementResult result = service.build(request);
        if (result.getCustomerCount() == 0) {
            throw new InvalidRequestException("This customer has no outstanding invoices under these filters; nothing to send.");
        }
        // What the operator chose to attach: the PDF (default), the Excel workbook, or both.
        List<EmailAttachment> files = new java.util.ArrayList<>(2);
        if (request.attachPdf()) {
            RenderedStatement rendered = pdf.render(result);
            files.add(new EmailAttachment(rendered.fileName(), rendered.pdf(), "application/pdf"));
        }
        if (request.attachExcel()) {
            CustomerStatementExcelService.RenderedWorkbook workbook = excel.render(result);
            files.add(new EmailAttachment(workbook.fileName(), workbook.content(), CustomerStatementExcelService.CONTENT_TYPE));
        }
        CustomerStatementMailService.MailOverrides overrides = new CustomerStatementMailService.MailOverrides(
                request.getSubject(), request.getBody(),
                request.getCc() == null ? null : CustomerStatementMailService.splitAddresses(request.getCc()));
        StatementSendResult sent = mail.send(request.getCompanyId(), result.getStatements().get(0), files,
                request.getEmails(), request.getReminder(), currentUser(), overrides);
        return ResponseEntity.ok(ApiResponse.success(sent,
                "Statement sent to " + String.join(", ", sent.sentTo())));
    }

    // ── Send Statements workspace ──────────────────────────────────────────

    /**
     * Start a bulk run: every named customer gets their own statement PDF,
     * mailed in the background. Answers at once with the run; poll
     * {@link #mailJob} for progress. One run per company at a time.
     */
    @PostMapping("/mail-jobs")
    public ResponseEntity<ApiResponse<StatementMailJobView>> startMailJob(@Valid @RequestBody StatementMailJobRequest request) {
        StatementMailJobView job = jobs.start(request, currentUser());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(job,
                job.pending() + " statement(s) queued, " + job.skipped() + " skipped"));
    }

    /** The run in progress for a company, or nothing — so a reloaded screen can pick it up again. */
    @GetMapping("/mail-jobs/active")
    public ResponseEntity<ApiResponse<StatementMailJobView>> activeMailJob(@RequestParam int companyId) {
        return ResponseEntity.ok(jobs.active(companyId)
                .map(job -> ApiResponse.success(job, "Run in progress"))
                .orElseGet(() -> ApiResponse.success(null, "No run in progress")));
    }

    @GetMapping("/mail-jobs/{id}")
    public ResponseEntity<ApiResponse<StatementMailJobView>> mailJob(@PathVariable String id) {
        return jobs.get(id)
                .map(job -> ResponseEntity.ok(ApiResponse.success(job, job.status())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("This run is no longer available", 404)));
    }

    /** Stop after the batch in flight. Customers not reached are marked cancelled; none is half-sent. */
    @PostMapping("/mail-jobs/{id}/cancel")
    public ResponseEntity<ApiResponse<StatementMailJobView>> cancelMailJob(@PathVariable String id) {
        return jobs.cancel(id)
                .map(job -> ResponseEntity.ok(ApiResponse.success(job, "Stopping after the current batch")))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("This run is no longer available", 404)));
    }

    /** When each customer of the company last received a statement mail, from the send log. */
    @GetMapping("/last-sent")
    public ResponseEntity<ApiResponse<List<StatementLastSent>>> lastSent(@RequestParam int companyId) {
        List<StatementLastSent> rows = sendLog.findLastSent(companyId);
        return ResponseEntity.ok(ApiResponse.success(rows,
                sendLog.isAvailable() ? rows.size() + " customer(s) mailed before" : "Send log unavailable"));
    }

    private static String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
}
