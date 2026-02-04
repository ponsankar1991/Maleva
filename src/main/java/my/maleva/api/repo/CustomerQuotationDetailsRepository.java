package my.maleva.api.repo;

import my.maleva.api.model.CustomerQuotationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerQuotationDetailsRepository extends JpaRepository<CustomerQuotationDetails, Integer> {
    List<CustomerQuotationDetails> findByCustomerQuotationRefId(Integer customerQuotationRefId);
}
