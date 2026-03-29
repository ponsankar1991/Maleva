package my.maleva.api.module.billing.billorder.mapper;

import org.mapstruct.*;
import my.maleva.api.module.billing.billorder.entity.BillsOrderMaster;
import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BillsOrderMasterMapper {

    BillsOrderMasterDto toDto(BillsOrderMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BillsOrderMaster toEntity(BillsOrderMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(BillsOrderMasterDto dto, @MappingTarget BillsOrderMaster entity);
}
