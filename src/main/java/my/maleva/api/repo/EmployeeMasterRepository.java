package my.maleva.api.repo;

import my.maleva.api.model.EmployeeMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeMasterRepository extends JpaRepository<EmployeeMaster, Integer> {
    Page<EmployeeMaster> findByEmployeeNameContainingIgnoreCase(String name, Pageable pageable);

    // allow lookup by userName for authentication
    Optional<EmployeeMaster> findByUserNameAndActive(String userName, Integer active);

    // Find employees by company ID and active status
    List<EmployeeMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    // Find employees by company ID, active status, and role ID
    List<EmployeeMaster> findByCompanyRefIdAndActiveAndRoleId(Integer companyRefId, Integer active, Integer roleId);

    // Custom query to find employees by company and multiple role IDs
    @Query("SELECT e FROM EmployeeMaster e WHERE e.companyRefId = :companyRefId AND e.active = 1 AND e.roleId IN :roleIds ORDER BY e.employeeName ASC")
    List<EmployeeMaster> findByCompanyAndRoleIds(@Param("companyRefId") Integer companyRefId, @Param("roleIds") List<Integer> roleIds);

    // Get all active employees for a company (SelectEmployeeAll equivalent)
    @Query("SELECT e FROM EmployeeMaster e WHERE e.companyRefId = :companyRefId AND e.active != 2 ORDER BY e.employeeName ASC")
    List<EmployeeMaster> findAllActiveByCompanyRefId(@Param("companyRefId") Integer companyRefId);

    // Get all active employees by company and employee type
    @Query("SELECT e FROM EmployeeMaster e WHERE e.companyRefId = :companyRefId AND e.active != 2 AND e.employeeType = :employeeType ORDER BY e.employeeName ASC")
    List<EmployeeMaster> findAllActiveByCompanyRefIdAndEmployeeType(@Param("companyRefId") Integer companyRefId, @Param("employeeType") String employeeType);
}


