package my.maleva.api.module.payment.repository;

import my.maleva.api.module.payment.entity.Payment;
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
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    /**
     * One-time write-back of the QNE identity after a successful pay-bill
     * push (QNE's Id and PaymentCode land in QNEId/QNECode). The empty-code
     * guard is the only dedup mechanism — the PayBills POST is create-once.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Payment p SET p.qneId = :qneId, p.qneCode = :qneCode " +
           "WHERE p.id = :id AND (p.qneCode IS NULL OR p.qneCode = '')")
    int claimQneIdentity(@Param("id") Integer id,
                         @Param("qneId") String qneId,
                         @Param("qneCode") String qneCode);

    /** Loads a payment by the running number a clerk typed, rather than its id. */
    Optional<Payment> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Payments entered a moment ago that look identical to the one being saved.
     *
     * <p>Used to absorb a double-click, a retry or a second tab: two clerks
     * genuinely paying the same supplier the same amount on the same day would
     * be minutes apart, not seconds, so the window is what keeps this from
     * rejecting real work.
     */
    @Query("SELECT p FROM Payment p WHERE p.companyRefId = :companyId "
            + "AND p.supplierRefId = :supplierRefId AND p.bankRefId = :bankRefId "
            + "AND p.paymentDate = :paymentDate AND p.amount = :amount "
            + "AND COALESCE(p.refNumber, '') = :refNumber "
            + "AND p.createdDate >= :since ORDER BY p.id DESC")
    List<Payment> findRecentlyEnteredLikeThis(@Param("companyId") Integer companyId,
                                              @Param("supplierRefId") Integer supplierRefId,
                                              @Param("bankRefId") Integer bankRefId,
                                              @Param("paymentDate") LocalDateTime paymentDate,
                                              @Param("amount") BigDecimal amount,
                                              @Param("refNumber") String refNumber,
                                              @Param("since") LocalDateTime since);
}
