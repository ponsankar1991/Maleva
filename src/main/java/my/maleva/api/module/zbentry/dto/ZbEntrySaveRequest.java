package my.maleva.api.module.zbentry.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZbEntrySaveRequest {

    private Integer id; // 0 or null for insert, >0 for update
    
    @NotBlank(message = "EntryDate cannot be blank")
    private String entryDate;
    
    private String chargeType;
    private String zbType;
    private String portChart;
    private String zbNumber;
    private String vesselName;
    private String jobNumber;
    private String ptwNo;
    private String amount;
    private Integer active;
}
