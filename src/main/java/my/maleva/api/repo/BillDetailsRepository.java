package my.maleva.api.repo;

import my.maleva.api.model.BillDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillDetailsRepository extends JpaRepository<BillDetails, Integer> {
    List<BillDetails> findByBillMasterRefId(Integer billMasterRefId);
}
