package my.maleva.api.repo;

import my.maleva.api.model.LeviEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeviEntryRepository extends JpaRepository<LeviEntry, Integer> {
}
