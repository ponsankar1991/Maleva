package my.maleva.api.module.accountsgroupmaster.service;

import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.accountsgroupmaster.dto.AccountsGroupMasterDto;
import my.maleva.api.module.accountsgroupmaster.dto.ClassificationDto;
import my.maleva.api.module.accountsgroupmaster.dto.ComboListDto;
import my.maleva.api.module.accountsgroupmaster.dto.GLAccountDto;

import java.util.List;
import java.util.UUID;

public interface AccountsGroupMasterService {

    ApiResponse<List<ComboListDto>> getAccountsGroupMaster(Integer companyRefId, String type);

    ApiResponse<Void> insertGLAccounts(Integer companyRefId, String accountCode);

    ApiResponse<AccountsGroupMasterDto> insertAccountsGroupMaster(AccountsGroupMasterDto dto, Integer companyRefId);

    ApiResponse<List<AccountsGroupMasterDto>> selectAccountsGroupMaster(Integer companyRefId);

    ApiResponse<Void> deleteAccountsGroupMaster(Integer id, Integer companyRefId);

    ApiResponse<List<GLAccountDto>> selectGLAccounts(Integer companyRefId);

    ApiResponse<Void> insertClassification(Integer companyRefId, Integer classificationId, UUID glAccountId);

    ApiResponse<List<ClassificationDto>> selectClassification();
}

