package my.maleva.api.repo;

import my.maleva.api.model.PhoneCallEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneCallEntryRepository extends JpaRepository<PhoneCallEntry, Integer> {
}
