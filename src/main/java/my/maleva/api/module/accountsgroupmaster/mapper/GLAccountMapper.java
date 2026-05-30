package my.maleva.api.module.accountsgroupmaster.mapper;

import my.maleva.api.module.accountsgroupmaster.dto.GLAccountDto;
import my.maleva.api.module.accountsgroupmaster.entity.GLAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GLAccountMapper {

    @Mapping(source = "glAccountCode", target = "glAccountCode")
    GLAccountDto toDto(GLAccount entity);

    @Mapping(source = "glAccountCode", target = "glAccountCode")
    GLAccount toEntity(GLAccountDto dto);
}

