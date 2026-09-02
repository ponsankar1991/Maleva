package my.maleva.api.integration.llm;

/** Read-only description of a provider for the settings screen and for the document adapter. */
public record LlmProviderInfo(
        String key,
        String label,
        String type,
        String model,
        String visionModel,
        boolean supportsVision,
        boolean supportsPdf,
        boolean configured,
        boolean free,
        String note) {
}
