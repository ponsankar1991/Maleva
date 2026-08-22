package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.FuelEntry;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FuelEntry data access.
 *
 * The list query is built from {@link my.maleva.api.module.fleet.specification.FuelEntrySpecification}
 * rather than expressed here, because the filters are all optional.
 */
@Repository
public interface FuelEntryRepository extends JpaRepository<FuelEntry, Integer>,
        JpaSpecificationExecutor<FuelEntry> {

    /** Default ordering of the list, matching the legacy ORDER BY Created_Date. */
    Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "createdDate");

    Optional<FuelEntry> findByIdAndCompanyRefIdAndActive(Integer id, Integer companyRefId, Integer active);

    /** Resolves the internal id from a printed fuel number. */
    @Query("select e.id from FuelEntry e "
            + "where e.companyRefId = :companyRefId and e.cNumber = :cNumber and e.active = 1")
    List<Integer> findIdsByCNumber(@Param("companyRefId") Integer companyRefId,
                                   @Param("cNumber") Integer cNumber);

    /**
     * Soft delete, matching the legacy {@code update FuelEntry set Active=2}.
     *
     * @param fStatus when non-null the row must also carry this FStatus, which is
     *                how the legacy call guarded deletes coming from the driver app
     */
    @Modifying
    @Query("update FuelEntry e set e.active = 2, e.modifiedDate = current_timestamp, "
            + "e.modifiedBy = :modifiedBy "
            + "where e.id = :id and e.companyRefId = :companyRefId and e.active = 1 "
            + "and (:fStatus is null or e.fStatus = :fStatus)")
    int softDelete(@Param("id") Integer id,
                   @Param("companyRefId") Integer companyRefId,
                   @Param("fStatus") Integer fStatus,
                   @Param("modifiedBy") String modifiedBy);
}
