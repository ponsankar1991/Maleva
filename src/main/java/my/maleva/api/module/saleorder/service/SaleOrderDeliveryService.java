package my.maleva.api.module.saleorder.service;

import my.maleva.api.module.saleorder.dto.SaleOrderDeliveryDto;
import java.util.List;
import java.util.Optional;

/**
 * SaleOrderDeliveryService - Business logic for SaleOrderDelivery
 */
public interface SaleOrderDeliveryService {

    List<SaleOrderDeliveryDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    Optional<SaleOrderDeliveryDto> getById(Integer id);

    SaleOrderDeliveryDto create(SaleOrderDeliveryDto dto);

    SaleOrderDeliveryDto update(Integer id, SaleOrderDeliveryDto dto);

    boolean delete(Integer id);

    long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    void validateDeliveryData(SaleOrderDeliveryDto dto);
}

