package my.maleva.api.module.purchase.mapper;

import my.maleva.api.module.purchase.dto.PurchaseOrderDetailsDto;
import my.maleva.api.module.purchase.entity.PurchaseOrderDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * PurchaseOrderDetailsMapper
 * MapStruct mapper for PurchaseOrderDetails entity to DTO and vice versa
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PurchaseOrderDetailsMapper {

    /**
     * Convert PurchaseOrderDetails entity to DTO
     */
    PurchaseOrderDetailsDto toDto(PurchaseOrderDetails entity);

    /**
     * Convert PurchaseOrderDetails DTO to entity
     */
    PurchaseOrderDetails toEntity(PurchaseOrderDetailsDto dto);

    /**
     * Update PurchaseOrderDetails entity from DTO
     */
    void updateEntityFromDto(PurchaseOrderDetailsDto dto, @MappingTarget PurchaseOrderDetails entity);
}

