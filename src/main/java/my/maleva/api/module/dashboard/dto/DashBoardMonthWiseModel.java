package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Dashboard Month-wise Sales Data
 * Maps to .NET DashBoardMonthWiseModel
 * Represents sales count and amount for a specific month
 *
 * Equivalent .NET class:
 * public class DashBoardMonthWiseModel
 * {
 *     public Int32 SalesCount { get; set; }
 *     public Double SalesAmount { get; set; }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashBoardMonthWiseModel {

    /**
     * Distinct count of sales records for the month
     * Column name: SalesCount
     */
    @JsonProperty("SalesCount")
    private Integer salesCount;

    /**
     * Total amount of sales for the month (rounded to 2 decimal places)
     * Column name: SalesAmount
     */
    @JsonProperty("SalesAmount")
    private Double salesAmount;

    /**
     * Optional: Month offset for sorting (0 = current month, 1 = previous month, etc.)
     */
    @JsonProperty("MonthOffset")
    private Integer monthOffset;

    /**
     * Optional: Month name for display (e.g., "January 2026")
     */
    @JsonProperty("MonthName")
    private String monthName;
}

