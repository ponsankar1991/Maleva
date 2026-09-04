package my.maleva.api.module.ai.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Per-row truck and driver picks with the reasons behind them, plus conflict warnings. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanningSuggestResponse {

    private String planningDate;
    private int historyPlans;
    private String historyFrom;
    private String historyTo;
    private List<RowSuggestion> rows;
    private List<PlanWarning> warnings;

    /** A ranked truck or driver. Score is 0-100 relative to the best candidate for that row. */
    public record Pick(Integer id, String name, int score, List<String> reasons) {
    }

    public record RowWarning(String code, String level, String message) {
    }

    public record PlanWarning(String code, String level, String message, List<String> rowKeys) {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowSuggestion {
        private String rowKey;
        private Integer saleOrderMasterRefId;
        private boolean skipped;
        private String skipReason;
        private Pick truck;
        private Pick driver;
        private List<Pick> alternativeTrucks;
        private List<Pick> alternativeDrivers;
        private List<RowWarning> warnings;
        /** Trip the row belongs to (1-based) and its position within it. */
        private Integer tripNo;
        private Integer tripPosition;
        /** "TRIP 2: NORTHPORT -> WESTPORT -> JOHOR". */
        private String tripLabel;
        /** Proposed SORT value: trips in pickup order, jobs in trip order. */
        private Integer sortBy;
    }
}
