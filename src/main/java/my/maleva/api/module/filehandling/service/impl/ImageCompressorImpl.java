package my.maleva.api.module.filehandling.service.impl;

import my.maleva.api.common.config.FileUploadConfig;
import my.maleva.api.module.filehandling.service.ImageCompressor;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * Downscales to a bounded box and re-encodes, matching the legacy
 * {@code Compressimage} helper: longest edge capped at 900px, aspect ratio
 * preserved, JPEG re-encoded at 50% quality.
 *
 * Two behaviours differ from the legacy helper on purpose. It decoded the
 * upload stream twice - once via {@code Image.FromStream} and again via
 * {@code new Bitmap(sourcePath)} on the already-consumed stream - which threw
 * for any non-seekable upload; the decode happens once here. And it drew every
 * format onto an opaque bitmap, so a transparent PNG came back with a black
 * background; formats that carry an alpha channel keep it.
 */
@Service
public class ImageCompressorImpl implements ImageCompressor {

    private static final Set<String> SUPPORTED = Set.of(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp");
    private static final Set<String> ALPHA_CAPABLE = Set.of(".png", ".gif", ".webp");

    private final FileUploadConfig config;

    public ImageCompressorImpl(FileUploadConfig config) {
        this.config = config;
    }

    @Override
    public boolean supports(String extension) {
        return extension != null && SUPPORTED.contains(extension.toLowerCase(Locale.ROOT));
    }

    @Override
    public void compressTo(InputStream source, Path target) throws IOException {
        BufferedImage original = ImageIO.read(source);
        if (original == null) {
            throw new IOException("Upload is not a readable image");
        }

        String extension = extensionOf(target);
        BufferedImage scaled = scale(original, extension);

        Files.createDirectories(target.getParent());
        if (extension.equals(".jpg") || extension.equals(".jpeg")) {
            writeJpeg(scaled, target);
        } else {
            if (extension.length() < 2) {
                // Reachable through IMAGES_ONLY, which compresses whatever it is
                // given: a file uploaded without an extension has no format to
                // encode back to.
                throw new IOException("Cannot store an image without a file extension");
            }
            String format = extension.substring(1);
            if (!ImageIO.write(scaled, format, target.toFile())) {
                throw new IOException("No image writer available for " + format);
            }
        }
    }

    private BufferedImage scale(BufferedImage original, String extension) {
        int maxDimension = config.getImageMaxDimension();
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        int width = originalWidth;
        int height = originalHeight;
        if (originalWidth > maxDimension || originalHeight > maxDimension) {
            double ratio = Math.min(
                    (double) maxDimension / originalWidth,
                    (double) maxDimension / originalHeight);
            width = Math.max(1, (int) Math.round(originalWidth * ratio));
            height = Math.max(1, (int) Math.round(originalHeight * ratio));
        }

        int imageType = ALPHA_CAPABLE.contains(extension)
                ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;

        BufferedImage scaled = new BufferedImage(width, height, imageType);
        Graphics2D graphics = scaled.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(original, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return scaled;
    }

    private void writeJpeg(BufferedImage image, Path target) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer available in this JVM");
        }

        ImageWriter writer = writers.next();
        try (OutputStream out = Files.newOutputStream(target);
             ImageOutputStream imageOut = ImageIO.createImageOutputStream(out)) {
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(config.getJpegQuality());

            writer.setOutput(imageOut);
            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }
    }

    private String extensionOf(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot).toLowerCase(Locale.ROOT) : "";
    }
}
