package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.PaymentDetails;
import my.maleva.api.dto.PaymentDetailsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentDetailsMapper {

    PaymentDetailsDto toDto(PaymentDetails entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PaymentDetails toEntity(PaymentDetailsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PaymentDetailsDto dto, @MappingTarget PaymentDetails entity);
}
