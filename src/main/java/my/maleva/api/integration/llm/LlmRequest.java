package my.maleva.api.integration.llm;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Provider-independent description of one model call. Feature modules build
 * this and hand it to {@link LlmGateway}; they never talk to a provider directly.
 */
@Getter
@Builder(toBuilder = true)
public class LlmRequest {

    /** Task key such as {@link LlmTasks#BILL_EXTRACTION}; drives provider selection and the call log. */
    private final String task;

    /** Company whose stored provider preference applies. */
    private final Integer companyRefId;

    /** Explicit provider key; overrides every stored or configured preference. */
    private final String providerKey;

    private final String systemPrompt;

    private final String userPrompt;

    @Builder.Default
    private final List<LlmAttachment> attachments = List.of();

    /** Ask the provider for a JSON object (enables the provider's JSON mode where it has one). */
    @Builder.Default
    private final boolean jsonOutput = false;

    /** Optional JSON schema for providers that support schema-constrained output. */
    private final Map<String, Object> jsonSchema;

    /** Overrides the provider's configured max output tokens. */
    private final Integer maxTokens;

    /** Sampling temperature for providers that accept one (never sent to Claude). */
    private final Double temperature;

    /** What the stub provider returns for this request, so screens can be exercised without a model. */
    private final String sampleOutput;

    public boolean hasBinaryAttachments() {
        return attachments.stream().anyMatch(a -> a.isImage() || a.isPdf());
    }
}
