package my.maleva.api.repo;

import my.maleva.api.model.CustomerQuotationGC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerQuotationGCRepository extends JpaRepository<CustomerQuotationGC, Integer> {
    List<CustomerQuotationGC> findByCompanyRefId(Integer companyRefId);
    List<CustomerQuotationGC> findByCustomerMasterRefId(Integer customerMasterRefId);
    List<CustomerQuotationGC> findByJobMasterRefId(Integer jobMasterRefId);
}
