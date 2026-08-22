package my.maleva.api.module.gps.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * WialonProperties - configuration for the Wialon Hosting GPS integration.
 *
 * Replaces the legacy .NET {@code Common/gpsapilist.cs}, which hardcoded the
 * endpoint, the API token and the numeric report ids in source. Everything here
 * comes from application.yaml / environment variables instead.
 *
 * @see <a href="https://sdk.wialon.com/wiki/en/sidebar/start">Wialon SDK</a>
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "wialon")
public class WialonProperties {

    /** Master switch. When false no Wialon call is ever made. */
    private boolean enabled = false;

    /** Wialon Hosting ajax endpoint, e.g. https://hst-api.wialon.com/wialon/ajax.html */
    private String baseUrl = "https://hst-api.wialon.com/wialon/ajax.html";

    /** API token issued in Wialon. MUST come from an environment variable - never commit it. */
    private String token;

    /** CompanyRefId stamped on every synced row. Legacy GPSJob hardcoded 6. */
    private Integer companyRefId = 6;

    /** How far back each sync run looks. Legacy GPSJob used a fixed 12 hour window. */
    private int windowHours = 12;

    /** HTTP read timeout for Wialon calls, in seconds. Report execution is slow. */
    private int timeoutSeconds = 300;

    /**
     * Legacy epoch behaviour. The .NET job computed {@code DateTime.Now - 1970-01-01},
     * i.e. it sent LOCAL time as if it were UTC. When true this is reproduced exactly so
     * the new sync selects the same interval as the old job. Set false to send true UTC.
     */
    private boolean legacyEpoch = true;

    /** Pause after setting the locale, mirroring the legacy Thread.Sleep(60s). */
    private int localeSettleSeconds = 60;

    private Locale locale = new Locale();
    private Reports reports = new Reports();
    private Sync sync = new Sync();

    @Getter
    @Setter
    public static class Locale {
        /** Base UTC offset in seconds. Malaysia (+08:00) = 28800, as in the legacy getTzOffset(). */
        private int tzBaseSeconds = 28800;
        /** Apply the Wialon DST flag (0x08000000) on top of the base offset. */
        private boolean dst = true;
        private String language = "en";
        private String formatDate = "%Y-%m-%E %H:%M:%S";
    }

    @Getter
    @Setter
    public static class Reports {
        private ReportConfig fuel = new ReportConfig();
        private ReportConfig speed = new ReportConfig();
        private ReportConfig engine = new ReportConfig();
    }

    /**
     * One Wialon report definition.
     *
     * {@code templateName} is preferred over {@code templateId}: the legacy engine-hours
     * report already resolved its template by name, which survives Wialon renumbering.
     * {@code resourceId} / {@code objectId} may be left null, in which case the first
     * avl_resource returned by the search is used.
     */
    @Getter
    @Setter
    public static class ReportConfig {
        private boolean enabled = true;
        /** Resolve the template id by this name from the avl_resource search. Wins over templateId. */
        private String templateName;
        /** Fallback numeric template id when templateName is not set. */
        private Integer templateId;
        private Long resourceId;
        private Long objectId;
        /** Name of the result table inside the report, e.g. unit_group_fillings. */
        private String table;
    }

    @Getter
    @Setter
    public static class Sync {
        private boolean enabled = false;
        /** Quartz-style cron for the scheduled pull. Legacy ran under Quartz every 12h. */
        private String cron = "0 0 */6 * * *";
    }
}
