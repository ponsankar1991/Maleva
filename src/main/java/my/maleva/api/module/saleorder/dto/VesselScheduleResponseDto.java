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
    private Integer totalJob;
    private String officer1Name;
    private Double officer1Amount;
    private String officer2Name;
    private Double officer2Amount;
    private String officer3Name;
    private Double officer3Amount;
    private String officer4Name;
    private Double officer4Amount;
    private String officer5Name;
    private Double officer5Amount;
    private Double totalAmount;
}
