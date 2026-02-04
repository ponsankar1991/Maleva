package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.CustomerQuotationGC;
import my.maleva.api.dto.CustomerQuotationGCDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerQuotationGCMapper {

    CustomerQuotationGCDto toDto(CustomerQuotationGC entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CustomerQuotationGC toEntity(CustomerQuotationGCDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CustomerQuotationGCDto dto, @MappingTarget CustomerQuotationGC entity);
}
