package my.maleva.api.service;

import my.maleva.api.dto.SaleOrderBONotRequiredDto;
import java.util.List;
import java.util.Optional;

/**
 * SaleOrderBONotRequiredService - Business logic for SaleOrderBONotRequired
 */
public interface SaleOrderBONotRequiredService {

    List<SaleOrderBONotRequiredDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    List<SaleOrderBONotRequiredDto> getByBoTypeId(Integer boTypeId);

    Optional<SaleOrderBONotRequiredDto> getById(Integer id);

    SaleOrderBONotRequiredDto create(SaleOrderBONotRequiredDto dto);

    SaleOrderBONotRequiredDto update(Integer id, SaleOrderBONotRequiredDto dto);

    boolean delete(Integer id);

    long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}

