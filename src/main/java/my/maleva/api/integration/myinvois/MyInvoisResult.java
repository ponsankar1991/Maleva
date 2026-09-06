package my.maleva.api.integration.myinvois;

/**
 * Outcome of one HTTP call to MyInvois.
 *
 * <p>The transport never throws. Every failure — LHDN said no, the network
 * failed, the integration is switched off — becomes an unsuccessful result
 * carrying a message the operator can read. Call sites branch on
 * {@link #success()} and show {@link #message()}; that is the contract the
 * legacy screens were written against.
 *
 * @param success whether LHDN answered 2xx
 * @param status  HTTP status, or 0 when the request never reached LHDN
 * @param body    raw response body, empty when the request never reached LHDN
 * @param message the body on success; on failure the rendered error envelope
 *                (see {@link MyInvoisErrors}) or the transport root cause
 */
public record MyInvoisResult(boolean success, int status, String body, String message) {

    public static MyInvoisResult ok(int status, String body) {
        return new MyInvoisResult(true, status, body, body);
    }

    public static MyInvoisResult failed(int status, String body, String message) {
        return new MyInvoisResult(false, status, body, message);
    }

    /** The request was never sent: integration disabled, no token, or the transport failed. */
    public static MyInvoisResult notSent(String message) {
        return new MyInvoisResult(false, 0, "", message);
    }

    public boolean isUnauthorized() {
        return status == 401;
    }

    public boolean isRateLimited() {
        return status == 429;
    }
}
