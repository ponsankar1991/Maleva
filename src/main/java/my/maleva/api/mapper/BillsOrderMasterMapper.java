package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.BillsOrderMaster;
import my.maleva.api.dto.BillsOrderMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BillsOrderMasterMapper {

    BillsOrderMasterDto toDto(BillsOrderMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BillsOrderMaster toEntity(BillsOrderMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(BillsOrderMasterDto dto, @MappingTarget BillsOrderMaster entity);
}
