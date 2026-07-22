package my.maleva.api.module.saleorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VesselScheduleResponseDto {
    private String etaDate;
    private String vesselName;
    private String vesselType;
    private String jobNumbers;
    private String boardingOfficer1Name;
    private String boardingOfficer2Name;
    private Double boardingOfficer1Amount;
    private Double boardingOfficer2Amount;
    private Double totalBoardingAmount;
    private Integer totalJobs;
}
