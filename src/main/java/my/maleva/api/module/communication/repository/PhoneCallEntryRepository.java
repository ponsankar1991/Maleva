package my.maleva.api.module.communication.repository;

import my.maleva.api.module.communication.entity.PhoneCallEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneCallEntryRepository extends JpaRepository<PhoneCallEntry, Integer> {
}
