package my.maleva.api.module.company.repository;

import my.maleva.api.module.company.entity.MasterSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterSettingRepository extends JpaRepository<MasterSetting, Integer> {
}
