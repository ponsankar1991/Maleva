package my.maleva.api.module.common.service;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import my.maleva.api.module.common.service.EmailService.EmailAttachment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** prepare + one-connection send: what the relay refused, and what only looked refused. */
@ExtendWith(MockitoExtension.class)
class EmailServiceBatchTest {

    @Mock private JavaMailSender sender;
    private EmailService service;

    @BeforeEach
    void setUp() {
        // lenient: the not-configured test never asks for a message
        lenient().when(sender.createMimeMessage()).thenAnswer(inv -> new MimeMessage((Session) null));
        service = new EmailService(sender, null);
        ReflectionTestUtils.setField(service, "fromEmail", "account@maleva.com.my");
        ReflectionTestUtils.setField(service, "fromName", "Maleva Accounts");
    }

    private MimeMessage prepared(String to) {
        return service.prepareHtmlMail(List.of(to), List.of("receivable@maleva.com.my"), "Subject " + to,
                "<p>body</p>", List.of(new EmailAttachment("s.pdf", new byte[]{1, 2}, "application/pdf")));
    }

    @Test
    @DisplayName("prepare builds the addressed message without sending it")
    void prepareDoesNotSend() throws Exception {
        MimeMessage m = prepared("a@x.example");

        assertThat(m.getSubject()).isEqualTo("Subject a@x.example");
        assertThat(m.getRecipients(Message.RecipientType.TO)).hasSize(1);
        assertThat(m.getRecipients(Message.RecipientType.CC)).hasSize(1);
        assertThat(m.getFrom()[0].toString()).contains("account@maleva.com.my");
        verify(sender, org.mockito.Mockito.never()).send(any(MimeMessage[].class));
    }

    @Test
    @DisplayName("all accepted: one send call for the whole batch, nothing refused")
    void allAccepted() {
        List<MimeMessage> batch = List.of(prepared("a@x"), prepared("b@x"), prepared("c@x"));

        Map<MimeMessage, String> refused = service.sendPrepared(batch);

        assertThat(refused).isEmpty();
        verify(sender).send(any(MimeMessage[].class));
    }

    @Test
    @DisplayName("a refused message is named with the relay's reason; the others count as sent")
    void partialRefusal() {
        MimeMessage a = prepared("a@x"), b = prepared("b@x"), c = prepared("c@x");
        Map<Object, Exception> failed = new HashMap<>();
        failed.put(b, new jakarta.mail.SendFailedException("550 5.1.1 User unknown"));
        doThrow(new MailSendException(failed)).when(sender).send(any(MimeMessage[].class));

        Map<MimeMessage, String> refused = service.sendPrepared(List.of(a, b, c));

        assertThat(refused).containsOnlyKeys(b);
        assertThat(refused.get(b)).isEqualTo("550 5.1.1 User unknown");
    }

    @Test
    @DisplayName("a connection that fails to close after sending is not a refused mail")
    void closeFailureIsNotARefusal() {
        doThrow(new MailSendException("Failed to close server connection after message sending",
                new RuntimeException("socket closed")))
                .when(sender).send(any(MimeMessage[].class));

        assertThat(service.sendPrepared(List.of(prepared("a@x")))).isEmpty();
    }

    @Test
    @DisplayName("a refused login is an exception: nothing went, and the next batch would fail the same way")
    void loginRefused() {
        doThrow(new MailAuthenticationException("535 5.7.8 Authentication failed"))
                .when(sender).send(any(MimeMessage[].class));

        assertThatThrownBy(() -> service.sendPrepared(List.of(prepared("a@x"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("refused the login")
                .hasMessageContaining("535");
    }

    @Test
    @DisplayName("no mail server configured")
    void notConfigured() {
        EmailService unconfigured = new EmailService(null, null);
        assertThat(unconfigured.isConfigured()).isFalse();
        assertThatThrownBy(() -> unconfigured.sendPrepared(List.of(new MimeMessage((Session) null))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not configured");
    }
}
