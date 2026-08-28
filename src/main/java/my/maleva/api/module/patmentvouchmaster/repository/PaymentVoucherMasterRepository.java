package my.maleva.api.module.patmentvouchmaster.repository;

import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentVoucherMasterRepository extends JpaRepository<PaymentVoucherMaster, Integer> {

    /**
     * Find distinct PayTo values for a company with active status
     * Equivalent to .NET SelectPaymentTo method
     * Returns distinct, trimmed, non-empty PayTo values ordered alphabetically
     * SQL: select distinct LTRIM(RTRIM(PayTo)) from PaymentVoucherMaster
     *      where PayTo != '' and PayTo is not null and Active != 2 and CompanyRefId = ?
     *      order by PayTo
     */
    @Query(value = "SELECT DISTINCT LTRIM(RTRIM(A.PayTo)) as payTo " +
            "FROM PaymentVoucherMaster A " +
            "WHERE A.PayTo != '' " +
            "AND A.PayTo IS NOT NULL " +
            "AND A.Active != 2 " +
            "AND A.CompanyRefId = :companyRefId " +
            "ORDER BY LTRIM(RTRIM(A.PayTo)) ASC",
            nativeQuery = true)
    List<String> findDistinctPayToByCompanyId(@Param("companyRefId") Integer companyRefId);

    /**
     * Find distinct PayFrom values for a company with active status
     * Equivalent to .NET SelectPaymentFrom method
     * Returns distinct, trimmed, non-empty PayFrom values ordered alphabetically
     * SQL: select distinct LTRIM(RTRIM(PayFrom)) from PaymentVoucherMaster
     *      where PayFrom != '' and PayFrom is not null and Active != 2 and CompanyRefId = ?
     *      order by PayFrom
     */
    @Query(value = "SELECT DISTINCT LTRIM(RTRIM(A.PayFrom)) as payFrom " +
            "FROM PaymentVoucherMaster A " +
            "WHERE A.PayFrom != '' " +
            "AND A.PayFrom IS NOT NULL " +
            "AND A.Active != 2 " +
            "AND A.CompanyRefId = :companyRefId " +
            "ORDER BY LTRIM(RTRIM(A.PayFrom)) ASC",
            nativeQuery = true)
    List<String> findDistinctPayFromByCompanyId(@Param("companyRefId") Integer companyRefId);

    /**
     * One-time write-back of the QNE identity after a successful voucher push
     * (QNE's Id and PaymentCode land in QNEId/QNECode). The empty-code guard
     * is the only dedup mechanism — the PaymentVouchers POST is create-once.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE PaymentVoucherMaster pvm SET pvm.qneId = :qneId, pvm.qneCode = :qneCode " +
           "WHERE pvm.id = :id AND (pvm.qneCode IS NULL OR pvm.qneCode = '')")
    int claimQneIdentity(@Param("id") Integer id,
                         @Param("qneId") String qneId,
                         @Param("qneCode") String qneCode);

    /** Loads a voucher by the running number a clerk typed, rather than its id. */
    Optional<PaymentVoucherMaster> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Vouchers entered a moment ago that look identical to the one being
     * saved — the double-click / retry / second-tab window. Two clerks
     * genuinely raising the same voucher would be minutes apart, not seconds.
     */
    @Query("SELECT v FROM PaymentVoucherMaster v WHERE v.companyRefId = :companyId "
            + "AND v.payTo = :payTo AND v.paymentById = :paymentById "
            + "AND v.paymentVoucherDate = :voucherDate AND v.amount = :amount "
            + "AND COALESCE(v.refNo, '') = :refNo AND v.active = 1 "
            + "AND v.createdDate >= :since ORDER BY v.id DESC")
    List<PaymentVoucherMaster> findRecentlyEnteredLikeThis(@Param("companyId") Integer companyId,
                                                           @Param("payTo") String payTo,
                                                           @Param("paymentById") Integer paymentById,
                                                           @Param("voucherDate") LocalDateTime voucherDate,
                                                           @Param("amount") BigDecimal amount,
                                                           @Param("refNo") String refNo,
                                                           @Param("since") LocalDateTime since);
}
