package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Top Employee Performers (Monthly Comparison)
 * Shows highest performing employees for current and previous months
 *
 * Architecture:
 * - TopPerformerDto: Individual performer record
 * - TopPerformersResponseDto: Complete response with metrics
 *
 * Business Logic:
 * - Current Month: Sales from day 1 to today
 * - Previous Month: Complete month sales
 * - Comparison: Identify top performer in each month
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopPerformerDto {

    @JsonProperty("monthType")
    private String monthType; // "CURRENT" or "PREVIOUS"

    @JsonProperty("employeeName")
    private String employeeName;

    @JsonProperty("totalSales")
    private Double totalSales;

    @JsonProperty("salesCount")
    private Integer salesCount;

    @JsonProperty("rank")
    private Integer rank; // 1 for top performer

    /**
     * Response wrapper with aggregated metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformersResponseDto {

        @JsonProperty("currentMonth")
        private TopPerformerDto currentMonthTopPerformer;

        @JsonProperty("previousMonth")
        private TopPerformerDto previousMonthTopPerformer;

        @JsonProperty("summary")
        private MetricsSummary summary;

        @JsonProperty("period")
        private String period; // Base date used for calculation

        /**
         * Aggregated metrics for dashboard display
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MetricsSummary {

            @JsonProperty("currentMonthTotalSales")
            private Double currentMonthTotalSales;

            @JsonProperty("previousMonthTotalSales")
            private Double previousMonthTotalSales;

            @JsonProperty("growthPercentage")
            private Double growthPercentage; // (Current - Previous) / Previous * 100

            @JsonProperty("performanceChange")
            private String performanceChange; // "UP", "DOWN", "SAME"

            @JsonProperty("generatedDate")
            private String generatedDate;
        }
    }

    /**
     * Detailed list response for analytics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformersListDto {

        @JsonProperty("currentMonthPerformers")
        private List<TopPerformerDto> currentMonthPerformers;

        @JsonProperty("previousMonthPerformers")
        private List<TopPerformerDto> previousMonthPerformers;

        @JsonProperty("totalEmployeesTracked")
        private Integer totalEmployeesTracked;

        @JsonProperty("period")
        private String period;
    }
}

