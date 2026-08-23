package my.maleva.api.module.filehandling.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Renders each page of a PDF to its own image file. */
public interface PdfPageRasterizer {

    /**
     * Writes {@code baseName_Page_1.jpg}, {@code baseName_Page_2.jpg} and so on
     * into {@code outputDirectory}.
     *
     * @return the written files, in page order
     * @throws IOException if the PDF cannot be read or a page cannot be written
     */
    List<Path> rasterize(Path pdfFile, Path outputDirectory, String baseName) throws IOException;
}
