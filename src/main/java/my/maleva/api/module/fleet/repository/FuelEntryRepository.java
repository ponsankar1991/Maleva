package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.FuelEntry;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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

    // No bulk soft-delete query here on purpose. This screen used to have one and
    // test its affected-row count against 0, but the pool runs `SET NOCOUNT ON` as
    // its connection-init SQL, so SQL Server sends no row count and JDBC reports -1
    // for every UPDATE - the count can never be 0 and the check never fired.
    // FuelEntryServiceImpl.delete loads the entry and updates it as a managed entity instead.
}
