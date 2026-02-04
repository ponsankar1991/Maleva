package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.ExpenseEntry;
import my.maleva.api.dto.ExpenseEntryDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExpenseEntryMapper {

    ExpenseEntryDto toDto(ExpenseEntry entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ExpenseEntry toEntity(ExpenseEntryDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ExpenseEntryDto dto, @MappingTarget ExpenseEntry entity);
}
