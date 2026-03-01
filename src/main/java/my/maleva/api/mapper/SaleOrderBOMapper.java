package my.maleva.api.mapper;

import my.maleva.api.dto.SaleOrderBODto;
import my.maleva.api.model.SaleOrderBO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleOrderBOMapper - MapStruct mapper for SaleOrderBO
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleOrderBOMapper {

    SaleOrderBODto toDto(SaleOrderBO entity);

    SaleOrderBO toEntity(SaleOrderBODto dto);

    void updateEntityFromDto(SaleOrderBODto dto, @MappingTarget SaleOrderBO entity);
}

