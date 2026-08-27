package my.maleva.api.module.patmentvouchmaster.repository;

import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentVoucherDetailsRepository extends JpaRepository<PaymentVoucherDetails, Integer> {

    /** Voucher lines in insertion order — the legacy QNE payload ordered by detail id. */
    List<PaymentVoucherDetails> findByPaymentVoucherMasterRefIdOrderByIdAsc(Integer paymentVoucherMasterRefId);
}
