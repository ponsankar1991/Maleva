package my.maleva.api.integration.llm;

import lombok.RequiredArgsConstructor;
import my.maleva.api.common.config.LlmProperties;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.integration.llm.dto.LlmSettingsResponse;
import my.maleva.api.integration.llm.dto.LlmSettingsUpdateRequest;
import my.maleva.api.integration.llm.dto.LlmTestRequest;
import my.maleva.api.integration.llm.dto.LlmTestResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reads and writes the per-company provider choice shown on the AI settings screen. */
@Service
@RequiredArgsConstructor
public class LlmSettingsService {

    private final LlmProperties properties;
    private final LlmProviderRegistry registry;
    private final LlmPreferenceStore preferences;
    private final LlmGateway gateway;

    public LlmSettingsResponse get(Integer companyRefId) {
        requireCompany(companyRefId);
        String yamlDefault = properties.getDefaultProvider();
        String companyDefault = preferences.get(companyRefId, null).orElse(null);
        String effectiveDefault = gateway.resolveProviderKey(companyRefId, null, null);
        List<LlmSettingsResponse.TaskSetting> tasks = new ArrayList<>();
        for (LlmTasks.TaskDef task : LlmTasks.CONFIGURABLE) {
            String stored = preferences.get(companyRefId, task.key()).orElse(null);
            String effective = gateway.resolveProviderKey(companyRefId, task.key(), null);
            tasks.add(new LlmSettingsResponse.TaskSetting(task.key(), task.label(), stored, effective));
        }
        return new LlmSettingsResponse(yamlDefault, companyDefault, effectiveDefault, tasks);
    }

    public LlmSettingsResponse update(Integer companyRefId, LlmSettingsUpdateRequest request) {
        requireCompany(companyRefId);
        if (request == null) {
            throw new InvalidRequestException("Request body is required");
        }
        validateProvider(request.getDefaultProvider());
        preferences.put(companyRefId, null, request.getDefaultProvider());
        if (request.getTasks() != null) {
            for (Map.Entry<String, String> entry : request.getTasks().entrySet()) {
                if (!LlmTasks.isConfigurable(entry.getKey())) {
                    throw new InvalidRequestException("Unknown AI task '" + entry.getKey() + "'");
                }
                validateProvider(entry.getValue());
                preferences.put(companyRefId, entry.getKey(), entry.getValue());
            }
        }
        return get(companyRefId);
    }

    public LlmTestResponse test(Integer companyRefId, String providerKey, LlmTestRequest request) {
        String prompt = request == null || request.getPrompt() == null || request.getPrompt().isBlank()
                ? "Reply with the single word OK." : request.getPrompt();
        if (providerKey != null && !providerKey.isBlank()) {
            validateProvider(providerKey);
        }
        LlmResponse response = gateway.complete(LlmRequest.builder()
                .task(LlmTasks.CONNECTION_TEST)
                .companyRefId(companyRefId)
                .providerKey(providerKey)
                .userPrompt(prompt)
                .maxTokens(512)
                .sampleOutput("OK (stub provider - no AI model was called)")
                .build());
        return new LlmTestResponse(response.providerKey(), response.model(), response.text(),
                response.inputTokens(), response.outputTokens(), response.latencyMs());
    }

    private void validateProvider(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        if (!registry.has(key)) {
            throw new InvalidRequestException("Unknown AI provider '" + key + "'. Available: " + registry.keys());
        }
    }

    private static void requireCompany(Integer companyRefId) {
        if (companyRefId == null) {
            throw new InvalidRequestException("companyRefId is required");
        }
    }
}
