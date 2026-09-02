package my.maleva.api.integration.llm;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Pulls a JSON document out of model output. Models wrap JSON in code fences
 * or add a sentence before it often enough that every caller needs this.
 */
public final class LlmJson {

    private LlmJson() {
    }

    /** Returns the outermost JSON object or array in {@code text}, or throws {@link LlmException} (BAD_RESPONSE). */
    public static String extractJson(String text, String providerKey) {
        if (text == null || text.isBlank()) {
            throw new LlmException(LlmException.Kind.BAD_RESPONSE, providerKey, "The model returned an empty answer");
        }
        String body = text.trim();
        if (body.startsWith("```")) {
            int firstNewline = body.indexOf('\n');
            body = firstNewline > 0 ? body.substring(firstNewline + 1) : body.substring(3);
            int fenceEnd = body.lastIndexOf("```");
            if (fenceEnd >= 0) {
                body = body.substring(0, fenceEnd);
            }
            body = body.trim();
        }
        int objectStart = body.indexOf('{');
        int arrayStart = body.indexOf('[');
        if (objectStart < 0 && arrayStart < 0) {
            throw new LlmException(LlmException.Kind.BAD_RESPONSE, providerKey,
                    "The model did not return JSON: " + abbreviate(body));
        }
        int start;
        char close;
        if (objectStart >= 0 && (arrayStart < 0 || objectStart < arrayStart)) {
            start = objectStart;
            close = '}';
        } else {
            start = arrayStart;
            close = ']';
        }
        int end = body.lastIndexOf(close);
        if (end <= start) {
            throw new LlmException(LlmException.Kind.BAD_RESPONSE, providerKey,
                    "The model returned truncated JSON: " + abbreviate(body));
        }
        return body.substring(start, end + 1);
    }

    public static <T> T parse(ObjectMapper mapper, String text, Class<T> type, String providerKey) {
        String json = extractJson(text, providerKey);
        try {
            return mapper.readValue(json, type);
        } catch (Exception ex) {
            throw new LlmException(LlmException.Kind.BAD_RESPONSE, providerKey,
                    "The model's JSON did not match the expected shape: " + ex.getMessage(), ex);
        }
    }

    static String abbreviate(String text) {
        return text.length() > 160 ? text.substring(0, 160) + "..." : text;
    }
}
