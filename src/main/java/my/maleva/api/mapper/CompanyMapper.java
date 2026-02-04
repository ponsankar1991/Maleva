package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.Company;
import my.maleva.api.dto.CompanyDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper {

    CompanyDto toDto(Company entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Company toEntity(CompanyDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CompanyDto dto, @MappingTarget Company entity);
}
