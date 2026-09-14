package my.maleva.api.module.saleorder.print;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Developer tool, not a test: reads a reference report PDF so a Jasper template
 * can be laid out against it. Writes every embedded image, a 150 dpi render and
 * one line per text run with its position (points from the top-left), size and
 * font. Runs only with {@code -Dlayout.in=<pdf> -Dlayout.out=<dir>}.
 */
class PdfLayoutDumpTool {

    @Test
    @EnabledIfSystemProperty(named = "layout.in", matches = ".+")
    void dump() throws Exception {
        File in = new File(System.getProperty("layout.in"));
        Path out = Path.of(System.getProperty("layout.out", in.getParent()));
        Files.createDirectories(out);

        try (PDDocument doc = Loader.loadPDF(in)) {
            PDPage page = doc.getPage(0);
            System.out.println("mediaBox=" + page.getMediaBox());

            ImageIO.write(new PDFRenderer(doc).renderImageWithDPI(0, 150), "png", out.resolve("page-150.png").toFile());

            int[] counter = {0};
            writeImages(page.getResources(), out, counter);

            List<String> lines = new ArrayList<>();
            PDFTextStripper stripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> positions) {
                    if (positions.isEmpty()) {
                        return;
                    }
                    TextPosition first = positions.get(0);
                    TextPosition last = positions.get(positions.size() - 1);
                    lines.add(String.format("x=%6.1f y=%6.1f w=%6.1f size=%4.1f font=%-28s | %s",
                            first.getXDirAdj(), first.getYDirAdj(),
                            last.getXDirAdj() + last.getWidthDirAdj() - first.getXDirAdj(),
                            first.getFontSizeInPt(),
                            first.getFont() == null ? "?" : first.getFont().getName(),
                            text));
                }
            };
            stripper.setSortByPosition(true);
            stripper.getText(doc);
            Files.write(out.resolve("layout.txt"), lines);
            System.out.println("images=" + counter[0] + " textRuns=" + lines.size());
        }
    }

    private static void writeImages(PDResources resources, Path out, int[] counter) throws IOException {
        if (resources == null) {
            return;
        }
        for (COSName name : resources.getXObjectNames()) {
            PDXObject xobject = resources.getXObject(name);
            if (xobject instanceof PDImageXObject image) {
                counter[0]++;
                File file = out.resolve("image-" + counter[0] + "-" + image.getWidth() + "x" + image.getHeight() + ".png").toFile();
                ImageIO.write(image.getImage(), "png", file);
            } else if (xobject instanceof PDFormXObject form) {
                writeImages(form.getResources(), out, counter);
            }
        }
    }
}
