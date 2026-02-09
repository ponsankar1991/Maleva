package my.maleva.api.repo;

import my.maleva.api.model.PaymentReceiptInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentReceiptInfoRepository extends JpaRepository<PaymentReceiptInfo, Integer> {
}
