package my.maleva.api.repo;

import my.maleva.api.model.EngineHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EngineHoursRepository extends JpaRepository<EngineHours, Integer> {
    List<EngineHours> findByCompanyRefId(Integer companyRefId);
    List<EngineHours> findByTruckRefId(Integer truckRefId);
}
