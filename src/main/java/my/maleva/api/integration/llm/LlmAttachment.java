package my.maleva.api.integration.llm;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

/**
 * A file handed to the model alongside the prompt: a PDF, an image, or plain
 * text that was already extracted from a document.
 */
public record LlmAttachment(String fileName, String mediaType, byte[] data) {

    public static final String TEXT_PLAIN = "text/plain";
    public static final String APPLICATION_PDF = "application/pdf";

    public static LlmAttachment text(String fileName, String text) {
        return new LlmAttachment(fileName, TEXT_PLAIN, text.getBytes(StandardCharsets.UTF_8));
    }

    public String normalizedMediaType() {
        if (mediaType == null) {
            return "";
        }
        String type = mediaType.trim().toLowerCase(Locale.ROOT);
        if (type.equals("image/jpg")) {
            return "image/jpeg";
        }
        return type;
    }

    public boolean isPdf() {
        return APPLICATION_PDF.equals(normalizedMediaType());
    }

    public boolean isImage() {
        return normalizedMediaType().startsWith("image/");
    }

    public boolean isText() {
        return TEXT_PLAIN.equals(normalizedMediaType());
    }

    public String base64() {
        return Base64.getEncoder().encodeToString(data);
    }

    public String asText() {
        return new String(data, StandardCharsets.UTF_8);
    }

    public int sizeBytes() {
        return data == null ? 0 : data.length;
    }
}
