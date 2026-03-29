package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.LeviEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeviEntryRepository extends JpaRepository<LeviEntry, Integer> {
}
