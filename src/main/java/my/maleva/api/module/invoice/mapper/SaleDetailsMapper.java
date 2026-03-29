package my.maleva.api.module.invoice.mapper;

import my.maleva.api.module.invoice.dto.SaleDetailsDto;
import my.maleva.api.module.invoice.entity.SaleDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleDetailsMapper
 * MapStruct mapper for SaleDetails entity to DTO and vice versa
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SaleDetailsMapper {

    SaleDetailsDto toDto(SaleDetails entity);
    SaleDetails toEntity(SaleDetailsDto dto);
    void updateEntityFromDto(SaleDetailsDto dto, @MappingTarget SaleDetails entity);
}

