package my.maleva.api.module.accountsgroupmaster.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.accountsgroupmaster.dto.AccountsGroupMasterDto;
import my.maleva.api.module.accountsgroupmaster.dto.ClassificationDto;
import my.maleva.api.module.accountsgroupmaster.dto.ComboListDto;
import my.maleva.api.module.accountsgroupmaster.dto.GLAccountDto;
import my.maleva.api.module.accountsgroupmaster.entity.AccountsGroupMaster;
import my.maleva.api.module.accountsgroupmaster.entity.Classification;
import my.maleva.api.module.accountsgroupmaster.entity.GLAccount;
import my.maleva.api.module.accountsgroupmaster.mapper.AccountsGroupMasterMapper;
import my.maleva.api.module.accountsgroupmaster.mapper.ClassificationMapper;
import my.maleva.api.module.accountsgroupmaster.mapper.GLAccountMapper;
import my.maleva.api.module.accountsgroupmaster.repository.AccountsGroupMasterRepository;
import my.maleva.api.module.accountsgroupmaster.repository.ClassificationRepository;
import my.maleva.api.module.accountsgroupmaster.repository.GLAccountRepository;
import my.maleva.api.module.accountsgroupmaster.service.AccountsGroupMasterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountsGroupMasterServiceImpl implements AccountsGroupMasterService {

    private final AccountsGroupMasterRepository accountsGroupMasterRepository;
    private final GLAccountRepository glAccountRepository;
    private final ClassificationRepository classificationRepository;
    private final AccountsGroupMasterMapper accountsMapper;
    private final GLAccountMapper glAccountMapper;
    private final ClassificationMapper classificationMapper;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ComboListDto>> getAccountsGroupMaster(Integer companyRefId, String type) {
        try {
            log.info("Getting accounts group master for companyRefId: {}, type: {}", companyRefId, type);

            String codes = getAccountCodesByType(type);
            List<AccountsGroupMaster> accounts = accountsGroupMasterRepository
                .findAccountsByTypeAndCompany(companyRefId, codes);

            List<ComboListDto> result = accounts.stream()
                .map(acc -> ComboListDto.builder()
                    .id(acc.getId())
                    .name(acc.getAccountName() + " (" + acc.getAccountCode() + ")")
                    .code(acc.getAccountCode())
                    .build())
                .collect(Collectors.toList());

            if (result.isEmpty()) {
                return ApiResponse.error("No accounts found", 404);
            }

            log.info("Retrieved {} accounts for type: {}", result.size(), type);
            return ApiResponse.success(result, "Accounts retrieved successfully");
        } catch (Exception ex) {
            log.error("Error in getAccountsGroupMaster", ex);
            return ApiResponse.error("Internal server error: " + ex.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<Void> insertGLAccounts(Integer companyRefId, String accountCode) {
        try {
            if (accountCode == null || accountCode.trim().isEmpty()) {
                return ApiResponse.error("Please enter the account code", 400);
            }

            log.info("Inserting GL accounts for code: {}", accountCode);

            List<GLAccount> glAccounts = glAccountRepository
                .findByGlAccountCodeAndIsActive(accountCode, 1);

            if (glAccounts.isEmpty()) {
                return ApiResponse.error("GL account not found", 404);
            }

            log.info("Found {} GL accounts for code: {}", glAccounts.size(), accountCode);
            return ApiResponse.success(null, "GL accounts retrieved successfully");
        } catch (Exception ex) {
            log.error("Error in insertGLAccounts", ex);
            return ApiResponse.error("Internal server error: " + ex.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<AccountsGroupMasterDto> insertAccountsGroupMaster(
            AccountsGroupMasterDto dto, Integer companyRefId) {
        try {
            log.info("Inserting accounts group master for companyRefId: {}", companyRefId);

            AccountsGroupMaster entity = accountsMapper.toEntity(dto);
            entity.setCompanyRefId(companyRefId);
            if (entity.getActive() == null) {
                entity.setActive(1);
            }

            AccountsGroupMaster saved = accountsGroupMasterRepository.save(entity);
            log.info("Saved accounts group master with id: {}", saved.getId());

            AccountsGroupMasterDto result = accountsMapper.toDto(saved);
            return ApiResponse.success(result, "Account group created successfully");
        } catch (Exception ex) {
            log.error("Error in insertAccountsGroupMaster", ex);
            return ApiResponse.error("Error creating account group: " + ex.getMessage(), 500);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AccountsGroupMasterDto>> selectAccountsGroupMaster(Integer companyRefId) {
        try {
            log.info("Selecting all accounts group master for companyRefId: {}", companyRefId);

            List<AccountsGroupMaster> accounts = accountsGroupMasterRepository
                .findByCompanyRefIdAndActiveNot(companyRefId, 2);

            List<AccountsGroupMasterDto> result = accounts.stream()
                .map(acc -> {
                    AccountsGroupMasterDto dto = accountsMapper.toDto(acc);
                    dto.setAccountName1(acc.getAccountName() + " (" + acc.getAccountCode() + ")");
                    return dto;
                })
                .collect(Collectors.toList());

            log.info("Retrieved {} accounts", result.size());
            return ApiResponse.success(result, "Accounts group retrieved successfully");
        } catch (Exception ex) {
            log.error("Error in selectAccountsGroupMaster", ex);
            return ApiResponse.error("Error retrieving accounts: " + ex.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<Void> deleteAccountsGroupMaster(Integer id, Integer companyRefId) {
        try {
            log.info("Deleting accounts group master id: {}, companyRefId: {}", id, companyRefId);

            Optional<AccountsGroupMaster> account = accountsGroupMasterRepository
                .findByIdAndCompanyRefId(id, companyRefId);

            if (account.isEmpty()) {
                return ApiResponse.error("Account not found", 404);
            }

            AccountsGroupMaster accountToDelete = account.get();
            accountToDelete.setActive(2); // Soft delete
            accountsGroupMasterRepository.save(accountToDelete);

            log.info("Soft deleted accounts group master id: {}", id);
            return ApiResponse.success(null, "Account group deleted successfully");
        } catch (Exception ex) {
            log.error("Error in deleteAccountsGroupMaster", ex);
            return ApiResponse.error("Error deleting account: " + ex.getMessage(), 500);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<GLAccountDto>> selectGLAccounts(Integer companyRefId) {
        try {
            log.info("Selecting all GL accounts for companyRefId: {}", companyRefId);

            List<GLAccount> glAccounts = glAccountRepository
                .findByCompanyRefIdAndIsActiveNot(companyRefId);

            List<GLAccountDto> result = glAccounts.stream()
                .map(gl -> {
                    GLAccountDto dto = glAccountMapper.toDto(gl);
                    dto.setAccountName1(gl.getDescription() + "(" + gl.getGlAccountCode() + ")");
                    return dto;
                })
                .collect(Collectors.toList());

            log.info("Retrieved {} GL accounts", result.size());
            return ApiResponse.success(result, "Operation successful");
        } catch (Exception ex) {
            log.error("Error in selectGLAccounts", ex);
            return ApiResponse.error("Error retrieving GL accounts: " + ex.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<Void> insertClassification(Integer companyRefId, Integer classificationId, UUID glAccountId) {
        try {
            log.info("Updating classification for glAccountId: {}, classificationId: {}", glAccountId, classificationId);

            Optional<GLAccount> glAccount = glAccountRepository.findById(glAccountId);

            if (glAccount.isEmpty()) {
                return ApiResponse.error("GL Account not found", 404);
            }

            GLAccount account = glAccount.get();
            account.setClassification(classificationId);
            glAccountRepository.save(account);

            log.info("Updated classification for glAccountId: {}", glAccountId);
            return ApiResponse.success(null, "Classification updated successfully");
        } catch (Exception ex) {
            log.error("Error in insertClassification", ex);
            return ApiResponse.error("Error updating classification: " + ex.getMessage(), 500);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ClassificationDto>> selectClassification() {
        try {
            log.info("Selecting all classifications");

            List<Classification> classifications = classificationRepository.findAll();

            List<ClassificationDto> result = classifications.stream()
                .map(classificationMapper::toDto)
                .collect(Collectors.toList());

            log.info("Retrieved {} classifications", result.size());
            return ApiResponse.success(result, "Classifications retrieved successfully");
        } catch (Exception ex) {
            log.error("Error in selectClassification", ex);
            return ApiResponse.error("Error retrieving classifications: " + ex.getMessage(), 500);
        }
    }

    /**
     * Maps type parameter to account codes
     * @param type The type parameter from API
     * @return Comma-separated account codes
     */
    private String getAccountCodesByType(String type) {
        return switch (type) {
            case "PV" -> "AGE,SCR,CUS,DRI,TRU,SEM,EMP,SUP,BAK";
            case "CUSTOMER" -> "CUS";
            case "EMPLOYEE" -> "EMP";
            case "SUPPLIER" -> "SUP";
            case "AGENT" -> "AGE";
            case "TRUCK" -> "TRU";
            case "DRIVER" -> "DRI";
            default -> "";
        };
    }
}

