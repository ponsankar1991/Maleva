package my.maleva.api.module.ai.planning.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** The rows currently on the Planning grid, as the screen sees them. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanningSuggestRequest {

    private Integer companyRefId;

    /** yyyy-MM-dd; null = derived from the rows' pickup dates, else today. */
    private String planningDate;

    /** The plan being edited; its own saved rows are not counted as conflicts. */
    private Integer planningMasterId;

    /** false = only rows missing a truck or a driver receive a suggestion. */
    private boolean replaceExisting;

    @Builder.Default
    private List<Row> rows = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Row {
        /** Echoed back so the screen can find the grid row. */
        private String rowKey;
        private Integer saleOrderMasterRefId;
        private Integer truckRefId;
        private Integer driverRefId;
        private String driverName;
        /** 'yyyy-MM-dd HH:mm' or 'yyyy-MM-dd'. */
        private String pickupDate;
    }
}
