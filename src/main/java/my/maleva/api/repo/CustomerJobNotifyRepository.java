package my.maleva.api.repo;

import my.maleva.api.model.CustomerJobNotify;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerJobNotifyRepository extends JpaRepository<CustomerJobNotify, Integer> {
    List<CustomerJobNotify> findByCompanyRefId(Integer companyRefId);
    List<CustomerJobNotify> findByCustomerDetailRefId(Integer customerDetailRefId);
    List<CustomerJobNotify> findBySaleOrderRefId(Integer saleOrderRefId);
}
