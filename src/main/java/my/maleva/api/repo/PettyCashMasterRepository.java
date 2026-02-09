package my.maleva.api.repo;

import my.maleva.api.model.PettyCashMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PettyCashMasterRepository extends JpaRepository<PettyCashMaster, Integer> {
}
