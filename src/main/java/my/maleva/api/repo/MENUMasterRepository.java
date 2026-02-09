package my.maleva.api.repo;

import my.maleva.api.model.MENUMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MENUMasterRepository extends JpaRepository<MENUMaster, Integer> {
}
