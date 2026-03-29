package my.maleva.api.module.payment.mapper;

import org.mapstruct.*;
import my.maleva.api.module.payment.entity.Payment;
import my.maleva.api.module.payment.dto.PaymentDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentMapper {

    PaymentDto toDto(Payment entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Payment toEntity(PaymentDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PaymentDto dto, @MappingTarget Payment entity);
}
