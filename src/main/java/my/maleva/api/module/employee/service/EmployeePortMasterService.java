package my.maleva.api.module.employee.service;

import my.maleva.api.module.employee.dto.EmployeePortMasterDto;
import java.util.List;

public interface EmployeePortMasterService {
    
    // Insert new Employee Port assignment
    EmployeePortMasterDto create(EmployeePortMasterDto dto);
    
    // Bulk insert new Employee Port assignments
    List<EmployeePortMasterDto> bulkCreate(List<EmployeePortMasterDto> dtos);
    
    // Get assigned ports for an employee
    List<EmployeePortMasterDto> getByEmployeeRefId(Integer companyRefId, Integer employeeRefId);
}
