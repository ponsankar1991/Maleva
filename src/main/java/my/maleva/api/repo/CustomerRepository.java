package my.maleva.api.repo;

import my.maleva.api.model.Customer;
import my.maleva.api.dto.CurrencyValueDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
