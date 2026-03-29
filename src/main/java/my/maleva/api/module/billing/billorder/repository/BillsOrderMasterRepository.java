package my.maleva.api.module.billing.billorder.repository;

import my.maleva.api.module.billing.billorder.entity.BillsOrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillsOrderMasterRepository extends JpaRepository<BillsOrderMaster, Integer> {
    List<BillsOrderMaster> findByCompanyRefId(Integer companyRefId);
    List<BillsOrderMaster> findBySupplierRefId(Integer supplierRefId);
}
