package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.PaymentVoucherDetails;
import my.maleva.api.dto.PaymentVoucherDetailsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentVoucherDetailsMapper {

    PaymentVoucherDetailsDto toDto(PaymentVoucherDetails entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PaymentVoucherDetails toEntity(PaymentVoucherDetailsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PaymentVoucherDetailsDto dto, @MappingTarget PaymentVoucherDetails entity);
}
