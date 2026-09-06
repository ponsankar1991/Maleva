package my.maleva.api.module.invoice.print;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Compiles the real template and renders a fixture invoice. This is the test
 * that catches a broken expression or a renamed getter in the .jrxml — the
 * template is only compiled at runtime, so nothing else would.
 */
class SaleInvoicePdfServiceTest {

    @Test
    void rendersAPdfFromTheTemplate() {
        InvoicePrintSnapshotLoader loader = Mockito.mock(InvoicePrintSnapshotLoader.class);
        when(loader.load(anyInt(), anyInt())).thenReturn(Optional.of(snapshot(true)));

        SaleInvoicePdfService service = new SaleInvoicePdfService(loader, Mockito.mock(InvoicePrintEInvoiceBackfill.class));
        SaleInvoicePdfService.RenderedInvoice rendered = service.render(4711, 1).orElseThrow();

        assertThat(rendered.fileName()).isEqualTo("INV000004711.pdf");
        assertThat(new String(rendered.pdf(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(rendered.pdf().length).isGreaterThan(2000);

        // -Dinvoice.pdf.out=<dir> writes the fixture PDF so the layout can be eyeballed.
        String out = System.getProperty("invoice.pdf.out");
        if (out != null && !out.isBlank()) {
            try {
                java.nio.file.Files.write(java.nio.file.Path.of(out, rendered.fileName()), rendered.pdf());
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Test
    void rendersWithoutTheLhdnBlockBeforeSubmission() {
        InvoicePrintSnapshotLoader loader = Mockito.mock(InvoicePrintSnapshotLoader.class);
        when(loader.load(anyInt(), anyInt())).thenReturn(Optional.of(snapshot(false)));

        SaleInvoicePdfService.RenderedInvoice rendered = new SaleInvoicePdfService(loader, Mockito.mock(InvoicePrintEInvoiceBackfill.class)).render(4711, 1).orElseThrow();

        assertThat(new String(rendered.pdf(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void unknownInvoiceIsEmpty() {
        InvoicePrintSnapshotLoader loader = Mockito.mock(InvoicePrintSnapshotLoader.class);
        when(loader.load(anyInt(), anyInt())).thenReturn(Optional.empty());

        assertThat(new SaleInvoicePdfService(loader, Mockito.mock(InvoicePrintEInvoiceBackfill.class)).render(1, 1)).isEmpty();
    }

    private static InvoicePrintSnapshot snapshot(boolean eInvoiced) {
        InvoicePrintSnapshot.InvoicePrintSnapshotBuilder b = InvoicePrintSnapshot.builder()
                .headerLines(List.of(
                        "MALEVA (M) SDN BHD(943786-K)",
                        "Tax Reg. No: W10-1809-32001584",
                        "No 20-1 Jalan MPMU 2,Medan Perniagaan Utama Mambau,",
                        "70300 Seremban, Negeri Sembilan, Malaysia.",
                        "Tel: 012-290 7151 & 012-241 7151",
                        "URL : www.maleva.com.my Email : operation@maleva.com.my"))
                .heading("INVOICE")
                .notes(List.of(
                        "1.All Cheque Should be crossed and made payable to MALEVA(M) SDN BHD A/C No. (MYR/USD/SGD): 80-0458 325-1 Bank: CIMB Bank - Swift code: CIBBMYKL",
                        "2.Any Discrepency should be notified within 7 days,otherwise all charges will be deemed correct.",
                        "3.We reserved the right to charge an interest rate of 1.5% p.a on overdue accounts."))
                .generatedNote("This is an computer generated invoice. No signatory is required")
                .invoiceId(4711)
                .invoiceNo("INV000004711")
                .invoiceDate(LocalDate.of(2026, 9, 5))
                .customerLine("ACME LOGISTICS SDN BHD/Handle with care")
                .customerAddress("LOT 1, JALAN INDUSTRI 2, SEKSYEN 15,\n40000 SHAH ALAM, SELANGOR")
                .attentionName("MR TAN")
                .attentionPhone("+60312345678")
                .paymentTerms("30 DAYS")
                .currencySymbol("RM")
                .jobNo("SO000009001")
                .origin("PORT KLANG").destination("SHAH ALAM")
                .weight("12,000 KG").packages("40")
                .vesselOnboard("MV STAR").vesselOffland("NIL")
                .doNo("DO000000123").commodity("STEEL COILS")
                .collectionDate(LocalDate.of(2026, 9, 1)).deliveryDate(LocalDate.of(2026, 9, 2))
                .blAwb("BL-778")
                .truckSize("40FT").truckName("WXY 1234")
                .reference("PO-778")
                .subtotal(new BigDecimal("250.00"))
                .taxTotal(new BigDecimal("12.00"))
                .roundingAdjustment(new BigDecimal("0.00"))
                .netTotal(new BigDecimal("262.00"))
                .amountInWords(AmountInWords.of("RM", new BigDecimal("262.00")))
                .lines(List.of(
                        InvoicePrintSnapshot.InvoicePrintLine.builder().rowNumber(1).productCode("HANDLING")
                                .description("HANDLING CHARGES VESSEL MV STAR")
                                .quantity(new BigDecimal("2.00")).uom("UNIT(S)").unitPrice(new BigDecimal("100.00"))
                                .discountPercent(new BigDecimal("0.00")).taxCode("SV6")
                                .taxPercent(new BigDecimal("6.00")).taxAmount(new BigDecimal("12.00"))
                                .lineSubtotal(new BigDecimal("200.00")).amount(new BigDecimal("212.00")).build(),
                        InvoicePrintSnapshot.InvoicePrintLine.builder().rowNumber(2).productCode("DOCFEE")
                                .description("DOCUMENTATION FEE")
                                .quantity(new BigDecimal("1.00")).uom("UNIT(S)").unitPrice(new BigDecimal("50.00"))
                                .discountPercent(new BigDecimal("0.00")).taxCode("")
                                .taxPercent(new BigDecimal("0.00")).taxAmount(new BigDecimal("0.00"))
                                .lineSubtotal(new BigDecimal("50.00")).amount(new BigDecimal("50.00")).build()))
                .eInvoiceUid("").eInvoiceLongId("").eInvoiceStatus("");

        if (eInvoiced) {
            b.eInvoiceUid("F9D425P6DS7BQ6X1TF8")
                    .eInvoiceLongId("YQH73576FY9VR57B")
                    .eInvoiceStatus("Valid")
                    .eInvoiceValidatedAt(LocalDateTime.of(2026, 9, 5, 9, 31))
                    .eInvoiceShareUrl("https://preprod.myinvois.hasil.gov.my/F9D425P6DS7BQ6X1TF8/share/YQH73576FY9VR57B")
                    .qrPng(new my.maleva.api.integration.myinvois.MyInvoisQrCode()
                            .png("https://preprod.myinvois.hasil.gov.my/F9D425P6DS7BQ6X1TF8/share/YQH73576FY9VR57B"));
        }
        return b.build();
    }
}
