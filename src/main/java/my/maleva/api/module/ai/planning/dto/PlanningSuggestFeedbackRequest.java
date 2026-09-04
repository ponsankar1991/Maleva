package my.maleva.api.module.ai.planning.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** What the planner finally saved for rows that received a suggestion. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanningSuggestFeedbackRequest {

    private Integer companyRefId;
    private String planningDate;
    private Integer planningMasterId;

    @Builder.Default
    private List<Row> rows = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Row {
        private Integer saleOrderMasterRefId;
        private Integer suggestedTruckId;
        private Integer suggestedDriverId;
        private Integer chosenTruckId;
        private Integer chosenDriverId;
    }
}
