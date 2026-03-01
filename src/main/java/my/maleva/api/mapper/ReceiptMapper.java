package my.maleva.api.mapper;

import my.maleva.api.dto.ReceiptDto;
import my.maleva.api.model.Receipt;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * ReceiptMapper
 * MapStruct mapper for Receipt entity to DTO and vice versa
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReceiptMapper {

    /**
     * Convert Receipt entity to DTO
     */
    ReceiptDto toDto(Receipt entity);

    /**
     * Convert Receipt DTO to entity
     */
    Receipt toEntity(ReceiptDto dto);

    /**
     * Update Receipt entity from DTO
     */
    void updateEntityFromDto(ReceiptDto dto, @MappingTarget Receipt entity);
}

