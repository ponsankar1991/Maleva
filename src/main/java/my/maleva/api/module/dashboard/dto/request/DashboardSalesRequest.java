package my.maleva.api.module.dashboard.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for sales data dashboard
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSalesRequest {

    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be positive")
    private Integer comId;

    @Builder.Default
    private Integer type = 0; // 0=Invoice, 1=SaleOrder, 2=Partial, 3=Pending
}
