package my.maleva.api.module.leave.repository;

import my.maleva.api.module.leave.entity.LeaveTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveTypeMaster, Integer> {
    List<LeaveTypeMaster> findByActive(Boolean active);
}
