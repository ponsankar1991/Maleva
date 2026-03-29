package my.maleva.api.module.paymentrecept.mapper;

import org.mapstruct.*;
import my.maleva.api.module.paymentrecept.entity.PaymentReceiptInfo;
import my.maleva.api.module.paymentrecept.dto.PaymentReceiptInfoDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentReceiptInfoMapper {

    PaymentReceiptInfoDto toDto(PaymentReceiptInfo entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PaymentReceiptInfo toEntity(PaymentReceiptInfoDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PaymentReceiptInfoDto dto, @MappingTarget PaymentReceiptInfo entity);
}
