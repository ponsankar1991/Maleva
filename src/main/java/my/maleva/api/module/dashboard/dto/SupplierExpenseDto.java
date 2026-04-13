package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for supplier expense data
 * Maps to legacy LoadSupplierExpenseData API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierExpenseDto {

    @JsonProperty("supplierExpenses")
    private List<SupplierExpenseItemDto> supplierExpenses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierExpenseItemDto {
        @JsonProperty("Id")
        private Integer id;

        @JsonProperty("SDueDate")
        private String sDueDate;

        @JsonProperty("SupplierName")
        private String supplierName;

        @JsonProperty("Amount")
        private Double amount;

        @JsonProperty("PStatus")
        private Integer pStatus; // 0=NotDue, 1=Expiring, 2=Expired
    }
}
