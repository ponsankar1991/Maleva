package my.maleva.api.module.ai.common;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.integration.llm.LlmAttachment;
import my.maleva.api.module.master.entity.PaymentTermsMaster;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helpers every supplier-document extractor needs: upload validation, the
 * date and number formats Malaysian documents use, payment-term parsing and
 * money rounding. Pure functions, no Spring wiring.
 */
public final class ExtractionSupport {

    public static final long MAX_BYTES = 10L * 1024 * 1024;

    private static final Map<String, String> MEDIA_BY_EXTENSION = Map.of(
            "pdf", LlmAttachment.APPLICATION_PDF,
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "webp", "image/webp",
            "gif", "image/gif");

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd/MM/yy"));

    private static final Pattern DAYS = Pattern.compile("(\\d{1,3})\\s*(?:DAYS?|D\\b)", Pattern.CASE_INSENSITIVE);
    private static final Pattern NET = Pattern.compile("NET\\s*(\\d{1,3})", Pattern.CASE_INSENSITIVE);

    private ExtractionSupport() {
    }

    /** Validates the upload (type, size) and wraps it for the LLM layer. */
    public static LlmAttachment toAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("Upload a PDF or image of the document");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new InvalidRequestException("The file is larger than 10 MB");
        }
        String name = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                ? "document" : file.getOriginalFilename();
        String extension = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        String mediaType = MEDIA_BY_EXTENSION.get(extension);
        if (mediaType == null && file.getContentType() != null) {
            String declared = file.getContentType().toLowerCase(Locale.ROOT);
            if (MEDIA_BY_EXTENSION.containsValue(declared)) {
                mediaType = declared;
            } else if (declared.equals("image/jpg")) {
                mediaType = "image/jpeg";
            }
        }
        if (mediaType == null) {
            throw new InvalidRequestException("Upload a PDF, PNG, JPG, WEBP or GIF file (got '" + name + "')");
        }
        try {
            return new LlmAttachment(name, mediaType, file.getBytes());
        } catch (IOException ex) {
            throw new InvalidRequestException("Could not read the uploaded file: " + ex.getMessage(), ex);
        }
    }

    /** Parses the layouts Malaysian documents use; adds a warning and returns null when none fit. */
    public static LocalDate parseDate(String raw, String label, List<String> warnings) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        String candidate = value.length() > 10 && value.charAt(10) == 'T' ? value.substring(0, 10) : value;
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(candidate, format);
            } catch (DateTimeParseException ignored) {
                // try the next layout
            }
        }
        warnings.add("Could not read the " + label + " '" + value + "'");
        return null;
    }

    /** "Net 30", "30 days", "cash" or an exact terms name, resolved to a PaymentTermsMaster id. */
    public static Integer termsFromText(String text, List<PaymentTermsMaster> terms) {
        String value = trimToNull(text);
        if (value == null || terms.isEmpty()) {
            return null;
        }
        Integer days = null;
        Matcher net = NET.matcher(value);
        Matcher plain = DAYS.matcher(value);
        if (net.find()) {
            days = Integer.parseInt(net.group(1));
        } else if (plain.find()) {
            days = Integer.parseInt(plain.group(1));
        } else if (value.toUpperCase(Locale.ROOT).contains("CASH") || value.toUpperCase(Locale.ROOT).contains("COD")) {
            days = 0;
        }
        if (days == null) {
            String upper = value.toUpperCase(Locale.ROOT);
            for (PaymentTermsMaster term : terms) {
                if (term.getTermsName() != null && upper.equals(term.getTermsName().trim().toUpperCase(Locale.ROOT))) {
                    return term.getId();
                }
            }
            return null;
        }
        for (PaymentTermsMaster term : terms) {
            if (term.getTDays() != null && term.getTDays().equals(days)) {
                return term.getId();
            }
        }
        return null;
    }

    public static Integer daysFor(Integer paymentTermsId, List<PaymentTermsMaster> terms) {
        if (paymentTermsId == null) {
            return null;
        }
        for (PaymentTermsMaster term : terms) {
            if (paymentTermsId.equals(term.getId())) {
                return term.getTDays();
            }
        }
        return null;
    }

    /** 3-letter code; "RM" and blank mean MYR. */
    public static String currency(String raw) {
        String value = trimToNull(raw);
        if (value == null) {
            return "MYR";
        }
        String upper = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (upper.equals("RM") || upper.isEmpty()) {
            return "MYR";
        }
        return upper.length() > 3 ? upper.substring(0, 3) : upper;
    }

    public static BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static String format(LocalDate date) {
        return date == null ? null : date.toString();
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() || trimmed.equalsIgnoreCase("null") ? null : trimmed;
    }

    /** Upper-case alphanumerics only: "JKA 1234" and "jka-1234" compare equal. */
    public static String compact(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }
}
