package my.maleva.api.module.accounting.mapper;

import org.mapstruct.*;
import my.maleva.api.module.accounting.entity.GLAccounts;
import my.maleva.api.module.accounting.dto.GLAccountsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GLAccountsMapper {

    GLAccountsDto toDto(GLAccounts entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    GLAccounts toEntity(GLAccountsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(GLAccountsDto dto, @MappingTarget GLAccounts entity);
}
