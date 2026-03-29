package my.maleva.api.module.accounting.mapper;

import org.mapstruct.*;
import my.maleva.api.module.accounting.entity.AccountsGroupMaster;
import my.maleva.api.module.accounting.dto.AccountsGroupMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountsGroupMasterMapper {

    AccountsGroupMasterDto toDto(AccountsGroupMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    AccountsGroupMaster toEntity(AccountsGroupMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AccountsGroupMasterDto dto, @MappingTarget AccountsGroupMaster entity);
}
