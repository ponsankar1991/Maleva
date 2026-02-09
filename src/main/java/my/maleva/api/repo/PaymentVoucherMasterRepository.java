package my.maleva.api.repo;

import my.maleva.api.model.PaymentVoucherMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentVoucherMasterRepository extends JpaRepository<PaymentVoucherMaster, Integer> {
}
