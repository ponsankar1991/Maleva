package my.maleva.api.module.gps.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * The GPS filling assigned to one fuel entry.
 *
 * Corresponds to the OUTER APPLY block of the legacy EditFuelEntry query, which
 * exposed GPSVehicle, GPSTime, GPSLocation, filled, GPSDriver and GPSCreatedDate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsFuelMatchDto {

    /** Id of the FuelEntry row this filling was assigned to. */
    private Integer fuelEntryId;

    /** Id of the FuelFillings row. Null when the entry got no match. */
    private Integer fuelFillingId;

    private String vehicle;
    private LocalDateTime gpsTime;
    private String gpsLocation;
    private String gpsDriver;
    private LocalDateTime gpsCreatedDate;

    /** Litres exactly as Wialon rendered them, for example "274.51 l". */
    private String filled;

    /** Litres parsed out of {@link #filled}. Null when the value held no number. */
    private Double filledLitres;

    /** Litres on the entry that this filling was matched against. */
    private Double enteredLitres;

    /** {@code |filledLitres - enteredLitres|} - how close the match is. */
    private Double differenceLitres;

    /**
     * Why no filling was assigned, when {@link #fuelFillingId} is null:
     * NO_GPS_DATA, OUT_OF_TOLERANCE or ALREADY_CLAIMED.
     */
    private String unmatchedReason;
}
