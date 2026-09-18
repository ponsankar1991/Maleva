package my.maleva.api.module.customerstatement.mail;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.common.service.EmailService;
import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.StatementMailJobRequest;
import my.maleva.api.module.customerstatement.dto.StatementMailJobView;
import my.maleva.api.module.customerstatement.dto.StatementRequest;
import my.maleva.api.module.customerstatement.dto.StatementResult;
import my.maleva.api.module.customerstatement.mail.CustomerStatementMailService.PreparedStatementMail;
import my.maleva.api.module.customerstatement.print.CustomerStatementPdfService;
import my.maleva.api.module.customerstatement.print.CustomerStatementPdfService.RenderedStatement;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The bulk runner with every collaborator mocked: one build for the run,
 * batches over one connection, per-customer outcomes, cancel, and the two
 * ways a run stops.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StatementMailJobServiceTest {

    @Mock private CustomerStatementService statements;
    @Mock private CustomerStatementPdfService pdf;
    @Mock private CustomerStatementMailService mail;
    @Mock private EmailService email;

    /** Messages handed out by prepare(), keyed by customer id, so a refusal can name one. */
    private final Map<Integer, MimeMessage> messages = new HashMap<>();
    private StatementMailJobService service;

    private static CustomerStatement customer(int id, String name, List<String> emails) {
        return CustomerStatement.builder()
                .customerId(id).customerName(name).currency("MYR").emails(emails)
                .statementDate(LocalDate.of(2026, 9, 11))
                .lines(List.of()).totalDebit(BigDecimal.TEN).totalCredit(BigDecimal.ZERO)
                .closingBalance(BigDecimal.TEN).ageing(List.of()).openingBalance(BigDecimal.ZERO)
                .overdueAmount(BigDecimal.TEN).overdueAsOf(LocalDate.of(2026, 8, 1))
                .build();
    }

    private static StatementResult result(CustomerStatement... all) {
        return StatementResult.builder()
                .statements(List.of(all)).customerCount(all.length).lineCount(0)
                .cutoffDate(LocalDate.of(2024, 10, 1)).periodFrom(LocalDate.of(2024, 10, 1))
                .ageingFrom(LocalDate.of(2025, 10, 1)).ageingTo(LocalDate.of(2026, 9, 30))
                .build();
    }

    private static StatementMailJobRequest request(String reminder, Object... idAndEmails) {
        StatementMailJobRequest request = new StatementMailJobRequest();
        request.setCompanyId(1);
        request.setReminder(reminder);
        List<StatementMailJobRequest.Recipient> recipients = new ArrayList<>();
        for (int i = 0; i < idAndEmails.length; i += 2) {
            StatementMailJobRequest.Recipient r = new StatementMailJobRequest.Recipient();
            r.setCustomerId((Integer) idAndEmails[i]);
            r.setEmails((String) idAndEmails[i + 1]);
            recipients.add(r);
        }
        request.setCustomers(recipients);
        return request;
    }

    private StatementMailJobView.Item item(StatementMailJobView view, int customerId) {
        return view.items().stream().filter(i -> i.customerId() == customerId).findFirst().orElseThrow();
    }

    @BeforeEach
    void setUp() {
        when(email.isConfigured()).thenReturn(true);
        when(statements.build(any())).thenReturn(result(
                customer(1, "ACS", List.of("a@acs.example")),
                customer(2, "BLANK", List.of()),
                customer(3, "CMA", List.of("c@cma.example", "c2@cma.example"))));
        when(pdf.renderOne(any(), any())).thenAnswer(inv -> {
            CustomerStatement s = inv.getArgument(1);      // null while a test re-stubs this
            return new RenderedStatement("CustomerStatement_" + (s == null ? "" : s.getCustomerName()) + ".pdf", new byte[]{1});
        });
        when(mail.prepare(any(), any(), anyList(), any())).thenAnswer(inv -> {
            CustomerStatement s = inv.getArgument(0);
            RenderedStatement r = inv.getArgument(1);
            MimeMessage m = new MimeMessage((Session) null);
            messages.put(s.getCustomerId(), m);
            return new PreparedStatementMail(m, "Subject " + s.getCustomerName(), inv.getArgument(2),
                    List.of("receivable@maleva.com.my"), r.fileName(), "<statement-" + s.getCustomerId() + "@maleva.com.my>");
        });
        when(email.sendPrepared(anyList())).thenReturn(Map.of());
        // batch of 2, no pause, worker on this thread
        service = new StatementMailJobService(statements, pdf, mail, email, 2, 0, Runnable::run);
    }

    @Test
    @DisplayName("one build for the run; every customer mailed once; no address or nothing outstanding is skipped, not failed")
    void happyRun() {
        StatementMailJobView view = service.start(
                request("Reminder 1", 1, null, 2, null, 3, "override@cma.example", 9, null, 1, null), "mala");

        // the worker ran on this thread, so the view returned by start is already final
        assertThat(view.status()).isEqualTo("DONE");
        assertThat(view.total()).isEqualTo(4);                       // the duplicate 1 collapsed
        assertThat(view.sent()).isEqualTo(2);
        assertThat(view.skipped()).isEqualTo(2);
        assertThat(view.failed()).isZero();
        assertThat(view.pending()).isZero();

        assertThat(item(view, 1).status()).isEqualTo("SENT");
        assertThat(item(view, 1).emails()).containsExactly("a@acs.example");    // from the customer master
        assertThat(item(view, 1).subject()).isEqualTo("Subject ACS");
        assertThat(item(view, 1).attachmentName()).isEqualTo("CustomerStatement_ACS.pdf");
        assertThat(item(view, 1).sentAt()).isNotNull();
        assertThat(item(view, 2).status()).isEqualTo("SKIPPED");
        assertThat(item(view, 2).error()).isEqualTo("No e-mail address");
        assertThat(item(view, 3).emails()).containsExactly("override@cma.example"); // the box wins over the master
        assertThat(item(view, 9).status()).isEqualTo("SKIPPED");
        assertThat(item(view, 9).error()).contains("No outstanding invoices");

        // ONE statement build, for every customer, with the run's filters
        ArgumentCaptor<StatementRequest> filters = ArgumentCaptor.forClass(StatementRequest.class);
        verify(statements, times(1)).build(filters.capture());
        assertThat(filters.getValue().getCustomerId()).isNull();
        assertThat(filters.getValue().getCompanyId()).isEqualTo(1);

        // both sendable customers went in ONE batch (size 2) over ONE connection
        verify(email, times(1)).sendPrepared(anyList());
        verify(mail, times(2)).record(eq(1), any(), anyList(), anyString(), anyString(), any(), eq("Reminder 1"),
                eq("SENT"), isNull(), eq(view.id()), eq("mala"));
        verify(mail, never()).record(anyInt(), any(), anyList(), any(), any(), any(), any(), eq("FAILED"), any(), any(), any());

        // readable afterwards, and no longer active
        assertThat(service.get(view.id())).isPresent();
        assertThat(service.active(1)).isEmpty();
    }

    @Test
    @DisplayName("a run with PDF + Excel attaches each customer's own PDF and workbook, in that order")
    @SuppressWarnings("unchecked")
    void runWithExcel() {
        ArgumentCaptor<List<EmailService.EmailAttachment>> files = ArgumentCaptor.forClass(List.class);
        when(mail.prepare(any(CustomerStatement.class), anyList(), anyList(), any(), any())).thenAnswer(inv -> {
            CustomerStatement s = inv.getArgument(0);
            List<EmailService.EmailAttachment> attached = inv.getArgument(1);
            MimeMessage m = new MimeMessage((Session) null);
            messages.put(s.getCustomerId(), m);
            String names = String.join(", ", attached.stream().map(EmailService.EmailAttachment::fileName).toList());
            return new PreparedStatementMail(m, "Subject " + s.getCustomerName(), inv.getArgument(2),
                    List.of("receivable@maleva.com.my"), names, "<statement-" + s.getCustomerId() + "@maleva.com.my>");
        });
        StatementMailJobRequest request = request("", 1, null, 3, null);
        request.setAttach("BOTH");

        StatementMailJobView view = service.start(request, "mala");

        assertThat(view.status()).isEqualTo("DONE");
        assertThat(view.attach()).isEqualTo("BOTH");
        assertThat(view.sent()).isEqualTo(2);
        verify(mail, times(2)).prepare(any(CustomerStatement.class), files.capture(), anyList(), any(), any());
        assertThat(files.getAllValues().get(0)).extracting(EmailService.EmailAttachment::fileName)
                .containsExactly("CustomerStatement_ACS.pdf", "CustomerStatement_ACS_" + LocalDate.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");
        assertThat(files.getAllValues().get(0).get(1).contentType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(item(view, 1).attachmentName()).contains(".pdf, ").endsWith(".xlsx");
        // the PDF-only path is not used
        verify(mail, never()).prepare(any(), any(RenderedStatement.class), anyList(), any());
    }

    @Test
    @DisplayName("a run with Excel only renders no PDF")
    void runWithExcelOnly() {
        when(mail.prepare(any(CustomerStatement.class), anyList(), anyList(), any(), any())).thenAnswer(inv -> {
            CustomerStatement s = inv.getArgument(0);
            MimeMessage m = new MimeMessage((Session) null);
            messages.put(s.getCustomerId(), m);
            return new PreparedStatementMail(m, "Subject", inv.getArgument(2), List.of(), "x.xlsx", "<id>");
        });
        StatementMailJobRequest request = request("", 1, null);
        request.setAttach("excel");

        StatementMailJobView view = service.start(request, "mala");

        assertThat(view.sent()).isEqualTo(1);
        assertThat(view.attach()).isEqualTo("EXCEL");
        verify(pdf, never()).renderOne(any(), any());
    }

    @Test
    @DisplayName("a mail the relay refuses fails that customer alone; the others in the batch are sent")
    void partialRefusal() {
        when(email.sendPrepared(anyList())).thenAnswer(inv ->
                Map.of(messages.get(3), "550 5.1.1 The email account does not exist"));

        StatementMailJobView view = service.start(request("", 1, null, 3, null), "mala");

        assertThat(view.status()).isEqualTo("DONE");
        assertThat(item(view, 1).status()).isEqualTo("SENT");
        assertThat(item(view, 3).status()).isEqualTo("FAILED");
        assertThat(item(view, 3).error()).contains("550");
        verify(mail).record(eq(1), any(), anyList(), anyString(), anyString(), any(), eq(""), eq("FAILED"),
                eq("550 5.1.1 The email account does not exist"), eq(view.id()), eq("mala"));
    }

    @Test
    @DisplayName("a PDF that cannot be rendered fails that customer alone and the batch still goes")
    void renderFailureIsPerCustomer() {
        doAnswer(inv -> {
            CustomerStatement s = inv.getArgument(1);
            if (s.getCustomerId() == 1) {
                throw new IllegalStateException("The customer statement could not be rendered");
            }
            return new RenderedStatement("x.pdf", new byte[]{1});
        }).when(pdf).renderOne(any(), any());

        StatementMailJobView view = service.start(request("", 1, null, 3, null), "mala");

        assertThat(item(view, 1).status()).isEqualTo("FAILED");
        assertThat(item(view, 1).error()).startsWith("Could not prepare the mail:");
        assertThat(item(view, 3).status()).isEqualTo("SENT");
        ArgumentCaptor<List<MimeMessage>> sent = ArgumentCaptor.forClass(List.class);
        verify(email).sendPrepared(sent.capture());
        assertThat(sent.getValue()).hasSize(1);
    }

    @Test
    @DisplayName("a refused login stops the run: what is left is failed, not retried batch after batch")
    void loginRefusedStopsRun() {
        when(email.sendPrepared(anyList()))
                .thenThrow(new IllegalStateException("Mail server refused the login: 535 Authentication failed"));

        StatementMailJobView view = service.start(request("", 1, null, 3, null), "mala");

        assertThat(view.status()).isEqualTo("FAILED");
        assertThat(view.error()).contains("535");
        assertThat(view.failed()).isEqualTo(2);
        assertThat(view.pending()).isZero();
        verify(email, times(1)).sendPrepared(anyList());
    }

    @Test
    @DisplayName("cancel stops before the next batch; customers not reached are marked cancelled, not sent")
    void cancel() {
        List<Runnable> parked = new ArrayList<>();
        service = new StatementMailJobService(statements, pdf, mail, email, 1, 0, parked::add);

        StatementMailJobView queued = service.start(request("", 1, null, 3, null), "mala");
        assertThat(queued.status()).isEqualTo("QUEUED");
        assertThat(service.active(1)).isPresent();

        service.cancel(queued.id());
        parked.get(0).run();                                          // the worker gets to it now

        StatementMailJobView view = service.get(queued.id()).orElseThrow();
        assertThat(view.status()).isEqualTo("CANCELLED");
        assertThat(view.cancelled()).isEqualTo(2);
        assertThat(view.sent()).isZero();
        verify(email, never()).sendPrepared(anyList());
    }

    @Test
    @DisplayName("one run per company at a time")
    void oneRunPerCompany() {
        service = new StatementMailJobService(statements, pdf, mail, email, 1, 0, r -> { });
        service.start(request("", 1, null), "mala");

        assertThatThrownBy(() -> service.start(request("", 3, null), "mala"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("already in progress");
    }

    @Test
    @DisplayName("no mail server configured is refused up front, before any statement is built")
    void notConfigured() {
        when(email.isConfigured()).thenReturn(false);

        assertThatThrownBy(() -> service.start(request("", 1, null), "mala"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("not configured");
        verify(statements, never()).build(any());
    }

    @Test
    @DisplayName("batches: five customers at two per connection is three connections")
    void batching() {
        when(statements.build(any())).thenReturn(result(
                customer(1, "A", List.of("a@x")), customer(2, "B", List.of("b@x")), customer(3, "C", List.of("c@x")),
                customer(4, "D", List.of("d@x")), customer(5, "E", List.of("e@x"))));

        StatementMailJobView view = service.start(request("", 1, null, 2, null, 3, null, 4, null, 5, null), "mala");

        assertThat(view.sent()).isEqualTo(5);
        verify(email, times(3)).sendPrepared(anyList());
    }
}
