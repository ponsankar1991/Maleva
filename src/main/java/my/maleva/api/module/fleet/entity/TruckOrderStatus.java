package my.maleva.api.module.fleet.entity;

import my.maleva.api.common.exception.InvalidRequestException;

import java.util.Arrays;
import java.util.List;

/**
 * Where a truck order stands.
 *
 * <p>PENDING -> CONFIRMED -> IN_TRANSIT -> DELIVERED is the intended run, though
 * nothing enforces the order: the screen is a dropdown and a clerk can set any
 * of them at any time.
 *
 * <p>{@code TruckOrderMaster.Status} is a free {@code varchar(30)} holding the
 * label, not the enum name, so {@link #getLabel()} is what goes to the database
 * and {@link #fromLabel} is what comes back. The legacy procedure accepted
 * whatever arrived, which was only safe while the sole writer was a closed
 * dropdown - it is a closed dropdown here too, and now the server checks.
 *
 * <p>Adding a status means adding it here and to TRUCK_ORDER_STATUSES in the
 * front end's types/truckOrder.ts. Keep the two in step.
 */
public enum TruckOrderStatus {

    /** Entered but not yet agreed with the customer. The default for a new order. */
    PENDING("Pending"),

    /** Agreed. The truck is committed to this day. */
    CONFIRMED("Confirmed"),

    /** The truck has left. */
    IN_TRANSIT("In Transit"),

    /** Completed. */
    DELIVERED("Delivered");

    private final String label;

    TruckOrderStatus(String label) {
        this.label = label;
    }

    /** The stored form, e.g. {@code "In Transit"}. */
    public String getLabel() {
        return label;
    }

    /** The labels in display order, for the dropdown and for error messages. */
    public static List<String> labels() {
        return Arrays.stream(values()).map(TruckOrderStatus::getLabel).toList();
    }

    /**
     * Resolves a stored or posted label, ignoring case and surrounding space.
     *
     * @throws InvalidRequestException if it is not one of the four
     */
    public static TruckOrderStatus fromLabel(String value) {
        String wanted = value == null ? "" : value.trim();
        return Arrays.stream(values())
                .filter(status -> status.label.equalsIgnoreCase(wanted))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(
                        "Status must be one of " + String.join(", ", labels())));
    }
}
