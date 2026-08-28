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
import my.maleva.api.integration.qne.QneGlAccountReader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Deliberately NOT {@code @Transactional}: every method here catches and
 * wraps its own errors, and inside a shared transaction a repository failure
 * marks it rollback-only — the caught error is then replaced at commit by an
 * opaque "Transaction silently rolled back" 500. Repositories run their own
 * short transactions (legacy issued single raw statements the same way); the
 * one multi-statement write, the GL import, runs in an explicit
 * {@link TransactionTemplate} with the catch outside it.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountsGroupMasterServiceImpl implements AccountsGroupMasterService {

    private final AccountsGroupMasterRepository accountsGroupMasterRepository;
    private final GLAccountRepository glAccountRepository;
    private final ClassificationRepository classificationRepository;
    private final AccountsGroupMasterMapper accountsMapper;
    private final GLAccountMapper glAccountMapper;
    private final ClassificationMapper classificationMapper;
    private final ObjectProvider<QneGlAccountReader> qneGlAccountReader;
    private final JdbcTemplate jdbcTemplate;
    private final PlatformTransactionManager transactionManager;

    @Override
    public ApiResponse<List<ComboListDto>> getAccountsGroupMaster(Integer companyRefId, String type) {
        try {
            log.info("Getting accounts group master for companyRefId: {}, type: {}", companyRefId, type);

            List<String> codes = getAccountCodesByType(type);
            List<AccountsGroupMaster> accounts = codes.isEmpty()
                    ? accountsGroupMasterRepository.findByCompanyRefIdAndActive(companyRefId, 1)
                    : accountsGroupMasterRepository.findAccountsByTypeAndCompany(companyRefId, codes);

            List<ComboListDto> result = accounts.stream()
                .map(acc -> ComboListDto.builder()
                    .id(acc.getId())
                    // Four spaces before the bracket, as legacy rendered it —
                    // these labels line up in the dropdowns that show them.
                    .name(acc.getAccountName() + "    (" + acc.getAccountCode() + ")")
                    .name1(acc.getAccountName())
                    .code(acc.getAccountCode())
                    .build())
                .collect(Collectors.toList());

            // An empty list is an answer, not a failure: legacy returned
            // success with no rows, and a 404 here makes callers treat "this
            // company has no accounts of that type yet" as a broken request.
            log.info("Retrieved {} accounts for type: {}", result.size(), type);
            return ApiResponse.success(result, "Accounts retrieved successfully");
        } catch (Exception ex) {
            log.error("Error in getAccountsGroupMaster", ex);
            return ApiResponse.error("Internal server error: " + ex.getMessage(), 500);
        }
    }

    /**
     * Imports chart-of-accounts entries from QNE into the local database.
     *
     * <p>Port of the legacy {@code InsertGLAccounts}: read the rows from
     * <b>QNE's own database</b> (their REST API has no GLAccounts endpoint),
     * then upsert them locally by {@code GLAccountCode} — {@code SP_GLAccounts}
     * reimplemented in {@link #upsertGlAccounts} rather than called, matching
     * how every other SP in this migration was ported. An earlier migration of
     * this method queried the <em>local</em> table — where the account by
     * definition does not exist yet — and inserted nothing, so the import
     * silently did no work.
     */
    @Override
    public ApiResponse<Void> insertGLAccounts(Integer companyRefId, String accountCode) {
        try {
            QneGlAccountReader reader = qneGlAccountReader.getIfAvailable();
            if (reader == null) {
                return ApiResponse.error(
                        "QNE database access is not configured on this server (qne.datasource)", 503);
            }

            // Blank imports the whole chart. Legacy reached the same outcome by
            // accident — it set "Please Enter the Account Code", then ran the
            // unfiltered query and imported everything anyway — but a
            // first-time import genuinely has no code to name, so refusing it
            // would remove a working path.
            String code = accountCode == null ? "" : accountCode.trim();
            List<Map<String, Object>> rows = code.isEmpty()
                    ? reader.findAll()
                    : reader.findByAccountCode(code);
            if (rows.isEmpty()) {
                return ApiResponse.error(code.isEmpty()
                        ? "QNE returned no chart of accounts to import"
                        : "GL account not found in QNE: " + code, 404);
            }

            // One transaction for the whole upsert: the batches must land
            // together, and SET IDENTITY_INSERT is per-connection — the ON,
            // the inserts and the OFF have to ride the same one.
            new TransactionTemplate(transactionManager)
                    .executeWithoutResult(status -> upsertGlAccounts(rows, companyRefId));

            log.info("Imported {} GL account row(s) for code {} from QNE", rows.size(), accountCode);
            return ApiResponse.success(null, "InsertGLAccounts created successfully");
        } catch (Exception ex) {
            log.error("Error in insertGLAccounts", ex);
            return ApiResponse.error("Internal server error: " + ex.getMessage(), 500);
        }
    }

    /** The reader's projection, in its order. Insert binds all of these plus CompanyRefId. */
    private static final String[] GL_COLUMNS = {
            "Id", "ParentId", "GLAccountCode", "AccountId", "SpecialAccountId", "CurrencyId",
            "GSTTypeId", "Description", "DRCR", "IsCreditCard", "IsActive", "GSTGroup",
            "IsRevaluation", "Notes", "IsSubAccount", "BankAccountNo", "GSTMSICCode",
            "OptimisticLockField", "SAC", "SSTTariffCode", "RowIndex", "HasChildInCoa",
            "IncludeInCashFlowForecastAdvisor", "TariffCodeId", "ATCCodeId", "Description2"};

    /**
     * What a re-import may change. Deliberately absent: {@code GLAccountCode}
     * (the match key), {@code CompanyRefId} (the first importer keeps
     * ownership), and {@code RowIndex} — the stable key every local detail
     * table joins on, which is why re-importing can never break a bill or
     * purchase-order line's account reference.
     */
    private static final String[] GL_UPDATE_COLUMNS = {
            "Id", "ParentId", "AccountId", "SpecialAccountId", "CurrencyId", "GSTTypeId",
            "Description", "DRCR", "IsCreditCard", "IsActive", "GSTGroup", "IsRevaluation",
            "Notes", "IsSubAccount", "BankAccountNo", "GSTMSICCode", "OptimisticLockField",
            "SAC", "SSTTariffCode", "HasChildInCoa", "IncludeInCashFlowForecastAdvisor",
            "TariffCodeId", "ATCCodeId", "Description2"};

    private static final String GL_INSERT_SQL =
            "INSERT INTO GLAccounts (Id, CompanyRefId, "
                    + Arrays.stream(GL_COLUMNS).filter(c -> !"Id".equals(c))
                            .collect(Collectors.joining(", "))
                    + ") VALUES (" + String.join(", ",
                            Collections.nCopies(GL_COLUMNS.length + 1, "?")) + ")";

    private static final String GL_UPDATE_SQL =
            "UPDATE GLAccounts SET "
                    + Arrays.stream(GL_UPDATE_COLUMNS).map(c -> c + " = ?")
                            .collect(Collectors.joining(", "))
                    + " WHERE GLAccountCode = ?";

    /**
     * {@code SP_GLAccounts}, reimplemented — the procedure stays in the
     * database untouched for the legacy .NET screen, but the Java no longer
     * calls it, so a fix here ships once instead of being ALTERed into four
     * databases.
     *
     * <p>Kept from the SP on purpose: the upsert matches {@code GLAccountCode}
     * with <b>no company filter</b> (the chart is global by code; a second
     * company's import updates the first company's row without re-owning it),
     * and the update re-points the QNE {@code Id} while leaving
     * {@code RowIndex} alone. Not kept: the {@code @Notes=@Notes}
     * self-assignment (Notes now updates), and the leaked
     * {@code IDENTITY_INSERT} — here it is switched off in a finally, because a
     * rollback does not reset session state and the pooled connection would
     * carry it to whatever ran next.
     */
    private void upsertGlAccounts(List<Map<String, Object>> rows, Integer companyRefId) {
        // Last row wins for a repeated code — where the SP's sequential
        // insert-then-update loop also ended up.
        Map<String, Map<String, Object>> byCode = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            byCode.put(String.valueOf(row.get("GLAccountCode")), row);
        }

        Set<String> existing = new HashSet<>();
        List<String> codes = new ArrayList<>(byCode.keySet());
        for (int i = 0; i < codes.size(); i += 1000) { // SQL Server caps ~2100 binds
            List<String> chunk = codes.subList(i, Math.min(i + 1000, codes.size()));
            String in = String.join(",", Collections.nCopies(chunk.size(), "?"));
            existing.addAll(jdbcTemplate.queryForList(
                    "SELECT GLAccountCode FROM GLAccounts WITH(NOLOCK) WHERE GLAccountCode IN (" + in + ")",
                    String.class, chunk.toArray()));
        }

        List<Object[]> inserts = new ArrayList<>();
        List<Object[]> updates = new ArrayList<>();
        for (Map<String, Object> row : byCode.values()) {
            if (existing.contains(String.valueOf(row.get("GLAccountCode")))) {
                Object[] args = new Object[GL_UPDATE_COLUMNS.length + 1];
                for (int i = 0; i < GL_UPDATE_COLUMNS.length; i++) {
                    args[i] = row.get(GL_UPDATE_COLUMNS[i]);
                }
                args[GL_UPDATE_COLUMNS.length] = row.get("GLAccountCode");
                updates.add(args);
            } else {
                Object[] args = new Object[GL_COLUMNS.length + 1];
                args[0] = row.get("Id");
                args[1] = companyRefId;
                int i = 2;
                for (String column : GL_COLUMNS) {
                    if (!"Id".equals(column)) {
                        args[i++] = row.get(column);
                    }
                }
                inserts.add(args);
            }
        }

        if (!updates.isEmpty()) {
            jdbcTemplate.batchUpdate(GL_UPDATE_SQL, updates);
        }
        if (!inserts.isEmpty()) {
            // RowIndex is an identity column and QNE's value must carry across.
            jdbcTemplate.execute("SET IDENTITY_INSERT GLAccounts ON");
            try {
                jdbcTemplate.batchUpdate(GL_INSERT_SQL, inserts);
            } finally {
                jdbcTemplate.execute("SET IDENTITY_INSERT GLAccounts OFF");
            }
        }
        log.info("GL account upsert: {} inserted, {} updated", inserts.size(), updates.size());
    }

    /**
     * Insert or update one account group — the Java port of
     * {@code SP_AccountsGroupMaster}.
     *
     * <p>Id 0/null inserts, anything else updates, and the two branches
     * deliberately touch different columns. The update writes only
     * AccountName, AccountCode, ParentId and Active; it leaves
     * {@code QNECode}, {@code UpdateId} and {@code CompanyRefId} alone, so a
     * rename cannot wipe the QNE mapping the import put there. Mapping the
     * whole DTO over the row would do exactly that whenever the caller omits a
     * field.
     */
    @Override
    public ApiResponse<AccountsGroupMasterDto> insertAccountsGroupMaster(
            AccountsGroupMasterDto dto, Integer companyRefId) {
        try {
            boolean isNew = dto.getId() == null || dto.getId() == 0;
            AccountsGroupMaster entity;

            if (isNew) {
                entity = new AccountsGroupMaster();
                entity.setCompanyRefId(companyRefId);
                // The SP seeds QNECode from the account code on insert and
                // ignores any QNECode sent with the request.
                entity.setQneCode(dto.getAccountCode());
            } else {
                entity = accountsGroupMasterRepository
                        .findByIdAndCompanyRefId(dto.getId(), companyRefId)
                        .orElse(null);
                if (entity == null) {
                    return ApiResponse.error("Account not found: " + dto.getId(), 404);
                }
            }

            entity.setAccountName(dto.getAccountName());
            entity.setAccountCode(dto.getAccountCode());
            entity.setParentId(dto.getParentId());
            entity.setActive(dto.getActive() == null ? 1 : dto.getActive());

            AccountsGroupMaster saved = accountsGroupMasterRepository.save(entity);
            log.info("{} accounts group master id {}", isNew ? "Inserted" : "Updated", saved.getId());

            AccountsGroupMasterDto result = accountsMapper.toDto(saved);
            return ApiResponse.success(result,
                    isNew ? "Account group created successfully" : "Account group updated successfully");
        } catch (Exception ex) {
            log.error("Error in insertAccountsGroupMaster", ex);
            return ApiResponse.error("Error saving account group: " + ex.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<List<AccountsGroupMasterDto>> selectAccountsGroupMaster(Integer companyRefId) {
        try {
            log.info("Selecting all accounts group master for companyRefId: {}", companyRefId);

            List<AccountsGroupMaster> accounts = accountsGroupMasterRepository
                .findByCompanyRefIdAndActiveNot(companyRefId, 2);

            List<AccountsGroupMasterDto> result = accounts.stream()
                .map(acc -> {
                    AccountsGroupMasterDto dto = accountsMapper.toDto(acc);
                    // Four spaces, matching legacy's '    (' label spacing.
                    dto.setAccountName1(acc.getAccountName() + "    (" + acc.getAccountCode() + ")");
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
    public ApiResponse<List<GLAccountDto>> selectGLAccounts(Integer companyRefId) {
        try {
            log.info("Selecting all GL accounts for companyRefId: {}", companyRefId);

            List<GLAccount> glAccounts = glAccountRepository
                .findByCompanyRefIdAndIsActiveNot(companyRefId);

            // The classification names legacy pulled in with a LEFT JOIN.
            Map<Integer, String> classificationNames = new HashMap<>();
            for (Classification c : classificationRepository.findAll()) {
                classificationNames.put(c.getId(), c.getDescription());
            }

            List<GLAccountDto> result = glAccounts.stream()
                .map(gl -> {
                    GLAccountDto dto = glAccountMapper.toDto(gl);
                    String classification = gl.getClassification() == null
                            ? null : classificationNames.get(gl.getClassification());
                    // Legacy composed this label in T-SQL, where '+' with any
                    // NULL operand yields NULL — an unclassified account's
                    // AccountName1 came back null, not a partial label.
                    dto.setAccountName1(
                            gl.getDescription() == null || gl.getGlAccountCode() == null
                                    || classification == null
                                    ? null
                                    : gl.getDescription() + "(" + gl.getGlAccountCode() + ")("
                                            + classification + ")");
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
            // The company was already a parameter here but went unused, so the
            // update was keyed on the account id alone — as legacy's raw UPDATE
            // was. Checking it stops one company's request touching another's
            // chart of accounts.
            if (!Objects.equals(account.getCompanyRefId(), companyRefId)) {
                return ApiResponse.error("GL Account does not belong to company " + companyRefId, 403);
            }
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
    /**
     * Parent account codes a type is filed under, or empty for "no filter".
     *
     * <p>An unrecognised type returns every active account, which is what
     * legacy did — it simply appended no WHERE clause.
     */
    private List<String> getAccountCodesByType(String type) {
        return switch (type == null ? "" : type.trim().toUpperCase()) {
            case "PV" -> List.of("AGE", "SCR", "CUS", "DRI", "TRU", "SEM", "EMP", "SUP", "BAK");
            case "CUSTOMER" -> List.of("CUS");
            case "EMPLOYEE" -> List.of("EMP");
            case "SUPPLIER" -> List.of("SUP");
            case "AGENT" -> List.of("AGE");
            case "TRUCK" -> List.of("TRU");
            case "DRIVER" -> List.of("DRI");
            default -> List.of();
        };
    }
}

