package my.maleva.api.module.jobs.repository;

import my.maleva.api.module.jobs.entity.JobStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobStatusMasterRepository extends JpaRepository<JobStatusMaster, Integer> {

    // Existing methods (none custom yet)

    // Added: find by exact active flag for company
    List<JobStatusMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);
}
