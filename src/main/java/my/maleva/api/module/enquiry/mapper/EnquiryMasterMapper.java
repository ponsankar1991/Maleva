package my.maleva.api.module.enquiry.mapper;

import org.mapstruct.*;
import my.maleva.api.module.enquiry.entity.EnquiryMaster;
import my.maleva.api.module.enquiry.dto.EnquiryMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EnquiryMasterMapper {

    EnquiryMasterDto toDto(EnquiryMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    EnquiryMaster toEntity(EnquiryMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(EnquiryMasterDto dto, @MappingTarget EnquiryMaster entity);
}
