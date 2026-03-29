package my.maleva.api.module.patmentvouchmaster.mapper;

import org.mapstruct.*;
import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherDetails;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherDetailsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentVoucherDetailsMapper {

    PaymentVoucherDetailsDto toDto(PaymentVoucherDetails entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PaymentVoucherDetails toEntity(PaymentVoucherDetailsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PaymentVoucherDetailsDto dto, @MappingTarget PaymentVoucherDetails entity);
}
