package my.maleva.api.module.payment.repository;

import my.maleva.api.module.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
}
