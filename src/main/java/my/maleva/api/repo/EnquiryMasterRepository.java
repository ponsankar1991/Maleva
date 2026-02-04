package my.maleva.api.repo;

import my.maleva.api.model.EnquiryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnquiryMasterRepository extends JpaRepository<EnquiryMaster, Integer> {
    List<EnquiryMaster> findByCompanyRefId(Integer companyRefId);
    List<EnquiryMaster> findByCustomerRefId(Integer customerRefId);
    List<EnquiryMaster> findByJobMasterRefId(Integer jobMasterRefId);
}
