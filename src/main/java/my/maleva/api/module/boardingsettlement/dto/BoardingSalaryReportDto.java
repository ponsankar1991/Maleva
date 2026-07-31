package my.maleva.api.module.boardingsettlement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardingSalaryReportDto {

    @JsonProperty("employeeRefId")
    private Integer employeeRefId;

    @JsonProperty("employeeName")
    private String employeeName;

    @JsonProperty("vesselName")
    private String vesselName;

    @JsonProperty("boardingDate")
    private String boardingDate;

    @JsonProperty("calculatedRate")
    private Double calculatedRate;

    @JsonProperty("relatedJobs")
    private String relatedJobs;

    @JsonProperty("tagTypes")
    private String tagTypes;

    @JsonProperty("portName")
    private String portName;
}
