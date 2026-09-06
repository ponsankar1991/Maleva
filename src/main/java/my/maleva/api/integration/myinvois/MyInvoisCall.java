package my.maleva.api.integration.myinvois;

/**
 * A {@link MyInvoisResult} plus the typed body parsed out of it.
 *
 * @param result what happened on the wire
 * @param data   the parsed body, or null when the call failed or the body did
 *               not parse as {@code T}
 */
public record MyInvoisCall<T>(MyInvoisResult result, T data) {

    public boolean success() {
        return result.success() && data != null;
    }

    /** The LHDN-or-transport message, for surfacing to the user on failure. */
    public String message() {
        return result.message();
    }

    public static <T> MyInvoisCall<T> failed(MyInvoisResult result) {
        return new MyInvoisCall<>(result, null);
    }
}
