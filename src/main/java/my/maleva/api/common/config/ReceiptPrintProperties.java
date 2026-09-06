package my.maleva.api.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * The fixed text on the printed receipt voucher ({@code receipt-print.*}).
 *
 * <p>Crystal's {@code CRReceipt2.rpt} took six header lines and a heading as
 * report parameters that {@code ReportViewer.aspx.cs} hard-coded in both
 * {@code ReceiptReport()} and {@code ReceiptExportReport()}, and carried the
 * "NB" note and the small print as static text inside the design. All of it
 * lives here instead, so a new phone number is a YAML change, not a rebuild.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "receipt-print")
public class ReceiptPrintProperties {

    /** The six lines beside the logo, top to bottom; the first is printed large and bold. */
    private List<String> headerLines = new ArrayList<>();

    /** The centred document title. */
    private String heading = "RECEIPT VOUCHER";

    /** The line under "NB :" at the bottom left. */
    private String nbNote = "Validity of this receipt subject to clearing of cheque";

    /** Small print above the FOR line. */
    private String generatedNote = "This is an computer generated receipt.";
}
