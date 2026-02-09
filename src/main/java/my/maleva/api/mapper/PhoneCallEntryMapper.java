package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.PhoneCallEntry;
import my.maleva.api.dto.PhoneCallEntryDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PhoneCallEntryMapper {

    PhoneCallEntryDto toDto(PhoneCallEntry entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PhoneCallEntry toEntity(PhoneCallEntryDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PhoneCallEntryDto dto, @MappingTarget PhoneCallEntry entity);
}
