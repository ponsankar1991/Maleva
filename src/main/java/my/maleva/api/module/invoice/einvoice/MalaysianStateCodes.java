package my.maleva.api.module.invoice.einvoice;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Maps a customer's free-text state to LHDN's two-digit state code.
 *
 * <p>The customer master stores the state as typed by a clerk ("Selangor",
 * "PULAU PINANG", "W.P. Kuala Lumpur"). LHDN wants {@code 10}, {@code 07},
 * {@code 14}. The legacy map recognised only the sixteen canonical spellings;
 * the aliases below were added after real customer rows failed to push. An
 * unknown Malaysian state still maps to nothing — the validator then names
 * the customer so the master data can be fixed, which is safer than guessing
 * a code the government will file the invoice under.
 *
 * <p>For a customer outside Malaysia the state is sent as typed; LHDN accepts
 * free text there and has done so for every foreign document sent so far.
 */
public final class MalaysianStateCodes {

    /** Country codes that mean Malaysia, as stored in CountryMaster.Code or typed free-hand. */
    private static final Set<String> MALAYSIA = Set.of("MYS", "MY", "MALAYSIA");

    private static final Map<String, String> CODES = Map.ofEntries(
            Map.entry("JOHOR", "01"),
            Map.entry("KEDAH", "02"),
            Map.entry("KELANTAN", "03"),
            Map.entry("MELAKA", "04"),
            Map.entry("MALACCA", "04"),
            Map.entry("NEGERI SEMBILAN", "05"),
            Map.entry("N. SEMBILAN", "05"),
            Map.entry("PAHANG", "06"),
            Map.entry("PENANG", "07"),
            Map.entry("PULAU PINANG", "07"),
            Map.entry("PERAK", "08"),
            Map.entry("PERLIS", "09"),
            Map.entry("SELANGOR", "10"),
            Map.entry("TERENGGANU", "11"),
            Map.entry("SABAH", "12"),
            Map.entry("SARAWAK", "13"),
            Map.entry("KUALA LUMPUR", "14"),
            Map.entry("W.P. KUALA LUMPUR", "14"),
            Map.entry("WP KUALA LUMPUR", "14"),
            Map.entry("WILAYAH PERSEKUTUAN KUALA LUMPUR", "14"),
            Map.entry("LABUAN", "15"),
            Map.entry("W.P. LABUAN", "15"),
            Map.entry("WILAYAH PERSEKUTUAN LABUAN", "15"),
            Map.entry("PUTRAJAYA", "16"),
            Map.entry("W.P. PUTRAJAYA", "16"),
            Map.entry("WILAYAH PERSEKUTUAN PUTRAJAYA", "16"));
    // LHDN also defines 17 = "Not Applicable". It is deliberately not mapped
    // for Malaysian addresses until a preprod submission has confirmed LHDN
    // accepts it; until then such a customer is named so the state can be set.

    private MalaysianStateCodes() {
    }

    public static boolean isMalaysia(String countryCode) {
        return countryCode != null && MALAYSIA.contains(countryCode.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * The value to send as {@code CountrySubentityCode}.
     *
     * @return the LHDN code for a recognised Malaysian state; the raw text for
     *         a foreign address; empty for a blank state or an unrecognised
     *         Malaysian one
     */
    public static Optional<String> subentityCode(String countryCode, String state) {
        if (state == null || state.isBlank()) {
            return Optional.empty();
        }
        String trimmed = state.trim();
        if (!isMalaysia(countryCode)) {
            return Optional.of(trimmed);
        }
        String normalised = trimmed.toUpperCase(Locale.ROOT).replaceAll("\\s+", " ");
        return Optional.ofNullable(CODES.get(normalised));
    }
}
