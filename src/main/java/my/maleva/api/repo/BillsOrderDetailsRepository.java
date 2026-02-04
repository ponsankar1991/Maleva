package my.maleva.api.repo;

import my.maleva.api.model.BillsOrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillsOrderDetailsRepository extends JpaRepository<BillsOrderDetails, Integer> {
    List<BillsOrderDetails> findByBillsOrderMasterRefId(Integer billsOrderMasterRefId);
}
