package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.TollEntry;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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

    // No bulk soft-delete query here on purpose. This screen used to have one and
    // test its affected-row count against 0, but the pool runs `SET NOCOUNT ON` as
    // its connection-init SQL, so SQL Server sends no row count and JDBC reports -1
    // for every UPDATE - the count can never be 0 and the check never fired.
    // TollEntryServiceImpl.delete loads the entry and updates it as a managed entity instead.
}
