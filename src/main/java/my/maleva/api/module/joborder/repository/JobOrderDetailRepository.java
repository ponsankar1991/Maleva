package my.maleva.api.module.joborder.repository;

import my.maleva.api.module.joborder.entity.JobOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobOrderDetailRepository extends JpaRepository<JobOrderDetail, Integer> {
    List<JobOrderDetail> findByJobOrderMasterRefId(Integer jobOrderMasterRefId);
    void deleteByJobOrderMasterRefId(Integer jobOrderMasterRefId);
}
