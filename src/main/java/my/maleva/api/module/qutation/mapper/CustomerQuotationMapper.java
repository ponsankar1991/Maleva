package my.maleva.api.module.qutation.mapper;

import org.mapstruct.*;
import my.maleva.api.module.qutation.entity.CustomerQuotation;
import my.maleva.api.module.qutation.dto.CustomerQuotationDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerQuotationMapper {

    CustomerQuotationDto toDto(CustomerQuotation entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CustomerQuotation toEntity(CustomerQuotationDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CustomerQuotationDto dto, @MappingTarget CustomerQuotation entity);
}
