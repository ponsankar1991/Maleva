package my.maleva.api.module.joborder.repository;

import my.maleva.api.module.joborder.entity.JobOrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobOrderMasterRepository extends JpaRepository<JobOrderMaster, Integer>, JpaSpecificationExecutor<JobOrderMaster> {
    
    List<JobOrderMaster> findByCompanyRefId(Integer companyRefId);
    
    Optional<JobOrderMaster> findByIdAndCompanyRefId(Integer id, Integer companyRefId);
}
