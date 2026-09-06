package my.maleva.api.module.invoice.repository;

import my.maleva.api.module.invoice.entity.SaleMaster;
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
 * SaleMasterRepository
 * Spring Data JPA Repository for SaleMaster entity
 */
@Repository
public interface SaleMasterRepository extends JpaRepository<SaleMaster, Integer> {

    List<SaleMaster> findByCompanyRefId(Integer companyRefId);
    List<SaleMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);
    List<SaleMaster> findByCustomerRefId(Integer customerRefId);
    List<SaleMaster> findByCompanyRefIdAndCustomerRefId(Integer companyRefId, Integer customerRefId);
    List<SaleMaster> findByEmployeeRefId(Integer employeeRefId);
    List<SaleMaster> findByCompanyRefIdAndEmployeeRefId(Integer companyRefId, Integer employeeRefId);
    List<SaleMaster> findByUserRefId(Integer userRefId);
    List<SaleMaster> findByCompanyRefIdAndBillType(Integer companyRefId, String billType);
    List<SaleMaster> findByCompanyRefIdAndSaleType(Integer companyRefId, String saleType);

    @Query("SELECT sm FROM SaleMaster sm WHERE sm.companyRefId = :companyRefId " +
           "AND sm.saleDate BETWEEN :startDate AND :endDate ORDER BY sm.saleDate DESC")
    List<SaleMaster> findByDateRange(@Param("companyRefId") Integer companyRefId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    Optional<SaleMaster> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);
    boolean existsByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    @Query("SELECT sm FROM SaleMaster sm WHERE sm.companyRefId = :companyRefId " +
           "AND sm.saleDate >= :startDate AND sm.saleDate <= :endDate " +
           "AND sm.active = :active ORDER BY sm.saleDate DESC")
    List<SaleMaster> findByDateAndStatus(@Param("companyRefId") Integer companyRefId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        @Param("active") Integer active);

    long countByCompanyRefId(Integer companyRefId);
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);
    long countByCustomerRefId(Integer customerRefId);

    List<SaleMaster> findByJobMasterRefId(Integer jobMasterRefId);
    List<SaleMaster> findByAgentMasterRefId(Integer agentMasterRefId);
    List<SaleMaster> findByDriverRefid(Integer driverRefid);

    @Query("SELECT MAX(sm.cNumber) FROM SaleMaster sm WHERE sm.companyRefId = :companyRefId")
    Optional<Integer> findMaxCNumberByCompanyId(@Param("companyRefId") Integer companyRefId);

    /**
     * One-time write-back of the QNE identity after a successful invoice push
     * (QNE's Id and InvoiceCode land in QNEId/QNECode). The empty-code guard
     * is the only dedup mechanism — the invoice POST is create-once, and the
     * live PUT update never rewrites these columns (legacy contract).
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE SaleMaster sm SET sm.qneId = :qneId, sm.qneCode = :qneCode " +
           "WHERE sm.id = :id AND (sm.qneCode IS NULL OR sm.qneCode = '')")
    int claimQneIdentity(@Param("id") Integer id,
                         @Param("qneId") String qneId,
                         @Param("qneCode") String qneCode);

    /**
     * Records that LHDN accepted the invoice for validation. Runs in its own
     * transaction and is called the moment the 202 arrives, BEFORE the status
     * is read back — legacy wrote the UUID only after that read, and a failure
     * in between left the UUID unsaved so the next click submitted the same
     * invoice to the government a second time.
     *
     * <p>Writes the identity of the new submission and clears the outcome of
     * any previous one (long id and validated time), so a resubmitted Invalid
     * invoice does not keep the old rejection's timestamp. Unconditional: the
     * service decides beforehand whether a submission is allowed, and the row
     * count is not meaningful here (the pool runs {@code SET NOCOUNT ON}).
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE SaleMaster sm SET sm.eInvoiceUid = :uuid, sm.eInvoiceSUid = :submissionUid, "
            + "sm.eInvoiceLongId = '', sm.eInvoiceStatus = :status, sm.eInvoicePushDT = :pushedAt, "
            + "sm.eInvoicePushVDT = NULL "
            + "WHERE sm.id = :id AND sm.companyRefId = :companyId")
    int claimEInvoiceSubmission(@Param("id") Integer id,
                                @Param("companyId") Integer companyId,
                                @Param("uuid") String uuid,
                                @Param("submissionUid") String submissionUid,
                                @Param("status") String status,
                                @Param("pushedAt") LocalDateTime pushedAt);

    /** LHDN reported a status but has not validated yet (no long id, no validated time). */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE SaleMaster sm SET sm.eInvoiceStatus = :status "
            + "WHERE sm.id = :id AND sm.companyRefId = :companyId")
    int recordEInvoiceStatus(@Param("id") Integer id,
                             @Param("companyId") Integer companyId,
                             @Param("status") String status);

    /**
     * LHDN reported the document's outcome. The long id (Valid documents) and
     * the validated time are whatever LHDN sent — either may be absent (an
     * Invalid document has a validated time but no long id); never "now" as a
     * placeholder, which is what legacy stored when the status read failed.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE SaleMaster sm SET sm.eInvoiceLongId = :longId, sm.eInvoiceStatus = :status, "
            + "sm.eInvoicePushVDT = :validatedAt "
            + "WHERE sm.id = :id AND sm.companyRefId = :companyId")
    int recordEInvoiceValidation(@Param("id") Integer id,
                                 @Param("companyId") Integer companyId,
                                 @Param("longId") String longId,
                                 @Param("status") String status,
                                 @Param("validatedAt") LocalDateTime validatedAt);
}

