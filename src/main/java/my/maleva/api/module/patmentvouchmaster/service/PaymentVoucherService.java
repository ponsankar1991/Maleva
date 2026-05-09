package my.maleva.api.module.patmentvouchmaster.service;

import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.patmentvouchmaster.mapper.PaymentVoucherMapper;
import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucher;
import my.maleva.api.module.patmentvouchmaster.repository.PaymentVoucherRepository;
import my.maleva.api.module.accounting.entity.GLAccounts;
import my.maleva.api.module.accounting.dto.COAExpenseResponseDto;
import my.maleva.api.module.accounting.repository.GLAccountsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentVoucherService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentVoucherService.class);

    private final PaymentVoucherRepository repository;
    private final PaymentVoucherMapper mapper;
    private final GLAccountsRepository glAccountsRepository;

    public PaymentVoucherService(PaymentVoucherRepository repository,
                                 PaymentVoucherMapper mapper,
                                 GLAccountsRepository glAccountsRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.glAccountsRepository = glAccountsRepository;
    }

    public List<PaymentVoucherDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public PaymentVoucherDto getById(Integer id) {
        PaymentVoucher ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucher not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public PaymentVoucherDto create(PaymentVoucherDto dto) {
        LocalDateTime now = LocalDateTime.now();
        PaymentVoucher ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        PaymentVoucher saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentVoucherDto update(Integer id, PaymentVoucherDto dto) {
        PaymentVoucher ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucher not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        PaymentVoucher saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        PaymentVoucher ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucher not found: " + id));
        repository.delete(ent);
    }

    /**
     * Select COA (Chart of Accounts) Expense Details
     *
     * Equivalent C# Method: SelectCOAExpense(int Comid, int Expenseid, String Keyword)
     *
     * Business Logic:
     * 1. If Expenseid = 0: Return all GL Accounts for the company (ordered by account code)
     * 2. If Expenseid > 0: Return specific GL Account by RowIndex
     * 3. All results are filtered for active accounts only
     *
     * @param comid Company ID (CompanyRefId)
     * @param expenseId GL Account RowIndex (0 = get all accounts, >0 = get specific account)
     * @param keyword Search keyword (for future filtering, can be null)
     * @return List of COAExpenseResponseDto objects containing GL Account details
     */
    @Transactional(readOnly = true)
    public List<COAExpenseResponseDto> selectCOAExpense(Integer comid, Integer expenseId, String keyword) {
        logger.info("Fetching COA Expense details - Company: {}, ExpenseId: {}, Keyword: {}", comid, expenseId, keyword);

        try {
            // Validate inputs
            if (comid == null || comid <= 0) {
                logger.warn("Invalid company ID: {}", comid);
                throw new IllegalArgumentException("Invalid company ID provided");
            }

            if (expenseId == null) {
                expenseId = 0; // Default to 0 to get all accounts
            }

            // Query GL Accounts based on expense ID
             // If expenseId = 0, retrieves all accounts; if > 0, retrieves specific account
             // Uses findByCompanyAndExpenseWithClassification to get Classification info via LEFT JOIN
             List<GLAccounts> glAccounts = glAccountsRepository.findByCompanyAndExpenseWithClassification(comid, expenseId);

            if (glAccounts.isEmpty()) {
                logger.warn("No GL Accounts found for Company: {}, ExpenseId: {}", comid, expenseId);
                return List.of();
            }

            logger.info("Retrieved {} GL Accounts for Company: {}", glAccounts.size(), comid);

            // Convert GLAccounts to COAExpenseResponseDto
            List<COAExpenseResponseDto> resultList = glAccounts.stream()
                    .map(this::convertGLAccountToCOADto)
                    .sorted((a, b) -> {
                        if (a.getAccountCode() == null || b.getAccountCode() == null) return 0;
                        return a.getAccountCode().compareTo(b.getAccountCode());
                    })
                    .collect(Collectors.toList());

            logger.info("Successfully converted {} GL Accounts to COA response DTOs", resultList.size());
            return resultList;

        } catch (Exception ex) {
            logger.error("Error fetching COA Expense details for Company: " + comid + ", ExpenseId: " + expenseId, ex);
            throw new RuntimeException("Error retrieving COA Expense details: " + ex.getMessage(), ex);
        }
    }

    /**
     * Convert GLAccounts entity to COAExpenseResponseDto
     * Maps database fields to response DTO fields
     * Populates classificationName from the related Classification entity
     *
     * @param glAccount GLAccounts entity object
     * @return COAExpenseResponseDto with mapped values
     */
    private COAExpenseResponseDto convertGLAccountToCOADto(GLAccounts glAccount) {
        if (glAccount == null) {
            return null;
        }

        // Get classification name from the related Classification entity
        String classificationName = null;
        if (glAccount.getClassificationEntity() != null) {
            classificationName = glAccount.getClassificationEntity().getDescription();
        }

        return COAExpenseResponseDto.builder()
                .id(glAccount.getRowIndex())                          // RowIndex → Id
                .comid(glAccount.getCompanyRefId())                   // CompanyRefId → Comid
                .classification(glAccount.getClassification())         // Classification → Classification
                .accountCode(glAccount.getGlAccountCode())            // GLAccountCode → AccountCode
                .classificationName(classificationName)               // Classification.Description → ClassificationName (FIXED)
                .accountName(glAccount.getDescription())              // Description → AccountName
                .accountName1(glAccount.getDescription2())            // Description2 → AccountName1
                .parentId(glAccount.getParentId() != null ?
                         glAccount.getParentId().toString() : null)   // ParentId → ParentId
                .rootId(null)                                         // RootId (would need separate query or hierarchy)
                .active(glAccount.getIsActive())                      // IsActive → Active
                .qneCode(glAccount.getGstMsicCode())                  // GSTMSICCode → QNECode
                .build();
    }
}
