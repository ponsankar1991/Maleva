package my.maleva.api.mapper;

import my.maleva.api.dto.PurchaseOrderMasterDto;
import my.maleva.api.model.PurchaseOrderMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * PurchaseOrderMasterMapper
 * MapStruct mapper for PurchaseOrderMaster entity to DTO and vice versa
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PurchaseOrderMasterMapper {

    /**
     * Convert PurchaseOrderMaster entity to DTO
     */
    PurchaseOrderMasterDto toDto(PurchaseOrderMaster entity);

    /**
     * Convert PurchaseOrderMaster DTO to entity
     */
    PurchaseOrderMaster toEntity(PurchaseOrderMasterDto dto);

    /**
     * Update PurchaseOrderMaster entity from DTO
     */
    void updateEntityFromDto(PurchaseOrderMasterDto dto, @MappingTarget PurchaseOrderMaster entity);
}

