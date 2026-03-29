package my.maleva.api.module.patmentvouchmaster.repository;

import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentVoucherRepository extends JpaRepository<PaymentVoucher, Integer> {
}
