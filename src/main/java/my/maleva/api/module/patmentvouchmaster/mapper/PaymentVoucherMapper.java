package my.maleva.api.module.patmentvouchmaster.mapper;

import org.mapstruct.*;
import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucher;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentVoucherMapper {

    PaymentVoucherDto toDto(PaymentVoucher entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PaymentVoucher toEntity(PaymentVoucherDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PaymentVoucherDto dto, @MappingTarget PaymentVoucher entity);
}
