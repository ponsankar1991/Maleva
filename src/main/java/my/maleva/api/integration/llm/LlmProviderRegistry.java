package my.maleva.api.integration.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.LlmProperties;
import my.maleva.api.integration.llm.provider.ClaudeLlmProvider;
import my.maleva.api.integration.llm.provider.OpenAiCompatibleLlmProvider;
import my.maleva.api.integration.llm.provider.StubLlmProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Builds one {@link LlmProvider} per {@code llm.providers.*} entry at startup. */
@Slf4j
@Component
public class LlmProviderRegistry {

    private final Map<String, LlmProvider> providers;

    @Autowired
    public LlmProviderRegistry(LlmProperties properties, ObjectMapper objectMapper) {
        this(build(properties, objectMapper));
    }

    /** Visible for tests. */
    LlmProviderRegistry(Map<String, LlmProvider> providers) {
        this.providers = new LinkedHashMap<>(providers);
    }

    static Map<String, LlmProvider> build(LlmProperties properties, ObjectMapper objectMapper) {
        Map<String, LlmProvider> out = new LinkedHashMap<>();
        for (Map.Entry<String, LlmProperties.Provider> entry : properties.getProviders().entrySet()) {
            String key = entry.getKey().trim().toLowerCase(Locale.ROOT);
            LlmProperties.Provider config = entry.getValue();
            String type = config.getType() == null ? "" : config.getType().trim().toLowerCase(Locale.ROOT);
            LlmProvider provider;
            switch (type) {
                case "claude":
                case "anthropic":
                    provider = new ClaudeLlmProvider(key, config);
                    break;
                case "openai-compatible":
                case "openai":
                case "ollama":
                    provider = new OpenAiCompatibleLlmProvider(key, config, objectMapper);
                    break;
                case "stub":
                case "fake":
                    provider = new StubLlmProvider(key, config);
                    break;
                default:
                    log.warn("LLM provider '{}' has unknown type '{}' and was skipped", key, config.getType());
                    continue;
            }
            LlmProviderInfo info = provider.info();
            log.info("LLM provider {} ({}) model={} vision={} pdf={} configured={} key={}",
                    key, info.type(), info.model(), info.supportsVision(), info.supportsPdf(), info.configured(),
                    "stub".equals(info.type()) ? "n/a" : LlmKeyMask.fingerprint(config.getApiKey()));
            out.put(key, provider);
        }
        if (out.isEmpty()) {
            log.warn("No LLM providers are declared under llm.providers - every AI feature will report 'not configured'");
        }
        return out;
    }

    public Optional<LlmProvider> find(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(providers.get(key.trim().toLowerCase(Locale.ROOT)));
    }

    public boolean has(String key) {
        return find(key).isPresent();
    }

    public Set<String> keys() {
        return providers.keySet();
    }

    public List<LlmProviderInfo> infos() {
        List<LlmProviderInfo> infos = new ArrayList<>();
        for (LlmProvider provider : providers.values()) {
            infos.add(provider.info());
        }
        return infos;
    }
}
