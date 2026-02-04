package my.maleva.api.repo;

import my.maleva.api.model.ForwardingSalary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForwardingSalaryRepository extends JpaRepository<ForwardingSalary, Integer> {
    List<ForwardingSalary> findByCompanyRefId(Integer companyRefId);
    List<ForwardingSalary> findByRtiMasterRefId(Integer rtiMasterRefId);
}
