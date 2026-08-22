package my.maleva.api.module.gps.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Read model for a stored speeding event, with the truck name joined in.
 *
 * Replaces the legacy FuelFillingsModel (the speed report reuses it). Timestamps stay as LocalDateTime and
 * are formatted in the browser; the legacy service returned them pre-formatted
 * as dd/MM/yyyy HH:mm:ss strings alongside a second raw copy used only for sorting.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsSpeedReportDto {

    private Integer id;
    private Integer truckRefId;
    private String truckName;

    /** Unit name as Wialon reported it, kept for traceability against truckName. */
    private String vehicle;

    private LocalDateTime time;
    private String location;

    /**
     * The speed limit at that point, shown on the screen as "Limit".
     * Named `count` because the speedings report reuses the fillings column
     * layout and the table column was never renamed.
     */
    private String count;

    /**
     * The speed the truck was actually doing, shown on the screen as "Speed".
     * Same story: the column is called `filled` only because it sits where the
     * litres sit in the fillings report.
     */
    private String filled;

    private String driver;
    private LocalDateTime createdDate;
}
