package my.maleva.api.integration.llm.provider;

import my.maleva.api.common.config.LlmProperties;
import my.maleva.api.integration.llm.LlmProvider;
import my.maleva.api.integration.llm.LlmProviderInfo;
import my.maleva.api.integration.llm.LlmRequest;
import my.maleva.api.integration.llm.LlmResponse;

/**
 * Never calls a model. Returns the request's {@code sampleOutput} so every
 * screen can be exercised end to end without an API key. Must be chosen
 * explicitly - the gateway never falls back to it on its own unless it is
 * listed under {@code llm.fallback-providers}.
 */
public class StubLlmProvider implements LlmProvider {

    public static final String TYPE = "stub";

    private final String key;
    private final LlmProperties.Provider config;

    public StubLlmProvider(String key, LlmProperties.Provider config) {
        this.key = key;
        this.config = config;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public LlmProviderInfo info() {
        String label = config.getLabel() == null || config.getLabel().isBlank() ? "Stub (sample data, no AI)" : config.getLabel();
        String note = config.getNote() == null || config.getNote().isBlank()
                ? "Returns built-in sample data so screens can be tested without any API key. Never use for real bills."
                : config.getNote();
        return new LlmProviderInfo(key, label, TYPE, "stub", null, true, true, config.isEnabled(), true, note);
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        String text;
        if (request.getSampleOutput() != null && !request.getSampleOutput().isBlank()) {
            text = request.getSampleOutput();
        } else if (request.isJsonOutput()) {
            text = "{\"stub\":true,\"message\":\"Stub provider returns sample data only\"}";
        } else {
            text = "OK (stub provider - no AI model was called)";
        }
        return new LlmResponse(key, "stub", text, 0L, 0L, 0L, "stub");
    }
}
