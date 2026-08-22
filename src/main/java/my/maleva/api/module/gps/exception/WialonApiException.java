package my.maleva.api.module.gps.exception;

/**
 * Raised when Wialon rejects a call. Wialon answers with HTTP 200 and a body of
 * {@code {"error":N}} instead of an HTTP error status, so this is thrown after
 * inspecting the payload rather than the status line.
 *
 * @see <a href="https://sdk.wialon.com/wiki/en/sidebar/remoteapi/codes">Wialon error codes</a>
 */
public class WialonApiException extends RuntimeException {

    /** Wialon error code, or null when the failure was transport level. */
    private final Integer errorCode;

    public WialonApiException(String message) {
        super(message);
        this.errorCode = null;
    }

    public WialonApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public WialonApiException(String message, Integer errorCode) {
        super(message + " (wialon error " + errorCode + ")");
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    /** Error 1 = invalid session; the caller should re-login and retry once. */
    public boolean isInvalidSession() {
        return errorCode != null && errorCode == 1;
    }
}
