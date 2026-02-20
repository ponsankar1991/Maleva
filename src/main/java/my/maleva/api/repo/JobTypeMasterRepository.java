package my.maleva.api.repo;

import my.maleva.api.model.JobTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobTypeMasterRepository extends JpaRepository<JobTypeMaster, Integer> {


    List<JobTypeMaster> findByCompanyRefIdAndActiveNot(Integer companyRefId, Integer active);
}
