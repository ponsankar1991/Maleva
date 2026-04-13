package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for monthly sale by employee
 * Maps to legacy SelectmonthlySaleEmployeereport API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySaleDto {

    @JsonProperty("monthlySales")
    private List<MonthlySaleItemDto> monthlySales;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySaleItemDto {
        @JsonProperty("CustomerName")
        private String customerName;

        @JsonProperty("Sname")
        private String symbolName;

        @JsonProperty("Actualnetamount")
        private Double actualNetAmount;
    }
}
