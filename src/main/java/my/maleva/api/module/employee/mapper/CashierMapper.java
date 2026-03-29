package my.maleva.api.module.employee.mapper;

import org.mapstruct.*;
import my.maleva.api.module.employee.entity.Cashier;
import my.maleva.api.module.employee.dto.CashierDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CashierMapper {

    CashierDto toDto(Cashier entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Cashier toEntity(CashierDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CashierDto dto, @MappingTarget Cashier entity);
}
