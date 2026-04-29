package my.maleva.api.module.transaction.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class F5Dto {
    // Added fields as per your C# F5ViewModel
    private String searchCriteria;
    private Integer pageNumber;
    private Integer pageSize;
}
