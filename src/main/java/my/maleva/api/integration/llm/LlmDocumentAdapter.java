package my.maleva.api.integration.llm;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reshapes attachments to what a provider can read: PDFs become page images
 * for vision-only providers and extracted text for text-only providers;
 * oversized images are downscaled. Providers that read PDFs natively get the
 * original bytes.
 */
@Slf4j
@Component
public class LlmDocumentAdapter {

    static final int MAX_PDF_PAGES = 5;
    static final float RENDER_DPI = 120f;
    static final int MAX_IMAGE_EDGE = 2000;
    static final int MAX_IMAGE_BYTES = 4 * 1024 * 1024;

    public List<LlmAttachment> adapt(List<LlmAttachment> attachments, LlmProviderInfo provider) {
        List<LlmAttachment> out = new ArrayList<>();
        for (LlmAttachment attachment : attachments) {
            if (attachment.isPdf()) {
                if (provider.supportsPdf()) {
                    out.add(attachment);
                } else if (provider.supportsVision()) {
                    out.addAll(rasterize(attachment, provider));
                } else {
                    out.add(extractText(attachment, provider));
                }
            } else if (attachment.isImage()) {
                if (!provider.supportsVision()) {
                    throw new LlmException(LlmException.Kind.UNSUPPORTED_INPUT, provider.key(),
                            provider.label() + " cannot read images. Choose a provider that reads images "
                                    + "(Claude, or Ollama/Groq/Gemini with a vision model) in AI Settings");
                }
                out.add(downscale(attachment, provider));
            } else {
                out.add(attachment);
            }
        }
        return out;
    }

    private List<LlmAttachment> rasterize(LlmAttachment pdf, LlmProviderInfo provider) {
        List<LlmAttachment> pages = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(pdf.data())) {
            int total = document.getNumberOfPages();
            int count = Math.min(total, MAX_PDF_PAGES);
            if (total > MAX_PDF_PAGES) {
                log.warn("PDF {} has {} pages; only the first {} are sent to {}", pdf.fileName(), total, MAX_PDF_PAGES, provider.key());
            }
            PDFRenderer renderer = new PDFRenderer(document);
            String base = pdf.fileName() == null ? "document" : pdf.fileName().replaceAll("(?i)\\.pdf$", "");
            for (int i = 0; i < count; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, RENDER_DPI, ImageType.RGB);
                pages.add(new LlmAttachment(base + "-page" + (i + 1) + ".jpg", "image/jpeg", jpeg(image)));
            }
        } catch (IOException ex) {
            throw new LlmException(LlmException.Kind.UNSUPPORTED_INPUT, provider.key(),
                    "Could not render the PDF " + pdf.fileName() + ": " + ex.getMessage(), ex);
        }
        if (pages.isEmpty()) {
            throw new LlmException(LlmException.Kind.UNSUPPORTED_INPUT, provider.key(),
                    "The PDF " + pdf.fileName() + " has no pages");
        }
        return pages;
    }

    private LlmAttachment extractText(LlmAttachment pdf, LlmProviderInfo provider) {
        String text;
        try (PDDocument document = Loader.loadPDF(pdf.data())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setEndPage(Math.min(document.getNumberOfPages(), MAX_PDF_PAGES * 2));
            text = stripper.getText(document);
        } catch (IOException ex) {
            throw new LlmException(LlmException.Kind.UNSUPPORTED_INPUT, provider.key(),
                    "Could not read the PDF " + pdf.fileName() + ": " + ex.getMessage(), ex);
        }
        if (text == null || text.isBlank()) {
            throw new LlmException(LlmException.Kind.UNSUPPORTED_INPUT, provider.key(),
                    "The PDF " + pdf.fileName() + " is a scan with no text layer, and " + provider.label()
                            + " cannot read images. Choose a provider that reads images in AI Settings");
        }
        return LlmAttachment.text(pdf.fileName(), text.trim());
    }

    private LlmAttachment downscale(LlmAttachment image, LlmProviderInfo provider) {
        BufferedImage source;
        try {
            source = ImageIO.read(new ByteArrayInputStream(image.data()));
        } catch (IOException ex) {
            source = null;
        }
        if (source == null) {
            // ImageIO has no WEBP reader; pass the bytes through as they are.
            return image;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        int longest = Math.max(width, height);
        if (longest <= MAX_IMAGE_EDGE && image.sizeBytes() <= MAX_IMAGE_BYTES) {
            return image;
        }
        double scale = Math.min(1.0, (double) MAX_IMAGE_EDGE / longest);
        int newWidth = Math.max(1, (int) Math.round(width * scale));
        int newHeight = Math.max(1, (int) Math.round(height * scale));
        BufferedImage target = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setColor(java.awt.Color.WHITE);
        graphics.fillRect(0, 0, newWidth, newHeight);
        graphics.drawImage(source, 0, 0, newWidth, newHeight, null);
        graphics.dispose();
        try {
            String name = image.fileName() == null ? "image.jpg" : image.fileName().replaceAll("\\.[A-Za-z0-9]+$", "") + ".jpg";
            log.debug("Downscaled {} from {}x{} to {}x{} for {}", image.fileName(), width, height, newWidth, newHeight, provider.key());
            return new LlmAttachment(name, "image/jpeg", jpeg(target));
        } catch (IOException ex) {
            return image;
        }
    }

    private static byte[] jpeg(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "jpg", out)) {
            throw new IOException("No JPEG writer available");
        }
        return out.toByteArray();
    }
}
