package my.maleva.api.module.payment.repository;

import my.maleva.api.module.payment.entity.PaymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentDetailsRepository extends JpaRepository<PaymentDetails, Integer> {

    /**
     * The documents one payment settles. SP_Payment cleared these and
     * re-inserted on every edit, so the posted grid is the only source of
     * truth for what a payment covers.
     */
    List<PaymentDetails> findByPaymentRefId(Integer paymentRefId);
}
