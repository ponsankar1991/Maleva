package my.maleva.api.integration.myinvois;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.MyInvoisProperties;
import org.springframework.stereotype.Component;

/**
 * One log line at startup saying which LHDN environment and which taxpayer
 * this server would submit under. Secrets are never printed; the client id
 * is shown as a fingerprint.
 *
 * <p>This is the operator's check that the environment variables were picked
 * up — the project convention is to verify configuration from the startup
 * log rather than by probing the integration with real calls.
 */
@Slf4j
@Component
public class MyInvoisStartupReport {

    private final MyInvoisProperties properties;

    public MyInvoisStartupReport(MyInvoisProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void report() {
        if (!properties.isEnabled()) {
            log.info("MyInvois e-invoicing: DISABLED (myinvois.enabled=false)");
            return;
        }
        String environment;
        try {
            environment = MyInvoisEnvironment.parse(properties.getEnvironment()).name();
        } catch (IllegalArgumentException bad) {
            log.error("MyInvois e-invoicing: {}", bad.getMessage());
            return;
        }
        String supplier;
        try {
            MyInvoisProperties.Supplier profile = properties.supplier();
            supplier = profile.getName() + " TIN " + profile.getTin()
                    + " (" + profile.getRegistrationScheme() + " " + profile.getRegistrationNo() + ")";
        } catch (IllegalStateException missing) {
            log.error("MyInvois e-invoicing: {}", missing.getMessage());
            return;
        }
        log.info("MyInvois e-invoicing: environment={} supplier={} clientId={} clientSecret={} lineAmounts={} foreignCurrency={} resubmitInvalid={}",
                environment,
                supplier,
                fingerprint(properties.getClientId()),
                isBlank(properties.getClientSecret()) ? "MISSING" : "set",
                properties.getLineAmountPolicy(),
                properties.isAllowForeignCurrency() ? "allowed" : "blocked",
                properties.isAllowResubmitInvalid() ? "allowed" : "blocked");
        if (isBlank(properties.getClientId()) || isBlank(properties.getClientSecret())) {
            log.warn("MyInvois e-invoicing: client credentials are not set "
                    + "(MYINVOIS_CLIENT_ID / MYINVOIS_CLIENT_SECRET); every push will fail at the token step");
        }
    }

    /** First four and last four characters, e.g. {@code 3583…250a}; enough to tell two ids apart. */
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
