package my.maleva.api.module.patmentvouchmaster.dto;

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
public class PaymentVoucherSaveResponseDto {

    private boolean success;

    private String message;

    private Integer id;

    /** The running number, e.g. {@code PV000002451}. */
    private String voucherNoDisplay;

    /**
     * True when the server recognised this as a repeat of a voucher already
     * stored — a double-click, a retry, a second tab — and returned that one
     * instead of entering it twice.
     */
    private boolean duplicate;
}
