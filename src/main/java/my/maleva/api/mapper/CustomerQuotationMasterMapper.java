package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.CustomerQuotationMaster;
import my.maleva.api.dto.CustomerQuotationMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerQuotationMasterMapper {

    CustomerQuotationMasterDto toDto(CustomerQuotationMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CustomerQuotationMaster toEntity(CustomerQuotationMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CustomerQuotationMasterDto dto, @MappingTarget CustomerQuotationMaster entity);
}
