package my.maleva.api.repo;

import my.maleva.api.model.EmployeeMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeMasterRepository extends JpaRepository<EmployeeMaster, Integer> {
    Page<EmployeeMaster> findByEmployeeNameContainingIgnoreCase(String name, Pageable pageable);

    // allow lookup by userName for authentication
    Optional<EmployeeMaster> findByUserNameAndActive(String userName,Integer active);
}
