package my.maleva.api.module.paymentrecept.print;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Compiles the real template and renders a fixture receipt. This is the test
 * that catches a broken expression or a renamed getter in the .jrxml — the
 * template is only compiled at runtime, so nothing else would.
 *
 * <p>{@code -Dreceipt.pdf.out=<dir>} writes the fixture PDF so the layout can
 * be compared with a legacy Crystal export (e.g. Pdf/ReceiptRC000002284.pdf).
 */
class ReceiptPdfServiceTest {

    @Test
    void rendersAPdfFromTheTemplate() {
        ReceiptPrintSnapshotLoader loader = Mockito.mock(ReceiptPrintSnapshotLoader.class);
        when(loader.load(anyInt(), anyInt())).thenReturn(Optional.of(snapshot()));

        ReceiptPdfService.RenderedReceipt rendered = new ReceiptPdfService(loader).render(2284, 6).orElseThrow();

        assertThat(rendered.fileName()).isEqualTo("ReceiptRC000002284.pdf");
        assertThat(new String(rendered.pdf(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(rendered.pdf().length).isGreaterThan(2000);

        String out = System.getProperty("receipt.pdf.out");
        if (out != null && !out.isBlank()) {
            try {
                java.nio.file.Files.write(java.nio.file.Path.of(out, rendered.fileName()), rendered.pdf());
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Test
    void rendersAReceiptWithNoLines() {
        ReceiptPrintSnapshotLoader loader = Mockito.mock(ReceiptPrintSnapshotLoader.class);
        ReceiptPrintSnapshot empty = snapshot();
        empty.setLines(List.of());
        when(loader.load(anyInt(), anyInt())).thenReturn(Optional.of(empty));

        assertThat(new ReceiptPdfService(loader).render(1, 6)).isPresent();
    }

    @Test
    void unknownReceiptIsEmpty() {
        ReceiptPrintSnapshotLoader loader = Mockito.mock(ReceiptPrintSnapshotLoader.class);
        when(loader.load(anyInt(), anyInt())).thenReturn(Optional.empty());

        assertThat(new ReceiptPdfService(loader).render(1, 6)).isEmpty();
    }

    @Test
    void fileNameKeepsOnlyLettersAndDigits() {
        assertThat(ReceiptPdfService.fileName("RC/0000-2284", 9)).isEqualTo("ReceiptRC00002284.pdf");
        assertThat(ReceiptPdfService.fileName("", 9)).isEqualTo("Receipt9.pdf");
        assertThat(ReceiptPdfService.fileName(null, 9)).isEqualTo("Receipt9.pdf");
    }

    /** The RC000002284 voucher from the legacy Pdf folder, as a snapshot. */
    static ReceiptPrintSnapshot snapshot() {
        return ReceiptPrintSnapshot.builder()
                .headerLines(List.of(
                        "MALEVA (M) SDN BHD(943786-K)",
                        "Tax Reg. No: W10-1809-32001584",
                        "No 20-1 Jalan MPMU 2,Medan Perniagaan Utama Mambau,",
                        "70300 Seremban, Negeri Sembilan, Malaysia.",
                        "Tel: 012-290 7151 & 012-241 7151",
                        "URL : www.maleva.com.my Email : operation@maleva.com.my"))
                .heading("RECEIPT VOUCHER")
                .nbNote("Validity of this receipt subject to clearing of cheque")
                .generatedNote("This is an computer generated receipt.")
                .receiptId(2284)
                .receiptNo("RC000002284")
                .receiptDate(LocalDate.of(2026, 8, 17))
                .chequeNo("2026081700006157")
                .customerName("3FM LOGISTICS PTE LTD")
                .customerAddress("10 BUROH STREET, #07-39, \nWEST CONNECT BUILDING, \nSINGAPORE 627564.")
                .customerPhone("+6581571960")
                .attentionName("ROCKY")
                .accountCode("700-3001")
                .accountName("3FM LOGISTICS PTE LTD")
                .description("SGD589.260@3.1119 by order of")
                .amount(new BigDecimal("589.26"))
                .currencyName("SINGAPORE")
                .currencySymbol("SGD")
                .subTotal(new BigDecimal("589.26"))
                .roundingAdjustment(new BigDecimal("0.00"))
                .netTotal(new BigDecimal("589.26"))
                .amountInWords("SINGAPORE   FIVE HUNDRED EIGHTY-NINE AND TWENTY-SIX CENTS ONLY")
                .lines(List.of(ReceiptPrintSnapshot.ReceiptPrintLine.builder()
                        .rowNumber(1).docType("INV").docNo("INV000043143").docDate("24/07/2026")
                        .description("MOL EARNEST")
                        .originalAmount(new BigDecimal("589.26")).paidAmount(new BigDecimal("589.26"))
                        .build()))
                .build();
    }
}
