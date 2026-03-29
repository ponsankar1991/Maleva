package my.maleva.api.module.pendingpayment.repository;

import my.maleva.api.module.pendingpayment.entity.PendingPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingPaymentRepository extends JpaRepository<PendingPayment, Integer> {
}
