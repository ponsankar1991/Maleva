package my.maleva.api.module.billing.bill.mapper;

import org.mapstruct.*;
import my.maleva.api.module.billing.bill.entity.BillMaster;
import my.maleva.api.module.billing.bill.dto.BillMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BillMasterMapper {

    BillMasterDto toDto(BillMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BillMaster toEntity(BillMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(BillMasterDto dto, @MappingTarget BillMaster entity);
}
