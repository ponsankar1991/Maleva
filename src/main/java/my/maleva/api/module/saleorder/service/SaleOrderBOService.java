package my.maleva.api.module.saleorder.service;

import my.maleva.api.module.saleorder.dto.SaleOrderBODto;
import java.util.List;
import java.util.Optional;

/**
 * SaleOrderBOService - Business logic for SaleOrderBO
 */
public interface SaleOrderBOService {

    List<SaleOrderBODto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    List<SaleOrderBODto> getByBoTypeId(Integer boTypeId);

    List<SaleOrderBODto> getByStatus(Integer status);

    Optional<SaleOrderBODto> getById(Integer id);

    SaleOrderBODto create(SaleOrderBODto dto);

    SaleOrderBODto update(Integer id, SaleOrderBODto dto);

    boolean delete(Integer id);

    long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}

