package my.maleva.api.repo;

import my.maleva.api.model.ExpenseMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseMasterRepository extends JpaRepository<ExpenseMaster, Integer> {
    List<ExpenseMaster> findByCompanyRefId(Integer companyRefId);
}
