package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * One fuel entry as the edit form needs it, with the GPS filling it was
 * matched against.
 *
 * Replaces the wide row the legacy EditFuelEntry query returned, where the GPS
 * columns arrived as GPSVehicle / GPSTime / GPSLocation / filled / GPSDriver
 * flattened alongside the entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelEntryDetailDto {

    private Integer id;
    private Integer companyRefId;
    private String cNumberDisplay;
    private Integer cNumber;

    private LocalDate saleDate;

    private Integer truckRefId;
    private String truckName;
    private Integer driverRefId;
    private String driverName;

    private Double aliter;
    private Double aAmount;
    private Double pliter;
    private Double pAmount;
    private Double gliter;
    private Double gAmount;
    private Double pRate;

    private Double dpLiter;
    private Double dpAmount;
    private Double dgLiter;
    private Double dgAmount;

    /** True when the patron billed more litres than the GPS sensor saw. */
    private boolean pumpOverGps;

    /** True when the patron billed more litres than the receipt claims. */
    private boolean pumpOverActual;

    private String remarks;
    private String filePath;
    private Integer fStatus;

    /**
     * The GPS filling this entry was matched to, or null when none applied.
     * Carries an unmatchedReason when null so the screen can say why.
     */
    private my.maleva.api.module.gps.dto.GpsFuelMatchDto gpsMatch;
}
