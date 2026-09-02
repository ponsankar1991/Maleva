package my.maleva.api.integration.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.LlmProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The single entry point feature modules use to call a model. Picks the
 * provider (explicit override, company preference, configured default),
 * adapts attachments to what that provider can read, and walks the fallback
 * chain when a provider fails. Every attempt lands in {@link LlmCallLog}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmGateway {

    private final LlmProperties properties;
    private final LlmProviderRegistry registry;
    private final LlmPreferenceStore preferences;
    private final LlmDocumentAdapter adapter;
    private final LlmCallLog callLog;

    /**
     * Provider key that would handle {@code task} for {@code companyRefId}:
     * explicit override, company task preference, company default, configured
     * task override, configured default - first one present wins.
     */
    public String resolveProviderKey(Integer companyRefId, String task, String explicit) {
        if (explicit != null && !explicit.isBlank()) {
            return explicit.trim();
        }
        if (companyRefId != null) {
            Optional<String> stored = preferences.get(companyRefId, task);
            if (stored.isPresent() && registry.has(stored.get())) {
                return stored.get();
            }
            Optional<String> companyDefault = preferences.get(companyRefId, null);
            if (companyDefault.isPresent() && registry.has(companyDefault.get())) {
                return companyDefault.get();
            }
        }
        if (task != null) {
            String configured = properties.getTasks().get(task);
            if (configured != null && !configured.isBlank()) {
                return configured.trim();
            }
        }
        return properties.getDefaultProvider() == null ? "" : properties.getDefaultProvider().trim();
    }

    /** Primary followed by the configured fallbacks, without duplicates or blanks. */
    public List<String> chainFor(String primary) {
        Set<String> chain = new LinkedHashSet<>();
        if (primary != null && !primary.isBlank()) {
            chain.add(primary.trim());
        }
        for (String fallback : properties.getFallbackProviders()) {
            if (fallback != null && !fallback.isBlank()) {
                chain.add(fallback.trim());
            }
        }
        return new ArrayList<>(chain);
    }

    public LlmResponse complete(LlmRequest request) {
        if (!properties.isEnabled()) {
            throw new LlmException(LlmException.Kind.DISABLED, null, "AI features are disabled (llm.enabled=false)");
        }
        boolean explicit = request.getProviderKey() != null && !request.getProviderKey().isBlank();
        String primary = resolveProviderKey(request.getCompanyRefId(), request.getTask(), request.getProviderKey());
        // An explicit choice is the user testing that provider: never silently swap it.
        List<String> chain = explicit ? List.of(primary) : chainFor(primary);
        if (chain.isEmpty()) {
            throw new LlmException(LlmException.Kind.NOT_CONFIGURED, null,
                    "No AI provider is configured (llm.default-provider is blank)");
        }

        LlmException first = null;
        for (String key : chain) {
            Optional<LlmProvider> provider = registry.find(key);
            if (provider.isEmpty()) {
                LlmException unknown = new LlmException(LlmException.Kind.NOT_CONFIGURED, key,
                        "Unknown AI provider '" + key + "'. Configured providers: " + registry.keys());
                callLog.record(new LlmCallRecord(Instant.now(), key, null, request.getTask(), request.getCompanyRefId(),
                        false, null, null, 0, unknown.getMessage()));
                first = first == null ? unknown : first;
                continue;
            }
            long start = System.nanoTime();
            try {
                List<LlmAttachment> adapted = adapter.adapt(request.getAttachments(), provider.get().info());
                LlmRequest effective = adapted == request.getAttachments() ? request
                        : request.toBuilder().attachments(adapted).build();
                if (properties.isLogPrompts()) {
                    log.debug("LLM prompt to {} for {}:\n{}\n{}", key, request.getTask(),
                            request.getSystemPrompt(), request.getUserPrompt());
                }
                LlmResponse response = provider.get().complete(effective);
                callLog.record(new LlmCallRecord(Instant.now(), key, response.model(), request.getTask(),
                        request.getCompanyRefId(), true, response.inputTokens(), response.outputTokens(),
                        response.latencyMs(), null));
                log.info("LLM call ok provider={} model={} task={} company={} in={} out={} latencyMs={}",
                        key, response.model(), request.getTask(), request.getCompanyRefId(),
                        response.inputTokens(), response.outputTokens(), response.latencyMs());
                return response;
            } catch (LlmException ex) {
                long latency = (System.nanoTime() - start) / 1_000_000;
                callLog.record(new LlmCallRecord(Instant.now(), key, provider.get().info().model(), request.getTask(),
                        request.getCompanyRefId(), false, null, null, latency, ex.getKind() + ": " + ex.getMessage()));
                log.warn("LLM call failed provider={} task={} kind={}: {}", key, request.getTask(), ex.getKind(), ex.getMessage());
                if (!ex.fallbackEligible()) {
                    throw ex;
                }
                first = first == null ? ex : first;
            }
        }
        throw first != null ? first
                : new LlmException(LlmException.Kind.NOT_CONFIGURED, primary, "No AI provider could handle the request");
    }
}
