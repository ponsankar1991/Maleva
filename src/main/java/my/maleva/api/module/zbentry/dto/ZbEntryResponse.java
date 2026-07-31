package my.maleva.api.module.zbentry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZbEntryResponse {

    private Integer id;
    private Integer companyRefId;
    private LocalDate entryDate;
    private String chargeType;
    private String zbType;
    private String portChart;
    private String zbNumber;
    private String vesselName;
    private String jobNumber;
    private String ptwNo;
    private BigDecimal amount;
    private Integer active;

}
