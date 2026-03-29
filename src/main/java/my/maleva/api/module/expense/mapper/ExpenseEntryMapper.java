package my.maleva.api.module.expense.mapper;

import org.mapstruct.*;
import my.maleva.api.module.expense.entity.ExpenseEntry;
import my.maleva.api.module.expense.dto.ExpenseEntryDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExpenseEntryMapper {

    ExpenseEntryDto toDto(ExpenseEntry entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ExpenseEntry toEntity(ExpenseEntryDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ExpenseEntryDto dto, @MappingTarget ExpenseEntry entity);
}
