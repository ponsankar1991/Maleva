package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for weekly report
 * Maps to legacy Selectweeklyreport API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportDto {

    @JsonProperty("weeklyReport")
    private List<WeeklyReportItemDto> weeklyReport;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyReportItemDto {
        @JsonProperty("CompanyName")
        private String companyName;

        @JsonProperty("Week1")
        private Integer week1;

        @JsonProperty("Week2")
        private Integer week2;

        @JsonProperty("Week3")
        private Integer week3;

        @JsonProperty("Week4")
        private Integer week4;

        @JsonProperty("Week5")
        private Integer week5;
    }
}
