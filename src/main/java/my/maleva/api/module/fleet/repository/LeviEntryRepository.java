package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.LeviEntry;
import org.springframework.stereotype.Repository;

/** LeviEntry data access. Everything it needs is on {@link PassEntryRepository}. */
@Repository
public interface LeviEntryRepository extends PassEntryRepository<LeviEntry> {
}
