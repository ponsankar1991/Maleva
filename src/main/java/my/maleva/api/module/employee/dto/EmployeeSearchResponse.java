package my.maleva.api.module.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSearchResponse {
    private List<EmployeeAllDto> data1;
    private Integer data4;
}
