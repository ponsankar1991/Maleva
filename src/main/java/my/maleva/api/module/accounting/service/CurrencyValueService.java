package my.maleva.api.module.accounting.service;

import my.maleva.api.module.accounting.dto.CurrencyValueDto;
import java.util.Optional;

/**
 * CurrencyValueService - Service for currency value operations
 * Handles retrieval of currency values for customers
 */
public interface CurrencyValueService {

    /**
     * Get currency value for a customer by company and customer ID
     * Fetches the currency value from SymbolMaster joined with Customer
     *
     * @param companyRefId Company Reference ID
     * @param customerId Customer ID
     * @return Optional containing CurrencyValueDto with CurrencyValue and SymbolRefId
     */
    Optional<CurrencyValueDto> getCurrencyValue(Integer companyRefId, Integer customerId);
}

