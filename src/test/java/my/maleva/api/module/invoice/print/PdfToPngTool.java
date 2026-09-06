package my.maleva.api.module.invoice.print;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Developer tool, not a test: renders a PDF to PNG pages and dumps its text,
 * so a report layout can be inspected where no PDF viewer is available.
 * Runs only with {@code -Dpdf.in=<file> -Dpdf.out=<dir>}.
 */
class PdfToPngTool {

    @Test
    @EnabledIfSystemProperty(named = "pdf.in", matches = ".+")
    void render() throws Exception {
        File in = new File(System.getProperty("pdf.in"));
        Path out = Path.of(System.getProperty("pdf.out", in.getParent()));
        Files.createDirectories(out);
        try (PDDocument doc = Loader.loadPDF(in)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 110);
                ImageIO.write(image, "png", out.resolve("page-" + (i + 1) + ".png").toFile());
            }
            Files.writeString(out.resolve("text.txt"), new PDFTextStripper().getText(doc));
            System.out.println("pages=" + doc.getNumberOfPages() + " size=" + doc.getPage(0).getMediaBox());
        }
    }
}
