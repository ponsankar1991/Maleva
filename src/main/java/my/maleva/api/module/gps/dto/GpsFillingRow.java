package my.maleva.api.module.gps.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One parsed row of the fuel-fillings or speedings report.
 *
 * Both reports share a column layout in Wialon, which is why the legacy code
 * reused its FuelFillings class for the speed report as well - the FuelFillings
 * and SpeedReport tables have identical columns.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsFillingRow {

    /** Unit name as Wialon reports it; matched against TruckMaster.TruckName. */
    private String vehicle;

    /** Parsed event time. Null when the report printed a placeholder. */
    private LocalDateTime time;

    /** Time exactly as Wialon rendered it, kept for diagnostics. */
    private String rawTime;

    private String location;

    /** Event count. The legacy job skipped rows where this is "0". */
    private String count;

    /** Litres filled, rendered with a unit suffix such as "120 l". */
    private String filled;

    private String driver;
}
