package my.maleva.api.module.user.mapper;

import org.mapstruct.*;
import my.maleva.api.module.user.entity.FormTransactionPassword;
import my.maleva.api.module.user.dto.FormTransactionPasswordDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FormTransactionPasswordMapper {

    FormTransactionPasswordDto toDto(FormTransactionPassword entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    FormTransactionPassword toEntity(FormTransactionPasswordDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(FormTransactionPasswordDto dto, @MappingTarget FormTransactionPassword entity);
}
