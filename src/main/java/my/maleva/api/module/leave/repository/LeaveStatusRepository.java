package my.maleva.api.module.leave.repository;

import my.maleva.api.module.leave.entity.LeaveStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveStatusRepository extends JpaRepository<LeaveStatusMaster, Integer> {
    List<LeaveStatusMaster> findByActive(Boolean active);
}
