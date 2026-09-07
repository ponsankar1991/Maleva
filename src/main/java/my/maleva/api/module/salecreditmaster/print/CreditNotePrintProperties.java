package my.maleva.api.module.salecreditmaster.print;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * The fixed text on the printed credit note ({@code credit-note-print.*}).
 *
 * <p>The letterhead and the logo are the company's and are shared with
 * {@code invoice-print}; everything here is what the Crystal
 * {@code CRCreditNote.rpt} carried as static text of its own — a different
 * title, a single payment note (the invoice prints three), and the two
 * small-print lines above the signature.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "credit-note-print")
public class CreditNotePrintProperties {

    /** The centred document title. */
    private String heading = "CREDIT NOTE";

    /** The notes under the amount in words; printed bold and underlined, as Crystal did. */
    private List<String> notes = new ArrayList<>(List.of(
            "1.All Cheque Should be crossed and made payable to MALEVA(M) SDN BHD "));

    /** The two centred lines above the signature rule. */
    private String generatedNoteLine1 = "This is an computer generated invoice.";
    private String generatedNoteLine2 = "No signatory is required";
}
