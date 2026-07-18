package my.maleva.api.module.employee.repository;

import my.maleva.api.module.employee.entity.EmployeePortMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeePortMasterRepository extends JpaRepository<EmployeePortMaster, Integer> {
    
    // Find all active ports assigned to an employee
    List<EmployeePortMaster> findByCompanyRefIdAndEmployeeRefIdAndActive(Integer companyRefId, Integer employeeRefId, Integer active);
    
    // Delete existing ports for an employee to prevent duplicates during bulk assign
    void deleteByCompanyRefIdAndEmployeeRefId(Integer companyRefId, Integer employeeRefId);
}
