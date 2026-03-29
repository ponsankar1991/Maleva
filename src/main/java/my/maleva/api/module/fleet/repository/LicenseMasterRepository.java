package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.LicenseMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenseMasterRepository extends JpaRepository<LicenseMaster, Integer> {
}
