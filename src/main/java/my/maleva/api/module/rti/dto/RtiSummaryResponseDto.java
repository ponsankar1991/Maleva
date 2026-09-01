package my.maleva.api.module.rti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RtiSummaryResponseDto {
    private Integer id;
    private String cNumberDisplay;
    private String comments;
    private Integer truckRefId;
    private String truckName;
    private Integer driverRefId;
    private String driverName;
    private Integer pckHandling;
    private Integer punctuality;
    private Integer documentSub;
}
