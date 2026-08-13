package my.maleva.api.module.joborder.repository;

import my.maleva.api.module.joborder.entity.JobOrderStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobOrderStatusMasterRepository extends JpaRepository<JobOrderStatusMaster, Integer> {
    List<JobOrderStatusMaster> findByIsActiveTrue();
}
