package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One line of the truck / driver contact list: which truck, who drives it, and
 * how to reach them.
 *
 * <p>Deliberately small. The header window that shows this is opened by everyone
 * many times a day, and TruckMasterDto carries forty columns of permits and
 * service dates that nobody reading a phone number needs.
 *
 * <p>The driver fields are null when no driver is assigned - that is a normal
 * state, not an error, and the window shows it as a gap to fill.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckDriverContactDto {

    private Integer truckId;
    private String truckName;
    private String truckNumber;

    /** The free text as recorded, e.g. {@code 40 FT SIDE CURTAIN}. */
    private String truckType;

    /** The size read out of {@link #truckType}, e.g. {@code 40FT}; null when unreadable. */
    private String sizeClass;

    /** ACTIVE, WORKSHOP or SOLD - the window marks a truck that is off the road. */
    private String truckStatus;

    /** When a WORKSHOP truck is due back; null means no date given. */
    private java.time.LocalDate workshopUntil;

    private Integer driverRefId;
    private String driverName;
    private String driverMobileNo;
}
