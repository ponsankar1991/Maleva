package my.maleva.api.module.qutation.mapper;

import org.mapstruct.*;
import my.maleva.api.module.qutation.entity.CustomerQuotationGC;
import my.maleva.api.module.qutation.dto.CustomerQuotationGCDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerQuotationGCMapper {

    CustomerQuotationGCDto toDto(CustomerQuotationGC entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CustomerQuotationGC toEntity(CustomerQuotationGCDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CustomerQuotationGCDto dto, @MappingTarget CustomerQuotationGC entity);
}
