package my.maleva.api.module.jobs.repository;

import my.maleva.api.module.jobs.entity.JobTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobTypeMasterRepository extends JpaRepository<JobTypeMaster, Integer> {


    List<JobTypeMaster> findByCompanyRefIdAndActiveNot(Integer companyRefId, Integer active);
}
