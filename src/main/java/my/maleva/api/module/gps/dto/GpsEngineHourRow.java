package my.maleva.api.module.gps.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One parsed row of the engine-hours report (table unit_engine_hours). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsEngineHourRow {

    private LocalDateTime beginTime;
    private String rawBeginTime;

    private LocalDateTime endTime;
    private String rawEndTime;

    private String beginLocation;
    private String endLocation;
    private String totalTime;
    private String inMotion;
    private String idling;
    private String mileage;

    /** Fuel consumed by the FLS sensor while idling, as rendered by Wialon. */
    private String consumedByFlsInIdleRun;
}
