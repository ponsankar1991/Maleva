package my.maleva.api.repo;

import my.maleva.api.model.PaymentVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentVoucherRepository extends JpaRepository<PaymentVoucher, Integer> {
}
