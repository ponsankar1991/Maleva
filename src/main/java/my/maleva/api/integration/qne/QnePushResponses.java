package my.maleva.api.integration.qne;

import my.maleva.api.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Renders a {@link QnePushResult} into the legacy response contract shared by
 * every push endpoint: local precondition failures are real HTTP errors, but a
 * QNE rejection after the document is committed locally answers 200 with
 * {@code IsSuccess=false} and QNE's message — the mixed signal the legacy
 * screens are written against.
 */
public final class QnePushResponses {

    private QnePushResponses() {
    }

    public static ResponseEntity<ApiResponse<Map<String, Object>>> toResponse(QnePushResult result) {
        if (result.errorStatus() != null) {
            return ResponseEntity.status(result.errorStatus())
                    .body(ApiResponse.error(result.message(), result.errorStatus()));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        if (result.qneId() != null) {
            data.put("qneId", result.qneId());
        }
        if (result.qneCode() != null) {
            data.put("qneCode", result.qneCode());
        }
        if (result.reportUrl() != null) {
            data.put("fileUrl", result.reportUrl());
        }

        if (!result.success()) {
            ApiResponse<Map<String, Object>> body = ApiResponse.error(result.message(), 200);
            body.setData1(data);
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.ok(ApiResponse.success(data, result.message()));
    }
}
