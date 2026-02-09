package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.PaymentVoucher;
import my.maleva.api.dto.PaymentVoucherDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentVoucherMapper {

    PaymentVoucherDto toDto(PaymentVoucher entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PaymentVoucher toEntity(PaymentVoucherDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PaymentVoucherDto dto, @MappingTarget PaymentVoucher entity);
}
