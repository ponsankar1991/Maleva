package my.maleva.api.service.impl;

import my.maleva.api.dto.CurrencyValueDto;
import my.maleva.api.repo.CustomerRepository;
import my.maleva.api.service.CurrencyValueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * CurrencyValueServiceImpl - Implementation for CurrencyValue service
 * Handles retrieval of currency values for customers by joining SymbolMaster
 */
@Service
public class CurrencyValueServiceImpl implements CurrencyValueService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyValueServiceImpl.class);

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Get currency value for a customer by company and customer ID
     * Fetches the currency value from SymbolMaster joined with Customer
     *
     * Implementation Details:
     * - Joins Customer and SymbolMaster tables
     * - Filters by CompanyRefId, Active status (not 2), and CustomerId
     * - Returns CurrencyValue and SymbolRefId
     *
     * @param companyRefId Company Reference ID (required, must be positive)
     * @param customerId Customer ID (required, must be positive)
     * @return Optional containing CurrencyValueDto with CurrencyValue and SymbolRefId
     *         Empty Optional if no data found
     */
    @Override
    public Optional<CurrencyValueDto> getCurrencyValue(Integer companyRefId, Integer customerId) {
        logger.info("Fetching currency value for CompanyRefId: {}, CustomerId: {}",
                   companyRefId, customerId);

        try {
            // Validate input parameters
            if (companyRefId == null || companyRefId <= 0) {
                logger.warn("Invalid companyRefId provided: {}", companyRefId);
                return Optional.empty();
            }
            if (customerId == null || customerId <= 0) {
                logger.warn("Invalid customerId provided: {}", customerId);
                return Optional.empty();
            }

            // Query the database using the custom repository method
            Optional<CurrencyValueDto> result = customerRepository.findCurrencyValueByCompanyAndCustomer(
                    companyRefId,
                    customerId
            );

            if (result.isPresent()) {
                CurrencyValueDto dto = result.get();
                logger.info("Currency value found - CurrencyValue: {}, SymbolRefId: {}",
                           dto.getCurrencyValue(), dto.getSymbolRefId());
            } else {
                logger.info("No currency value found for CompanyRefId: {}, CustomerId: {}",
                           companyRefId, customerId);
            }

            return result;

        } catch (Exception e) {
            logger.error("Error fetching currency value for CompanyRefId: {}, CustomerId: {}, Error: {}",
                        companyRefId, customerId, e.getMessage(), e);
            return Optional.empty();
        }
    }
}

