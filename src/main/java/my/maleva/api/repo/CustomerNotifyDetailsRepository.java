package my.maleva.api.repo;

import my.maleva.api.model.CustomerNotifyDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerNotifyDetailsRepository extends JpaRepository<CustomerNotifyDetails, Integer> {
    List<CustomerNotifyDetails> findByCompanyRefId(Integer companyRefId);
    List<CustomerNotifyDetails> findByCustomerMasterRefId(Integer customerMasterRefId);
}
