package my.maleva.api.module.customer.repository;

import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.accounting.dto.CurrencyValueDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    List<Customer> findByCustomerNameContainingIgnoreCase(String name);

    /**
     * Fetch currency value for a customer by company and customer ID
     * Joins SymbolMaster table to get CurrencyValue and SymbolRefId
     *
     * Uses native SQL query for better compatibility with DTO projection
     * Returns DECIMAL (not FLOAT) to match BigDecimal in CurrencyValueDto
     * This ensures precision is maintained (3.08 stays 3.08, not 3.0799999237060547)
     *
     * @param companyRefId Company Reference ID
     * @param customerId Customer ID
     * @return Optional containing CurrencyValueDto with exact CurrencyValue and SymbolRefId
     */
    @Query(value = "SELECT CAST(s.CurrencyValue AS DECIMAL(10,2)) as currencyValue, s.Id as symbolRefId " +
                   "FROM SymbolMaster s " +
                   "INNER JOIN Customer c ON s.Id = c.SymbolRefid " +
                   "WHERE c.CompanyRefId = :companyRefId " +
                   "AND c.Active != 2 " +
                   "AND c.Id = :customerId",
           nativeQuery = true)
    Optional<CurrencyValueDto> findCurrencyValueByCompanyAndCustomer(
            @Param("companyRefId") Integer companyRefId,
            @Param("customerId") Integer customerId
    );

    /**
     * Find customer with symbol information by company and customer ID
     *
     * @param companyRefId Company Reference ID
     * @param customerId Customer ID
     * @return Optional containing Customer entity with associated SymbolMaster data
     */
    Optional<Customer> findByIdAndCompanyRefId(Integer customerId, Integer companyRefId);

    /**
     * Write-back of the QNE identity after a successful create push. Customer
     * is the one table where the QNE GUID lives in UpdateId and the QNE code
     * in CompanyCode (legacy naming). The empty-code guard makes the write a
     * one-time claim — QNE sync is create-once, and this is the only dedup
     * mechanism. REQUIRES_NEW because the push runs in an after-commit hook,
     * where the completed transaction's resources are still bound.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Customer c SET c.updateId = :qneId, c.companyCode = :qneCode " +
           "WHERE c.id = :id AND (c.companyCode IS NULL OR c.companyCode = '')")
    int claimQneIdentity(@Param("id") Integer id,
                         @Param("qneId") String qneId,
                         @Param("qneCode") String qneCode);

    /** Customers that exist in QNE (CompanyCode set) but whose GUID was never stored. */
    @Query("SELECT c FROM Customer c WHERE c.companyRefId = :companyRefId " +
           "AND c.companyCode IS NOT NULL AND c.companyCode <> '' " +
           "AND (c.updateId IS NULL OR c.updateId = '')")
    List<Customer> findQneBackfillCandidates(@Param("companyRefId") Integer companyRefId);

    /** Repairs UpdateId from a QNE lookup, matching on the QNE company code (legacy UpdateCustomerId1). */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Customer c SET c.updateId = :qneId " +
           "WHERE c.companyRefId = :companyRefId AND c.companyCode = :companyCode " +
           "AND (c.updateId IS NULL OR c.updateId = '')")
    int backfillQneId(@Param("companyRefId") Integer companyRefId,
                      @Param("companyCode") String companyCode,
                      @Param("qneId") String qneId);
}
