package my.maleva.api.module.master.repository;

import my.maleva.api.module.master.entity.LocationMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationMasterRepository extends JpaRepository<LocationMaster, Integer> {
    List<LocationMaster> findByCompanyRefIdAndActiveNot(Integer companyRefId, Integer active);
}
