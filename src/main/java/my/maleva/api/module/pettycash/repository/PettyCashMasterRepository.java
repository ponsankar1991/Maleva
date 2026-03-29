package my.maleva.api.module.pettycash.repository;

import my.maleva.api.module.pettycash.entity.PettyCashMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PettyCashMasterRepository extends JpaRepository<PettyCashMaster, Integer> {
}
