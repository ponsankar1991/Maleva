package my.maleva.api.module.saleorder.service;

import my.maleva.api.module.saleorder.dto.SaleOrderForwardingDto;
import java.util.List;
import java.util.Optional;

/**
 * SaleOrderForwardingService - Business logic for SaleOrderForwarding
 * Incorporates SP_SaleOrderMaster stored procedure logic for forwarding
 */
public interface SaleOrderForwardingService {

    List<SaleOrderForwardingDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    Optional<SaleOrderForwardingDto> getById(Integer id);

    SaleOrderForwardingDto create(SaleOrderForwardingDto dto);

    SaleOrderForwardingDto update(Integer id, SaleOrderForwardingDto dto);

    boolean delete(Integer id);

    long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    void validateForwardingData(SaleOrderForwardingDto dto);
}

