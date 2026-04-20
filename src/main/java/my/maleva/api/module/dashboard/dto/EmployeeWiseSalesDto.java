package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Employee-wise Sales Data
 * Shows current month sales breakdown by employee
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWiseSalesDto {

    @JsonProperty("employeeName")
    private String employeeName;

    @JsonProperty("currentMonthSales")
    private Double currentMonthSales;

    @JsonProperty("salesCount")
    private Integer salesCount;

    /**
     * Wrapper for list of employee sales
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeWiseSalesListDto {

        @JsonProperty("totalEmployees")
        private Integer totalEmployees;

        @JsonProperty("totalSales")
        private Double totalSales;

        @JsonProperty("employees")
        private List<EmployeeWiseSalesDto> employees;

        @JsonProperty("period")
        private String period;
    }
}

