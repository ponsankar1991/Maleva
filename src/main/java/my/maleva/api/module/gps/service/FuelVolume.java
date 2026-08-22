package my.maleva.api.module.gps.service;

import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the litre value out of a Wialon volume string such as {@code "274.51 l"}.
 *
 * The legacy code did this in SQL as
 * {@code CAST(REPLACE(FF.filled, ' l', '') AS FLOAT)}, which throws
 * "Error converting data type nvarchar to float" the moment a row holds
 * anything else. One such row exists in production (the Wialon "-----"
 * placeholder stored as data), and it makes the whole EditFuelEntry query fail
 * for that truck and date. Parsing here returns empty instead of throwing.
 */
public final class FuelVolume {

    /** First number in the string, with an optional decimal part. */
    private static final Pattern NUMBER = Pattern.compile("-?\\d+(?:\\.\\d+)?");

    private FuelVolume() {
    }

    /**
     * Parses the litres out of a rendered volume.
     *
     * @param value for example "274.51 l", "239 l", "-----" or null
     * @return the litres, or empty when the string holds no number
     */
    public static OptionalDouble parseLitres(String value) {
        if (value == null || value.isBlank()) {
            return OptionalDouble.empty();
        }
        Matcher matcher = NUMBER.matcher(value);
        if (!matcher.find()) {
            return OptionalDouble.empty();
        }
        try {
            return OptionalDouble.of(Double.parseDouble(matcher.group()));
        } catch (NumberFormatException ex) {
            return OptionalDouble.empty();
        }
    }
}
