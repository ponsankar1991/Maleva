package my.maleva.api.integration.llm;

/**
 * Masks an API key for logs and error messages: enough to recognise which
 * key was used (Google AI Studio shows keys as "...8db4"), never enough to
 * reuse it.
 */
public final class LlmKeyMask {

    private LlmKeyMask() {
    }

    public static String fingerprint(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "(no key)";
        }
        String key = apiKey.trim();
        if (key.length() <= 8) {
            return "*".repeat(key.length()) + " (length " + key.length() + ")";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4) + " (length " + key.length() + ")";
    }
}
