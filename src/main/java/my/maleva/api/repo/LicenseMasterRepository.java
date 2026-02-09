package my.maleva.api.repo;

import my.maleva.api.model.LicenseMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenseMasterRepository extends JpaRepository<LicenseMaster, Integer> {
}
