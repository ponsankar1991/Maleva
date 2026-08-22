package my.maleva.api.module.gps.service;

/**
 * How a fuel entry came to be linked to a GPS filling.
 *
 * Stored in {@code FuelEntry.FuelFillingMatchType} alongside
 * {@code FuelFillingRefId}.
 */
public final class FuelMatchType {

    /** Chosen by {@link FuelGpsMatchService}. May be recomputed. */
    public static final String AUTO = "AUTO";

    /** Chosen by a person on the screen. Never overwritten by the matcher. */
    public static final String MANUAL = "MANUAL";

    private FuelMatchType() {
    }

    public static boolean isManual(String value) {
        return MANUAL.equalsIgnoreCase(value);
    }
}
