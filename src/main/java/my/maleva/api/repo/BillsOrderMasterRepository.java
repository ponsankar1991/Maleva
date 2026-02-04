package my.maleva.api.repo;

import my.maleva.api.model.BillsOrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillsOrderMasterRepository extends JpaRepository<BillsOrderMaster, Integer> {
    List<BillsOrderMaster> findByCompanyRefId(Integer companyRefId);
    List<BillsOrderMaster> findBySupplierRefId(Integer supplierRefId);
}
