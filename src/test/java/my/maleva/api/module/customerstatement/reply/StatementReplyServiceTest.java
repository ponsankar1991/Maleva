package my.maleva.api.module.customerstatement.reply;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import my.maleva.api.common.config.FileUploadConfig;
import my.maleva.api.common.config.MailProperties;
import my.maleva.api.module.common.service.EmailService;
import my.maleva.api.module.customerstatement.dto.ReplySendRequest;
import my.maleva.api.module.customerstatement.dto.ReplySendResult;
import my.maleva.api.module.customerstatement.mail.CustomerStatementMailService;
import my.maleva.api.module.customerstatement.mail.StatementMailLogRepository;
import my.maleva.api.module.customerstatement.print.CustomerStatementPdfService;
import my.maleva.api.module.customerstatement.reply.StatementReplyRepository.Reply;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository.CustomerHeader;
import my.maleva.api.module.customerstatement.service.CustomerStatementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reply / Reply All recipients, threading headers, the log row and the read mark, with the relay mocked. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StatementReplyServiceTest {

    private static final Set<String> OWN = Set.of("account@maleva.com.my", "receivable@maleva.com.my", "mala@maleva.com.my");

    @Mock private StatementReplyRepository replies;
    @Mock private StatementMailLogRepository logs;
    @Mock private CustomerStatementMailService mail;
    @Mock private EmailService email;
    @Mock private CustomerStatementService statements;
    @Mock private CustomerStatementPdfService pdf;
    @Mock private CustomerStatementQueryRepository queries;
    @Mock private FileUploadConfig uploads;

    private StatementReplyService service;
    private final MimeMessage outgoing = new MimeMessage((Session) null);

    private static Reply customerMail() {
        return new Reply(55L, 1, 17, 900L, "<CAF123@mail.acs.example>", "<statement-9f3c@maleva.com.my>",
                "cayden@acs.example", "Cayden Lim", "account@maleva.com.my, titus@acs.example",
                "receivable@maleva.com.my, Mala@maleva.com.my, finance@acs.example",
                "RE: Statement of Account & Payment Request - ACS", "Payment was made on 10 Sep.", false,
                LocalDateTime.of(2026, 9, 11, 10, 30), null, null);
    }

    @BeforeEach
    void setUp() {
        MailProperties props = mock(MailProperties.class);
        MailProperties.From from = mock(MailProperties.From.class);
        MailProperties.Smtp smtp = mock(MailProperties.Smtp.class);
        when(props.getFrom()).thenReturn(from);
        when(props.getSmtp()).thenReturn(smtp);
        when(from.getEmail()).thenReturn("account@maleva.com.my");
        when(smtp.getUsername()).thenReturn("account@maleva.com.my");
        when(mail.cc()).thenReturn(List.of("receivable@maleva.com.my", "mala@maleva.com.my"));
        when(mail.signatureHtml()).thenReturn("<p>Maleva Accounts</p>");
        when(mail.stamp(any(), anyString())).thenReturn("<reply-1@maleva.com.my>");
        when(email.isConfigured()).thenReturn(true);
        when(email.prepareHtmlMail(anyList(), anyList(), anyString(), anyString(), anyList())).thenReturn(outgoing);
        when(email.sendPrepared(anyList())).thenReturn(Map.of());
        when(queries.findHeaders(anyInt(), any())).thenReturn(List.of(new CustomerHeader(
                17, "ACS FREIGHT SERVICES PTE LTD", null, null, null, null, null, null, null, "SGD",
                "cayden@acs.example", "titus@acs.example", null, null)));
        service = new StatementReplyService(replies, logs, mail, email, statements, pdf, queries, uploads, props);
    }

    @Test
    @DisplayName("the notification snippet is the customer's own words, not the quoted statement mail")
    void snippetStopsAtTheQuote() {
        assertThat(StatementReplyService.snippet("Payment was made on 10 Sep.\n\nOn Fri, Sep 11, 2026 at 2:30 PM Maleva Accounts <account@maleva.com.my> wrote:\n> Dear Sir"))
                .isEqualTo("Payment was made on 10 Sep.");
        assertThat(StatementReplyService.snippet("Noted, thanks\n> quoted line")).isEqualTo("Noted, thanks");
        assertThat(StatementReplyService.snippet("test\r\nFrom: Maleva Accounts\r\nSent: Friday")).isEqualTo("test");
        assertThat(StatementReplyService.snippet("> only a quote\n> nothing else")).isEqualTo("> only a quote > nothing else");
        assertThat(StatementReplyService.snippet("   ")).isNull();
        assertThat(StatementReplyService.snippet("x".repeat(200))).hasSize(158).endsWith("…");
    }

    @Test
    @DisplayName("Reply goes to the writer only")
    void recipientsReply() {
        assertThat(StatementReplyService.recipients("REPLY", customerMail(), OWN, List.of()))
                .containsExactly("cayden@acs.example");
    }

    @Test
    @DisplayName("Reply All goes to the writer and everyone on their mail, minus our own addresses, plus extras, no duplicates")
    void recipientsReplyAll() {
        assertThat(StatementReplyService.recipients("REPLY_ALL", customerMail(), OWN, List.of("ops@acs.example", "TITUS@acs.example")))
                .containsExactly("cayden@acs.example", "titus@acs.example", "finance@acs.example", "ops@acs.example");
    }

    @Test
    @DisplayName("an answer is threaded under the customer's mail, logged as a REPLY, filed in Sent, and marks the mail read")
    void replyAllThreadsAndLogs() throws Exception {
        when(replies.findById(55L)).thenReturn(Optional.of(customerMail()));
        ReplySendRequest request = new ReplySendRequest();
        request.setCompanyId(1);
        request.setCustomerId(17);
        request.setReplyToId(55L);
        request.setMode("REPLY_ALL");
        request.setBody("Thank you, noted.\nWe will update the ledger.");

        ReplySendResult result = service.send(request, "mala");

        assertThat(result.sentTo()).containsExactly("cayden@acs.example", "titus@acs.example", "finance@acs.example");
        assertThat(result.subject()).isEqualTo("RE: Statement of Account & Payment Request - ACS");
        assertThat(outgoing.getHeader("In-Reply-To")[0]).isEqualTo("<CAF123@mail.acs.example>");
        assertThat(outgoing.getHeader("References")[0]).isEqualTo("<statement-9f3c@maleva.com.my> <CAF123@mail.acs.example>");

        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);
        verify(email).prepareHtmlMail(anyList(), eq(List.of("receivable@maleva.com.my", "mala@maleva.com.my")),
                eq("RE: Statement of Account & Payment Request - ACS"), html.capture(), anyList());
        assertThat(html.getValue()).contains("Thank you, noted.<br/>We will update the ledger.")
                .contains("Maleva Accounts")
                .contains("Cayden Lim &lt;cayden@acs.example&gt; wrote:")
                .contains("Payment was made on 10 Sep.");

        ArgumentCaptor<StatementMailLogRepository.Entry> row = ArgumentCaptor.forClass(StatementMailLogRepository.Entry.class);
        verify(logs).insert(row.capture());
        assertThat(row.getValue().kind()).isEqualTo("REPLY");
        assertThat(row.getValue().status()).isEqualTo("SENT");
        assertThat(row.getValue().messageId()).isEqualTo("<reply-1@maleva.com.my>");
        assertThat(row.getValue().inReplyTo()).isEqualTo("<CAF123@mail.acs.example>");
        assertThat(row.getValue().replyToRefId()).isEqualTo(55L);
        assertThat(row.getValue().sentBy()).isEqualTo("mala");
        assertThat(row.getValue().customerName()).isEqualTo("ACS FREIGHT SERVICES PTE LTD");

        verify(mail).fileSentCopy(eq(outgoing), anyString());
        verify(replies).markRead(55L, "mala");
    }

    @Test
    @DisplayName("a fresh mail (nothing to answer) goes to the customer master's statement addresses with a plain subject")
    void freshMail() {
        ReplySendRequest request = new ReplySendRequest();
        request.setCompanyId(1);
        request.setCustomerId(17);
        request.setBody("Please confirm the payment date.");

        ReplySendResult result = service.send(request, "mala");

        assertThat(result.sentTo()).containsExactly("cayden@acs.example", "titus@acs.example");
        assertThat(result.subject()).isEqualTo("Statement of Account - ACS FREIGHT SERVICES PTE LTD");
        verify(replies, never()).markRead(anyLong(), anyString());
    }

    @Test
    @DisplayName("a refused mail is logged FAILED and reported, not swallowed")
    void refused() {
        when(replies.findById(55L)).thenReturn(Optional.of(customerMail()));
        when(email.sendPrepared(anyList())).thenReturn(Map.of(outgoing, "550 mailbox unavailable"));
        ReplySendRequest request = new ReplySendRequest();
        request.setCompanyId(1);
        request.setCustomerId(17);
        request.setReplyToId(55L);
        request.setBody("x");

        assertThatThrownBy(() -> service.send(request, "mala")).isInstanceOf(IllegalStateException.class).hasMessageContaining("550");

        ArgumentCaptor<StatementMailLogRepository.Entry> row = ArgumentCaptor.forClass(StatementMailLogRepository.Entry.class);
        verify(logs).insert(row.capture());
        assertThat(row.getValue().status()).isEqualTo("FAILED");
        verify(mail, never()).fileSentCopy(any(), anyString());
        verify(replies, never()).markRead(anyLong(), anyString());
    }
}
