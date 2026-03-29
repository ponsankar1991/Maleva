package my.maleva.api.module.master.repository;

import my.maleva.api.module.master.entity.CountryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryMasterRepository extends JpaRepository<CountryMaster, Integer> {
}
