package my.maleva.api.module.gps.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Read model for a stored engine-hours interval, with the truck name joined in.
 * Replaces the legacy EngineHoursModel.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsEngineHoursDto {

    private Integer id;
    private Integer truckRefId;
    private String truckName;

    private LocalDateTime beginTime;
    private LocalDateTime endTime;

    private String beginLocation;
    private String endLocation;

    /** Durations and distances stay as Wialon rendered them, e.g. "3:42:10", "128 km". */
    private String totalTime;
    private String inMotion;
    private String idling;
    private String mileage;
    private String consumedByFlsInIdleRun;

    private LocalDateTime createdDate;
}
