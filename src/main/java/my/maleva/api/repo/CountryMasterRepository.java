package my.maleva.api.repo;

import my.maleva.api.model.CountryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryMasterRepository extends JpaRepository<CountryMaster, Integer> {
}
