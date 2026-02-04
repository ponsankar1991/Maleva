package my.maleva.api.repo;

import my.maleva.api.model.FuelEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuelEntryRepository extends JpaRepository<FuelEntry, Integer> {
    List<FuelEntry> findByCompanyRefId(Integer companyRefId);
    List<FuelEntry> findByTruckRefid(Integer truckRefid);
    List<FuelEntry> findByDriverRefId(Integer driverRefId);
}
