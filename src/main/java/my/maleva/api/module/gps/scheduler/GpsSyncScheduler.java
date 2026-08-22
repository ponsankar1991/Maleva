package my.maleva.api.module.gps.scheduler;

import my.maleva.api.module.gps.dto.GpsReportSyncResult;
import my.maleva.api.module.gps.dto.GpsSyncResultDto;
import my.maleva.api.module.gps.service.GpsSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs the GPS sync on a schedule, replacing the legacy Quartz job registration.
 *
 * The bean only exists when {@code wialon.sync.enabled=true}, so a developer
 * machine never pulls from Wialon by accident.
 */
@Component
@ConditionalOnProperty(prefix = "wialon.sync", name = "enabled", havingValue = "true")
public class GpsSyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(GpsSyncScheduler.class);

    private final GpsSyncService syncService;

    public GpsSyncScheduler(GpsSyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * The sync itself refuses to overlap, so a run that overshoots the cron
     * interval is skipped rather than queued.
     */
    @Scheduled(cron = "${wialon.sync.cron}")
    public void run() {
        logger.info("Scheduled GPS sync starting");
        GpsSyncResultDto result = syncService.sync();

        for (GpsReportSyncResult report : result.getReports()) {
            logger.info("GPS sync [{}] {} fetched={} written={} noTruck={} noTime={} zeroCount={} {}",
                    report.getReport(),
                    report.getStatus(),
                    report.getRowsFetched(),
                    report.getRowsWritten(),
                    report.getRowsSkippedNoTruck(),
                    report.getRowsSkippedNoTime(),
                    report.getRowsSkippedZeroCount(),
                    report.getMessage() == null ? "" : report.getMessage());
        }

        if (!result.isSuccess()) {
            logger.error("Scheduled GPS sync finished with failures");
        }
    }
}
