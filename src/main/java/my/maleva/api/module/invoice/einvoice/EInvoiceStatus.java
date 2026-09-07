package my.maleva.api.module.invoice.einvoice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * LHDN's document-status vocabulary and the two conversions every caller of
 * MyInvois needs. Shared by the sale invoice and the sale credit note, which
 * submit different documents but read back the same statuses.
 */
public final class EInvoiceStatus {

    /** All local timestamps in this database are Malaysian wall-clock time. */
    public static final ZoneId MALAYSIA = ZoneId.of("Asia/Kuala_Lumpur");

    public static final String SUBMITTED = "Submitted";
    public static final String VALID = "Valid";
    public static final String INVALID = "Invalid";
    public static final String CANCELLED = "Cancelled";

    private EInvoiceStatus() {
    }

    /**
     * Whether LHDN's word on the document is final. Invalid and Cancelled are
     * final on their own; Valid is final only once its long id is stored,
     * because the long id is what the printed QR needs — a Valid row without
     * one is re-read on the next click.
     */
    public static boolean isFinal(String status, String longId) {
        if (INVALID.equalsIgnoreCase(status) || CANCELLED.equalsIgnoreCase(status)) {
            return true;
        }
        return VALID.equalsIgnoreCase(status) && !isBlank(longId);
    }

    /** LHDN's casing varies between endpoints ("Valid" vs "valid"); store one form. */
    public static String normalise(String status) {
        if (isBlank(status)) {
            return null;
        }
        String s = status.trim();
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    /**
     * LHDN's ISO-8601 instant as Malaysian wall-clock time, or null. Accepts
     * an offset ({@code ...+08:00}), a Z, or — as LHDN has been seen to send —
     * no zone at all, which is taken as UTC.
     */
    public static LocalDateTime parseInstant(String value) {
        if (isBlank(value)) {
            return null;
        }
        String v = value.trim();
        try {
            return OffsetDateTime.parse(v).atZoneSameInstant(MALAYSIA).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return Instant.parse(v).atZone(MALAYSIA).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return LocalDateTime.parse(v).atOffset(ZoneOffset.UTC).atZoneSameInstant(MALAYSIA).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
