package my.maleva.api.module.employee.repository;

import my.maleva.api.module.employee.dto.EmployeeAllDto;
import my.maleva.api.module.employee.dto.EmployeeSearchRequest;

import java.util.List;

public interface EmployeeMasterRepositoryCustom {
    
    /**
     * Search employees by dynamic criteria (matches legacy C# SelectEmployee)
     * @param request The search parameters
     * @return List of matching employees
     */
    List<EmployeeAllDto> searchEmployees(EmployeeSearchRequest request);

    /**
     * Count employees for pagination when no keyword is provided
     * @param request The search parameters
     * @return Total count matching the filters
     */
    int countSearchEmployees(EmployeeSearchRequest request);
}
