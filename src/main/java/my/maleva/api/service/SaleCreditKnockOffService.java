package my.maleva.api.service;

import my.maleva.api.dto.SaleCreditKnockOffDto;

import java.util.List;
import java.util.Optional;

/**
 * SaleCreditKnockOffService
 * Business logic interface for SaleCreditKnockOff operations
 */
public interface SaleCreditKnockOffService {

    /**
     * Get all SaleCreditKnockOff records by Sale Credit Master Reference ID
     */
    List<SaleCreditKnockOffDto> getBySaleCreditMasterRefId(Integer saleCreditMasterRefId);

    /**
     * Get SaleCreditKnockOff by ID
     */
    Optional<SaleCreditKnockOffDto> getById(Integer id);

    /**
     * Create new SaleCreditKnockOff record
     */
    SaleCreditKnockOffDto create(SaleCreditKnockOffDto dto);

    /**
     * Update SaleCreditKnockOff record
     */
    SaleCreditKnockOffDto update(Integer id, SaleCreditKnockOffDto dto);

    /**
     * Delete SaleCreditKnockOff record
     */
    boolean delete(Integer id);

    /**
     * Get SaleCreditKnockOff records by company ID
     */
    List<SaleCreditKnockOffDto> getByCompanyRefId(Integer companyRefId);

    /**
     * Get SaleCreditKnockOff records by Sale Master Reference ID
     */
    List<SaleCreditKnockOffDto> getBySaleMasterRefId(Integer saleMasterRefId);

    /**
     * Get SaleCreditKnockOff records by customer ID
     */
    List<SaleCreditKnockOffDto> getByCustomerOpenRefId(Integer customerOpenRefId);

    /**
     * Count knock-off records by Sale Credit Master Reference ID
     */
    long countBySaleCreditMasterRefId(Integer saleCreditMasterRefId);

    /**
     * Get knock-off records by company and Sale Credit Master
     */
    List<SaleCreditKnockOffDto> getByCompanyAndSaleCreditMaster(Integer companyRefId, Integer saleCreditMasterRefId);

    /**
     * Delete all knock-off records for a Sale Credit Master
     */
    void deleteAllBySaleCreditMasterRefId(Integer saleCreditMasterRefId);
}

