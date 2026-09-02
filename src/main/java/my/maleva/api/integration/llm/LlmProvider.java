package my.maleva.api.integration.llm;

/**
 * One way of reaching a language model. Implementations must be thread-safe
 * and must translate every failure into an {@link LlmException} so the gateway
 * can decide whether to fall back to another provider.
 */
public interface LlmProvider {

    String key();

    LlmProviderInfo info();

    default boolean isConfigured() {
        return info().configured();
    }

    /**
     * Runs one completion. The gateway has already adapted attachments to what
     * {@link #info()} says this provider can read.
     */
    LlmResponse complete(LlmRequest request);
}
