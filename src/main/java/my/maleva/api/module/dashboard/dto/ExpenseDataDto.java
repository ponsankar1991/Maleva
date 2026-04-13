package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for expense data response
 * Maps to legacy GetExpData API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDataDto {

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

    @JsonProperty("expenses")
    private List<ExpenseBreakdownDto> expenses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseBreakdownDto {
        @JsonProperty("ExpenseName")
        private String expenseName;

        @JsonProperty("ExpCount")
        private Integer expCount;

        @JsonProperty("ExpAmount")
        private Double expAmount;
    }
}
