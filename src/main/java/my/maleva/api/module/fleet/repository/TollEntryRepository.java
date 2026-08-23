package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.TollEntry;
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
 * TollEntry data access.
 *
 * The list query is built from
 * {@link my.maleva.api.module.fleet.specification.TollEntrySpecification},
 * because every filter is optional.
 */
@Repository
public interface TollEntryRepository extends JpaRepository<TollEntry, Integer>,
        JpaSpecificationExecutor<TollEntry> {

    /** Default ordering of the list: oldest toll date first, matching the legacy sort. */
    Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "saleDate");

    Optional<TollEntry> findByIdAndCompanyRefIdAndActive(Integer id, Integer companyRefId, Integer active);

    /** Resolves the internal id from a printed toll number. */
    @Query("select t.id from TollEntry t "
            + "where t.companyRefId = :companyRefId and t.cNumber = :cNumber and t.active = 1")
    List<Integer> findIdsByCNumber(@Param("companyRefId") Integer companyRefId,
                                   @Param("cNumber") Integer cNumber);

    /** Soft delete, matching the legacy {@code update TollEntry set Active=2}. */
    @Modifying
    @Query("update TollEntry t set t.active = 2, t.modifiedDate = current_timestamp, "
            + "t.modifiedBy = :modifiedBy "
            + "where t.id = :id and t.companyRefId = :companyRefId and t.active = 1")
    int softDelete(@Param("id") Integer id,
                   @Param("companyRefId") Integer companyRefId,
                   @Param("modifiedBy") String modifiedBy);
}
