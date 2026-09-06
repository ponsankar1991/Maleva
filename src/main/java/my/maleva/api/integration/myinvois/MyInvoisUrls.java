package my.maleva.api.integration.myinvois;

import my.maleva.api.common.config.MyInvoisProperties;
import org.springframework.stereotype.Component;

/**
 * Every MyInvois URL the integration uses, in one place, resolved from the
 * configured environment on each call. The properties themselves are bound
 * once at startup, so changing {@code MYINVOIS_ENVIRONMENT} needs a restart.
 */
@Component
public class MyInvoisUrls {

    private final MyInvoisProperties properties;

    public MyInvoisUrls(MyInvoisProperties properties) {
        this.properties = properties;
    }

    public MyInvoisEnvironment environment() {
        return MyInvoisEnvironment.parse(properties.getEnvironment());
    }

    /** OAuth2 client-credentials token endpoint. */
    public String token() {
        return environment().apiBaseUrl() + "/connect/token";
    }

    /** POST here to submit one or more documents. */
    public String documentSubmissions() {
        return environment().apiBaseUrl() + "/api/v1.0/documentsubmissions";
    }

    /** GET here to learn the validation outcome of a submission. */
    public String documentSubmission(String submissionUid) {
        return documentSubmissions() + "/" + submissionUid;
    }

    /**
     * The public share link for a validated document; this is what the QR on
     * the printed invoice encodes.
     */
    public String documentShareLink(String documentUuid, String longId) {
        return environment().portalBaseUrl() + "/" + documentUuid + "/share/" + longId;
    }
}
