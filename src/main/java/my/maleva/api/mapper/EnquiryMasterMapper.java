package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.EnquiryMaster;
import my.maleva.api.dto.EnquiryMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EnquiryMasterMapper {

    EnquiryMasterDto toDto(EnquiryMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    EnquiryMaster toEntity(EnquiryMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(EnquiryMasterDto dto, @MappingTarget EnquiryMaster entity);
}
