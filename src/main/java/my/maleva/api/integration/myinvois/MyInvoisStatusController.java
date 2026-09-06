package my.maleva.api.integration.myinvois;

import my.maleva.api.common.config.MyInvoisProperties;
import my.maleva.api.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Operational view of the MyInvois integration: which environment and
 * taxpayer this server would submit under, and whether it holds a token.
 * Secrets are never shown — only whether they are configured.
 */
@RestController
@RequestMapping("/api/myinvois")
public class MyInvoisStatusController {

    private final MyInvoisProperties properties;
    private final MyInvoisUrls urls;
    private final MyInvoisTokenProvider tokens;

    public MyInvoisStatusController(MyInvoisProperties properties, MyInvoisUrls urls, MyInvoisTokenProvider tokens) {
        this.properties = properties;
        this.urls = urls;
        this.tokens = tokens;
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status(@RequestParam Integer companyId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", properties.isEnabled());
        try {
            MyInvoisEnvironment environment = urls.environment();
            data.put("environment", environment.name());
            data.put("apiBaseUrl", environment.apiBaseUrl());
            data.put("portalBaseUrl", environment.portalBaseUrl());
        } catch (IllegalArgumentException bad) {
            data.put("environmentError", bad.getMessage());
        }
        data.put("clientIdConfigured", !isBlank(properties.getClientId()));
        data.put("clientSecretConfigured", !isBlank(properties.getClientSecret()));
        data.put("lineAmountPolicy", properties.getLineAmountPolicy().name());
        data.put("allowResubmitInvalid", properties.isAllowResubmitInvalid());
        data.put("allowForeignCurrency", properties.isAllowForeignCurrency());
        data.put("tokenCached", tokens.hasCachedToken(companyId));
        try {
            MyInvoisProperties.Supplier supplier = properties.supplier();
            data.put("supplierName", supplier.getName());
            data.put("supplierTin", supplier.getTin());
        } catch (IllegalStateException | IllegalArgumentException missingProfile) {
            data.put("supplierProfileError", missingProfile.getMessage());
        }
        return ResponseEntity.ok(ApiResponse.success(data, "MyInvois status"));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
