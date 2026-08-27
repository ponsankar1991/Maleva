package my.maleva.api.integration.qne;

/**
 * Outcome of one QNE call, carrying the legacy contract of
 * {@code commonfunctions.QneApi}: a success flag plus a message that is the
 * raw response body on success — callers parse ids and codes out of it — and
 * an error description on failure.
 *
 * <p>The legacy method never threw; every failure mode collapsed into
 * {@code IsSuccess=false} with a message. That is preserved here because the
 * call sites are written against it: they branch on the flag and surface the
 * message to the user verbatim.
 *
 * @param success whether QNE accepted the call
 * @param status  HTTP status code, or 0 when the request never reached QNE
 *                (disabled by configuration, or a transport failure)
 * @param body    raw response body exactly as QNE sent it; empty when the
 *                request never reached QNE
 * @param message what the legacy contract calls Message: the body on success
 *                and on 400/404, the parsed {@code code\nMessage} pair on
 *                other HTTP errors, and the root-cause description on
 *                transport failure
 */
public record QneResult(boolean success, int status, String body, String message) {

    public static QneResult ok(int status, String body) {
        return new QneResult(true, status, body, body);
    }

    public static QneResult failed(int status, String body, String message) {
        return new QneResult(false, status, body, message);
    }

    /** The request was never sent: QNE is disabled or the transport failed. */
    public static QneResult notSent(String message) {
        return new QneResult(false, 0, "", message);
    }
}
