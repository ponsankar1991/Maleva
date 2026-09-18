package my.maleva.api.module.customerstatement.mail;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.common.service.EmailService;
import my.maleva.api.module.common.service.EmailService.EmailAttachment;
import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.StatementSendResult;
import my.maleva.api.module.customerstatement.print.CustomerStatementPdfService.RenderedStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The mail step: wording, subject, recipients, attachment and the log row, with the sender mocked. */
@ExtendWith(MockitoExtension.class)
class CustomerStatementMailServiceTest {

    private static final String CRLF = String.valueOf((char) 13) + (char) 10;

    @Mock private EmailService email;
    @Mock private StatementMailLogRepository logs;
    @Captor private ArgumentCaptor<List<String>> toCaptor;
    @Captor private ArgumentCaptor<List<String>> ccCaptor;
    @Captor private ArgumentCaptor<String> subjectCaptor;
    @Captor private ArgumentCaptor<String> bodyCaptor;
    @Captor private ArgumentCaptor<List<EmailAttachment>> attachmentCaptor;
    @Captor private ArgumentCaptor<StatementMailLogRepository.Entry> logCaptor;

    private CustomerStatementMailService service;
    private final MimeMessage message = new MimeMessage((Session) null);

    @BeforeEach
    void setUp() {
        service = new CustomerStatementMailService(email, logs, Optional.empty(),
                "receivable@maleva.com.my, mala@maleva.com.my",
                "https://www.maleva.my/Content/images/pngimages/MalevaBanner.png",
                "account@maleva.com.my");
    }

    private static CustomerStatement acs() {
        return CustomerStatement.builder()
                .customerId(17).customerName("ACS FREIGHT & SERVICES PTE LTD")
                .currency("SGD").emails(List.of())
                .statementDate(LocalDate.of(2026, 9, 11))
                .lines(List.of()).totalDebit(new BigDecimal("18836.45")).totalCredit(BigDecimal.ZERO)
                .closingBalance(new BigDecimal("18836.45")).ageing(List.of()).openingBalance(BigDecimal.ZERO)
                .overdueAmount(new BigDecimal("18836.45")).overdueAsOf(LocalDate.of(2026, 9, 9))
                .build();
    }

    private static RenderedStatement pdf() {
        return new RenderedStatement("CustomerStatement_ACS_20260911.pdf", "%PDF-1.4 fake".getBytes());
    }

    @Test
    @DisplayName("the plain statement mail: legacy subject, template wording, figures from the statement, one log row")
    void sendsPlainStatement() throws Exception {
        when(email.prepareHtmlMail(anyList(), anyList(), anyString(), anyString(), anyList())).thenReturn(message);
        when(email.sendPrepared(anyList())).thenReturn(Map.of());

        StatementSendResult result = service.send(1, acs(), pdf(),
                "cayden@acs.example; titus@acs.example, cayden@acs.example", "", "mala");

        verify(email).prepareHtmlMail(toCaptor.capture(), ccCaptor.capture(), subjectCaptor.capture(),
                bodyCaptor.capture(), attachmentCaptor.capture());
        verify(email).sendPrepared(List.of(message));

        // trimmed, split on ; and , and de-duplicated in order
        assertThat(toCaptor.getValue()).containsExactly("cayden@acs.example", "titus@acs.example");
        assertThat(ccCaptor.getValue()).containsExactly("receivable@maleva.com.my", "mala@maleva.com.my");
        assertThat(subjectCaptor.getValue()).isEqualTo("Statement of Account & Payment Request - ACS FREIGHT & SERVICES PTE LTD");

        String body = bodyCaptor.getValue();
        assertThat(body).contains("Dear <b>ACS FREIGHT &amp; SERVICES PTE LTD</b>");   // escaped for HTML
        assertThat(body).contains("as of <b>11 September 2026</b>");
        assertThat(body).contains("SGD 18,836.45");                                   // currency, not a bare number
        assertThat(body).contains("MalevaBanner.png");
        assertThat(body).contains("Manimala Nynasarul");                              // signature block
        assertThat(body).doesNotContain("{Customer Name}").doesNotContain("{TOTAL_OVERDUE}")
                .doesNotContain("{SIGNATURE}").doesNotContain("{ImageUrls}").doesNotContain("{CURRENT_DATE}");

        EmailAttachment attachment = attachmentCaptor.getValue().get(0);
        assertThat(attachment.fileName()).isEqualTo("CustomerStatement_ACS_20260911.pdf");
        assertThat(attachment.contentType()).isEqualTo("application/pdf");

        assertThat(result.sentTo()).hasSize(2);
        assertThat(result.overdueAmount()).isEqualByComparingTo("18836.45");
        assertThat(result.currency()).isEqualTo("SGD");

        verify(logs).insert(logCaptor.capture());
        StatementMailLogRepository.Entry row = logCaptor.getValue();
        assertThat(row.companyId()).isEqualTo(1);
        assertThat(row.customerId()).isEqualTo(17);
        assertThat(row.status()).isEqualTo("SENT");
        assertThat(row.reminder()).isEmpty();
        assertThat(row.sentBy()).isEqualTo("mala");
        assertThat(row.jobId()).isNull();
        assertThat(row.sentTo()).containsExactly("cayden@acs.example", "titus@acs.example");
        assertThat(row.kind()).isEqualTo("STATEMENT");
        assertThat(row.messageId()).startsWith("<statement-").endsWith("@maleva.com.my>");   // ours, on the wire and in the log
        assertThat(message.getHeader("Message-ID")[0]).isEqualTo(row.messageId());
    }

    @Test
    @DisplayName("Reminder 1 and 2 use their own subject and wording")
    void reminders() {
        when(email.prepareHtmlMail(anyList(), anyList(), anyString(), anyString(), anyList())).thenReturn(message);
        when(email.sendPrepared(anyList())).thenReturn(Map.of());

        service.send(1, acs(), pdf(), "a@b.c", "Reminder 1", "mala");
        verify(email).prepareHtmlMail(anyList(), anyList(), subjectCaptor.capture(), bodyCaptor.capture(), anyList());
        assertThat(subjectCaptor.getValue()).startsWith("Friendly Payment Reminder");
        assertThat(bodyCaptor.getValue()).contains("follow up on the Statement of Account previously sent");

        assertThat(service.subjectFor("Reminder 2", "X")).startsWith("Second Payment Reminder");
        assertThat(service.bodyFor("Reminder 2", acs())).contains("invoices are now overdue");
    }

    @Test
    @DisplayName("Reminder 3-5 fall back to the plain statement, as they always did")
    void unknownReminderIsPlain() {
        assertThat(service.subjectFor("Reminder 4", "X")).startsWith("Statement of Account");
        assertThat(service.bodyFor("Reminder 4", acs())).contains("Please find attached your Statement of Account");
        assertThat(CustomerStatementMailService.wordingKey("Reminder 4")).isEmpty();
        assertThat(CustomerStatementMailService.wordingKey("Reminder 2")).isEqualTo("Reminder 2");
    }

    @Test
    @DisplayName("no usable address is refused before anything is sent or logged")
    void refusesNoRecipients() {
        assertThatThrownBy(() -> service.send(1, acs(), pdf(), " ; ,not-an-address", "", "mala"))
                .isInstanceOf(InvalidRequestException.class);
        verify(email, never()).prepareHtmlMail(anyList(), anyList(), anyString(), anyString(), anyList());
        verify(email, never()).sendPrepared(anyList());
        verify(logs, never()).insert(any());
    }

    @Test
    @DisplayName("a refusal from the mail server is logged as FAILED and not swallowed")
    void serverRefusalPropagates() {
        when(email.prepareHtmlMail(anyList(), anyList(), anyString(), anyString(), anyList())).thenReturn(message);
        when(email.sendPrepared(anyList())).thenReturn(Map.of(message, "550 mailbox unavailable"));

        assertThatThrownBy(() -> service.send(1, acs(), pdf(), "a@b.c", "", "mala"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("550");

        verify(logs).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().status()).isEqualTo("FAILED");
        assertThat(logCaptor.getValue().error()).isEqualTo("550 mailbox unavailable");
    }

    @Test
    @DisplayName("money carries the statement's currency")
    void moneyFormat() {
        assertThat(CustomerStatementMailService.money("SGD", new BigDecimal("18836.45"))).isEqualTo("SGD 18,836.45");
        assertThat(CustomerStatementMailService.money(null, new BigDecimal("5"))).isEqualTo("5.00");
    }

    @Test
    @DisplayName("the preview is the template mail Send would build: subject, filled body, To asked for, configured CC")
    void previewIsTheTemplateMail() {
        var preview = service.preview(acs(), List.of("cayden@acs.example"), "Reminder 1", "CustomerStatement_ACS_20260911.pdf",
                "CustomerStatement_ACS_20260911.xlsx");

        assertThat(preview.reminder()).isEqualTo("Reminder 1");
        assertThat(preview.subject()).isEqualTo("Friendly Payment Reminder – Outstanding Balance - ACS FREIGHT & SERVICES PTE LTD");
        assertThat(preview.body()).contains("ACS FREIGHT &amp; SERVICES PTE LTD").contains("SGD 18,836.45")
                .doesNotContain("{Customer Name}").doesNotContain("{SIGNATURE}");
        assertThat(preview.to()).containsExactly("cayden@acs.example");
        assertThat(preview.cc()).containsExactly("receivable@maleva.com.my", "mala@maleva.com.my");
        assertThat(preview.attachmentName()).isEqualTo("CustomerStatement_ACS_20260911.pdf");
        assertThat(preview.excelAttachmentName()).isEqualTo("CustomerStatement_ACS_20260911.xlsx");
        verify(email, never()).sendPrepared(anyList());
    }

    @Test
    @DisplayName("PDF and Excel chosen: both files attached in that order, both names in the result and the log")
    void attachesTheChosenFiles() {
        when(email.prepareHtmlMail(anyList(), anyList(), anyString(), anyString(), anyList())).thenReturn(message);
        when(email.sendPrepared(anyList())).thenReturn(Map.of());
        var files = List.of(
                new EmailAttachment("CustomerStatement_ACS_20260911.pdf", "%PDF".getBytes(), "application/pdf"),
                new EmailAttachment("CustomerStatement_ACS_20260911.xlsx", new byte[]{80, 75},
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        StatementSendResult result = service.send(1, acs(), files, "cayden@acs.example", "", "mala",
                CustomerStatementMailService.MailOverrides.NONE);

        verify(email).prepareHtmlMail(toCaptor.capture(), ccCaptor.capture(), subjectCaptor.capture(),
                bodyCaptor.capture(), attachmentCaptor.capture());
        assertThat(attachmentCaptor.getValue()).extracting(EmailAttachment::fileName)
                .containsExactly("CustomerStatement_ACS_20260911.pdf", "CustomerStatement_ACS_20260911.xlsx");
        assertThat(result.attachmentName())
                .isEqualTo("CustomerStatement_ACS_20260911.pdf, CustomerStatement_ACS_20260911.xlsx");
        verify(logs).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().attachmentName()).isEqualTo(result.attachmentName());
    }

    @Test
    @DisplayName("nothing chosen to attach is refused before anything is sent")
    void refusesNoAttachment() {
        assertThatThrownBy(() -> service.send(1, acs(), List.of(), "cayden@acs.example", "", "mala",
                CustomerStatementMailService.MailOverrides.NONE))
                .isInstanceOf(InvalidRequestException.class);
        verify(email, never()).sendPrepared(anyList());
    }

    @Test
    @DisplayName("what was changed on the preview is what goes out: subject, body and CC, and the log row says so")
    void sendsTheEditedMail() {
        when(email.prepareHtmlMail(anyList(), anyList(), anyString(), anyString(), anyList())).thenReturn(message);
        when(email.sendPrepared(anyList())).thenReturn(Map.of());

        StatementSendResult result = service.send(1, acs(), pdf(), "cayden@acs.example", "Reminder 2", "mala",
                new CustomerStatementMailService.MailOverrides("Statement for Sept" + CRLF + "Bcc: evil@x.example",
                        "<p>Dear Cayden, as discussed.</p>", List.of("mala@maleva.com.my")));

        verify(email).prepareHtmlMail(toCaptor.capture(), ccCaptor.capture(), subjectCaptor.capture(),
                bodyCaptor.capture(), attachmentCaptor.capture());
        assertThat(subjectCaptor.getValue()).isEqualTo("Statement for Sept Bcc: evil@x.example");
        assertThat(bodyCaptor.getValue()).isEqualTo("<p>Dear Cayden, as discussed.</p>");
        assertThat(ccCaptor.getValue()).containsExactly("mala@maleva.com.my");
        assertThat(attachmentCaptor.getValue()).hasSize(1);
        assertThat(result.cc()).containsExactly("mala@maleva.com.my");
        assertThat(result.subject()).isEqualTo("Statement for Sept Bcc: evil@x.example");

        verify(logs).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().cc()).containsExactly("mala@maleva.com.my");
        assertThat(logCaptor.getValue().subject()).isEqualTo("Statement for Sept Bcc: evil@x.example");
    }

    @Test
    @DisplayName("blank overrides and an empty CC: template subject and body, sent without CC")
    void blankOverridesKeepTheTemplate() {
        when(email.prepareHtmlMail(anyList(), anyList(), anyString(), anyString(), anyList())).thenReturn(message);
        when(email.sendPrepared(anyList())).thenReturn(Map.of());

        service.send(1, acs(), pdf(), "cayden@acs.example", "", "mala",
                new CustomerStatementMailService.MailOverrides("  ", "", List.of()));

        verify(email).prepareHtmlMail(toCaptor.capture(), ccCaptor.capture(), subjectCaptor.capture(),
                bodyCaptor.capture(), attachmentCaptor.capture());
        assertThat(subjectCaptor.getValue()).isEqualTo("Statement of Account & Payment Request - ACS FREIGHT & SERVICES PTE LTD");
        assertThat(bodyCaptor.getValue()).contains("SGD 18,836.45");
        assertThat(ccCaptor.getValue()).isEmpty();
    }
}
