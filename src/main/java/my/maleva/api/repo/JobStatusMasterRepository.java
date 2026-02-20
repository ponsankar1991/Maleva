package my.maleva.api.repo;

import my.maleva.api.model.JobStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobStatusMasterRepository extends JpaRepository<JobStatusMaster, Integer> {

    // Existing methods (none custom yet)

    // Added: find by exact active flag for company
    List<JobStatusMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);
}
