package my.maleva.api.repo;

import my.maleva.api.model.PaymentVoucherDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentVoucherDetailsRepository extends JpaRepository<PaymentVoucherDetails, Integer> {
}
