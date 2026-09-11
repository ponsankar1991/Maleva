package my.maleva.api.module.customerstatement.controller;

import jakarta.validation.Valid;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.customerstatement.dto.ReplyInbox;
import my.maleva.api.module.customerstatement.dto.ReplySendRequest;
import my.maleva.api.module.customerstatement.dto.ReplySendResult;
import my.maleva.api.module.customerstatement.dto.ReplySyncResult;
import my.maleva.api.module.customerstatement.dto.StatementConversation;
import my.maleva.api.module.customerstatement.dto.StatementReplySummary;
import my.maleva.api.module.customerstatement.reply.NotifyRoles;
import my.maleva.api.module.customerstatement.reply.StatementReplyImporter;
import my.maleva.api.module.customerstatement.reply.StatementReplyService;
import org.springframework.http.HttpHeaders;
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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Customers' replies to their statements, and answering them — the
 * conversation side of the Send Statements screen.
 */
@RestController
@RequestMapping("/api/customer-statements/replies")
public class CustomerStatementReplyController {

    private final StatementReplyService service;
    private final Optional<StatementReplyImporter> importer;
    private final NotifyRoles notifyRoles;

    public CustomerStatementReplyController(StatementReplyService service, Optional<StatementReplyImporter> importer,
                                            NotifyRoles notifyRoles) {
        this.service = service;
        this.importer = importer;
        this.notifyRoles = notifyRoles;
    }

    /**
     * The header bell: unread replies for the company, newest first — only for
     * a role on {@code mail.statement.replies.notify-roles}. Any other role gets
     * {@code allowed = false} and an empty list, and the bell stays hidden.
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<ReplyInbox>> unread(@RequestParam int companyId,
                                                          @RequestParam(defaultValue = "15") int limit) {
        boolean allowed = notifyRoles.allows(SecurityContextHolder.getContext().getAuthentication());
        ReplyInbox inbox = service.inbox(companyId, Math.min(Math.max(limit, 1), 50), allowed);
        return ResponseEntity.ok(ApiResponse.success(inbox,
                !allowed ? "Not on the notify list (" + String.join(", ", notifyRoles.configured()) + ")"
                        : inbox.count() + " unread repl" + (inbox.count() == 1 ? "y" : "ies")));
    }

    /** The latest reply per customer, with counts — the screen's "Reply" column. */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<StatementReplySummary>>> latest(@RequestParam int companyId) {
        List<StatementReplySummary> rows = service.latest(companyId);
        return ResponseEntity.ok(ApiResponse.success(rows, rows.size() + " customer(s) have replied"));
    }

    /** Everything exchanged with one customer, oldest first. */
    @GetMapping("/conversation")
    public ResponseEntity<ApiResponse<StatementConversation>> conversation(@RequestParam int companyId, @RequestParam int customerId) {
        StatementConversation c = service.conversation(companyId, customerId);
        return ResponseEntity.ok(ApiResponse.success(c, c.entries().size() + " message(s)"));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable long id, @RequestParam int companyId) {
        service.markRead(id, companyId, currentUser());
        return ResponseEntity.ok(ApiResponse.success(null, "Marked read"));
    }

    /** Reply / Reply All / a fresh mail to the customer, from the accounts address, threaded and logged. */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ReplySendResult>> send(@Valid @RequestBody ReplySendRequest request) {
        ReplySendResult sent = service.send(request, currentUser());
        return ResponseEntity.ok(ApiResponse.success(sent, "Sent to " + String.join(", ", sent.sentTo())));
    }

    /** A file the customer attached to their reply. */
    @GetMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> attachment(@PathVariable long id, @PathVariable long attachmentId,
                                             @RequestParam int companyId) throws IOException {
        StatementReplyService.StoredFile file = service.attachment(id, attachmentId, companyId);
        String safeName = file.fileName().replace("\"", "");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeName + "\"")
                .body(file.content());
    }

    /** Read the mailbox now instead of waiting for the timer. */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<ReplySyncResult>> sync() {
        ReplySyncResult result = importer.map(StatementReplyImporter::sync)
                .orElseGet(() -> new ReplySyncResult("", "", 0, 0, null, "The reply reader is switched off (mail.statement.replies.enabled)", false));
        String message = result.error() != null ? result.error()
                : result.imported() + " new repl" + (result.imported() == 1 ? "y" : "ies") + " (" + result.scanned() + " mail(s) checked)";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }

    /** How the last scan went. */
    @GetMapping("/sync")
    public ResponseEntity<ApiResponse<ReplySyncResult>> syncStatus() {
        ReplySyncResult result = importer.map(StatementReplyImporter::status)
                .orElseGet(() -> new ReplySyncResult("", "", 0, 0, null, "The reply reader is switched off (mail.statement.replies.enabled)", false));
        return ResponseEntity.ok(ApiResponse.success(result, result.error() == null ? "OK" : result.error()));
    }

    private static String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
}
