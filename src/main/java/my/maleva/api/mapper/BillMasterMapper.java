package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.BillMaster;
import my.maleva.api.dto.BillMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BillMasterMapper {

    BillMasterDto toDto(BillMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BillMaster toEntity(BillMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(BillMasterDto dto, @MappingTarget BillMaster entity);
}
