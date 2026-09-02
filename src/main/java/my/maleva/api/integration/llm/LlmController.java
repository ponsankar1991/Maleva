package my.maleva.api.integration.llm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.constant.SecurityConstants;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.integration.llm.dto.LlmSettingsResponse;
import my.maleva.api.integration.llm.dto.LlmSettingsUpdateRequest;
import my.maleva.api.integration.llm.dto.LlmTestRequest;
import my.maleva.api.integration.llm.dto.LlmTestResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI provider status and per-company provider choice. Reads are open to every
 * logged-in user (the bills screen needs to know what is available); writes
 * are admin-only.
 */
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
@Tag(name = "AI / LLM", description = "AI provider status, per-company provider choice, connection test and call log")
public class LlmController {

    private final LlmProviderRegistry registry;
    private final LlmSettingsService settings;
    private final LlmCallLog callLog;

    @GetMapping("/providers")
    @Operation(summary = "List AI providers", description = "Every provider declared under llm.providers with whether it is configured")
    public ResponseEntity<ApiResponse<List<LlmProviderInfo>>> providers() {
        return ResponseEntity.ok(ApiResponse.success(registry.infos(), "LLM providers"));
    }

    @GetMapping("/settings")
    @Operation(summary = "Company AI settings", description = "Stored and effective provider per task for a company")
    public ResponseEntity<ApiResponse<LlmSettingsResponse>> getSettings(@RequestParam Integer companyRefId) {
        return ResponseEntity.ok(ApiResponse.success(settings.get(companyRefId), "LLM settings"));
    }

    @PutMapping("/settings")
    @PreAuthorize(SecurityConstants.ROLE_ADMIN_SUPERADMIN)
    @Operation(summary = "Update company AI settings", description = "Admin only. Blank provider resets to the configured default")
    public ResponseEntity<ApiResponse<LlmSettingsResponse>> updateSettings(@RequestParam Integer companyRefId,
                                                                          @RequestBody LlmSettingsUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(settings.update(companyRefId, request), "LLM settings saved"));
    }

    @PostMapping("/test")
    @Operation(summary = "Test a provider", description = "Sends a short prompt to the chosen (or effective) provider")
    public ResponseEntity<ApiResponse<LlmTestResponse>> test(@RequestParam(required = false) Integer companyRefId,
                                                            @RequestParam(required = false) String provider,
                                                            @RequestBody(required = false) LlmTestRequest request) {
        return ResponseEntity.ok(ApiResponse.success(settings.test(companyRefId, provider, request), "LLM test completed"));
    }

    @GetMapping("/calls")
    @Operation(summary = "Recent AI calls", description = "In-memory log of recent model calls with tokens and latency")
    public ResponseEntity<ApiResponse<List<LlmCallRecord>>> calls(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.success(callLog.recent(Math.min(Math.max(limit, 1), 500)), "LLM calls"));
    }
}
