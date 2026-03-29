package my.maleva.api.module.purchase.mapper;

import my.maleva.api.module.purchase.dto.PurchaseDetailsDto;
import my.maleva.api.module.purchase.entity.PurchaseDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PurchaseDetailsMapper {

    /**
     * Convert PurchaseDetails entity to DTO
     */
    PurchaseDetailsDto toDto(PurchaseDetails entity);

    /**
     * Convert PurchaseDetails DTO to entity
     */
    PurchaseDetails toEntity(PurchaseDetailsDto dto);

    /**
     * Update PurchaseDetails entity from DTO
     */
    void updateEntityFromDto(PurchaseDetailsDto dto, @MappingTarget PurchaseDetails entity);
}
