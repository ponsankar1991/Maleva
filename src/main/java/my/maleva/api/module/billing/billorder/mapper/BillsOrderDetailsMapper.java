package my.maleva.api.module.billing.billorder.mapper;

import org.mapstruct.*;
import my.maleva.api.module.billing.billorder.entity.BillsOrderDetails;
import my.maleva.api.module.billing.billorder.dto.BillsOrderDetailsDto;
import my.maleva.api.module.billing.billorder.dto.BillsOrderDetailsInsertDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BillsOrderDetailsMapper {

    BillsOrderDetailsDto toDto(BillsOrderDetails entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BillsOrderDetails toEntity(BillsOrderDetailsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(BillsOrderDetailsDto dto, @MappingTarget BillsOrderDetails entity);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BillsOrderDetails insertDtoToEntity(BillsOrderDetailsInsertDto dto);
}
