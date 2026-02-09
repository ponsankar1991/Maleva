package my.maleva.api.repo;

import my.maleva.api.model.JobStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobStatusMasterRepository extends JpaRepository<JobStatusMaster, Integer> {
}
