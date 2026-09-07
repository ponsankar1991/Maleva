package my.maleva.api.module.invoice.print;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.File;
import java.util.List;

/**
 * Developer tool, not a test: prints every text run of a PDF with its font,
 * size and position, so a Crystal page can be measured instead of guessed at
 * when matching the Jasper template to it.
 *
 * <pre>
 * mvn -o -q test -Dtest=PdfFontDumpTool -Dsurefire.failIfNoSpecifiedTests=false \
 *     -Dfont.pdf.in=C:/tmp/crReport.pdf
 * </pre>
 */
class PdfFontDumpTool {

    @Test
    @EnabledIfSystemProperty(named = "font.pdf.in", matches = ".+")
    void run() throws Exception {
        File in = new File(System.getProperty("font.pdf.in"));
        try (PDDocument doc = Loader.loadPDF(in)) {
            PDFTextStripper stripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> positions) {
                    if (text.isBlank() || positions.isEmpty()) {
                        return;
                    }
                    TextPosition first = positions.get(0);
                    String font = first.getFont() == null ? "?" : first.getFont().getName();
                    System.out.printf("%-34s size=%5.2f  x=%6.1f y=%6.1f  %s%n",
                            font,
                            first.getFontSizeInPt(),
                            first.getXDirAdj(),
                            first.getYDirAdj(),
                            text.length() > 60 ? text.substring(0, 60) : text);
                }
            };
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            stripper.getText(doc);
        }
    }
}
