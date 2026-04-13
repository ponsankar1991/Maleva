package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for employee sales data
 * Maps to legacy GetEmployeeSalesData API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalesDto {

    @JsonProperty("employeeSales")
    private List<EmployeeSalesItemDto> employeeSales;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeSalesItemDto {
        @JsonProperty("EmployeeName")
        private String employeeName;

        @JsonProperty("SalesCount")
        private Integer salesCount;

        @JsonProperty("Amount")
        private Double amount;
    }
}
