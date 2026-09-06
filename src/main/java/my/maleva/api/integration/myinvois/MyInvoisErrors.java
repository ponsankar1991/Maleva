package my.maleva.api.integration.myinvois;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.integration.myinvois.dto.ErrorResponse;

import java.util.List;

/**
 * Renders LHDN's error envelope into the text operators already recognise.
 *
 * <p>The format is the legacy one, kept on purpose — support staff have read
 * these lines for two years:
 * <pre>
 * Error Code: CF321
 * Message: Document validation failed
 *  - Detail Code: CF364
 *    Target: InvoiceLine[0]
 *    Message: Classification code is invalid
 * </pre>
 * What changed: the raw JSON body is no longer prepended to the message shown
 * to the user (it goes to the log instead), and a body that is not the
 * envelope — HTML from a gateway, an empty 401 — is described rather than
 * causing a parse failure that replaced the real error.
 */
public final class MyInvoisErrors {

    private MyInvoisErrors() {
    }

    /** Text for a non-2xx HTTP response body. */
    public static String describeHttpError(ObjectMapper objectMapper, int status, String body) {
        String rendered = renderEnvelope(objectMapper, body);
        if (rendered != null) {
            return rendered;
        }
        if (status == 401) {
            return "LHDN rejected the access token (HTTP 401)";
        }
        if (status == 429) {
            return "LHDN is rate-limiting this taxpayer (HTTP 429); wait a moment and try again";
        }
        if (status >= 500) {
            return "LHDN MyInvois is unavailable (HTTP " + status + ")";
        }
        String trimmed = body == null ? "" : body.trim();
        return trimmed.isEmpty()
                ? "LHDN returned HTTP " + status + " with no message"
                : "LHDN returned HTTP " + status + ": " + abbreviate(trimmed, 500);
    }

    /** Text for one entry of {@code rejectedDocuments[]}. */
    public static String describeRejection(ErrorResponse.ErrorDetail error) {
        if (error == null) {
            return "LHDN rejected the document without giving a reason";
        }
        return render(error);
    }

    /** The envelope rendered, or null when the body is not the envelope. */
    private static String renderEnvelope(ObjectMapper objectMapper, String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            ErrorResponse envelope = objectMapper.readValue(body, ErrorResponse.class);
            if (envelope == null || envelope.getError() == null) {
                return null;
            }
            ErrorResponse.ErrorDetail error = envelope.getError();
            if (error.getCode() == null && error.getMessage() == null) {
                return null;
            }
            return render(error);
        } catch (Exception notTheEnvelope) {
            return null;
        }
    }

    private static String render(ErrorResponse.ErrorDetail error) {
        StringBuilder sb = new StringBuilder();
        sb.append("Error Code: ").append(orDash(error.getCode())).append('\n');
        sb.append("Message: ").append(error.getMessage() == null ? "(no message)" : error.getMessage()).append('\n');
        appendDetails(sb, error.getDetails(), 1);
        return sb.toString().stripTrailing();
    }

    private static void appendDetails(StringBuilder sb, List<ErrorResponse.ErrorDetail> details, int depth) {
        if (details == null) {
            return;
        }
        String indent = " ".repeat(depth);
        for (ErrorResponse.ErrorDetail detail : details) {
            if (detail == null) {
                continue;
            }
            sb.append(indent).append("- Detail Code: ").append(orDash(detail.getCode())).append('\n');
            sb.append(indent).append("  Target: ").append(orDash(detail.getTarget())).append('\n');
            sb.append(indent).append("  Message: ").append(orDash(detail.getMessage())).append('\n');
            appendDetails(sb, detail.getDetails(), depth + 2);
        }
    }

    private static String orDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    static String abbreviate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max) + "…";
    }
}
