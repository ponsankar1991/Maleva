package my.maleva.api.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One attempt at a transaction password, for
 * {@code POST /api/form-transaction-passwords/verify}.
 *
 * <p>{@code transactionName} is the legacy gate name the screen is asking
 * about — {@code SpclPower} for the QNE push, and {@code EditPassword},
 * {@code FormConfig}, {@code AdminPower}, {@code SpclEdit} for the other
 * legacy password windows.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionPasswordVerifyRequest {

    @NotNull
    private Integer companyId;

    @NotBlank
    @Size(max = 100)
    private String transactionName;

    @NotBlank
    @Size(max = 100)
    private String password;
}
