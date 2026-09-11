package my.maleva.api.module.customerstatement.reply;

import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/** Reading a real (in-memory) MIME mail the way a customer's client builds one. */
class MailboxReaderParseTest {

    private static MimeMessage customerReply() throws Exception {
        MimeMessage m = new MimeMessage(Session.getInstance(new Properties()));
        m.setFrom(new InternetAddress("cayden@acs.example", "Cayden Lim"));
        m.setRecipient(Message.RecipientType.TO, new InternetAddress("account@maleva.com.my"));
        m.setRecipients(Message.RecipientType.CC, InternetAddress.parse("receivable@maleva.com.my, titus@acs.example"));
        m.setSubject("RE: Statement of Account & Payment Request - ACS FREIGHT SERVICES PTE LTD");
        m.setHeader("Message-ID", "<CAF123@mail.acs.example>");
        m.setHeader("In-Reply-To", "<statement-9f3c@maleva.com.my>");
        m.setHeader("References", "<statement-9f3c@maleva.com.my>");
        m.setSentDate(new Date());

        MimeMultipart alternative = new MimeMultipart("alternative");
        MimeBodyPart text = new MimeBodyPart();
        text.setText("Hi,\nPayment for INV000043580 was made on 10 Sep.\nRegards", StandardCharsets.UTF_8.name());
        MimeBodyPart html = new MimeBodyPart();
        html.setContent("<p>Hi,</p><p>Payment for <b>INV000043580</b> was made on 10 Sep.</p>", "text/html; charset=UTF-8");
        alternative.addBodyPart(text);
        alternative.addBodyPart(html);
        MimeBodyPart body = new MimeBodyPart();
        body.setContent(alternative);

        MimeBodyPart attachment = new MimeBodyPart();
        attachment.setDataHandler(new DataHandler(new ByteArrayDataSource("%PDF-1.4 remittance".getBytes(), "application/pdf")));
        attachment.setFileName("remittance advice.pdf");
        attachment.setDisposition(MimeBodyPart.ATTACHMENT);

        MimeMultipart mixed = new MimeMultipart("mixed");
        mixed.addBodyPart(body);
        mixed.addBodyPart(attachment);
        m.setContent(mixed);
        m.saveChanges();
        // saveChanges regenerates the Message-ID; put ours back as a real mail would carry it
        m.setHeader("Message-ID", "<CAF123@mail.acs.example>");
        return m;
    }

    @Test
    @DisplayName("headers: threading ids, sender, recipients, subject")
    void headers() throws Exception {
        InboundMail head = MailboxReader.headers(customerReply(), 42);

        assertThat(head.uid()).isEqualTo(42);
        assertThat(head.messageId()).isEqualTo("<CAF123@mail.acs.example>");
        assertThat(head.inReplyTo()).isEqualTo("<statement-9f3c@maleva.com.my>");
        assertThat(head.references()).containsExactly("<statement-9f3c@maleva.com.my>");
        assertThat(head.fromAddress()).isEqualTo("cayden@acs.example");
        assertThat(head.fromName()).isEqualTo("Cayden Lim");
        assertThat(head.to()).isEqualTo("account@maleva.com.my");
        assertThat(head.cc()).isEqualTo("receivable@maleva.com.my, titus@acs.example");
        assertThat(head.subject()).startsWith("RE: Statement of Account");
        assertThat(head.receivedAt()).isNotNull();
    }

    @Test
    @DisplayName("body: the plain text, the HTML, and the attachment with its bytes")
    void body() throws Exception {
        InboundMail.Body body = MailboxReader.body(customerReply());

        assertThat(body.text()).contains("Payment for INV000043580 was made on 10 Sep.");
        assertThat(body.html()).contains("<b>INV000043580</b>");
        assertThat(body.attachments()).hasSize(1);
        InboundMail.Attachment a = body.attachments().get(0);
        assertThat(a.fileName()).isEqualTo("remittance advice.pdf");
        assertThat(a.contentType()).isEqualTo("application/pdf");
        assertThat(new String(a.content())).startsWith("%PDF-1.4");
    }

    @Test
    @DisplayName("HTML-only replies get a readable text fallback")
    void htmlToText() {
        String text = StatementReplyImporter.htmlToText(
                "<html><body><style>p{}</style><p>Hi,</p><p>Paid &amp; done on <b>10 Sep</b>.<br>Regards</p></body></html>");
        assertThat(text).startsWith("Hi,").contains("Paid & done on 10 Sep").endsWith("Regards");
        assertThat(text).doesNotContain("<").doesNotContain("p{}").doesNotContain("&amp;");
    }
}
