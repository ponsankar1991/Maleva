package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.BillsOrderDetails;
import my.maleva.api.dto.BillsOrderDetailsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BillsOrderDetailsMapper {

    BillsOrderDetailsDto toDto(BillsOrderDetails entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BillsOrderDetails toEntity(BillsOrderDetailsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(BillsOrderDetailsDto dto, @MappingTarget BillsOrderDetails entity);
}
