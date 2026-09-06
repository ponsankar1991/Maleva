package my.maleva.api.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Configuration for the LHDN MyInvois e-invoice integration ({@code myinvois.*}).
 *
 * <p>Everything the legacy code hard-coded in {@code EInvoiceapilist.cs} lives
 * here instead: the live/preprod switch, the OAuth client credentials, and the
 * supplier (Maleva) identity that goes on every document. Credentials come from
 * user-scope environment variables per project convention — they are never
 * written into a YAML default.
 *
 * <p>Two things here are deliberate policy choices, not just settings; read
 * their javadoc before changing them:
 * {@link #lineAmountPolicy} and {@link #allowResubmitInvalid}.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "myinvois")
public class MyInvoisProperties {

    /** Master switch. When false no document is ever sent; the push answers "disabled". */
    private boolean enabled = true;

    /**
     * {@code live} or {@code preprod}. Chooses the API host, the portal host used
     * in the QR link, AND the supplier profile below — the preprod taxpayer LHDN
     * issues is a different legal person from the live one.
     */
    private String environment = "preprod";

    /** OAuth2 client id issued by LHDN for this taxpayer. */
    private String clientId = "";

    /** OAuth2 client secret. Never logged; see {@code MyInvoisTokenProvider}. */
    private String clientSecret = "";

    /**
     * Read timeout for LHDN calls. Legacy used 30 minutes, which parks a request
     * thread for half an hour when LHDN hangs. Two minutes is generous for a
     * single-document submission.
     */
    private Duration timeout = Duration.ofSeconds(120);

    /** Safety margin subtracted from the token lifetime so a token is never used in its last seconds. */
    private Duration tokenRefreshMargin = Duration.ofSeconds(60);

    /**
     * How line amounts are reported to LHDN.
     *
     * <p>The Sale Invoice screen stores each line's {@code Amount} tax-INCLUSIVE
     * (Qty × Rate + tax). UBL's {@code LineExtensionAmount} and
     * {@code TaxableAmount} are tax-EXCLUSIVE. The legacy push sent the stored
     * inclusive figure anyway, so on every taxed invoice LHDN recorded a taxable
     * base inflated by the tax itself, and the line sum did not reconcile to the
     * header. {@code EXCLUSIVE} (the default) sends the stored tax-inclusive
     * {@code Amount} minus the stored {@code TaxAmount} — equal to Qty × Rate
     * within a sen — derived from the same two stored figures as the header
     * total, so lines and header reconcile exactly. {@code LEGACY_INCLUSIVE}
     * reproduces the old document shape (inclusive line amounts, no taxable
     * base on the document subtotals) and exists only for comparison against
     * what is already on LHDN's record.
     */
    private LineAmountPolicy lineAmountPolicy = LineAmountPolicy.EXCLUSIVE;

    /**
     * Whether an invoice LHDN validated as {@code Invalid} may be submitted again.
     * Legacy blocked any re-push once a UUID existed, which left such invoices
     * stuck forever. LHDN permits resubmission of an Invalid document under the
     * same number, so this defaults to true.
     */
    private boolean allowResubmitInvalid = true;

    /**
     * Whether invoices in a currency other than MYR may be pushed.
     *
     * <p>Four in ten invoices this business has e-invoiced are SGD or USD, and
     * LHDN validated them as sent: document and tax currency both the foreign
     * code, no exchange rate. That shape is kept, so this defaults to true.
     * Set it false only to hold foreign-currency invoices back deliberately,
     * for instance while an exchange-rate representation is being verified.
     */
    private boolean allowForeignCurrency = true;

    /**
     * One supplier profile per environment, keyed {@code live} / {@code preprod}.
     * LHDN's preprod credentials belong to a different taxpayer than the live
     * ones, so the supplier block must switch with the environment or every
     * preprod submission fails on a TIN mismatch.
     */
    private Map<String, Supplier> supplierProfiles = new LinkedHashMap<>();

    /** The supplier profile for the configured environment. */
    public Supplier supplier() {
        // Resolve through the same parser the URLs use, so an alias such as
        // "prod" finds the "live" profile rather than none at all.
        String key = my.maleva.api.integration.myinvois.MyInvoisEnvironment.parse(environment)
                .name().toLowerCase(Locale.ROOT);
        Supplier profile = supplierProfiles.get(key);
        if (profile == null) {
            throw new IllegalStateException(
                    "No myinvois.supplier-profiles." + key + " block is configured for environment '" + environment + "'");
        }
        return profile;
    }

    public enum LineAmountPolicy {
        /** Line amounts are stored Amount − stored TaxAmount (≈ Qty × Rate). Reconciles with the header. */
        EXCLUSIVE,
        /** Line amounts are the stored tax-inclusive figure and the document subtotals carry no base, as legacy sent. */
        LEGACY_INCLUSIVE
    }

    /**
     * The supplier party on every document — Maleva's own registration as LHDN
     * knows it. Set per environment; the values legacy used are in
     * {@code application.yaml}.
     */
    @Getter
    @Setter
    public static class Supplier {
        private String name = "";
        private String tin = "";
        private String tinScheme = "TIN";
        private String registrationNo = "";
        /** BRN for a company, NRIC for an individual taxpayer (the preprod profile). */
        private String registrationScheme = "BRN";
        private String sstNo = "";
        private String sstScheme = "SST";
        /** MSIC 2008 industry code, e.g. 52299. */
        private String msicCode = "";
        private String msicDescription = "";
        private String addressLine1 = "";
        private String addressLine2 = "";
        private String city = "";
        private String postalZone = "";
        /** LHDN state code, e.g. 10 = Selangor. */
        private String stateCode = "";
        /** ISO 3166-1 alpha-3. */
        private String countryCode = "MYS";
        private String phone = "";
        private String email = "";
    }
}
