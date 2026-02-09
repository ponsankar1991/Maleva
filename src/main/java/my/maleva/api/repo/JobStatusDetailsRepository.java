package my.maleva.api.repo;

import my.maleva.api.model.JobStatusDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobStatusDetailsRepository extends JpaRepository<JobStatusDetails, Integer> {
}
