package my.maleva.api.module.saleorder.service;

import my.maleva.api.module.saleorder.dto.SaleOrderDetailsDto;

import java.util.List;
import java.util.Optional;

/**
 * SaleOrderDetailsService - Business logic for SaleOrderDetails
 */
public interface SaleOrderDetailsService {

    List<SaleOrderDetailsDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
    Optional<SaleOrderDetailsDto> getById(Integer id);
    SaleOrderDetailsDto create(SaleOrderDetailsDto dto);
    SaleOrderDetailsDto update(Integer id, SaleOrderDetailsDto dto);
    boolean delete(Integer id);

    List<SaleOrderDetailsDto> getByItemMasterRefId(Integer itemMasterRefId);
    long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
    void deleteAllBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    SaleOrderDetailsDto calculateLineAmount(SaleOrderDetailsDto dto);
    void validateLineItemData(SaleOrderDetailsDto dto);
}

