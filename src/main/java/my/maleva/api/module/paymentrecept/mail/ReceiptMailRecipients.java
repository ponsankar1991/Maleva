package my.maleva.api.module.paymentrecept.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Recipient handling for the receipt mail — the port of the legacy
 * {@code SplitEmailIds} plus the screen-side {@code MailSplit}/{@code MailPattern}
 * checks, done once on the server so a hand-edited list cannot slip past.
 *
 * <p>Addresses may arrive as a list or as one string separated by commas or
 * semicolons; blanks are dropped, duplicates removed case-insensitively in
 * first-seen order, and anything that is not shaped like an address is
 * reported back rather than silently dropped.
 */
public final class ReceiptMailRecipients {

    /** Same shape test the legacy screen used: one @, a dot in the domain, no separators. */
    static final Pattern ADDRESS = Pattern.compile("^[^\\s@,;]+@[^\\s@,;]+\\.[^\\s@,;]{2,}$");

    private ReceiptMailRecipients() {
    }

    /** Splits and trims; keeps order, drops blanks and case-insensitive duplicates. */
    public static List<String> split(Collection<String> values) {
        Set<String> seen = new LinkedHashSet<>();
        List<String> out = new ArrayList<>();
        if (values == null) {
            return out;
        }
        for (String value : values) {
            if (value == null) {
                continue;
            }
            for (String piece : value.split("[,;]")) {
                String one = piece.trim();
                if (one.isEmpty()) {
                    continue;
                }
                if (seen.add(one.toLowerCase(Locale.ROOT))) {
                    out.add(one);
                }
            }
        }
        return out;
    }

    public static List<String> split(String value) {
        return split(value == null ? List.of() : List.of(value));
    }

    public static boolean isValid(String address) {
        return address != null && ADDRESS.matcher(address.trim()).matches();
    }

    /** The entries of {@code addresses} that are not shaped like an address. */
    public static List<String> invalid(List<String> addresses) {
        return addresses.stream().filter(a -> !isValid(a)).toList();
    }
}
