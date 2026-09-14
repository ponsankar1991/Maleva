package my.maleva.api.module.saleorder.print;

import my.maleva.api.common.config.InvoicePrintProperties;
import my.maleva.api.module.saleorder.dto.DoConvertResult;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Compiles the real delivery-order template and renders a fixture DO. The
 * template is only compiled at runtime, so this is the test that catches a
 * broken expression or a renamed getter in the .jrxml.
 *
 * <p>The fixture is the legacy print DOReport2605324.pdf, so the rendered text
 * can be compared with what the Crystal report put on paper.
 */
class DeliveryOrderPdfServiceTest {

    @Test
    void rendersEveryLegacyFieldOnOnePage() throws Exception {
        DeliveryOrderPdfService.RenderedDeliveryOrder rendered = service().render(prepared(
                row("PUMPING OF IBF FOR SILVER LUCKY")));

        assertThat(rendered.fileName()).isEqualTo("DO000016139.pdf");
        assertThat(new String(rendered.pdf(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");

        try (PDDocument doc = Loader.loadPDF(rendered.pdf())) {
            assertThat(doc.getNumberOfPages()).isEqualTo(1);
            String text = new PDFTextStripper().getText(doc);
            assertThat(text).contains(
                    "MY002605324", "MALEVA (M) SDN BHD(943786-K)", "Delivery Report",
                    "MARQUIS OIL(M)SDN BHD", "ISKANDAR PUTERI JOHOR", "ACCOUNTS DEPT",
                    "LAND TRANSPORT ONBOARD", "SILVER LUCKY", "DO000016139", "14/09/2026", "9 IBCS",
                    "PUMPING OF IBF FOR SILVER LUCKY", "CAPTAIN EMAIL ADDRESS:", "TERMS AND CONDITIONS:",
                    "consignee's task", "Start Loading :", "End Loading :",
                    "Boarding Officer", "Recipient's Chop and Signature", "Page 1 of");
        }

        // -Ddo.pdf.out=<dir> writes the fixture PDF so the layout can be eyeballed.
        String out = System.getProperty("do.pdf.out");
        if (out != null && !out.isBlank()) {
            java.nio.file.Files.write(java.nio.file.Path.of(out, rendered.fileName()), rendered.pdf());
        }
    }

    @Test
    void manyDescribedLinesFlowOntoMorePagesAndBlankOnesAreSkipped() throws Exception {
        DoConvertResult.DoView[] rows = new DoConvertResult.DoView[60];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = row(i % 10 == 0 ? "   " : "Line " + (i + 1) + " of the delivery");
        }

        DeliveryOrderPdfService.RenderedDeliveryOrder rendered = service().render(prepared(rows));

        try (PDDocument doc = Loader.loadPDF(rendered.pdf())) {
            assertThat(doc.getNumberOfPages()).isGreaterThan(1);
            String text = new PDFTextStripper().getText(doc);
            assertThat(text).contains("Line 2 of the delivery").doesNotContain("Line 1 of the delivery");
        }
    }

    @Test
    void refusesADoThatWasNotPrepared() {
        assertThatThrownBy(() -> service().render(DoConvertResult.failure("No Record")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fileNameIsSafeForTheDownloadPath() {
        assertThat(DeliveryOrderPdfService.fileNameFor("DO 00/12")).isEqualTo("DO_00_12.pdf");
        assertThat(DeliveryOrderPdfService.fileNameFor("  ")).isEqualTo("delivery-order.pdf");
    }

    @Test
    void termsAreSeparatedByABlankLineAndBlanksDropped() {
        assertThat(DeliveryOrderPdfService.termsText(List.of(" 1. First ", "", "2. Second")))
                .isEqualTo("1. First\n\n2. Second");
    }

    private static DeliveryOrderPdfService service() {
        InvoicePrintProperties letterhead = new InvoicePrintProperties();
        letterhead.setHeaderLines(List.of(
                "MALEVA (M) SDN BHD(943786-K)",
                "Tax Reg. No: W10-1809-32001584",
                "No 20-1 Jalan MPMU 2,Medan Perniagaan Utama Mambau,",
                "70300 Seremban, Negeri Sembilan, Malaysia.",
                "Tel: 012-290 7151 & 012-241 7151",
                "URL : www.maleva.com.my Email : operation@maleva.com.my"));
        return new DeliveryOrderPdfService(letterhead, new DeliveryOrderPrintProperties());
    }

    private static DoConvertResult prepared(DoConvertResult.DoView... rows) {
        return new DoConvertResult(true, "DO DO000016139 prepared", 16139, "DO000016139", List.of(rows));
    }

    private static DoConvertResult.DoView row(String description) {
        return DoConvertResult.DoView.builder()
                .doNo("DO000016139")
                .jobNo("MY002605324")
                .saleDate("14/09/2026")
                .customerName("MARQUIS OIL(M)SDN BHD")
                .address("NO 2 JALAN MEGA 1/7,\nTAMAN PERINDUSTRIAN NUSA CEMERLANG 79200\nISKANDAR PUTERI JOHOR")
                .attnName("ACCOUNTS DEPT")
                .jobName("LAND TRANSPORT ONBOARD")
                .awbNo("")
                .blCopy(null)
                .loadingVesselName("")
                .offVesselName("SILVER LUCKY")
                .quantity("9 IBCS")
                .totalWeight("")
                .doDescription(description)
                .build();
    }
}
