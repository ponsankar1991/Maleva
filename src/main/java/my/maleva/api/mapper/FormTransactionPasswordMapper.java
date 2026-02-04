package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.FormTransactionPassword;
import my.maleva.api.dto.FormTransactionPasswordDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FormTransactionPasswordMapper {

    FormTransactionPasswordDto toDto(FormTransactionPassword entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    FormTransactionPassword toEntity(FormTransactionPasswordDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(FormTransactionPasswordDto dto, @MappingTarget FormTransactionPassword entity);
}
