package my.maleva.api.module.saleorder.print;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * The fixed text on the printed delivery order ({@code delivery-order-print.*}).
 *
 * <p>The letterhead is shared with the invoice ({@code invoice-print.header-lines}).
 * Everything here was static text inside the Crystal {@code CRDoReport.rpt}; it
 * lives in YAML so a change to the terms is a configuration change, not a rebuild.
 * The defaults are the legacy wording, so the print is complete without any YAML.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "delivery-order-print")
public class DeliveryOrderPrintProperties {

    /** The centred document title under the letterhead. */
    private String heading = "Delivery Report";

    /** The bold line above the terms, followed by a space to write the address in. */
    private String captainEmailLabel = "CAPTAIN EMAIL ADDRESS:";

    /** The underlined caption above the terms. */
    private String deliveryTermLabel = "Delivery Term :";

    /** The underlined title of the numbered terms. */
    private String termsTitle = "TERMS AND CONDITIONS:";

    /**
     * The numbered terms, one paragraph each, printed in order. Verbatim from the
     * Crystal report, typos included ("not with standing", "3,", "consignee's task"):
     * these are contract terms, so any rewording is the company's decision, made in YAML.
     */
    private List<String> terms = new ArrayList<>(List.of(
            "1. MALEVA (M) SDN BHD will not be liable or held responsible for any loss, damage and delay of any goods "
                    + "conveyed by its lorries to holdups, robbery, accidents and unforeseen circumstances beyond the "
                    + "control of the Company.",
            "2. Consignee is required to appoint their own surveyor at place of loading to inspect all their goods "
                    + "before loading into our lorry. All goods loaded into our lorry shall be considered as being "
                    + "accepted by the consignee in its original conditions, not with standing whether surveyed or not. "
                    + "(Contents unknown & unchecked)",
            "3, All goods transported from the place of loading to destination are at consignee's task and insurance "
                    + "covered only upon receipt of written Instructions.",
            "4. Kindly note that we have received the cargo on behalf of the customer and delivered it to the vessel. "
                    + "Please be informed that we were not aware of the specific contents inside the cargo, as it was "
                    + "sealed upon receipt.\nIf any items are missing or not received, kindly indicate this in the "
                    + "delivery order (DO) remarks. We will then inform the customer immediately for further action."));

    /** Caption under the left signature line. */
    private String issuerSignatureLabel = "Boarding Officer";

    /** Caption under the right signature line. */
    private String recipientSignatureLabel = "Recipient's Chop and Signature";
}
