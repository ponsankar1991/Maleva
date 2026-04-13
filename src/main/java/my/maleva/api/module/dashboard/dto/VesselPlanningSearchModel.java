package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * VesselPlanningSearchModel - Search criteria for Vessel Planning and Air Freight dashboards
 * Converted from C# VESSEL PLANNING methods
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VesselPlanningSearchModel {

    @JsonProperty("comId")
    private Integer comId;

    @JsonProperty("employeeId")
    private Integer employeeId;

    @JsonProperty("etaType")
    private Integer etaType; // 1=OETA, 2=ETA, 3=Both, 5=FlightTime

    @JsonProperty("fromDate")
    private String fromDate;

    @JsonProperty("toDate")
    private String toDate;

    @JsonProperty("search")
    private String search; // Port or AWB numbers

    @JsonProperty("statusId")
    private Integer statusId;
}

