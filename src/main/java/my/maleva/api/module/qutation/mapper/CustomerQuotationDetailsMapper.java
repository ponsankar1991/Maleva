package my.maleva.api.module.qutation.mapper;

import org.mapstruct.*;
import my.maleva.api.module.qutation.entity.CustomerQuotationDetails;
import my.maleva.api.module.qutation.dto.CustomerQuotationDetailsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerQuotationDetailsMapper {

    CustomerQuotationDetailsDto toDto(CustomerQuotationDetails entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CustomerQuotationDetails toEntity(CustomerQuotationDetailsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CustomerQuotationDetailsDto dto, @MappingTarget CustomerQuotationDetails entity);
}
