package my.maleva.api.module.master.mapper;

import org.mapstruct.*;
import my.maleva.api.module.master.entity.PaymentTermsMaster;
import my.maleva.api.module.master.dto.PaymentTermsMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentTermsMasterMapper {

    PaymentTermsMasterDto toDto(PaymentTermsMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PaymentTermsMaster toEntity(PaymentTermsMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PaymentTermsMasterDto dto, @MappingTarget PaymentTermsMaster entity);
}
