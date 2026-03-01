package my.maleva.api.service;

import my.maleva.api.dto.SaleMasterReferenceDto;
import java.util.List;
import java.util.Optional;

/**
 * SaleMasterReferenceService - Business logic for SaleMasterReference
 */
public interface SaleMasterReferenceService {

    List<SaleMasterReferenceDto> getBySaleMasterRefId(Integer saleMasterRefId);

    List<SaleMasterReferenceDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    Optional<SaleMasterReferenceDto> getById(Integer id);

    SaleMasterReferenceDto create(SaleMasterReferenceDto dto);

    SaleMasterReferenceDto update(Integer id, SaleMasterReferenceDto dto);

    boolean delete(Integer id);

    long countBySaleMasterRefId(Integer saleMasterRefId);

    void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}

