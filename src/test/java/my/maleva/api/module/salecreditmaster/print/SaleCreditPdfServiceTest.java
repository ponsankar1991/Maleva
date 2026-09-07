package my.maleva.api.module.salecreditmaster.print;

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
 * Compiles the real credit-note template and renders a fixture note. This is
 * the test that catches a broken expression or a renamed getter in the
 * .jrxml — the template is only compiled at runtime, so nothing else would.
 */
class SaleCreditPdfServiceTest {

    @Test
    void rendersAPdfFromTheTemplate() {
        CreditNotePrintSnapshotLoader loader = Mockito.mock(CreditNotePrintSnapshotLoader.class);
        when(loader.load(anyInt(), anyInt())).thenReturn(Optional.of(snapshot(true)));

        SaleCreditPdfService service = new SaleCreditPdfService(loader, Mockito.mock(CreditNotePrintEInvoiceBackfill.class));
        SaleCreditPdfService.RenderedCreditNote rendered = service.render(77, 6).orElseThrow();

        assertThat(rendered.fileName()).isEqualTo("CN000000042.pdf");
        assertThat(new String(rendered.pdf(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(rendered.pdf().length).isGreaterThan(2000);

        // -Dcredit.note.pdf.out=<dir> writes the fixture PDF so the layout can be eyeballed.
        String out = System.getProperty("credit.note.pdf.out");
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
        CreditNotePrintSnapshotLoader loader = Mockito.mock(CreditNotePrintSnapshotLoader.class);
        when(loader.load(anyInt(), anyInt())).thenReturn(Optional.of(snapshot(false)));

        SaleCreditPdfService.RenderedCreditNote rendered =
                new SaleCreditPdfService(loader, Mockito.mock(CreditNotePrintEInvoiceBackfill.class))
                        .render(77, 6).orElseThrow();

        assertThat(new String(rendered.pdf(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void unknownCreditNoteIsEmpty() {
        CreditNotePrintSnapshotLoader loader = Mockito.mock(CreditNotePrintSnapshotLoader.class);
        when(loader.load(anyInt(), anyInt())).thenReturn(Optional.empty());

        assertThat(new SaleCreditPdfService(loader, Mockito.mock(CreditNotePrintEInvoiceBackfill.class)).render(1, 6))
                .isEmpty();
    }

    private static CreditNotePrintSnapshot snapshot(boolean eInvoiced) {
        CreditNotePrintSnapshot.CreditNotePrintSnapshotBuilder builder = CreditNotePrintSnapshot.builder()
                .headerLines(List.of(
                        "MALEVA (M) SDN BHD(943786-K)",
                        "Tax Reg. No: W10-1809-32001584",
                        "No 20-1 Jalan MPMU 2,Medan Perniagaan Utama Mambau,",
                        "70300 Seremban, Negeri Sembilan, Malaysia.",
                        "Tel: 012-290 7151 & 012-241 7151",
                        "URL : www.maleva.com.my Email : operation@maleva.com.my"))
                .heading("CREDIT NOTE")
                .notes(List.of("1.All Cheque Should be crossed and made payable to MALEVA(M) SDN BHD "))
                .generatedNoteLine1("This is an computer generated invoice.")
                .generatedNoteLine2("No signatory is required")
                .creditNoteId(77)
                .creditNoteNo("CN000000042")
                .creditNoteDate(LocalDate.of(2026, 8, 4))
                .customerName("ANCHOR MARINE SUPPLIES PTE LTD")
                .customerAddress("126, GUL CIRCLE," + System.lineSeparator() + "SINGAPORE 629594")
                .customerPhone("+6587994911")
                .attentionName("ACCOUNTS DEPT")
                .salesMan("VASUNTRA DEVI A/P")
                .invoiceNo("INV000039783")
                .invoiceDate(LocalDate.of(2026, 2, 28))
                .taxInvoiceNo("INV000039783(28/02/2026)")
                .subtotal(new BigDecimal("14.20"))
                .gstAmount(new BigDecimal("0.00"))
                .roundingAdjustment(new BigDecimal("0.00"))
                .netTotal(new BigDecimal("14.20"))
                .amountInWords("SINGAPORE   FOURTEEN AND TWENTY CENTS ONLY")
                .lines(List.of(
                        CreditNotePrintSnapshot.CreditNotePrintLine.builder()
                                .rowNumber(1)
                                .productCode("CREDIT NOTE")
                                .description("CREDIT NOTE")
                                .quantity(new BigDecimal("1.00"))
                                .uom("UNIT(S)")
                                .unitPrice(new BigDecimal("14.20"))
                                .discountAmount(new BigDecimal("0.00"))
                                .taxableAmount(new BigDecimal("0.00"))
                                .taxPercent(new BigDecimal("0.00"))
                                .taxAmount(new BigDecimal("0.00"))
                                .netAmount(new BigDecimal("14.20"))
                                .build()));

        if (eInvoiced) {
            builder.eInvoiceUid("UUID-1")
                    .eInvoiceLongId("LONG-1")
                    .eInvoiceStatus("Valid")
                    .eInvoiceValidatedAt(LocalDateTime.of(2026, 8, 4, 9, 40))
                    .eInvoiceShareUrl("https://preprod.myinvois.hasil.gov.my/UUID-1/share/LONG-1");
        }
        return builder.build();
    }
}
