package my.maleva.api.integration.myinvois.ubl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;

import java.util.List;

import static my.maleva.api.integration.myinvois.ubl.UblValues.*;

/**
 * The structure of an LHDN UBL 2.1 invoice, as nested records.
 *
 * <p>Only the elements this business sends are modelled. Every element is a
 * {@code List} because UBL-JSON wraps each child in an array even when there
 * can only be one. Property names are the UBL element names verbatim
 * ({@code ID}, {@code IssueDate}, …) — Jackson uses the {@code @JsonProperty}
 * name, so the Java field names are free to be readable.
 *
 * <p>A null field is omitted from the JSON. Whether an element is mandatory is
 * decided by {@code EInvoiceValidator} before the document is built, not by
 * this model; the model only knows shape.
 */
public final class UblInvoice {

    private UblInvoice() {
    }

    /** The invoice: header, parties, totals and lines. */
    @Builder
    @JsonPropertyOrder({"ID", "IssueDate", "IssueTime", "InvoiceTypeCode", "DocumentCurrencyCode",
            "TaxCurrencyCode", "InvoicePeriod", "BillingReference", "AccountingSupplierParty",
            "AccountingCustomerParty", "TaxTotal", "LegalMonetaryTotal", "InvoiceLine"})
    public record Invoice(
            @JsonProperty("ID") List<Id> id,
            @JsonProperty("IssueDate") List<Text> issueDate,
            @JsonProperty("IssueTime") List<Text> issueTime,
            @JsonProperty("InvoiceTypeCode") List<TypeCode> invoiceTypeCode,
            @JsonProperty("DocumentCurrencyCode") List<Currency> documentCurrencyCode,
            @JsonProperty("TaxCurrencyCode") List<Currency> taxCurrencyCode,
            @JsonProperty("InvoicePeriod") List<InvoicePeriod> invoicePeriod,
            @JsonProperty("BillingReference") List<BillingReference> billingReference,
            @JsonProperty("AccountingSupplierParty") List<SupplierParty> accountingSupplierParty,
            @JsonProperty("AccountingCustomerParty") List<CustomerParty> accountingCustomerParty,
            @JsonProperty("TaxTotal") List<TaxTotal> taxTotal,
            @JsonProperty("LegalMonetaryTotal") List<LegalMonetaryTotal> legalMonetaryTotal,
            @JsonProperty("InvoiceLine") List<InvoiceLine> invoiceLine) {
    }

    public record InvoicePeriod(
            @JsonProperty("StartDate") List<Text> startDate,
            @JsonProperty("EndDate") List<Text> endDate,
            @JsonProperty("Description") List<Text> description) {
    }

    /** Legacy sent the invoice's Remarks1 as an "additional document reference". */
    public record BillingReference(
            @JsonProperty("AdditionalDocumentReference") List<DocumentReference> additionalDocumentReference) {
    }

    public record DocumentReference(@JsonProperty("ID") List<Id> id) {
    }

    public record SupplierParty(@JsonProperty("Party") List<Party> party) {
    }

    public record CustomerParty(@JsonProperty("Party") List<Party> party) {
    }

    /** A party — supplier or customer. The supplier carries the industry code; the customer does not. */
    @Builder
    @JsonPropertyOrder({"IndustryClassificationCode", "PartyIdentification", "PostalAddress",
            "PartyLegalEntity", "Contact"})
    public record Party(
            @JsonProperty("IndustryClassificationCode") List<IndustryCode> industryClassificationCode,
            @JsonProperty("PartyIdentification") List<PartyIdentification> partyIdentification,
            @JsonProperty("PostalAddress") List<PostalAddress> postalAddress,
            @JsonProperty("PartyLegalEntity") List<PartyLegalEntity> partyLegalEntity,
            @JsonProperty("Contact") List<Contact> contact) {
    }

    /** One identification, e.g. {@code {"ID":[{"_":"C123","schemeID":"TIN"}]}}. */
    public record PartyIdentification(@JsonProperty("ID") List<Id> id) {

        /** Null when the value is blank, so the caller can filter it out. */
        public static PartyIdentification of(String value, String scheme) {
            List<Id> id = UblValues.id(value, scheme);
            return id == null ? null : new PartyIdentification(id);
        }
    }

    @Builder
    @JsonPropertyOrder({"CityName", "PostalZone", "CountrySubentityCode", "AddressLine", "Country"})
    public record PostalAddress(
            @JsonProperty("CityName") List<Text> cityName,
            @JsonProperty("PostalZone") List<Text> postalZone,
            @JsonProperty("CountrySubentityCode") List<Text> countrySubentityCode,
            @JsonProperty("AddressLine") List<AddressLine> addressLine,
            @JsonProperty("Country") List<Country> country) {
    }

    public record AddressLine(@JsonProperty("Line") List<Text> line) {
    }

    public record Country(@JsonProperty("IdentificationCode") List<CountryCode> identificationCode) {
    }

    public record PartyLegalEntity(@JsonProperty("RegistrationName") List<Text> registrationName) {
    }

    public record Contact(
            @JsonProperty("Telephone") List<Text> telephone,
            @JsonProperty("ElectronicMail") List<Text> electronicMail) {
    }

    /** Document-level or line-level tax total. */
    public record TaxTotal(
            @JsonProperty("TaxAmount") List<Amount> taxAmount,
            @JsonProperty("TaxSubtotal") List<TaxSubtotal> taxSubtotal) {
    }

    @Builder
    @JsonPropertyOrder({"TaxableAmount", "TaxAmount", "Percent", "TaxCategory", "PerUnitAmount", "BaseUnitMeasure"})
    public record TaxSubtotal(
            @JsonProperty("TaxableAmount") List<Amount> taxableAmount,
            @JsonProperty("TaxAmount") List<Amount> taxAmount,
            @JsonProperty("Percent") List<Numeric> percent,
            @JsonProperty("TaxCategory") List<TaxCategory> taxCategory,
            @JsonProperty("PerUnitAmount") List<Amount> perUnitAmount,
            @JsonProperty("BaseUnitMeasure") List<Quantity> baseUnitMeasure) {
    }

    /** {@code ID} is the LHDN tax type: 01 sales tax, 06 not applicable, E exempt … */
    public record TaxCategory(
            @JsonProperty("ID") List<Id> id,
            @JsonProperty("TaxExemptionReason") List<Text> taxExemptionReason,
            @JsonProperty("TaxScheme") List<TaxScheme> taxScheme) {
    }

    public record TaxScheme(@JsonProperty("ID") List<Id> id) {

        /** The only scheme LHDN's JSON samples use: OTH under UN/ECE 5153, agency 6. */
        public static List<TaxScheme> other() {
            return List.of(new TaxScheme(UblValues.id("OTH", "UN/ECE 5153", "6")));
        }
    }

    @Builder
    @JsonPropertyOrder({"LineExtensionAmount", "TaxExclusiveAmount", "TaxInclusiveAmount", "AllowanceTotalAmount",
            "ChargeTotalAmount", "PayableRoundingAmount", "PayableAmount"})
    public record LegalMonetaryTotal(
            @JsonProperty("LineExtensionAmount") List<Amount> lineExtensionAmount,
            @JsonProperty("TaxExclusiveAmount") List<Amount> taxExclusiveAmount,
            @JsonProperty("TaxInclusiveAmount") List<Amount> taxInclusiveAmount,
            @JsonProperty("AllowanceTotalAmount") List<Amount> allowanceTotalAmount,
            @JsonProperty("ChargeTotalAmount") List<Amount> chargeTotalAmount,
            @JsonProperty("PayableRoundingAmount") List<Amount> payableRoundingAmount,
            @JsonProperty("PayableAmount") List<Amount> payableAmount) {
    }

    @Builder
    @JsonPropertyOrder({"ID", "InvoicedQuantity", "LineExtensionAmount", "TaxTotal", "Item", "Price",
            "ItemPriceExtension"})
    public record InvoiceLine(
            @JsonProperty("ID") List<Id> id,
            @JsonProperty("InvoicedQuantity") List<Quantity> invoicedQuantity,
            @JsonProperty("LineExtensionAmount") List<Amount> lineExtensionAmount,
            @JsonProperty("TaxTotal") List<TaxTotal> taxTotal,
            @JsonProperty("Item") List<Item> item,
            @JsonProperty("Price") List<Price> price,
            @JsonProperty("ItemPriceExtension") List<ItemPriceExtension> itemPriceExtension) {
    }

    public record Item(
            @JsonProperty("CommodityClassification") List<CommodityClassification> commodityClassification,
            @JsonProperty("Description") List<Text> description) {
    }

    public record CommodityClassification(
            @JsonProperty("ItemClassificationCode") List<ClassificationCode> itemClassificationCode) {
    }

    public record Price(@JsonProperty("PriceAmount") List<Amount> priceAmount) {
    }

    public record ItemPriceExtension(@JsonProperty("Amount") List<Amount> amount) {
    }
}
