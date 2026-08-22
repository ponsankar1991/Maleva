package my.maleva.api.module.gps.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** Outcome of one full GPS sync run, one entry per report. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsSyncResultDto {

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    /** Start of the interval requested from Wialon. */
    private LocalDateTime windowFrom;

    /** End of the interval requested from Wialon. */
    private LocalDateTime windowTo;

    private Integer companyRefId;

    private List<GpsReportSyncResult> reports;

    /** True when every enabled report finished without failing. */
    private boolean success;
}
