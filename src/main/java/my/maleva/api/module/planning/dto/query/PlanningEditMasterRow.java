package my.maleva.api.module.planning.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningEditMasterRow {

    private Integer id;
    private Integer companyRefId;
    private Integer userRefId;
    private Integer employeeRefId;
    private Integer lastEmployeeRefId;
    private String sFDate;
    private String sTDate;
    private String saleDate;
    private String sSaleDate;
    private String cNumberDisplay;
    private Integer cNumber;
    private String remarks;
    private String search;
    private Integer active;
    private String createdDate;
    private String createdBy;
    private String modifiedDate;
    private String modifiedBy;
}
