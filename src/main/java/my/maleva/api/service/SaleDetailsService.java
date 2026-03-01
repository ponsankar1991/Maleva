package my.maleva.api.service;

import my.maleva.api.dto.SaleDetailsDto;

import java.util.List;
import java.util.Optional;

/**
 * SaleDetailsService
 * Business logic interface for SaleDetails operations
 */
public interface SaleDetailsService {

    List<SaleDetailsDto> getBySaleMasterRefId(Integer saleMasterRefId);
    Optional<SaleDetailsDto> getById(Integer id);
    SaleDetailsDto create(SaleDetailsDto dto);
    SaleDetailsDto update(Integer id, SaleDetailsDto dto);
    boolean delete(Integer id);

    List<SaleDetailsDto> getByItemMasterRefId(Integer itemMasterRefId);
    long countBySaleMasterRefId(Integer saleMasterRefId);
    void deleteAllBySaleMasterRefId(Integer saleMasterRefId);

    /**
     * Calculate line amount from quantity and rate
     */
    SaleDetailsDto calculateLineAmount(SaleDetailsDto dto);

    /**
     * Validate line item data
     */
    void validateLineItemData(SaleDetailsDto dto);
}

