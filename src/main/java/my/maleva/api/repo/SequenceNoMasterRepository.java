package my.maleva.api.repo;

import my.maleva.api.model.SequenceNoMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for SequenceNoMaster entity
 * Provides database operations for sequence number management
 */
@Repository
public interface SequenceNoMasterRepository extends JpaRepository<SequenceNoMaster, Integer> {

    /**
     * Find all sequences for a specific company
     *
     * @param companyRefId the company ID
     * @return list of sequences for the company
     */
    List<SequenceNoMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find a sequence by company and sequence name
     *
     * @param companyRefId the company ID
     * @param sequenceName the sequence name
     * @return the sequence if found
     */
    Optional<SequenceNoMaster> findByCompanyRefIdAndSequenceName(
            @Param("companyRefId") Integer companyRefId,
            @Param("sequenceName") String sequenceName);

    /**
     * Get the maximum sequence number for a specific company and sequence name
     * This is used for generating the next sequence number
     *
     * @param companyRefId the company ID
     * @param sequenceName the sequence name
     * @return the maximum sequence number
     */
    @Query("SELECT COALESCE(MAX(s.sequenceNo), 0) FROM SequenceNoMaster s " +
            "WHERE s.companyRefId = :companyRefId AND s.sequenceName = :sequenceName")
    Integer findMaxSequenceNoByCompanyAndName(
            @Param("companyRefId") Integer companyRefId,
            @Param("sequenceName") String sequenceName);

    /**
     * Get the maximum sequence number for a specific company and bill type
     * Automatically concatenates 'SaleOrderMaster' + billType to match sequence name pattern
     * Example: billType='SO' searches for sequenceName='SaleOrderMasterSO'
     *
     * @param companyRefId the company ID
     * @param billType the bill type (SO, INV, QT, PO, etc.)
     * @return the maximum sequence number for that bill type
     */
    @Query("SELECT COALESCE(MAX(s.sequenceNo), 0) FROM SequenceNoMaster s " +
            "WHERE s.companyRefId = :companyRefId AND s.sequenceName = CONCAT('SaleOrderMaster', :billType)")
    Integer findMaxSequenceNoByCompanyAndBillType(
            @Param("companyRefId") Integer companyRefId,
            @Param("billType") String billType);

    /**
     * Find sequences by company ID and sequence year
     *
     * @param companyRefId the company ID
     * @param sequenceYear the year
     * @return list of sequences for the year
     */
    List<SequenceNoMaster> findByCompanyRefIdAndSequenceYear(
            Integer companyRefId,
            Integer sequenceYear);
}

