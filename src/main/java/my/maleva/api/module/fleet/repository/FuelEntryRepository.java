package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.FuelEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuelEntryRepository extends JpaRepository<FuelEntry, Integer> {
    List<FuelEntry> findByCompanyRefId(Integer companyRefId);
    List<FuelEntry> findByTruckRefid(Integer truckRefid);
    List<FuelEntry> findByDriverRefId(Integer driverRefId);
}
