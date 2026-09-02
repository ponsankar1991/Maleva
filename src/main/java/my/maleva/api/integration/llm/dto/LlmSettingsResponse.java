package my.maleva.api.integration.llm.dto;

import java.util.List;

/** What the AI settings screen shows and edits for one company. */
public record LlmSettingsResponse(
        String yamlDefault,
        String companyDefault,
        String effectiveDefault,
        List<TaskSetting> tasks) {

    public record TaskSetting(String key, String label, String stored, String effective) {
    }
}
