package my.maleva.api.module.customerstatement.reply;

import my.maleva.api.common.config.FileUploadConfig;
import my.maleva.api.module.customerstatement.mail.StatementMailLogRepository;
import my.maleva.api.module.customerstatement.mail.StatementMailLogRepository.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Which inbox mail is a statement reply: by thread header, by subject, or —
 * for the customer writing from a one-off address — by an address we sent to.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StatementReplyImporterMatchTest {

    @Mock private MailboxReader reader;
    @Mock private StatementReplyRepository replies;
    @Mock private StatementMailLogRepository logs;
    @Mock private FileUploadConfig uploads;

    private StatementReplyImporter importer;

    private static final Row MTT = new Row(900L, 1, 44, "MTT SHIPPING SDN BHD",
            "mttsmgrfin@mttshipmanager.com.my, karthick123svks@gmail.com", "receivable@maleva.com.my",
            "Statement of Account & Payment Request - MTT SHIPPING SDN BHD", "", "CustomerStatement_MTT.pdf",
            "SENT", null, "mages", LocalDateTime.of(2026, 9, 11, 16, 27), "STATEMENT",
            "<statement-9f3c@maleva.com.my>", null, null);

    private static InboundMail mail(String inReplyTo, String subject, String from) {
        return new InboundMail(1, "<x@y>", inReplyTo, List.of(), from, "Someone", "account@maleva.com.my", null, subject,
                LocalDateTime.of(2026, 9, 11, 17, 34));
    }

    @BeforeEach
    void setUp() {
        importer = new StatementReplyImporter(Optional.of(reader), replies, logs, uploads, Optional.empty(), "INBOX", 7, 200);
        when(logs.findByMessageId(anyString())).thenReturn(Optional.empty());
        when(logs.findLatestBySubject(anyString())).thenReturn(Optional.empty());
        when(logs.findLatestBySentTo(anyString())).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("1. the thread header names our Message-ID: matched whatever address it came from")
    void byHeader() {
        when(logs.findByMessageId("<statement-9f3c@maleva.com.my>")).thenReturn(Optional.of(MTT));

        assertThat(importer.match(mail("<statement-9f3c@maleva.com.my>", "Anything at all", "temp-user@gmail.com")))
                .contains(MTT);
        verify(logs, never()).findLatestBySubject(anyString());
    }

    @Test
    @DisplayName("2. no header: the subject with its Re: stripped is one we sent")
    void bySubject() {
        when(logs.findLatestBySubject("Statement of Account & Payment Request - MTT SHIPPING SDN BHD")).thenReturn(Optional.of(MTT));

        assertThat(importer.match(mail(null, "RE: Statement of Account & Payment Request - MTT SHIPPING SDN BHD", "temp-user@gmail.com")))
                .contains(MTT);
    }

    @Test
    @DisplayName("3. neither: a reply-style mail from an address we sent a statement to belongs to that customer")
    void bySentToAddress() {
        when(logs.findLatestBySentTo("karthick123svks@gmail.com")).thenReturn(Optional.of(MTT));

        assertThat(importer.match(mail(null, "Re: payment done", "karthick123svks@gmail.com"))).contains(MTT);
    }

    @Test
    @DisplayName("a mail with no reply prefix is never taken by sender alone")
    void freshMailFromKnownAddressIsNotARepl() {
        when(logs.findLatestBySentTo("karthick123svks@gmail.com")).thenReturn(Optional.of(MTT));

        assertThat(importer.match(mail(null, "Newsletter September", "karthick123svks@gmail.com"))).isEmpty();
        verify(logs, never()).findLatestBySentTo(anyString());
    }

    @Test
    @DisplayName("unrelated inbox mail matches nothing")
    void unrelated() {
        assertThat(importer.match(mail(null, "Re: your quotation", "stranger@example.com"))).isEmpty();
    }
}
