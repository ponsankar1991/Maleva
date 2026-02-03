package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.AccountsGroupMaster;
import my.maleva.api.dto.AccountsGroupMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountsGroupMasterMapper {

    AccountsGroupMasterDto toDto(AccountsGroupMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    AccountsGroupMaster toEntity(AccountsGroupMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AccountsGroupMasterDto dto, @MappingTarget AccountsGroupMaster entity);
}
