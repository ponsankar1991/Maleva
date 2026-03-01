package my.maleva.api.service;

import my.maleva.api.dto.SaleCreditDetailsDto;

import java.util.List;
import java.util.Optional;

/**
 * SaleCreditDetailsService
 * Business logic interface for SaleCreditDetails operations
 */
public interface SaleCreditDetailsService {

    /**
     * Get all SaleCreditDetails by Sale Credit Master Reference ID
     */
    List<SaleCreditDetailsDto> getBySaleCreditMasterRefId(Integer saleCreditMasterRefId);

    /**
     * Get SaleCreditDetails by ID
     */
    Optional<SaleCreditDetailsDto> getById(Integer id);

    /**
     * Create new SaleCreditDetails record
     */
    SaleCreditDetailsDto create(SaleCreditDetailsDto dto);

    /**
     * Update SaleCreditDetails record
     */
    SaleCreditDetailsDto update(Integer id, SaleCreditDetailsDto dto);

    /**
     * Delete SaleCreditDetails record
     */
    boolean delete(Integer id);

    /**
     * Get SaleCreditDetails by item ID
     */
    List<SaleCreditDetailsDto> getByItemMasterRefId(Integer itemMasterRefId);

    /**
     * Count details by Sale Credit Master Reference ID
     */
    long countBySaleCreditMasterRefId(Integer saleCreditMasterRefId);

    /**
     * Delete all details for a Sale Credit Master
     */
    void deleteAllBySaleCreditMasterRefId(Integer saleCreditMasterRefId);
}

