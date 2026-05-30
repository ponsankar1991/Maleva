package my.maleva.api.module.accountsgroupmaster.mapper;

import my.maleva.api.module.accountsgroupmaster.dto.AccountsGroupMasterDto;
import my.maleva.api.module.accountsgroupmaster.entity.AccountsGroupMaster;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountsGroupMasterMapper {

    @Mapping(source = "companyRefId", target = "companyRefId")
    AccountsGroupMasterDto toDto(AccountsGroupMaster entity);

    @Mapping(source = "companyRefId", target = "companyRefId")
    AccountsGroupMaster toEntity(AccountsGroupMasterDto dto);
}

