package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * One document or service that has expired or is about to.
 *
 * A truck carries fourteen separate expiry dates and a driver another twelve, so
 * the dashboard flattens them: every date becomes a row of the same shape and
 * the screen sorts the whole fleet by urgency rather than by vehicle.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpiryAlertDto {

    /** TRUCK or DRIVER. */
    private String entityType;

    private Integer entityId;

    /** Truck name or driver name. */
    private String entityName;

    /** Plate number for a truck, licence number for a driver. */
    private String entityRef;

    /** What expires: "Insurance", "Service", "Licence", "PTP Pass" and so on. */
    private String category;

    /**
     * Whether this is paperwork or workshop work, so the screen can group them.
     * DOCUMENT, SERVICE or PORT_PASS.
     */
    private String group;

    private LocalDate expiryDate;

    /** Negative when already past. Zero means it expires today. */
    private Integer daysRemaining;

    /** EXPIRED, CRITICAL or WARNING. */
    private String severity;
}
