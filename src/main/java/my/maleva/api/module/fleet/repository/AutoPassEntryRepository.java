package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.AutoPassEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoPassEntryRepository extends JpaRepository<AutoPassEntry, Integer> {
    List<AutoPassEntry> findByCompanyRefId(Integer companyRefId);
}
