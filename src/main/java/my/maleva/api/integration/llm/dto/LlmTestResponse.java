package my.maleva.api.integration.llm.dto;

public record LlmTestResponse(
        String provider,
        String model,
        String text,
        Long inputTokens,
        Long outputTokens,
        long latencyMs) {
}
