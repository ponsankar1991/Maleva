package my.maleva.api.mapper;

import my.maleva.api.dto.SaleOrderDetailsDto;
import my.maleva.api.model.SaleOrderDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * SaleOrderDetailsMapper - MapStruct mapper for SaleOrderDetails
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleOrderDetailsMapper {
    SaleOrderDetailsDto toDto(SaleOrderDetails entity);
    SaleOrderDetails toEntity(SaleOrderDetailsDto dto);
    List<SaleOrderDetails> toEntityList(List<SaleOrderDetailsDto> dtoList);
    void updateEntityFromDto(SaleOrderDetailsDto dto, @MappingTarget SaleOrderDetails entity);
}

