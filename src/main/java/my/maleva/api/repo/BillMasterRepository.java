package my.maleva.api.repo;

import my.maleva.api.model.BillMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillMasterRepository extends JpaRepository<BillMaster, Integer> {
    List<BillMaster> findByCompanyRefId(Integer companyRefId);
    List<BillMaster> findBySupplierRefId(Integer supplierRefId);
}
