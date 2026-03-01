package my.maleva.api.mapper;

import my.maleva.api.dto.ReceiptDetailsDto;
import my.maleva.api.model.ReceiptDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * ReceiptDetailsMapper
 * MapStruct mapper for ReceiptDetails entity to DTO and vice versa
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReceiptDetailsMapper {

    /**
     * Convert ReceiptDetails entity to DTO
     */
    ReceiptDetailsDto toDto(ReceiptDetails entity);

    /**
     * Convert ReceiptDetails DTO to entity
     */
    ReceiptDetails toEntity(ReceiptDetailsDto dto);

    /**
     * Update ReceiptDetails entity from DTO
     */
    void updateEntityFromDto(ReceiptDetailsDto dto, @MappingTarget ReceiptDetails entity);
}

