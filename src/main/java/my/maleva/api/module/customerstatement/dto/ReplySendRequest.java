package my.maleva.api.module.customerstatement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * An answer written from the screen.
 *
 * <p>With {@code replyToId} it answers that customer mail: {@code REPLY} goes
 * to whoever wrote it, {@code REPLY_ALL} to everyone on it minus our own
 * addresses, threaded under it. Without, it is a fresh mail to the
 * customer's statement addresses (or {@code to}).
 */
@Data
public class ReplySendRequest {

    public static final String MODE_REPLY = "REPLY";
    public static final String MODE_REPLY_ALL = "REPLY_ALL";

    @NotNull(message = "Company is required")
    @Positive
    private Integer companyId;

    @NotNull(message = "Customer is required")
    @Positive
    private Integer customerId;

    /** The customer mail being answered; null for a fresh mail. */
    private Long replyToId;

    /** REPLY (default) or REPLY_ALL. */
    private String mode = MODE_REPLY;

    /** Extra or overriding To addresses, comma or semicolon separated. */
    private String to;

    /** Leave blank for "Re: " + the original subject. */
    @Size(max = 300)
    private String subject;

    @NotBlank(message = "Write a message")
    @Size(max = 20000)
    private String body;

    /** Attach the customer's current statement PDF. */
    private boolean attachStatement;
}
