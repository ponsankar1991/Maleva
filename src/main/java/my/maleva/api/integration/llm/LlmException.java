package my.maleva.api.integration.llm;

import lombok.Getter;

/**
 * The only exception the LLM layer throws. {@link Kind} tells the gateway
 * whether another provider is worth trying and tells the HTTP layer which
 * status to answer with.
 */
@Getter
public class LlmException extends RuntimeException {

    public enum Kind {
        /** llm.enabled=false. */
        DISABLED,
        /** No API key / base URL for the chosen provider. */
        NOT_CONFIGURED,
        /** Provider cannot read the attachment type (e.g. images on a text-only model). */
        UNSUPPORTED_INPUT,
        AUTHENTICATION,
        RATE_LIMITED,
        TIMEOUT,
        /** Any other error reported by the provider or the transport. */
        PROVIDER_ERROR,
        /** The model declined to answer (Claude stop_reason=refusal). */
        REFUSED,
        /** The model answered but the text could not be parsed as expected. */
        BAD_RESPONSE
    }

    private final Kind kind;
    private final String providerKey;

    public LlmException(Kind kind, String providerKey, String message) {
        super(message);
        this.kind = kind;
        this.providerKey = providerKey;
    }

    public LlmException(Kind kind, String providerKey, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
        this.providerKey = providerKey;
    }

    /** Everything except a global disable is worth retrying on the next provider in the chain. */
    public boolean fallbackEligible() {
        return kind != Kind.DISABLED;
    }
}
