package my.maleva.api.module.billing.bill.repository;

import my.maleva.api.module.billing.bill.entity.BillDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillDetailsRepository extends JpaRepository<BillDetails, Integer> {
    List<BillDetails> findByBillMasterRefId(Integer billMasterRefId);
}
