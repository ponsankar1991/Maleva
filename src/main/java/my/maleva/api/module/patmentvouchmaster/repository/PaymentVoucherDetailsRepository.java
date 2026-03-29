package my.maleva.api.module.patmentvouchmaster.repository;

import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentVoucherDetailsRepository extends JpaRepository<PaymentVoucherDetails, Integer> {
}
