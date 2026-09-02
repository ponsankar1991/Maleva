package my.maleva.api.integration.llm;

/** What a provider hands back. Token counts are null when the provider does not report them. */
public record LlmResponse(
        String providerKey,
        String model,
        String text,
        Long inputTokens,
        Long outputTokens,
        long latencyMs,
        String stopReason) {
}
