package my.maleva.api.module.expense.mapper;

import my.maleva.api.module.expense.dto.SubExpenseMasterDto;
import my.maleva.api.module.expense.entity.SubExpenseMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SubExpenseMasterMapper - MapStruct mapper for SubExpenseMaster
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SubExpenseMasterMapper {

    SubExpenseMasterDto toDto(SubExpenseMaster entity);

    SubExpenseMaster toEntity(SubExpenseMasterDto dto);

    void updateEntityFromDto(SubExpenseMasterDto dto, @MappingTarget SubExpenseMaster entity);
}

