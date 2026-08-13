package my.maleva.api.module.joborder.repository;

import my.maleva.api.module.joborder.entity.JobOrderPriorityMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobOrderPriorityMasterRepository extends JpaRepository<JobOrderPriorityMaster, Integer> {
    List<JobOrderPriorityMaster> findByIsActiveTrue();
}
