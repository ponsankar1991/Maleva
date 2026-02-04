package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.BillDetails;
import my.maleva.api.dto.BillDetailsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BillDetailsMapper {

    BillDetailsDto toDto(BillDetails entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BillDetails toEntity(BillDetailsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(BillDetailsDto dto, @MappingTarget BillDetails entity);
}
