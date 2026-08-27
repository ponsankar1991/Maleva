package my.maleva.api.integration.qne;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.common.config.QneProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Operational visibility into the QNE integration.
 *
 * <p>Read-only on purpose. The legacy system compiled these flags into
 * {@code qneapilist.cs}, so "which tenant is this server posting to?" meant
 * reading source. Here it is one GET — but changing the flags stays a
 * deployment concern ({@code qne.*} configuration), because flipping demo/live
 * at runtime mid-posting-run is how documents end up in the wrong tenant.
 */
@RestController
@RequestMapping("/api/qne")
@RequiredArgsConstructor
@Tag(name = "QNE Integration", description = "Status of the QNE cloud accounting integration")
public class QneStatusController {

    private final QneProperties properties;
    private final QneClient client;

    @GetMapping("/status")
    @Operation(
            summary = "QNE integration status",
            description = "Which QNE tenant this server talks to and which feature gates are on. "
                    + "Mirrors the legacy qneapilist flags: enabled (qneapi), demo (qnedemo), "
                    + "view (qneview), reportView (qnereportview).")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", properties.isEnabled());
        status.put("demo", properties.isDemo());
        // The tenant actually being addressed, resolved the same way the
        // client resolves it — not just the raw config values.
        status.put("dbCode", client.dbCode());
        status.put("baseUrl", properties.getBaseUrl());
        status.put("view", properties.isView());
        status.put("reportView", properties.isReportView());
        status.put("controlCodes", Map.of(
                "customer", properties.getControlCodes().getCustomer(),
                "supplier", properties.getControlCodes().getSupplier()));
        return ResponseEntity.ok(ApiResponse.success(status, "QNE status"));
    }
}
