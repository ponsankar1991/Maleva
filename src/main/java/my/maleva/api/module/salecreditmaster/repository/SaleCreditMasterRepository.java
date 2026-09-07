package my.maleva.api.module.salecreditmaster.repository;

import my.maleva.api.module.salecreditmaster.entity.SaleCreditMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SaleCreditMasterRepository
 * Spring Data JPA Repository for SaleCreditMaster entity
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SaleCreditMasterRepository extends JpaRepository<SaleCreditMaster, Integer> {

    /**
     * Find all SaleCreditMaster records by company ID
     */
    List<SaleCreditMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find SaleCreditMaster records by company ID and C Status
     */
    List<SaleCreditMaster> findByCompanyRefIdAndCStatus(Integer companyRefId, Integer cStatus);

    /**
     * Find SaleCreditMaster records by customer ID
     */
    List<SaleCreditMaster> findByCustomerRefId(Integer customerRefId);

    /**
     * Find SaleCreditMaster records by company ID and customer ID
     */
    List<SaleCreditMaster> findByCompanyRefIdAndCustomerRefId(Integer companyRefId, Integer customerRefId);

    /**
     * Find SaleCreditMaster by reference number
     */
    Optional<SaleCreditMaster> findByCompanyRefIdAndRefNumber(Integer companyRefId, String refNumber);

    /**
     * Find SaleCreditMaster by C Number.
     *
     * <p>Spelled out as JPQL rather than derived from the method name. A
     * derived {@code ...AndCNumber} resolves through the Lombok accessor
     * {@code getCNumber()}, whose bean property name keeps both capitals
     * ({@code CNumber}) because {@link java.beans.Introspector} does not
     * decapitalise a name whose first two letters are upper case. Hibernate
     * maps this entity by field and only knows {@code cNumber}, so the
     * generated query fails at execution time with "Could not resolve
     * attribute 'CNumber'".
     */
    @Query("SELECT scm FROM SaleCreditMaster scm WHERE scm.companyRefId = :companyRefId "
           + "AND scm.cNumber = :cNumber")
    Optional<SaleCreditMaster> findByCompanyRefIdAndCNumber(@Param("companyRefId") Integer companyRefId,
                                                            @Param("cNumber") Integer cNumber);

    /**
     * Find SaleCreditMaster records by date range
     */
    @Query("SELECT scm FROM SaleCreditMaster scm WHERE scm.companyRefId = :companyRefId " +
           "AND scm.saleDate BETWEEN :startDate AND :endDate ORDER BY scm.saleDate DESC")
    List<SaleCreditMaster> findByDateRange(@Param("companyRefId") Integer companyRefId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find SaleCreditMaster records by employee ID
     */
    List<SaleCreditMaster> findByEmployeeRefId(Integer employeeRefId);

    /**
     * Find SaleCreditMaster records by user ID
     */
    List<SaleCreditMaster> findByUserRefId(Integer userRefId);

    /**
     * Find SaleCreditMaster records by Sale Master Reference ID
     */
    List<SaleCreditMaster> findBySaleMasterRefId(Integer saleMasterRefId);

    /**
     * Count SaleCreditMaster by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count SaleCreditMaster by company and status
     */
    long countByCompanyRefIdAndCStatus(Integer companyRefId, Integer cStatus);

    /**
     * Count SaleCreditMaster by customer
     */
    long countByCustomerRefId(Integer customerRefId);

    /** Check if C Number exists. Spelled out as JPQL for the reason above. */
    @Query("SELECT CASE WHEN COUNT(scm) > 0 THEN true ELSE false END FROM SaleCreditMaster scm "
           + "WHERE scm.companyRefId = :companyRefId AND scm.cNumber = :cNumber")
    boolean existsByCompanyRefIdAndCNumber(@Param("companyRefId") Integer companyRefId,
                                           @Param("cNumber") Integer cNumber);

    /**
     * Check if reference number exists
     */
    boolean existsByCompanyRefIdAndRefNumber(Integer companyRefId, String refNumber);

    /**
     * Find SaleCreditMaster records by date and company with specific status
     */
    @Query("SELECT scm FROM SaleCreditMaster scm WHERE scm.companyRefId = :companyRefId " +
           "AND scm.saleDate >= :startDate AND scm.saleDate <= :endDate " +
           "AND scm.cStatus = :cStatus ORDER BY scm.saleDate DESC")
    List<SaleCreditMaster> findByDateAndStatus(@Param("companyRefId") Integer companyRefId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               @Param("cStatus") Integer cStatus);

    /**
     * Find SaleCreditMaster records by company and employee
     */
    List<SaleCreditMaster> findByCompanyRefIdAndEmployeeRefId(Integer companyRefId, Integer employeeRefId);

    /**
     * One-time write-back of the QNE identity after a successful credit-note
     * push (QNE's Id and CnCode land in QNEId/QNECode). The empty-code guard
     * is the only dedup mechanism — the CN POST is create-once.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE SaleCreditMaster scm SET scm.qneId = :qneId, scm.qneCode = :qneCode " +
           "WHERE scm.id = :id AND (scm.qneCode IS NULL OR scm.qneCode = '')")
    int claimQneIdentity(@Param("id") Integer id,
                         @Param("qneId") String qneId,
                         @Param("qneCode") String qneCode);

    /**
     * The highest credit note sequence issued so far, straight off the notes
     * themselves rather than {@code SequenceNoMaster}.
     *
     * <p>{@code SequenceNoMaster} is the allocator, but a company that has
     * never had its row created there reports 0 forever, because the SP's
     * {@code update SequenceNoMaster ...} updates no rows and silently
     * succeeds — every note then took number 1. The service seeds the missing
     * row from this value so the sequence continues rather than restarting.
     */
    @Query("SELECT COALESCE(MAX(scm.cNumber), 0) FROM SaleCreditMaster scm WHERE scm.companyRefId = :companyRefId")
    Integer findMaxCNumber(@Param("companyRefId") Integer companyRefId);

    /**
     * Records LHDN's acceptance the moment it is known, in its own
     * transaction. Legacy wrote the UUID only after the follow-up status call,
     * so an error in between lost it and the next click submitted the same
     * credit note to the government twice.
     *
     * <p>The affected-row count is not meaningful here: the pool runs
     * {@code SET NOCOUNT ON}.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE SaleCreditMaster scm SET scm.eInvoiceUid = :uuid, scm.eInvoiceSUid = :submissionUid, "
           + "scm.eInvoiceLongId = '', scm.eInvoiceStatus = :status, scm.eInvoicePushDT = :pushedAt, "
           + "scm.eInvoicePushVDT = NULL "
           + "WHERE scm.id = :id AND scm.companyRefId = :companyId")
    int claimEInvoiceSubmission(@Param("id") Integer id,
                                @Param("companyId") Integer companyId,
                                @Param("uuid") String uuid,
                                @Param("submissionUid") String submissionUid,
                                @Param("status") String status,
                                @Param("pushedAt") LocalDateTime pushedAt);

    /** LHDN reported a status but has not validated yet (no long id, no validated time). */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE SaleCreditMaster scm SET scm.eInvoiceStatus = :status "
           + "WHERE scm.id = :id AND scm.companyRefId = :companyId")
    int recordEInvoiceStatus(@Param("id") Integer id,
                             @Param("companyId") Integer companyId,
                             @Param("status") String status);

    /**
     * LHDN reported the document's outcome. The long id and validated time are
     * whatever LHDN sent — either may be absent; never "now" as a placeholder,
     * which is what legacy stored whenever the status read failed.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE SaleCreditMaster scm SET scm.eInvoiceLongId = :longId, scm.eInvoiceStatus = :status, "
           + "scm.eInvoicePushVDT = :validatedAt "
           + "WHERE scm.id = :id AND scm.companyRefId = :companyId")
    int recordEInvoiceValidation(@Param("id") Integer id,
                                 @Param("companyId") Integer companyId,
                                 @Param("longId") String longId,
                                 @Param("status") String status,
                                 @Param("validatedAt") LocalDateTime validatedAt);
}

