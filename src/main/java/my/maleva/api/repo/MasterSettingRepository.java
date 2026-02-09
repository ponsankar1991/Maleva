package my.maleva.api.repo;

import my.maleva.api.model.MasterSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterSettingRepository extends JpaRepository<MasterSetting, Integer> {
}
