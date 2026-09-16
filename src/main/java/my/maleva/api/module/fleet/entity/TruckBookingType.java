package my.maleva.api.module.fleet.entity;

import my.maleva.api.common.exception.InvalidRequestException;

import java.util.Arrays;
import java.util.List;

/**
 * How a truck order is covered.
 *
 * <p>{@code TruckOrderMaster.BookingType} stores the enum name. Every order that
 * existed before the column was added reads {@link #OWN}.
 *
 * <p>Adding a type means adding it here and to TRUCK_BOOKING_TYPES in the front
 * end's types/truckOrder.ts. Keep the two in step.
 */
public enum TruckBookingType {

    /** One of our trucks, reserved for the day. At most one per truck per day. */
    OWN,

    /**
     * Put on one of our trucks that is already booked that day. The dispatcher
     * has judged that it fits; the system does no load arithmetic.
     */
    SHARED,

    /** A hired truck. Carries no truck of ours and is never counted against the fleet. */
    OUTSIDE;

    /** OWN and SHARED occupy one of our trucks; OUTSIDE does not. */
    public boolean usesOwnTruck() {
        return this != OUTSIDE;
    }

    /** The names, for error messages. */
    public static List<String> codes() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }

    /**
     * Resolves a posted or stored value, ignoring case and surrounding space.
     * Blank means {@link #OWN}, which is what an order means when nobody says
     * otherwise - including every row written before the column existed.
     *
     * @throws InvalidRequestException if it is not one of the three
     */
    public static TruckBookingType fromCode(String value) {
        if (value == null || value.isBlank()) {
            return OWN;
        }
        String wanted = value.trim();
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(wanted))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(
                        "Booking type must be one of " + String.join(", ", codes())));
    }
}
