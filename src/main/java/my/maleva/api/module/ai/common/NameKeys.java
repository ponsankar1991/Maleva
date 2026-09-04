package my.maleva.api.module.ai.common;

import java.util.Locale;
import java.util.Set;

/**
 * Comparison keys for names that are typed by hand across screens: drivers
 * ("AHMAD BIN ALI", "Ahmad Ali-0123456789") and places ("Westport",
 * "WEST PORT, KLANG").
 */
public final class NameKeys {

    private static final Set<String> NAME_CONNECTORS = Set.of("BIN", "BINTI", "AL", "AP", "B", "BT");

    private NameKeys() {
    }

    /** Upper-case tokens without patronymic connectors or a trailing "-mobile" suffix. */
    public static String driver(String value) {
        if (value == null) {
            return "";
        }
        String text = value.trim();
        int dash = text.lastIndexOf('-');
        if (dash > 0 && text.substring(dash + 1).trim().matches("[0-9 +]+")) {
            text = text.substring(0, dash);
        }
        String upper = text.toUpperCase(Locale.ROOT)
                .replaceAll("\\bA\\s*/\\s*[LP]\\b", " ") // "A/L" (anak lelaki) and "A/P" (anak perempuan)
                .replaceAll("[^A-Z0-9 ]", " ");
        StringBuilder sb = new StringBuilder();
        for (String token : upper.trim().split("\\s+")) {
            if (token.isEmpty() || NAME_CONNECTORS.contains(token)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(token);
        }
        return sb.toString();
    }

    /** Short forms planners type for the same place. Keys and values are already compacted. */
    private static final java.util.Map<String, String> PLACE_ALIASES = java.util.Map.ofEntries(
            java.util.Map.entry("SG", "SINGAPORE"),
            java.util.Map.entry("SPORE", "SINGAPORE"),
            java.util.Map.entry("WP", "WESTPORT"),
            java.util.Map.entry("WESTPORTS", "WESTPORT"),
            java.util.Map.entry("NP", "NORTHPORT"),
            java.util.Map.entry("NORTHPORTS", "NORTHPORT"),
            java.util.Map.entry("SP", "SOUTHPORT"),
            java.util.Map.entry("PG", "PASIRGUDANG"),
            java.util.Map.entry("JB", "JOHORBAHRU"),
            java.util.Map.entry("JOHOR", "JOHORBAHRU"),
            java.util.Map.entry("PORTKLANG", "KLANG"),
            java.util.Map.entry("PELABUHANKLANG", "KLANG"),
            java.util.Map.entry("TANJUNGPELEPAS", "PTP"),
            java.util.Map.entry("PORTOFTANJUNGPELEPAS", "PTP"));

    /** Upper-case alphanumerics with short forms expanded, so "West Port", "WP" and "WESTPORT" compare equal. */
    public static String place(String value) {
        if (value == null) {
            return "";
        }
        String key = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        return PLACE_ALIASES.getOrDefault(key, key);
    }
}
