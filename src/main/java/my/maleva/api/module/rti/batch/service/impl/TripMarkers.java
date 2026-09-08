package my.maleva.api.module.rti.batch.service.impl;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the trip a planner wrote in a planning row's REMARKS.
 *
 * <p>"1ST TRIP" and "2ND TRIP" are the two most-used remarks in the whole
 * system (918 and 893 rows in the 2026 data), and they mean something the rest
 * of the plan does not say: the same truck and driver run this load, come back,
 * and run another. Each of those runs is its own RTI.
 *
 * <p>Planners type it many ways — "1ST TRIP", "TRIP 3", "1STRIP", "2NDNTRIP",
 * "4RTH TRIP", "27TH ETD(2ND TRIP)" — so the number is read from wherever it
 * sits next to the word TRIP rather than from a fixed format. A remark with no
 * trip in it (a driver's name, "SG TO PTP", "MONDAY DELIVERY") leaves the row
 * unmarked, and unmarked rows stay together as they always have.
 */
final class TripMarkers {

    /** A number, then up to a few ordinal letters, then TRIP: "1ST TRIP", "1STRIP", "2NDNTRIP". */
    private static final Pattern NUMBER_THEN_TRIP = Pattern.compile("(\\d+)[A-Z]{0,4}\\s*TRIPS?\\b");

    /** TRIP, then the number: "TRIP 3". */
    private static final Pattern TRIP_THEN_NUMBER = Pattern.compile("\\bTRIPS?\\s*(\\d+)");

    private TripMarkers() {
    }

    /**
     * The trip number written in this remark, or 0 when none is.
     *
     * <p>The number has to sit beside the word TRIP. "27TH ETD(2ND TRIP)" is
     * trip 2, not trip 27 — the date in front is not a trip number.
     */
    static int tripNumber(String remark) {
        String text = normalise(remark);
        if (text.isEmpty() || !text.contains("TRIP")) {
            return 0;
        }
        Matcher beside = NUMBER_THEN_TRIP.matcher(text);
        if (beside.find()) {
            return parse(beside.group(1));
        }
        Matcher after = TRIP_THEN_NUMBER.matcher(text);
        if (after.find()) {
            return parse(after.group(1));
        }
        return 0;
    }

    /**
     * True when the planner wrote COMBINE on this row.
     *
     * <p>It means "this load rides with another one", and the row it rides with
     * is the one going the same way — same origin, same destination.
     */
    static boolean isCombine(String remark) {
        return normalise(remark).contains("COMBINE");
    }

    /** How a trip is named on screen. */
    static String label(int tripNumber) {
        return tripNumber > 0 ? "Trip " + tripNumber : "";
    }

    private static String normalise(String remark) {
        return remark == null ? "" : remark.toUpperCase(Locale.ROOT).trim();
    }

    private static int parse(String digits) {
        try {
            int value = Integer.parseInt(digits);
            // A plan has a handful of trips a day, never dozens. Anything larger
            // is a date or a quantity that happened to land next to the word.
            return value > 0 && value <= 20 ? value : 0;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
