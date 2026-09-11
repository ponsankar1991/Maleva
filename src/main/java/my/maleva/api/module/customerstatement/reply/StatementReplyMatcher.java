package my.maleva.api.module.customerstatement.reply;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The pure part of deciding whether a mailbox mail answers a statement we
 * sent: which Message-IDs it points at, and what its subject was before the
 * mail clients prefixed it.
 */
public final class StatementReplyMatcher {

    /** "Re:", "RE :", "Fwd:", "FW:", "AW:", "SV:" … any number of times, in any order. */
    private static final Pattern PREFIX = Pattern.compile("^(?:(?:re|fw|fwd|aw|sv|wg|tr|vs)\\s*:\\s*)+", Pattern.CASE_INSENSITIVE);
    /** "[EXTERNAL]"-style tags some relays prepend. */
    private static final Pattern TAG = Pattern.compile("^\\[[^\\]]{1,40}\\]\\s*");

    private StatementReplyMatcher() {
    }

    /**
     * The Message-IDs a reply may point at, the most specific first:
     * In-Reply-To, then References newest to oldest. Trimmed, de-duplicated,
     * angle brackets kept — that is how the log stores ours.
     */
    public static List<String> candidateIds(InboundMail mail) {
        Set<String> ids = new LinkedHashSet<>();
        add(ids, mail.inReplyTo());
        if (mail.references() != null) {
            List<String> refs = new ArrayList<>(mail.references());
            for (int i = refs.size() - 1; i >= 0; i--) {
                add(ids, refs.get(i));
            }
        }
        return new ArrayList<>(ids);
    }

    private static void add(Set<String> ids, String raw) {
        if (raw == null) {
            return;
        }
        for (String part : raw.trim().split("\\s+")) {
            String id = part.trim();
            if (!id.isEmpty() && id.contains("@")) {
                ids.add(id.startsWith("<") ? id : "<" + id.replaceAll("^<|>$", "") + ">");
            }
        }
    }

    /**
     * The subject with reply and forward prefixes and relay tags stripped, so
     * "RE: [EXTERNAL] Re: Statement of Account & Payment Request - ACS" is
     * "Statement of Account & Payment Request - ACS" — what we sent.
     */
    public static String normaliseSubject(String subject) {
        if (subject == null) {
            return "";
        }
        String s = subject.trim();
        String before;
        do {
            before = s;
            s = PREFIX.matcher(s).replaceFirst("");
            s = TAG.matcher(s).replaceFirst("");
            s = s.trim();
        } while (!s.equals(before));
        return s;
    }

    /** "Re: " + subject, unless it already starts that way. */
    public static String reSubject(String subject) {
        String s = subject == null ? "" : subject.trim();
        return s.regionMatches(true, 0, "re:", 0, 3) ? s : "Re: " + s;
    }
}
