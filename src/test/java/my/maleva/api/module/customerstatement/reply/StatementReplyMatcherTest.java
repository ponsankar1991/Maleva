package my.maleva.api.module.customerstatement.reply;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Which Message-IDs a reply points at, and what its subject was before the mail clients got to it. */
class StatementReplyMatcherTest {

    private static InboundMail mail(String inReplyTo, List<String> references, String subject) {
        return new InboundMail(7, "<abc@customer.example>", inReplyTo, references, "a@customer.example", "A",
                "account@maleva.com.my", null, subject, LocalDateTime.of(2026, 9, 11, 10, 0));
    }

    @Test
    @DisplayName("In-Reply-To first, then References newest to oldest, no duplicates, brackets kept")
    void candidateIds() {
        InboundMail m = mail("<statement-1@maleva.com.my>",
                List.of("<old@x>", "<statement-1@maleva.com.my>", "<reply-2@maleva.com.my>"), "Re: x");

        assertThat(StatementReplyMatcher.candidateIds(m))
                .containsExactly("<statement-1@maleva.com.my>", "<reply-2@maleva.com.my>", "<old@x>");
    }

    @Test
    @DisplayName("a bare id without brackets is normalised to the form the log stores")
    void bracketsAdded() {
        InboundMail m = mail("statement-1@maleva.com.my", List.of(), "Re: x");
        assertThat(StatementReplyMatcher.candidateIds(m)).containsExactly("<statement-1@maleva.com.my>");
    }

    @Test
    @DisplayName("no headers at all gives no candidates, not an exception")
    void noHeaders() {
        assertThat(StatementReplyMatcher.candidateIds(mail(null, null, null))).isEmpty();
    }

    @Test
    @DisplayName("Re:, RE :, Fwd:, FW:, AW: and [EXTERNAL] tags are stripped, however many and in any order")
    void normaliseSubject() {
        assertThat(StatementReplyMatcher.normaliseSubject("Re: Statement of Account & Payment Request - ACS"))
                .isEqualTo("Statement of Account & Payment Request - ACS");
        assertThat(StatementReplyMatcher.normaliseSubject("RE : [EXTERNAL] Fwd: Re: Statement of Account & Payment Request - ACS"))
                .isEqualTo("Statement of Account & Payment Request - ACS");
        assertThat(StatementReplyMatcher.normaliseSubject("AW: Friendly Payment Reminder – Outstanding Balance - X"))
                .isEqualTo("Friendly Payment Reminder – Outstanding Balance - X");
        assertThat(StatementReplyMatcher.normaliseSubject("  Statement of Account  ")).isEqualTo("Statement of Account");
        assertThat(StatementReplyMatcher.normaliseSubject(null)).isEmpty();
        // "Reply" or "Revised" are not prefixes
        assertThat(StatementReplyMatcher.normaliseSubject("Revised statement")).isEqualTo("Revised statement");
    }

    @Test
    @DisplayName("Re: is added once")
    void reSubject() {
        assertThat(StatementReplyMatcher.reSubject("Statement of Account - ACS")).isEqualTo("Re: Statement of Account - ACS");
        assertThat(StatementReplyMatcher.reSubject("RE: Statement of Account - ACS")).isEqualTo("RE: Statement of Account - ACS");
        assertThat(StatementReplyMatcher.reSubject(null)).isEqualTo("Re: ");
    }
}
