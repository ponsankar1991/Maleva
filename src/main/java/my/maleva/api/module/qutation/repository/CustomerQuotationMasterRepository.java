package my.maleva.api.module.qutation.repository;

import my.maleva.api.module.qutation.entity.CustomerQuotationMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerQuotationMasterRepository extends JpaRepository<CustomerQuotationMaster, Integer> {
    List<CustomerQuotationMaster> findByCompanyRefId(Integer companyRefId);
    List<CustomerQuotationMaster> findByCustomerRefId(Integer customerRefId);
}
