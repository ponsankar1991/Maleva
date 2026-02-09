package my.maleva.api.repo;

import my.maleva.api.model.MainSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MainSettingRepository extends JpaRepository<MainSetting, Integer> {
}
