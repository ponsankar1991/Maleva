package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.FuelFillings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuelFillingsRepository extends JpaRepository<FuelFillings, Integer> {
    List<FuelFillings> findByCompanyRefId(Integer companyRefId);
    List<FuelFillings> findByTruckRefId(Integer truckRefId);
}
