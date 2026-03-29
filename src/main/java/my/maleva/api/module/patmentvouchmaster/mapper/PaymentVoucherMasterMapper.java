package my.maleva.api.module.patmentvouchmaster.mapper;

import org.mapstruct.*;
import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherMaster;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentVoucherMasterMapper {

    PaymentVoucherMasterDto toDto(PaymentVoucherMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PaymentVoucherMaster toEntity(PaymentVoucherMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PaymentVoucherMasterDto dto, @MappingTarget PaymentVoucherMaster entity);
}
