package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.PaymentVoucherMaster;
import my.maleva.api.dto.PaymentVoucherMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentVoucherMasterMapper {

    PaymentVoucherMasterDto toDto(PaymentVoucherMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PaymentVoucherMaster toEntity(PaymentVoucherMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PaymentVoucherMasterDto dto, @MappingTarget PaymentVoucherMaster entity);
}
