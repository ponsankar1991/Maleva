package my.maleva.api.repo;

import my.maleva.api.model.DriverMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverMasterRepository extends JpaRepository<DriverMaster, Integer> {
    List<DriverMaster> findByCompanyRefId(Integer companyRefId);
    List<DriverMaster> findByAccountRefid(Integer accountRefid);
}
