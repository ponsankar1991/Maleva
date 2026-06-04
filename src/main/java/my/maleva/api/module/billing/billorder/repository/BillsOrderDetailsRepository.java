package my.maleva.api.module.billing.billorder.repository;

import my.maleva.api.module.billing.billorder.entity.BillsOrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillsOrderDetailsRepository extends JpaRepository<BillsOrderDetails, Integer> {
    List<BillsOrderDetails> findByBillsOrderMasterRefId(Integer billsOrderMasterRefId);
    void deleteByBillsOrderMasterRefId(Integer billsOrderMasterRefId);
}
