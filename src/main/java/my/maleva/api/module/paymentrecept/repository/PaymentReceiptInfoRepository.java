package my.maleva.api.module.paymentrecept.repository;

import my.maleva.api.module.paymentrecept.entity.PaymentReceiptInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentReceiptInfoRepository extends JpaRepository<PaymentReceiptInfo, Integer> {
}
