package my.maleva.api.integration.qne;

/**
 * Outcome of one module-level QNE push, shaped for the legacy response
 * contract: once the local document is committed, a QNE rejection is not an
 * HTTP failure of this API — it is a successful call whose body says
 * {@code IsSuccess=false} with QNE's own message, exactly what the legacy
 * screens display. Only local problems (missing row, wrong company, an
 * unmapped prerequisite) carry an HTTP error status.
 *
 * @param success     whether the document is (now) in QNE
 * @param qneId       QNE's GUID for the document, when known
 * @param qneCode     QNE's human document code, when known
 * @param reportUrl   QNE-hosted printable document URL, when the view gate
 *                    ({@code qne.view}) is on and the fetch succeeded
 * @param message     what to show the user — QNE's text verbatim on rejection
 * @param errorStatus HTTP status for a local precondition failure
 *                    (404, 403, 409…); null when the request reached the
 *                    push itself
 */
public record QnePushResult(
        boolean success,
        String qneId,
        String qneCode,
        String reportUrl,
        String message,
        Integer errorStatus) {

    public static QnePushResult ok(String qneId, String qneCode, String reportUrl, String message) {
        return new QnePushResult(true, qneId, qneCode, reportUrl, message, null);
    }

    /** The empty-code guard found the document already synced — a no-op success. */
    public static QnePushResult alreadyPushed(String qneId, String qneCode, String message) {
        return new QnePushResult(true, qneId, qneCode, null, message, null);
    }

    /** QNE (or the transport) refused the document; the local row is untouched. */
    public static QnePushResult rejected(String message) {
        return new QnePushResult(false, null, null, null, message, null);
    }

    /** A local precondition failed before anything was sent to QNE. */
    public static QnePushResult localError(int status, String message) {
        return new QnePushResult(false, null, null, null, message, status);
    }
}
