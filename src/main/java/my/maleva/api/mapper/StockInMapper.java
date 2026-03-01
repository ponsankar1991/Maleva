package my.maleva.api.mapper;

import my.maleva.api.dto.StockInDto;
import my.maleva.api.model.StockIn;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * StockInMapper - MapStruct mapper for StockIn
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StockInMapper {

    StockInDto toDto(StockIn entity);

    StockIn toEntity(StockInDto dto);

    void updateEntityFromDto(StockInDto dto, @MappingTarget StockIn entity);
}

