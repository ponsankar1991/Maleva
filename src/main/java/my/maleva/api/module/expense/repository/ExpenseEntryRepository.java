package my.maleva.api.module.expense.repository;

import my.maleva.api.module.expense.entity.ExpenseEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseEntryRepository extends JpaRepository<ExpenseEntry, Integer> {
    List<ExpenseEntry> findByCompanyRefId(Integer companyRefId);
    List<ExpenseEntry> findByExpenseMasterRefId(Integer expenseMasterRefId);
    List<ExpenseEntry> findBySubExpenseMasterRefId(Integer subExpenseMasterRefId);
}
