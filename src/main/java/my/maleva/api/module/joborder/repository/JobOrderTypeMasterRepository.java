package my.maleva.api.module.joborder.repository;

import my.maleva.api.module.joborder.entity.JobOrderTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobOrderTypeMasterRepository extends JpaRepository<JobOrderTypeMaster, Integer> {
    List<JobOrderTypeMaster> findByIsActiveTrue();
}
