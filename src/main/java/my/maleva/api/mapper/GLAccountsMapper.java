package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.GLAccounts;
import my.maleva.api.dto.GLAccountsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GLAccountsMapper {

    GLAccountsDto toDto(GLAccounts entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    GLAccounts toEntity(GLAccountsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(GLAccountsDto dto, @MappingTarget GLAccounts entity);
}
