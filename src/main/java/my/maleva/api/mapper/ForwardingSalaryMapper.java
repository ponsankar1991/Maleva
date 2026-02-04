package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.ForwardingSalary;
import my.maleva.api.dto.ForwardingSalaryDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ForwardingSalaryMapper {

    ForwardingSalaryDto toDto(ForwardingSalary entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ForwardingSalary toEntity(ForwardingSalaryDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ForwardingSalaryDto dto, @MappingTarget ForwardingSalary entity);
}
