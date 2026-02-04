package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.CustomerJobNotify;
import my.maleva.api.dto.CustomerJobNotifyDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerJobNotifyMapper {

    CustomerJobNotifyDto toDto(CustomerJobNotify entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CustomerJobNotify toEntity(CustomerJobNotifyDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CustomerJobNotifyDto dto, @MappingTarget CustomerJobNotify entity);
}
