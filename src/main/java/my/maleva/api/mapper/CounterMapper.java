package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.Counter;
import my.maleva.api.dto.CounterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CounterMapper {

    CounterDto toDto(Counter entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Counter toEntity(CounterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CounterDto dto, @MappingTarget Counter entity);
}
