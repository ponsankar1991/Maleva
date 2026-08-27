package my.maleva.api.integration.qne;

import org.springframework.web.util.HtmlUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Value formatting shared by the module payload builders. Each rule here is a
 * legacy wire behaviour, not a style choice — QNE already holds years of data
 * written in these shapes, and a formatting drift stops codes from matching.
 */
public final class QnePayloads {

    /**
     * Json.NET serialised .NET DateTime as {@code yyyy-MM-ddTHH:mm:ss}.
     * {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} would drop {@code :ss}
     * when seconds are zero, so the pattern is pinned.
     */
    private static final DateTimeFormatter QNE_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private QnePayloads() {
    }

    public static String date(LocalDateTime value) {
        return value == null ? null : QNE_DATE.format(value);
    }

    public static String date(LocalDate value) {
        return value == null ? null : QNE_DATE.format(value.atStartOfDay());
    }

    /**
     * Legacy address mapping ({@code commonfunctions.SplitInParts}): a local
     * Address1 longer than 100 characters is sliced blind into fixed 100-char
     * chunks across QNE's Address1–4; anything past the fourth chunk is
     * discarded. At 100 characters or less it goes to Address1 untouched.
     */
    public static String[] addressChunks(String address1) {
        String[] parts = new String[4];
        if (address1 == null || address1.length() <= 100) {
            parts[0] = address1;
            return parts;
        }
        for (int i = 0; i < parts.length && i * 100 < address1.length(); i++) {
            parts[i] = address1.substring(i * 100, Math.min((i + 1) * 100, address1.length()));
        }
        return parts;
    }

    /**
     * Legacy applied {@code HttpUtility.HtmlEncode} to every stock code on
     * invoice and credit-note lines (and nothing else). It is meaningless in a
     * JSON body, but QNE's stock records were created through the same
     * encoding — a code containing {@code &} only matches its QNE counterpart
     * when encoded the same way.
     */
    public static String htmlEncode(String value) {
        return value == null ? null : HtmlUtils.htmlEscape(value);
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    public static double d(Number value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    /** Splits a code list so one OData {@code in [...]} filter keeps a sane URL length. */
    public static <T> List<List<T>> chunks(List<T> values, int size) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < values.size(); i += size) {
            result.add(values.subList(i, Math.min(i + size, values.size())));
        }
        return result;
    }
}
