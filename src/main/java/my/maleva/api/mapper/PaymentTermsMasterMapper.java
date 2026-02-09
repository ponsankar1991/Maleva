package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.PaymentTermsMaster;
import my.maleva.api.dto.PaymentTermsMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentTermsMasterMapper {

    PaymentTermsMasterDto toDto(PaymentTermsMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PaymentTermsMaster toEntity(PaymentTermsMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PaymentTermsMasterDto dto, @MappingTarget PaymentTermsMaster entity);
}
