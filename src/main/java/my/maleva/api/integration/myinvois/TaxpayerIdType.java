package my.maleva.api.integration.myinvois;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Which kind of identifier a registration number is, in LHDN's terms — the
 * {@code idType} that has to travel beside {@code idValue} on a taxpayer
 * lookup. The port of legacy {@code EInvoiceapilist.GetIdType}.
 *
 * <p>Order matters and is copied exactly. A 12-digit number starting 19 or 20
 * is a business registration number, and only a 12-digit number that is
 * <em>not</em> one of those is tested as an NRIC — so 199801012345 is a BRN
 * even though its first six digits also parse as a date. Getting that order
 * wrong sends companies to LHDN labelled as people.
 */
public enum TaxpayerIdType {
    BRN, NRIC, PASSPORT, ARMY, UNKNOWN;

    /** New-format business registration number: 12 digits beginning 19 or 20. */
    private static final Pattern BRN_PATTERN = Pattern.compile("^(19|20)\\d{10}$");
    private static final Pattern TWELVE_DIGITS = Pattern.compile("^\\d{12}$");
    /** One letter then 7-9 digits. */
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[A-Z]\\d{7,9}$");
    private static final Pattern ARMY_PATTERN = Pattern.compile("^\\d{10,12}$");

    private static final DateTimeFormatter YYMMDD =
            DateTimeFormatter.ofPattern("yyMMdd", Locale.ROOT);

    public static TaxpayerIdType of(String idValue) {
        if (idValue == null || idValue.isBlank()) {
            return UNKNOWN;
        }

        String cleaned = idValue.trim().toUpperCase(Locale.ROOT);
        String digitsOnly = cleaned.replace("-", "");

        if (BRN_PATTERN.matcher(digitsOnly).matches()) {
            return BRN;
        }
        if (TWELVE_DIGITS.matcher(digitsOnly).matches() && isBirthDate(digitsOnly.substring(0, 6))) {
            return NRIC;
        }
        // Against the value as typed, dashes included: a passport number has none.
        if (PASSPORT_PATTERN.matcher(cleaned).matches()) {
            return PASSPORT;
        }
        if (ARMY_PATTERN.matcher(digitsOnly).matches()) {
            return ARMY;
        }
        return UNKNOWN;
    }

    /** An NRIC opens with the holder's birth date, so the first six digits must parse as one. */
    private static boolean isBirthDate(String yymmdd) {
        try {
            YYMMDD.parse(yymmdd);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
