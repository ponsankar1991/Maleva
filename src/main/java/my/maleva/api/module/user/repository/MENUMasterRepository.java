package my.maleva.api.module.user.repository;

import my.maleva.api.module.user.entity.MENUMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MENUMasterRepository extends JpaRepository<MENUMaster, Integer> {
}
