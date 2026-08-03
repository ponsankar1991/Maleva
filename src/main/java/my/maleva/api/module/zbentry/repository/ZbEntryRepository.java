package my.maleva.api.module.zbentry.repository;

import my.maleva.api.module.zbentry.entity.ZbEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ZbEntryRepository extends JpaRepository<ZbEntry, Integer> {

    Optional<ZbEntry> findByIdAndCompanyRefId(Integer id, Integer companyRefId);

    String SEARCH_QUERY = """
            SELECT * FROM ZbEntry S
            WHERE S.CompanyRefId = :companyRefId
            AND S.Active != 2
            AND CONVERT(date, S.EntryDate, 103) >= :fromDate
            AND CONVERT(date, S.EntryDate, 103) <= :toDate
            AND (:chargeType IS NULL OR :chargeType = '' OR S.ChargeType = :chargeType)
            AND (:keyword IS NULL OR :keyword = '' 
                 OR S.VesselName LIKE '%' + :keyword + '%' 
                 OR S.JobNumber LIKE '%' + :keyword + '%' 
                 OR S.ZBNumber LIKE '%' + :keyword + '%')
            """;

    String COUNT_QUERY = """
            SELECT count(*) FROM ZbEntry S
            WHERE S.CompanyRefId = :companyRefId
            AND S.Active != 2
            AND CONVERT(date, S.EntryDate, 103) >= :fromDate
            AND CONVERT(date, S.EntryDate, 103) <= :toDate
            AND (:chargeType IS NULL OR :chargeType = '' OR S.ChargeType = :chargeType)
            AND (:keyword IS NULL OR :keyword = '' 
                 OR S.VesselName LIKE '%' + :keyword + '%' 
                 OR S.JobNumber LIKE '%' + :keyword + '%' 
                 OR S.ZBNumber LIKE '%' + :keyword + '%')
            """;

    @Query(value = SEARCH_QUERY, countQuery = COUNT_QUERY, nativeQuery = true)
    Page<ZbEntry> searchZbEntries(
            @Param("companyRefId") Integer companyRefId,
            @Param("chargeType") String chargeType,
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );
}
