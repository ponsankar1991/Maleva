package my.maleva.api.integration.qne;

/**
 * A {@link QneResult} plus the typed payload parsed out of its body.
 *
 * <p>Call sites need both halves: the typed data to persist (a QNE id and
 * document code), and the raw result to surface QNE's own message to the user
 * when the call failed — the legacy screens show that text verbatim.
 *
 * @param result what happened on the wire
 * @param data   the parsed body, or null when the call failed or the body did
 *               not parse as {@code T}
 */
public record QneCall<T>(QneResult result, T data) {

    public boolean success() {
        return result.success() && data != null;
    }

    /** The QNE-or-transport message, for surfacing to the user on failure. */
    public String message() {
        return result.message();
    }
}
