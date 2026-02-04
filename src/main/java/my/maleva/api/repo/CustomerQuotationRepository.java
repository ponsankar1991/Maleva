package my.maleva.api.repo;

import my.maleva.api.model.CustomerQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerQuotationRepository extends JpaRepository<CustomerQuotation, Integer> {
    List<CustomerQuotation> findByCompanyRefId(Integer companyRefId);
    List<CustomerQuotation> findByCustomerMasterRefId(Integer customerMasterRefId);
    List<CustomerQuotation> findByJobMasterRefId(Integer jobMasterRefId);
}
