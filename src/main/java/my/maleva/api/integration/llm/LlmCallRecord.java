package my.maleva.api.integration.llm;

import java.time.Instant;

/** One entry of the in-memory call log shown on the AI settings screen. */
public record LlmCallRecord(
        Instant timestamp,
        String provider,
        String model,
        String task,
        Integer companyRefId,
        boolean success,
        Long inputTokens,
        Long outputTokens,
        long latencyMs,
        String error) {
}
