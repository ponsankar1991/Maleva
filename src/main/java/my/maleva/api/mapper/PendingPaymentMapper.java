package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.PendingPayment;
import my.maleva.api.dto.PendingPaymentDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PendingPaymentMapper {

    PendingPaymentDto toDto(PendingPayment entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PendingPayment toEntity(PendingPaymentDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PendingPaymentDto dto, @MappingTarget PendingPayment entity);
}
