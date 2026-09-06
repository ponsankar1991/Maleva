package my.maleva.api.module.invoice.einvoice;

import my.maleva.api.common.config.MyInvoisProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A complete, arithmetically consistent invoice for the e-invoice tests.
 *
 * <pre>
 *   line 1: 2 × 100.00 @ 6%  → tax 12.00, amount 212.00 (tax-inclusive, as stored)
 *   line 2: 1 ×  50.00 @ 0%  → tax  0.00, amount  50.00
 *   header: amount 262.00, tax 12.00, gross 262.00
 * </pre>
 */
final class EInvoiceFixtures {

    private EInvoiceFixtures() {
    }

    static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2);
    }

    static EInvoiceSnapshot.Header header() {
        return EInvoiceSnapshot.Header.builder()
                .invoiceId(4711)
                .companyId(1)
                .invoiceNo("INV000004711")
                .saleDate(LocalDateTime.of(2026, 9, 5, 10, 0))
                .referenceNo("PO-778")
                .amount(money("262.00"))
                .taxAmount(money("12.00"))
                .grossAmount(money("262.00"))
                .active(true)
                .build();
    }

    static EInvoiceSnapshot.Customer customer() {
        return EInvoiceSnapshot.Customer.builder()
                .customerId(9)
                .name("ACME LOGISTICS SDN BHD")
                .tin("C1234567890")
                .registrationNo("201101015652")
                .phone("+60312345678")
                .email("ap@acme.example")
                .city("SHAH ALAM")
                .postalZone("40000")
                .address1("LOT 1, JALAN INDUSTRI 2, SEKSYEN 15")
                .state("Selangor")
                .countryCode("MYS")
                .currencyCode("MYR")
                .build();
    }

    static EInvoiceSnapshot.Line taxedLine() {
        return EInvoiceSnapshot.Line.builder()
                .rowNumber(1)
                .detailId(101)
                .itemMasterRefId(55)
                .productCode("HANDLING")
                .productName("CARGO HANDLING")
                .remarks("HANDLING CHARGES VESSEL MV STAR")
                .quantity(money("2.00"))
                .unitPrice(money("100.00"))
                .taxPercent(money("6.00"))
                .taxAmount(money("12.00"))
                .amount(money("212.00"))
                .uom("UNIT(S)")
                .classificationCode(22)
                .build();
    }

    static EInvoiceSnapshot.Line untaxedLine() {
        return EInvoiceSnapshot.Line.builder()
                .rowNumber(2)
                .detailId(102)
                .itemMasterRefId(56)
                .productCode("DOCFEE")
                .productName("DOCUMENTATION FEE")
                .remarks(null)
                .quantity(money("1.00"))
                .unitPrice(money("50.00"))
                .taxPercent(money("0.00"))
                .taxAmount(money("0.00"))
                .amount(money("50.00"))
                .uom("UNIT(S)")
                .classificationCode(22)
                .build();
    }

    static EInvoiceSnapshot snapshot() {
        return EInvoiceSnapshot.builder()
                .header(header())
                .customer(customer())
                .lines(List.of(taxedLine(), untaxedLine()))
                .loadProblems(List.of())
                .build();
    }

    static MyInvoisProperties properties() {
        MyInvoisProperties properties = new MyInvoisProperties();
        properties.setEnabled(true);
        properties.setEnvironment("preprod");
        properties.setClientId("client");
        properties.setClientSecret("secret");

        MyInvoisProperties.Supplier supplier = new MyInvoisProperties.Supplier();
        supplier.setName("MALEVA (M) SDN BHD");
        supplier.setTin("C22173439020");
        supplier.setRegistrationNo("201101015652");
        supplier.setSstNo("W10-1809-32001584");
        supplier.setMsicCode("52299");
        supplier.setMsicDescription("Other transportation support activities n.e.c.");
        supplier.setAddressLine1("B50 JALAN FZ4 - P3, PORT KLANG, FREE ZONE /KS 12,");
        supplier.setAddressLine2("42920 PULAU INDAH, SELANGOR, MALAYSIA.");
        supplier.setCity("PULAU INDAH");
        supplier.setPostalZone("42920");
        supplier.setStateCode("10");
        supplier.setCountryCode("MYS");
        supplier.setPhone("+60331023251");
        supplier.setEmail("operation@maleva.com.my");
        properties.getSupplierProfiles().put("preprod", supplier);
        properties.getSupplierProfiles().put("live", supplier);
        return properties;
    }
}
