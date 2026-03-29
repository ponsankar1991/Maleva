package my.maleva.api.module.expense.repository;

import my.maleva.api.module.expense.entity.ExpenseMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseMasterRepository extends JpaRepository<ExpenseMaster, Integer> {
    List<ExpenseMaster> findByCompanyRefId(Integer companyRefId);
}
