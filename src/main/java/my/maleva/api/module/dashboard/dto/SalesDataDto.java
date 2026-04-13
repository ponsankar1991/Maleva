package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for sales data response
 * Maps to legacy GetSalesData API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesDataDto {

    @JsonProperty("TodaySales")
    private Integer todaySales;

    @JsonProperty("TodayAmount")
    private Double todayAmount;

    @JsonProperty("YesterdaySales")
    private Integer yesterdaySales;

    @JsonProperty("YesterdayAmount")
    private Double yesterdayAmount;

    @JsonProperty("WeekSales")
    private Integer weekSales;

    @JsonProperty("WeekAmount")
    private Double weekAmount;

    @JsonProperty("MonthSales")
    private Integer monthSales;

    @JsonProperty("MonthAmount")
    private Double monthAmount;

    @JsonProperty("monthlySales")
    private List<MonthlySalesDto> monthlySales;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySalesDto {
        @JsonProperty("SalesCount")
        private Integer salesCount;

        @JsonProperty("SalesAmount")
        private Double salesAmount;

        @JsonProperty("MonthName")
        private String monthName;
    }
}
