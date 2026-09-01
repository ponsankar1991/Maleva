package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.PassEntry;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Data access shared by the truck pass documents.
 *
 * The queries use {@code #{#entityName}}, which Spring Data resolves per
 * concrete repository, so levi and auto pass entries get the same three
 * operations without either declaring them.
 *
 * List queries come from
 * {@link my.maleva.api.module.fleet.specification.PassEntrySpecification},
 * because every filter on both screens is optional.
 */
@NoRepositoryBean
public interface PassEntryRepository<T extends PassEntry>
        extends JpaRepository<T, Integer>, JpaSpecificationExecutor<T> {

    /** Default ordering of both lists: oldest document date first, matching the legacy grids. */
    Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "saleDate");

    Optional<T> findByIdAndCompanyRefIdAndActive(Integer id, Integer companyRefId, Integer active);

    /**
     * Resolves the internal id from a printed document number.
     *
     * Both legacy lookups ran against a {@code ...Master} table that does not
     * exist in the database ({@code LeviEntryMaster}, {@code AutoPassEntryMaster}),
     * so the "open by number" path threw every time it was used. These read the
     * real tables.
     */
    @Query("select e.id from #{#entityName} e "
            + "where e.companyRefId = :companyRefId and e.cNumber = :cNumber and e.active = 1")
    List<Integer> findIdsByCNumber(@Param("companyRefId") Integer companyRefId,
                                   @Param("cNumber") Integer cNumber);

    // No bulk soft-delete query here on purpose. This screen used to have one and
    // test its affected-row count against 0, but the pool runs `SET NOCOUNT ON` as
    // its connection-init SQL, so SQL Server sends no row count and JDBC reports -1
    // for every UPDATE - the count can never be 0 and the check never fired.
    // AbstractPassEntryService.delete loads the entry and updates it as a managed entity instead.
}
