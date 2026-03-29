package my.maleva.api.module.payment.mapper;

import org.mapstruct.*;
import my.maleva.api.module.payment.entity.PaymentDetails;
import my.maleva.api.module.payment.dto.PaymentDetailsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentDetailsMapper {

    PaymentDetailsDto toDto(PaymentDetails entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PaymentDetails toEntity(PaymentDetailsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PaymentDetailsDto dto, @MappingTarget PaymentDetails entity);
}
