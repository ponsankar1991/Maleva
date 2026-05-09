package my.maleva.api.module.accounting.repository;

import my.maleva.api.module.accounting.entity.GLAccounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GLAccountsRepository extends JpaRepository<GLAccounts, UUID> {
    List<GLAccounts> findByCompanyRefId(Integer companyRefId);
    List<GLAccounts> findByParentId(UUID parentId);

    /**
     * Find GL Accounts by company ID with optional expense ID filter
     * Equivalent to C# query: SELECT * FROM GLAccounts
     * WHERE IsActive != 2 AND CompanyRefId = :companyRefId AND (RowIndex = :expenseId OR :expenseId = 0)
     *
     * @param companyRefId Company reference ID
     * @param expenseId Expense ID (RowIndex from GLAccounts), use 0 to get all accounts
     * @return List of GL Accounts
     */
    @Query("SELECT g FROM GLAccounts g " +
            "WHERE g.isActive = true " +
            "AND g.companyRefId = :companyRefId " +
            "AND (g.rowIndex = :expenseId OR :expenseId = 0) " +
            "ORDER BY g.glAccountCode ASC")
    List<GLAccounts> findByCompanyAndExpense(@Param("companyRefId") Integer companyRefId,
                                             @Param("expenseId") Integer expenseId);

    /**
     * Find GL Accounts with Classification details using LEFT JOIN
     * 
     * Equivalent C# SQL Query:
     * SELECT A.RowIndex as Id, A.IsActive as Active, A.GLAccountCode as AccountCode, 
     *        A.Description as AccountName, A.Classification, CS.Description as ClassificationName
     * FROM GLAccounts A WITH(NOLOCK)
     * LEFT JOIN Classification CS ON A.Classification = CS.Id
     * WHERE A.IsActive = 1 AND A.CompanyRefId = :companyRefId 
     * AND (A.RowIndex = :expenseId OR :expenseId = 0)
     * ORDER BY A.GLAccountCode ASC
     * 
     * Uses LEFT JOIN to fetch Classification entity so that classificationEntity.description is available
     * 
     * @param companyRefId Company reference ID
     * @param expenseId Expense ID (RowIndex from GLAccounts), use 0 to get all accounts
     * @return List of GL Accounts with Classification info populated via LEFT JOIN
     */
    @Query("SELECT DISTINCT g FROM GLAccounts g " +
            "LEFT JOIN FETCH g.classificationEntity c " +
            "WHERE g.isActive = true " +
            "AND g.companyRefId = :companyRefId " +
            "AND (g.rowIndex = :expenseId OR :expenseId = 0) " +
            "ORDER BY g.glAccountCode ASC")
    List<GLAccounts> findByCompanyAndExpenseWithClassification(
            @Param("companyRefId") Integer companyRefId,
            @Param("expenseId") Integer expenseId);
}
