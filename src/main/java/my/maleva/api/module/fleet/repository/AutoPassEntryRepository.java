package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.AutoPassEntry;
import org.springframework.stereotype.Repository;

import java.util.List;

/** AutoPassEntry data access. Everything else it needs is on {@link PassEntryRepository}. */
@Repository
public interface AutoPassEntryRepository extends PassEntryRepository<AutoPassEntry> {

    /** Kept from the original scaffold; used by callers outside the pass entry screens. */
    List<AutoPassEntry> findByCompanyRefId(Integer companyRefId);
}
