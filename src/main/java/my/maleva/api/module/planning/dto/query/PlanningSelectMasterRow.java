package my.maleva.api.module.planning.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningSelectMasterRow {

    private Integer id;
    private Integer planningNo;
    private String planningNoDisplay;
    private String planningDate;
    private String remarks;
    private String employeeName;
    private Integer totalOrders;
}
