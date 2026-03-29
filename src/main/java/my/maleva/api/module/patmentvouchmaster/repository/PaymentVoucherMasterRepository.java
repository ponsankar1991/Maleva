package my.maleva.api.module.patmentvouchmaster.repository;

import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentVoucherMasterRepository extends JpaRepository<PaymentVoucherMaster, Integer> {
}
