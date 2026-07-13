package my.maleva.api.module.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSearchRequest {
    private Integer comid;
    private Integer startindex;
    private Integer pageCount;
    private String keyword;
    private String column;
    private String type;
}
