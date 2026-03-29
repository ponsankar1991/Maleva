package my.maleva.api.module.pendingpayment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingPaymentDto {
    private Integer id;

    @NotNull
    private Integer subExpenseRefId;

    @NotNull
    private LocalDate dueDate;

    private LocalDateTime createdDate;

    @NotNull
    @Size(max = 200)
    private String createdBy;

    @NotNull
    private Integer companyRefId;

    private Integer paidStatus;

    @Size(max = 500)
    private String paidAmount;

    private LocalDateTime paidDate;
}
