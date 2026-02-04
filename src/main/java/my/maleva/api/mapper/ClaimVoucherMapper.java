package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.ClaimVoucher;
import my.maleva.api.dto.ClaimVoucherDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClaimVoucherMapper {

    ClaimVoucherDto toDto(ClaimVoucher entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ClaimVoucher toEntity(ClaimVoucherDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ClaimVoucherDto dto, @MappingTarget ClaimVoucher entity);
}
