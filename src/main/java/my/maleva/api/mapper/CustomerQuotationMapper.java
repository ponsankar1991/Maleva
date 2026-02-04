package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.CustomerQuotation;
import my.maleva.api.dto.CustomerQuotationDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerQuotationMapper {

    CustomerQuotationDto toDto(CustomerQuotation entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CustomerQuotation toEntity(CustomerQuotationDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CustomerQuotationDto dto, @MappingTarget CustomerQuotation entity);
}
