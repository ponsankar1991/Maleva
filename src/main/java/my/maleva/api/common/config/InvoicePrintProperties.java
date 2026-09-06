package my.maleva.api.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * The fixed text on the printed sale invoice ({@code invoice-print.*}).
 *
 * <p>Crystal's {@code CRInvoice.rpt} took six header lines and a heading as
 * report parameters that {@code ReportViewer.aspx.cs} hard-coded, and carried
 * the payment notes as static text inside the design. All of it lives here
 * instead, so a new bank account or phone number is a YAML change, not a
 * rebuild.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "invoice-print")
public class InvoicePrintProperties {

    /** The six lines beside the logo, top to bottom; the first is printed large and bold. */
    private List<String> headerLines = new ArrayList<>();

    /** The centred document title. */
    private String heading = "INVOICE";

    /** The numbered notes under the amount in words. */
    private List<String> notes = new ArrayList<>();

    /** Small print beside the signature. */
    private String generatedNote = "This is an computer generated invoice. No signatory is required";
}
