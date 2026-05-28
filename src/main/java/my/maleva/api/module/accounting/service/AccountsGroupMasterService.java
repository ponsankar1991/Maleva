package my.maleva.api.module.accounting.service;

import my.maleva.api.module.accounting.dto.ComboListDto;

import java.util.List;

public interface AccountsGroupMasterService {

    List<ComboListDto> getAccountsGroupMaster(Integer companyId, String type);
}