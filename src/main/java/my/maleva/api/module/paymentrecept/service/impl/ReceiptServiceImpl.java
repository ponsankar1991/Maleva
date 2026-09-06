package my.maleva.api.module.paymentrecept.service.impl;

import my.maleva.api.module.paymentrecept.dto.ReceiptDto;
import my.maleva.api.module.paymentrecept.mapper.ReceiptMapper;
import my.maleva.api.module.paymentrecept.entity.Receipt;
import my.maleva.api.module.paymentrecept.repository.ReceiptRepository;
import my.maleva.api.module.paymentrecept.service.ReceiptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import my.maleva.api.module.master.repository.BankMasterRepository;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.paymentrecept.dto.ReceiptBillDto;
import my.maleva.api.module.paymentrecept.dto.ReceiptSaveRequest;
import my.maleva.api.module.paymentrecept.dto.ReceiptSaveResponseDto;
import my.maleva.api.module.paymentrecept.entity.Receipt;
import my.maleva.api.module.paymentrecept.entity.ReceiptDetails;
import my.maleva.api.module.paymentrecept.mapper.ReceiptMapper;
import my.maleva.api.module.paymentrecept.repository.ReceiptDetailsRepository;
import my.maleva.api.module.paymentrecept.repository.ReceiptRepository;
import my.maleva.api.module.paymentrecept.service.ReceiptService;
import my.maleva.api.module.user.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ReceiptServiceImpl
 * Service implementation for Receipt
 * Implements SP_Receipt stored procedure logic
 */
@Service
public class ReceiptServiceImpl implements ReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptServiceImpl.class);

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private ReceiptDetailsRepository receiptDetailsRepository;

    @Autowired
    private SequenceNoMasterRepository sequenceNoMasterRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private BankMasterRepository bankMasterRepository;

    @Autowired
    private EmployeeMasterRepository employeeMasterRepository;

    @Autowired
    private ReceiptMapper mapper;

    @Override
    public List<ReceiptDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all Receipt records for company: {}", companyRefId);
        return receiptRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReceiptDto> getById(Integer id) {
        logger.info("Fetching Receipt by ID: {}", id);
        return receiptRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public ReceiptDto create(ReceiptDto dto) {
        logger.info("Creating new Receipt for company: {}", dto.getCompanyRefId());
        Receipt entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());
        if (entity.getPvStatus() == null) {
            entity.setPvStatus(0);
        }
        Receipt saved = receiptRepository.save(entity);
        logger.info("Receipt created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public ReceiptDto update(Integer id, ReceiptDto dto) {
        logger.info("Updating Receipt with ID: {}", id);
        Receipt entity = receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found with ID: " + id));
        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        Receipt updated = receiptRepository.save(entity);
        logger.info("Receipt updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting Receipt with ID: {}", id);
        if (receiptRepository.existsById(id)) {
            receiptRepository.deleteById(id);
            logger.info("Receipt deleted with ID: {}", id);
            return true;
        }
        logger.warn("Receipt not found with ID: {}", id);
        return false;
    }

    @Override
    public List<ReceiptDto> getByCustomer(Integer companyRefId, Integer customerRefId) {
        logger.info("Fetching Receipt for customer: {}", customerRefId);
        return receiptRepository.findByCompanyRefIdAndCustomerRefId(companyRefId, customerRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReceiptDto> getByBank(Integer companyRefId, Integer bankRefId) {
        logger.info("Fetching Receipt for bank: {}", bankRefId);
        return receiptRepository.findByCompanyRefIdAndBankRefId(companyRefId, bankRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReceiptDto> getByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Fetching Receipt by CNumber: {}", cNumber);
        return receiptRepository.findByCompanyRefIdAndCNumber(companyRefId, cNumber)
                .map(mapper::toDto);
    }

    @Override
    public List<ReceiptDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching Receipt between dates: {} to {}", startDate, endDate);
        return receiptRepository.findByCompanyRefIdAndReceiptDateBetween(companyRefId, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReceiptDto> getByRefNumber(Integer companyRefId, String refNumber) {
        logger.info("Fetching Receipt by reference number: {}", refNumber);
        return receiptRepository.findByCompanyRefIdAndRefNumber(companyRefId, refNumber)
                .map(mapper::toDto);
    }

    @Override
    public Optional<ReceiptDto> getByCNumberDisplay(String cNumberDisplay) {
        logger.info("Fetching Receipt by CNumberDisplay: {}", cNumberDisplay);
        return receiptRepository.findByCNumberDisplay(cNumberDisplay)
                .map(mapper::toDto);
    }

    @Override
    public List<ReceiptDto> getByPvStatus(Integer companyRefId, Integer pvStatus) {
        logger.info("Fetching Receipt by PV Status: {}", pvStatus);
        return receiptRepository.findByCompanyRefIdAndPvStatus(companyRefId, pvStatus)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Checking if CNumber exists: {}", cNumber);
        return receiptRepository.existsByCompanyRefIdAndCNumber(companyRefId, cNumber);
    }

    @Override
    public long countByCompanyId(Integer companyRefId) {
        logger.info("Counting Receipt records for company: {}", companyRefId);
        return receiptRepository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countByPvStatus(Integer companyRefId, Integer pvStatus) {
        logger.info("Counting Receipt by PV Status for company: {}", companyRefId);
        return receiptRepository.countByCompanyRefIdAndPvStatus(companyRefId, pvStatus);
    }

    @Override
    public String generateCNumberDisplay(Integer cNumber) {
        logger.info("Generating CNumberDisplay for CNumber: {}", cNumber);
        // Format: RC + 9 digit zero-padded number (e.g., RC000000001)
        return String.format("RC%09d", cNumber);
    }

    @Override
    @Transactional
    public ReceiptDto changeStatus(Integer id, Integer pvStatus) {
        logger.info("Changing Receipt status to: {}", pvStatus);
        Receipt entity = receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found with ID: " + id));
        entity.setPvStatus(pvStatus);
        entity.setModifiedDate(LocalDateTime.now());
        Receipt updated = receiptRepository.save(entity);
        logger.info("Receipt status changed with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    public String getMaxReceiptNo(Integer companyRefId, String billType) {
        Integer maxSeq = sequenceNoMasterRepository.findMaxSequenceNoByCompanyAndSequenceName(companyRefId, "Receipt");
        if (maxSeq == null) {
            maxSeq = 0;
        }
        int nextNo = maxSeq + 1;
        return "RC" + String.format("%09d", nextNo);
    }

    @Autowired
    private my.maleva.api.module.paymentrecept.repository.ReceiptBillQueryRepository receiptBillQueryRepository;

    @Override
    public List<my.maleva.api.module.paymentrecept.dto.ReceiptBillDto> selectCustomerBalance(my.maleva.api.module.paymentrecept.dto.ReceiptViewBillRequest request) {
        logger.info("Executing selectCustomerBalance for companyRefId: {}, tilldate: {}", request.getCompanyRefId(), request.getTilldate());
        return receiptBillQueryRepository.selectCustomerBalance(request);
    }

    @Override
    public List<my.maleva.api.module.paymentrecept.dto.ReceiptBillDto> selectCustomerBills(my.maleva.api.module.paymentrecept.dto.ReceiptViewBillRequest request) {
        logger.info("Executing selectCustomerBills for customerId: {}, companyRefId: {}, id2: {}",
                request.getId(), request.getCompanyRefId(), request.getId2());
        return receiptBillQueryRepository.selectCustomerBills(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReceiptSaveResponseDto insertReceipt(List<ReceiptSaveRequest> requestList, Integer headerCompanyId) {
        if (requestList == null || requestList.isEmpty()) {
            return ReceiptSaveResponseDto.builder()
                    .ok(false)
                    .isSuccess(false)
                    .message("Receipt data is empty")
                    .build();
        }

        ReceiptSaveRequest request = requestList.get(0);
        Integer companyId = request.getCompanyRefId() != null ? request.getCompanyRefId() : headerCompanyId;
        if (companyId == null || companyId <= 0) {
            return ReceiptSaveResponseDto.builder()
                    .ok(false)
                    .isSuccess(false)
                    .message("Company Reference ID is required")
                    .build();
        }

        // 1. Reference check: AppUser (matching SP_Receipt)
        Integer userRefId = request.getUserRefId();
        if (userRefId != null && userRefId != 0) {
            boolean userExists = appUserRepository.existsByIdAndCompanyRefIdAndActive(userRefId, companyId, 1);
            if (!userExists) {
                logger.warn("Validation failed: Login user not found id={}", userRefId);
                return ReceiptSaveResponseDto.builder()
                        .ok(false)
                        .isSuccess(false)
                        .message("Login User Not Found Issue id" + userRefId)
                        .build();
            }
        }

        // 2. Reference check: BankMaster (matching SP_Receipt)
        Integer bankRefId = request.getBankRefId();
        if (bankRefId != null && bankRefId != 0) {
            boolean bankExists = bankMasterRepository.existsByIdAndCompanyRefIdAndActive(bankRefId, companyId, 1);
            if (!bankExists) {
                logger.warn("Validation failed: Bank not found id={}", bankRefId);
                return ReceiptSaveResponseDto.builder()
                        .ok(false)
                        .isSuccess(false)
                        .message("Bank  Not Found Issue id" + bankRefId)
                        .build();
            }
        }

        // 3. Reference check: EmployeeMaster (matching SP_Receipt)
        Integer employeeRefId = request.getEmployeeRefId();
        if (employeeRefId != null && employeeRefId != 0) {
            boolean empExists = employeeMasterRepository.existsByIdAndCompanyRefIdAndActive(employeeRefId, companyId, 1);
            if (!empExists) {
                logger.warn("Validation failed: Employee not found id={}", employeeRefId);
                return ReceiptSaveResponseDto.builder()
                        .ok(false)
                        .isSuccess(false)
                        .message("Employee Not Found Issue id" + employeeRefId)
                        .build();
            }
        }

        LocalDateTime receiptDate = parseDate(request.getReceiptDate());
        if (receiptDate == null) {
            receiptDate = LocalDateTime.now();
        }

        boolean isEdit = request.getId() != null && request.getId() > 0;
        Receipt receipt;
        String billNoDisplay;

        if (isEdit) {
            // Edit Process: load existing
            receipt = receiptRepository.findById(request.getId()).orElse(null);
            if (receipt == null) {
                return ReceiptSaveResponseDto.builder()
                        .ok(false)
                        .isSuccess(false)
                        .message("Receipt not found with ID: " + request.getId())
                        .build();
            }

            // Wholesale replacement of details matching SP_Receipt: delete from ReceiptDetails where ReceiptRefId=@Id
            receiptDetailsRepository.deleteByReceiptRefId(receipt.getId());

            receipt.setCustomerRefId(request.getCustomerRefId() != null ? request.getCustomerRefId() : 0);
            receipt.setBankRefId(request.getBankRefId() != null ? request.getBankRefId() : 0);
            receipt.setLastEmployeeRefId(request.getEmployeeRefId());
            receipt.setUserRefId(request.getUserRefId());
            receipt.setReceiptDate(receiptDate);
            receipt.setAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO);
            receipt.setBankCharges(request.getBankCharges() != null ? request.getBankCharges() : 0.0);
            receipt.setActualNetAmount(request.getActualNetAmount() != null ? request.getActualNetAmount() : 0.0);
            receipt.setCurrencyValue(request.getCurrencyValue() != null ? request.getCurrencyValue() : 1.0);
            receipt.setFileUpload(request.getFileUpload() != null ? request.getFileUpload() : 0);
            receipt.setRemarks(request.getRemarks());
            receipt.setRefNumber(request.getRefNumber());
            receipt.setModifiedDate(LocalDateTime.now());
            receipt.setPvStatus(request.getPvStatus() != null ? request.getPvStatus() : 0);

            receipt = receiptRepository.save(receipt);
            billNoDisplay = receipt.getCNumberDisplay();
            logger.info("Updated Receipt ID: {}, Number: {}", receipt.getId(), billNoDisplay);
        } else {
            // Insert Process
            receipt = new Receipt();
            receipt.setCompanyRefId(companyId);
            receipt.setUserRefId(request.getUserRefId());
            receipt.setEmployeeRefId(request.getEmployeeRefId());
            receipt.setLastEmployeeRefId(request.getEmployeeRefId());
            receipt.setFileUpload(request.getFileUpload() != null ? request.getFileUpload() : 0);
            receipt.setCustomerRefId(request.getCustomerRefId() != null ? request.getCustomerRefId() : 0);
            receipt.setBankRefId(request.getBankRefId() != null ? request.getBankRefId() : 0);
            receipt.setRefNumber(request.getRefNumber());
            receipt.setReceiptDate(receiptDate);
            receipt.setAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO);
            receipt.setBankCharges(request.getBankCharges() != null ? request.getBankCharges() : 0.0);
            receipt.setActualNetAmount(request.getActualNetAmount() != null ? request.getActualNetAmount() : 0.0);
            receipt.setCurrencyValue(request.getCurrencyValue() != null ? request.getCurrencyValue() : 1.0);
            receipt.setRemarks(request.getRemarks());
            receipt.setCreatedDate(LocalDateTime.now());
            receipt.setCreatedBy("SYSTEM");
            receipt.setModifiedDate(LocalDateTime.now());
            receipt.setModifiedBy("SYSTEM");
            receipt.setPvStatus(request.getPvStatus() != null ? request.getPvStatus() : 0);
            receipt.setCNumber(0);
            receipt.setCNumberDisplay("");

            // Save first to get identity ID
            receipt = receiptRepository.save(receipt);

            // Sequence allocation matching SP_Receipt
            int nextSeq;
            Integer maxSeq = sequenceNoMasterRepository.findMaxSequenceNoByCompanyAndSequenceName(companyId, "Receipt");
            if (maxSeq == null || maxSeq == 0) {
                nextSeq = 1;
            } else {
                nextSeq = maxSeq + 1;
            }

            SequenceNoMaster seqEntity = sequenceNoMasterRepository
                    .findByCompanyRefIdAndSequenceName(companyId, "Receipt")
                    .orElseGet(() -> {
                        SequenceNoMaster fresh = new SequenceNoMaster();
                        fresh.setCompanyRefId(companyId);
                        fresh.setSequenceName("Receipt");
                        fresh.setSequenceDate(LocalDateTime.now());
                        return fresh;
                    });
            seqEntity.setSequenceNo(nextSeq);
            seqEntity.setSequenceDate(LocalDateTime.now());
            sequenceNoMasterRepository.save(seqEntity);

            billNoDisplay = "RC" + String.format("%09d", nextSeq);
            receipt.setCNumber(nextSeq);
            receipt.setCNumberDisplay(billNoDisplay);
            receipt = receiptRepository.save(receipt);
            logger.info("Inserted new Receipt ID: {}, Number: {}", receipt.getId(), billNoDisplay);
        }

        // Insert ReceiptDetails line items
        if (request.getReceiptDetails() != null && !request.getReceiptDetails().isEmpty()) {
            List<ReceiptDetails> linesToSave = new ArrayList<>();
            for (ReceiptBillDto line : request.getReceiptDetails()) {
                if (line.getAmount() == null || line.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                ReceiptDetails detail = new ReceiptDetails();
                detail.setCompanyRefId(companyId);
                detail.setReceiptRefId(receipt.getId());
                detail.setSaleMasterRefId(line.getSaleMasterRefId() != null && line.getSaleMasterRefId() > 0 ? line.getSaleMasterRefId() : null);
                detail.setCustomerOpenRefId(line.getCustomeropenRefId() != null && line.getCustomeropenRefId() > 0 ? line.getCustomeropenRefId() : null);
                detail.setReceiptAmount(line.getAmount());
                detail.setCreatedDate(LocalDateTime.now());
                detail.setCurrencyValue(line.getCurrencyValue() != null ? line.getCurrencyValue().doubleValue() : 1.0);
                detail.setActualAmount(line.getActualAmount() != null ? line.getActualAmount().doubleValue() : 0.0);
                linesToSave.add(detail);
            }
            if (!linesToSave.isEmpty()) {
                receiptDetailsRepository.saveAll(linesToSave);
                logger.info("Saved {} line items for Receipt ID: {}", linesToSave.size(), receipt.getId());
            }
        }

        String msg = isEdit ? "Receipt Updated Successfully" : "Receipt Created Successfully";
        return ReceiptSaveResponseDto.builder()
                .ok(true)
                .isSuccess(true)
                .message(msg)
                .name(billNoDisplay)
                .id(receipt.getId())
                .build();
    }

    @Autowired
    private my.maleva.api.module.paymentrecept.repository.ReceiptViewQueryRepository receiptViewQueryRepository;

    @Autowired
    private my.maleva.api.module.filehandling.service.AttachmentStorageService attachmentStorageService;

    /** Storage folder the legacy screen used for receipt attachments (ATTACHMENT_FOLDERS.receipt). */
    private static final String ATTACHMENT_FOLDER = "Receipt";

    @Override
    public my.maleva.api.module.paymentrecept.dto.ReceiptViewDto search(
            my.maleva.api.module.paymentrecept.dto.ReceiptSearchRequest request) {
        if (request.getCompanyId() == null || request.getCompanyId() <= 0) {
            throw new my.maleva.api.common.exception.InvalidRequestException("Company is required");
        }
        List<my.maleva.api.module.paymentrecept.dto.ReceiptViewRowDto> rows = receiptViewQueryRepository.selectReceipts(request);
        // legacy painted a row red when its /Upload/{comid}/Receipt/{id} folder held
        // a file or Fileupload = 1 — the clerk's cue that a document was attached
        for (my.maleva.api.module.paymentrecept.dto.ReceiptViewRowDto row : rows) {
            boolean flagged = row.getFileUpload() != null && row.getFileUpload() == 1;
            if (!flagged) {
                try {
                    flagged = !attachmentStorageService.list(
                            my.maleva.api.module.filehandling.model.AttachmentScope.of(
                                    request.getCompanyId(), ATTACHMENT_FOLDER, row.getId(), null)).isEmpty();
                } catch (RuntimeException ex) {
                    logger.debug("Attachment folder check failed for receipt {}: {}", row.getId(), ex.getMessage());
                }
            }
            row.setHasAttachments(flagged);
        }
        return my.maleva.api.module.paymentrecept.dto.ReceiptViewDto.builder()
                .receiptMaster(rows)
                .receiptDetails(receiptViewQueryRepository.selectReceiptDetails(request))
                .totalAmount(receiptViewQueryRepository.sumAmount(request))
                .count(rows.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<my.maleva.api.module.paymentrecept.dto.ReceiptEditDto> edit(
            Integer companyId, Integer id, Integer receiptNumber) {
        if (companyId == null || companyId <= 0) {
            throw new my.maleva.api.common.exception.InvalidRequestException("Company is required");
        }
        Receipt receipt = null;
        if (receiptNumber != null && receiptNumber != 0) {
            receipt = receiptRepository.findByCompanyRefIdAndCNumber(companyId, receiptNumber).orElse(null);
        } else if (id != null && id > 0) {
            receipt = receiptRepository.findById(id)
                    .filter(r -> java.util.Objects.equals(r.getCompanyRefId(), companyId)).orElse(null);
        }
        if (receipt == null) {
            return Optional.empty();
        }
        // legacy: a receipt pushed to the receipt voucher (PVStatus <> 0) is no longer editable
        if (receipt.getPvStatus() != null && receipt.getPvStatus() != 0) {
            throw new my.maleva.api.common.exception.InvalidRequestException(
                    "Receipt " + receipt.getCNumberDisplay() + " has been pushed to the voucher and cannot be edited");
        }

        // the customer's whole outstanding list, excluding this receipt so the
        // documents it settles still read as outstanding ...
        my.maleva.api.module.paymentrecept.dto.ReceiptViewBillRequest billsRequest =
                new my.maleva.api.module.paymentrecept.dto.ReceiptViewBillRequest();
        billsRequest.setId(receipt.getCustomerRefId());
        billsRequest.setCompanyRefId(companyId);
        billsRequest.setId2(receipt.getId());
        List<ReceiptBillDto> outstanding = receiptBillQueryRepository.selectCustomerBills(billsRequest);

        // ... with this receipt's amounts merged back onto the rows it settles.
        // Legacy matched invoice lines only, so an opening-balance amount was
        // lost on every edit; both keys are matched here.
        List<ReceiptDetails> saved = receiptDetailsRepository.findByReceiptRefId(receipt.getId());
        for (ReceiptDetails line : saved) {
            for (ReceiptBillDto row : outstanding) {
                boolean sameInvoice = line.getSaleMasterRefId() != null
                        && line.getSaleMasterRefId().equals(row.getSaleMasterRefId());
                boolean sameOpening = line.getSaleMasterRefId() == null && line.getCustomerOpenRefId() != null
                        && line.getCustomerOpenRefId().equals(row.getCustomeropenRefId());
                if (sameInvoice || sameOpening) {
                    row.setAmount(line.getReceiptAmount() == null ? BigDecimal.ZERO : line.getReceiptAmount());
                    row.setSdId(line.getId());
                    row.setReceiptRefId(receipt.getId());
                    if (line.getCurrencyValue() != null) {
                        row.setCurrencyValue(BigDecimal.valueOf(line.getCurrencyValue()));
                    }
                    if (line.getActualAmount() != null) {
                        row.setActualAmount(BigDecimal.valueOf(line.getActualAmount()));
                    }
                }
            }
        }

        LocalDateTime date = receipt.getReceiptDate();
        return Optional.of(my.maleva.api.module.paymentrecept.dto.ReceiptEditDto.builder()
                .id(receipt.getId())
                .companyRefId(receipt.getCompanyRefId())
                .customerRefId(receipt.getCustomerRefId())
                .bankRefId(receipt.getBankRefId())
                .employeeRefId(receipt.getEmployeeRefId())
                .lastEmployeeRefId(receipt.getLastEmployeeRefId())
                .cNumber(receipt.getCNumber())
                .cNumberDisplay(receipt.getCNumberDisplay())
                .receiptDate(date == null ? "" : date.toLocalDate().toString())
                .sReceiptDate(date == null ? "" : date.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .amount(receipt.getAmount())
                // CurrencyValue/ActualNetAmount/BankCharges are float32 at rest, so the
                // raw double reads 3.0799999237; round to what was actually typed
                .currencyValue(round4(receipt.getCurrencyValue()))
                .actualNetAmount(round2(receipt.getActualNetAmount()))
                .bankCharges(round2(receipt.getBankCharges()))
                .remarks(receipt.getRemarks())
                .refNumber(receipt.getRefNumber())
                .pvStatus(receipt.getPvStatus())
                .fileUpload(receipt.getFileUpload())
                .qneCode(receipt.getQneCode())
                .qneId(receipt.getQneId())
                .receiptDetails(outstanding)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteReceipt(Integer id, Integer companyId) {
        if (id == null || id <= 0 || companyId == null || companyId <= 0) {
            throw new my.maleva.api.common.exception.InvalidRequestException("Receipt and company are required");
        }
        Receipt receipt = receiptRepository.findById(id)
                .filter(r -> java.util.Objects.equals(r.getCompanyRefId(), companyId))
                .orElseThrow(() -> new my.maleva.api.common.exception.InvalidRequestException(
                        "Receipt " + id + " was not found for this company"));
        // Legacy ran a bare "Delete Receipt where Id=" — also on a receipt that
        // QNE already holds, which left the two ledgers out of step. Refuse that
        // case, as the payment and voucher screens already do.
        if (receipt.getQneCode() != null && !receipt.getQneCode().isBlank()) {
            throw new my.maleva.api.common.exception.InvalidRequestException(
                    "Receipt " + receipt.getCNumberDisplay() + " is already in QNE as " + receipt.getQneCode()
                            + " and cannot be deleted here. Cancel it in QNE first.");
        }
        if (receipt.getPvStatus() != null && receipt.getPvStatus() != 0) {
            throw new my.maleva.api.common.exception.InvalidRequestException(
                    "Receipt " + receipt.getCNumberDisplay() + " has been pushed to the voucher and cannot be deleted");
        }
        // legacy left the ReceiptDetails rows orphaned; remove them with the master
        receiptDetailsRepository.deleteByReceiptRefId(receipt.getId());
        receiptRepository.delete(receipt);
        logger.info("Deleted receipt {} ({}) for company {}", receipt.getId(), receipt.getCNumberDisplay(), companyId);
        return "Receipt " + receipt.getCNumberDisplay() + " Deleted Successfully";
    }

    private static Double round4(Double value) {
        return value == null ? null : Math.round(value * 10000d) / 10000d;
    }

    private static Double round2(Double value) {
        return value == null ? null : Math.round(value * 100d) / 100d;
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        dateStr = dateStr.trim();
        try {
            if (dateStr.contains("T")) {
                return LocalDateTime.parse(dateStr);
            } else if (dateStr.contains("-")) {
                return LocalDate.parse(dateStr).atStartOfDay();
            } else if (dateStr.contains("/")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(dateStr, formatter).atStartOfDay();
            }
        } catch (Exception e) {
            logger.warn("Unable to parse receipt date: {}", dateStr);
        }
        return LocalDateTime.now();
    }

}

