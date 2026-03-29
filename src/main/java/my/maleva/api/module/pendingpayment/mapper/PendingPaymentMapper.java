package my.maleva.api.module.pendingpayment.mapper;

import org.mapstruct.*;
import my.maleva.api.module.pendingpayment.entity.PendingPayment;
import my.maleva.api.module.pendingpayment.dto.PendingPaymentDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PendingPaymentMapper {

    PendingPaymentDto toDto(PendingPayment entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PendingPayment toEntity(PendingPaymentDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PendingPaymentDto dto, @MappingTarget PendingPayment entity);
}
