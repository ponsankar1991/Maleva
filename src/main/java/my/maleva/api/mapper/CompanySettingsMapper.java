package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.CompanySettings;
import my.maleva.api.dto.CompanySettingsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanySettingsMapper {

    CompanySettingsDto toDto(CompanySettings entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CompanySettings toEntity(CompanySettingsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CompanySettingsDto dto, @MappingTarget CompanySettings entity);
}
