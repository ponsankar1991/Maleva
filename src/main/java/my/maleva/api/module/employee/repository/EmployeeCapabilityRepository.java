package my.maleva.api.module.employee.repository;

import my.maleva.api.module.employee.entity.EmployeeCapability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeCapabilityRepository extends JpaRepository<EmployeeCapability, Integer> {
    List<EmployeeCapability> findByEmployeeIdAndIsActiveTrue(Integer employeeId);
    List<EmployeeCapability> findByEmployeeId(Integer employeeId);
}
