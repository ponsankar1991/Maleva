package my.maleva.api.module.fleet.entity;

import my.maleva.api.common.exception.InvalidRequestException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Whether a truck is on the road, being repaired, or gone.
 *
 * <p>{@code TruckMaster.Active} only says yes or no, so a truck in the workshop
 * stayed fully bookable and somebody had to remember. This says why a truck is
 * not available, without hiding it from its own history.
 *
 * <p>Stored as the enum name in {@code TruckMaster.TruckStatus}; rows written
 * before the column existed read {@link #ACTIVE}.
 *
 * <p>Adding a status means adding it here and to TRUCK_STATUSES in the front
 * end's truck master types. Keep the two in step.
 */
public enum TruckStatus {

    /** On the road and bookable. */
    ACTIVE("Active"),

    /**
     * Being repaired. Not offered for booking until
     * {@code TruckMaster.WorkshopUntil} has passed; an empty date means until
     * further notice.
     */
    WORKSHOP("In workshop"),

    /** Sold or scrapped. Never offered again, but its history stays readable. */
    SOLD("Sold");

    private final String label;

    TruckStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static List<String> codes() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }

    /**
     * Resolves a stored or posted value. Blank means {@link #ACTIVE} - what a
     * truck is when nobody has said otherwise.
     *
     * @throws InvalidRequestException if it is not one of the three
     */
    public static TruckStatus fromCode(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVE;
        }
        String wanted = value.trim();
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(wanted))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(
                        "Truck status must be one of " + String.join(", ", codes())));
    }

    /**
     * Can a truck in this state be booked for one particular day?
     *
     * <p>Asked per day, because a truck due back on Friday is bookable from
     * Friday without anyone having to switch it back. A WORKSHOP truck with no
     * return date is out until someone sets it ACTIVE again.
     *
     * @param workshopUntil the day it is expected back, or null for "no date"
     * @param day           the day being booked
     */
    public boolean bookableOn(LocalDate workshopUntil, LocalDate day) {
        return switch (this) {
            case ACTIVE -> true;
            case SOLD -> false;
            case WORKSHOP -> workshopUntil != null && day != null && day.isAfter(workshopUntil);
        };
    }
}
