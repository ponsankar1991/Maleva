package my.maleva.api.module.invoice.einvoice;

import my.maleva.api.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Renders an {@link EInvoicePushResult} into the response contract shared by
 * every push endpoint (see {@code QnePushResponses}): a local precondition
 * failure is a real HTTP error; anything LHDN or the validator said answers
 * 200 with {@code IsSuccess=false}, the message, and the individual reasons in
 * {@code Data1.problems} so the screen can list them.
 */
public final class EInvoicePushResponses {

    private EInvoicePushResponses() {
    }

    public static ResponseEntity<ApiResponse<Map<String, Object>>> toResponse(EInvoicePushResult result) {
        if (result.errorStatus() != null) {
            return ResponseEntity.status(result.errorStatus())
                    .body(ApiResponse.error(result.message(), result.errorStatus()));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("outcome", result.outcome().name());
        putIfPresent(data, "eInvoiceUid", result.uuid());
        putIfPresent(data, "eInvoiceSubmissionUid", result.submissionUid());
        putIfPresent(data, "eInvoiceLongId", result.longId());
        putIfPresent(data, "eInvoiceStatus", result.status());
        putIfPresent(data, "shareUrl", result.shareUrl());
        putIfPresent(data, "qrPngBase64", result.qrPngBase64());
        if (result.details() != null && !result.details().isEmpty()) {
            data.put("problems", result.details());
        }

        // A document LHDN validated as Invalid is not an e-invoice, whatever
        // the outcome of this particular call; the screen must not show green.
        if (!result.success() || result.lhdnInvalid()) {
            ApiResponse<Map<String, Object>> body = ApiResponse.error(result.message(), 200);
            body.setData1(data);
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.ok(ApiResponse.success(data, result.message()));
    }

    private static void putIfPresent(Map<String, Object> data, String key, String value) {
        if (value != null && !value.isBlank()) {
            data.put(key, value);
        }
    }
}
