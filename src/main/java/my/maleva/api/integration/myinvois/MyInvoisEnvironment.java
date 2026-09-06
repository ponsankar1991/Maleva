package my.maleva.api.integration.myinvois;

import java.util.Locale;

/**
 * The two LHDN deployments. Legacy switched between them with a static boolean
 * ({@code EInvoiceeapidemo}); here the choice is configuration
 * ({@code myinvois.environment}) so a rebuild is never needed to move a server
 * between preprod and live.
 */
public enum MyInvoisEnvironment {

    LIVE("https://api.myinvois.hasil.gov.my", "https://myinvois.hasil.gov.my"),
    PREPROD("https://preprod-api.myinvois.hasil.gov.my", "https://preprod.myinvois.hasil.gov.my");

    /** Base of the System API (token, submissions, documents). */
    private final String apiBaseUrl;

    /** Base of the taxpayer portal; the QR on a printed invoice links here. */
    private final String portalBaseUrl;

    MyInvoisEnvironment(String apiBaseUrl, String portalBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
        this.portalBaseUrl = portalBaseUrl;
    }

    public String apiBaseUrl() {
        return apiBaseUrl;
    }

    public String portalBaseUrl() {
        return portalBaseUrl;
    }

    /** Parses {@code live} / {@code preprod} (any case); anything else is a configuration error. */
    public static MyInvoisEnvironment parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("myinvois.environment must be 'live' or 'preprod'");
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "live", "prod", "production" -> LIVE;
            case "preprod", "sandbox", "demo", "test" -> PREPROD;
            default -> throw new IllegalArgumentException(
                    "myinvois.environment must be 'live' or 'preprod', got '" + value + "'");
        };
    }
}
