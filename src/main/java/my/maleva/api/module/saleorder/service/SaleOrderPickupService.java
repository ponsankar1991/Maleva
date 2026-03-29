package my.maleva.api.module.saleorder.service;

import my.maleva.api.module.saleorder.dto.SaleOrderPickupDto;
import java.util.List;
import java.util.Optional;

/**
 * SaleOrderPickupService - Business logic for SaleOrderPickup
 */
public interface SaleOrderPickupService {

    List<SaleOrderPickupDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    Optional<SaleOrderPickupDto> getById(Integer id);

    SaleOrderPickupDto create(SaleOrderPickupDto dto);

    SaleOrderPickupDto update(Integer id, SaleOrderPickupDto dto);

    boolean delete(Integer id);

    long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    void validatePickupData(SaleOrderPickupDto dto);
}

