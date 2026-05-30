package my.maleva.api.module.accountsgroupmaster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.accountsgroupmaster.dto.AccountsGroupMasterDto;
import my.maleva.api.module.accountsgroupmaster.dto.ClassificationDto;
import my.maleva.api.module.accountsgroupmaster.dto.ComboListDto;
import my.maleva.api.module.accountsgroupmaster.dto.GLAccountDto;
import my.maleva.api.module.accountsgroupmaster.service.AccountsGroupMasterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts-group-master")
@RequiredArgsConstructor
@Slf4j
public class AccountsGroupMasterController {

    private final AccountsGroupMasterService accountsGroupMasterService;

    /**
     * Get Accounts Group Master by type
     */
    @GetMapping("/by-type")
    public ResponseEntity<ApiResponse<List<ComboListDto>>> getAccountsGroupMaster(
            @RequestParam Integer companyRefId,
            @RequestParam String type) {

        log.info("GET /by-type - companyRefId: {}, type: {}", companyRefId, type);
        ApiResponse<List<ComboListDto>> response = accountsGroupMasterService
            .getAccountsGroupMaster(companyRefId, type);

        return ResponseEntity.ok(response);
    }

    /**
     * Insert GL Accounts
     */
    @PostMapping("/gl-accounts")
    public ResponseEntity<ApiResponse<Void>> insertGLAccounts(
            @RequestParam Integer companyRefId,
            @RequestParam String accountCode) {

        log.info("POST /gl-accounts - companyRefId: {}, accountCode: {}", companyRefId, accountCode);
        ApiResponse<Void> response = accountsGroupMasterService
            .insertGLAccounts(companyRefId, accountCode);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getStatusCode()).body(response);
        }
    }

    /**
     * Create Accounts Group Master
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AccountsGroupMasterDto>> createAccountsGroupMaster(
            @RequestBody AccountsGroupMasterDto dto,
            @RequestParam Integer companyRefId) {

        log.info("POST / - Creating accounts group master for companyRefId: {}", companyRefId);
        ApiResponse<AccountsGroupMasterDto> response = accountsGroupMasterService
            .insertAccountsGroupMaster(dto, companyRefId);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(response.getStatusCode()).body(response);
        }
    }

    /**
     * Get All Accounts Group Master
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<AccountsGroupMasterDto>>> selectAccountsGroupMaster(
            @RequestParam Integer companyRefId) {

        log.info("POST /list - companyRefId: {}", companyRefId);
        ApiResponse<List<AccountsGroupMasterDto>> response = accountsGroupMasterService
            .selectAccountsGroupMaster(companyRefId);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete Accounts Group Master
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccountsGroupMaster(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId) {

        log.info("DELETE /{id} - id: {}, companyRefId: {}", id, companyRefId);
        ApiResponse<Void> response = accountsGroupMasterService
            .deleteAccountsGroupMaster(id, companyRefId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getStatusCode()).body(response);
        }
    }

    /**
     * Get GL Accounts
     */
    @PostMapping("/gl-accounts/list")
    public ResponseEntity<ApiResponse<List<GLAccountDto>>> selectGLAccounts(
            @RequestParam Integer companyRefId) {

        log.info("POST /gl-accounts/list - companyRefId: {}", companyRefId);
        ApiResponse<List<GLAccountDto>> response = accountsGroupMasterService
            .selectGLAccounts(companyRefId);

        return ResponseEntity.ok(response);
    }

    /**
     * Update GL Account Classification
     */
    @PostMapping("/gl-accounts/classification")
    public ResponseEntity<ApiResponse<Void>> insertClassification(
            @RequestParam Integer companyRefId,
            @RequestParam Integer classificationId,
            @RequestParam UUID glAccountId) {

        log.info("POST /gl-accounts/classification - classificationId: {}, glAccountId: {}",
            classificationId, glAccountId);
        ApiResponse<Void> response = accountsGroupMasterService
            .insertClassification(companyRefId, classificationId, glAccountId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getStatusCode()).body(response);
        }
    }

    /**
     * Get Classifications
     */
    @PostMapping("/classifications")
    public ResponseEntity<ApiResponse<List<ClassificationDto>>> selectClassification() {
        log.info("POST /classifications");
        ApiResponse<List<ClassificationDto>> response = accountsGroupMasterService
            .selectClassification();

        return ResponseEntity.ok(response);
    }
}

