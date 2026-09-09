package my.maleva.api.module.gps.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * One log line at startup saying whether the GPS sync is armed and whether it
 * has a token. The token itself is never printed; it is shown as a
 * fingerprint, as the MyInvois report does.
 *
 * <p>This exists because the sync fails <em>quietly</em> without it. The
 * scheduler runs every six hours, in the background, with nobody watching — so
 * a missing token showed up as "the GPS screens are empty" hours later, with
 * nothing in the log at the time anyone was looking. That is exactly what
 * happened when the IDE was launched without the Wialon environment variable:
 * the application started perfectly and simply never pulled anything.
 *
 * <p>The project convention is to verify configuration from the startup log
 * rather than by probing an integration with real calls, and GPS was the one
 * integration with no line to check.
 */
@Slf4j
@Component
public class WialonStartupReport {

    private final WialonProperties properties;

    public WialonStartupReport(WialonProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void report() {
        if (!properties.isEnabled()) {
            log.info("Wialon GPS: DISABLED (wialon.enabled=false)");
            return;
        }

        boolean scheduled = properties.getSync() != null && properties.getSync().isEnabled();
        log.info("Wialon GPS: enabled token={} company={} window={}h scheduled={} cron={} legacyEpoch={}",
                fingerprint(properties.getToken()),
                properties.getCompanyRefId(),
                properties.getWindowHours(),
                scheduled ? "yes" : "no (manual only)",
                scheduled && properties.getSync().getCron() != null ? properties.getSync().getCron() : "-",
                properties.isLegacyEpoch());

        if (isBlank(properties.getToken())) {
            log.warn("Wialon GPS: no token set (wialon.token in secrets.yaml, or WIALON_TOKEN); "
                    + "every sync will fail and the GPS screens will stay empty");
        }
    }

    /** First four and last four characters; enough to tell two tokens apart. */
    static String fingerprint(String value) {
        if (isBlank(value)) {
            return "MISSING";
        }
        String v = value.trim();
        return v.length() <= 8 ? "set" : v.substring(0, 4) + "…" + v.substring(v.length() - 4);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
