package my.maleva.api.repo;

import my.maleva.api.model.DoMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoMasterRepository extends JpaRepository<DoMaster, Integer> {
    List<DoMaster> findByCompanyRefId(Integer companyRefId);
    List<DoMaster> findByCustomerRefId(Integer customerRefId);
    List<DoMaster> findByJobMasterRefId(Integer jobMasterRefId);
}
