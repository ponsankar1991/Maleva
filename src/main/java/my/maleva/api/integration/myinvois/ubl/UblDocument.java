package my.maleva.api.integration.myinvois.ubl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * The root of an LHDN UBL-JSON document: three namespace declarations and the
 * invoice. The namespace keys are literally {@code _D}, {@code _A}, {@code _B}.
 */
@JsonPropertyOrder({"_D", "_A", "_B", "Invoice"})
public record UblDocument(
        @JsonProperty("_D") String documentNamespace,
        @JsonProperty("_A") String aggregateNamespace,
        @JsonProperty("_B") String basicNamespace,
        @JsonProperty("Invoice") List<UblInvoice.Invoice> invoice) {

    public static final String NS_INVOICE = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
    public static final String NS_AGGREGATE = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    public static final String NS_BASIC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";

    public static UblDocument of(UblInvoice.Invoice invoice) {
        return new UblDocument(NS_INVOICE, NS_AGGREGATE, NS_BASIC, List.of(invoice));
    }
}
