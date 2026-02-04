package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.CustomerNotifyDetails;
import my.maleva.api.dto.CustomerNotifyDetailsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerNotifyDetailsMapper {

    CustomerNotifyDetailsDto toDto(CustomerNotifyDetails entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CustomerNotifyDetails toEntity(CustomerNotifyDetailsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CustomerNotifyDetailsDto dto, @MappingTarget CustomerNotifyDetails entity);
}
