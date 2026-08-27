package my.maleva.api.module.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The outcome of a save. {@code id} is what an attachment upload hangs off, so
 * it is returned on update as well as insert.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSaveResponseDto {

    private boolean success;

    private String message;

    private Integer id;

    /** The running number, e.g. {@code PY000000042}. */
    private String paymentNoDisplay;

    /**
     * True when the server recognised this as a repeat of a payment already
     * stored — a double-click, a retry, a second tab — and returned that one
     * instead of entering the supplier's money twice.
     */
    private boolean duplicate;
}
