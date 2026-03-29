package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.EngineHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EngineHoursRepository extends JpaRepository<EngineHours, Integer> {
    List<EngineHours> findByCompanyRefId(Integer companyRefId);
    List<EngineHours> findByTruckRefId(Integer truckRefId);
}
