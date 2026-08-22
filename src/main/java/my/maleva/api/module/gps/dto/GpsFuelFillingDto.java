package my.maleva.api.module.gps.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Read model for a stored fuel filling, with the truck name joined in.
 *
 * Replaces the legacy FuelFillingsModel. Timestamps stay as LocalDateTime and
 * are formatted in the browser; the legacy service returned them pre-formatted
 * as dd/MM/yyyy HH:mm:ss strings alongside a second raw copy used only for sorting.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsFuelFillingDto {

    private Integer id;
    private Integer truckRefId;
    private String truckName;

    /** Unit name as Wialon reported it, kept for traceability against truckName. */
    private String vehicle;

    private LocalDateTime time;
    private String location;
    private String count;

    /** Litres filled, as rendered by Wialon (for example "120 l"). */
    private String filled;

    private String driver;
    private LocalDateTime createdDate;
}
