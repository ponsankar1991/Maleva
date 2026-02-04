package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.ExpenseMaster;
import my.maleva.api.dto.ExpenseMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExpenseMasterMapper {

    ExpenseMasterDto toDto(ExpenseMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ExpenseMaster toEntity(ExpenseMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ExpenseMasterDto dto, @MappingTarget ExpenseMaster entity);
}
