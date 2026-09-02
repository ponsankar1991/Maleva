package my.maleva.api.integration.llm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/** Blank or null provider keys clear the stored preference. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LlmSettingsUpdateRequest {

    private String defaultProvider;

    /** task key -> provider key (or null / blank to reset that task to the default). */
    private Map<String, String> tasks = new LinkedHashMap<>();
}
