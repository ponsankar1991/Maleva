package my.maleva.api.mapper;

import my.maleva.api.dto.PurchaseMasterDto;
import my.maleva.api.model.PurchaseMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PurchaseMasterMapper {
    /**
     * Convert PurchaseMaster entity to DTO
     */
    PurchaseMasterDto toDto(PurchaseMaster entity);

    /**
     * Convert PurchaseMaster DTO to entity
     */
    PurchaseMaster toEntity(PurchaseMasterDto dto);

    /**
     * Update PurchaseMaster entity from DTO
     */
    void updateEntityFromDto(PurchaseMasterDto dto, @MappingTarget PurchaseMaster entity);
}
