package my.maleva.api.module.filehandling.service.impl;

import my.maleva.api.common.config.FileUploadConfig;
import my.maleva.api.module.filehandling.service.PdfPageRasterizer;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders PDF pages with PDFBox, which is already a dependency of this service.
 *
 * The legacy implementation shelled out to Ghostscript through
 * {@code Ghostscript.NET}, loading {@code gsdll64.dll} from the web root by
 * absolute path. That made the feature fail on any machine where the DLL was
 * missing or the wrong bitness, and it is the reason the PDF branch existed in
 * only one of the four upload actions.
 */
@Service
public class PdfPageRasterizerImpl implements PdfPageRasterizer {

    private final FileUploadConfig config;

    public PdfPageRasterizerImpl(FileUploadConfig config) {
        this.config = config;
    }

    @Override
    public List<Path> rasterize(Path pdfFile, Path outputDirectory, String baseName) throws IOException {
        Files.createDirectories(outputDirectory);

        List<Path> pages = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(pdfFile.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                BufferedImage image = renderer.renderImageWithDPI(
                        pageIndex, config.getPdfRenderDpi(), ImageType.RGB);

                Path pagePath = outputDirectory.resolve(baseName + "_Page_" + (pageIndex + 1) + ".jpg");
                if (!ImageIO.write(image, "jpg", pagePath.toFile())) {
                    throw new IOException("No JPEG writer available for PDF page " + (pageIndex + 1));
                }
                pages.add(pagePath);
            }
        }
        return pages;
    }
}
