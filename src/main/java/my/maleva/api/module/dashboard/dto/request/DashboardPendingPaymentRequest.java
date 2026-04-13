package my.maleva.api.module.dashboard.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for pending payment dashboard
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardPendingPaymentRequest {

    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be positive")
    private Integer comId;

    @NotBlank(message = "Due date is required")
    private String dueDate;

    private String toDate;

    private Integer supplierId; // 1=HirePurchase, 2=Vendor, 3=Utility, 4=Tenancy, 5=MonthlyPurpose

    @Builder.Default
    private Boolean isExpense = true; // true for expense, false for vendor
}
