package my.maleva.api.module.billing.billorder.service;

import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterInsertDto;
import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterResponseDto;
import my.maleva.api.module.billing.billorder.dto.BillsOrderDetailsInsertDto;
import my.maleva.api.module.billing.billorder.entity.BillsOrderMaster;
import my.maleva.api.module.billing.billorder.entity.BillsOrderDetails;
import my.maleva.api.module.billing.billorder.repository.BillsOrderMasterRepository;
import my.maleva.api.module.billing.billorder.repository.BillsOrderDetailsRepository;
import my.maleva.api.module.user.repository.AppUserRepository;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.payment.repository.PaymentTermsMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for BillsOrderMaster insert/update operations
 * Re-written from Stored Procedure into pure Java/JPA to avoid JSON serialization and NULL constraints
 */
@Service
public class BillsOrderMasterInsertServiceImpl implements IBillsOrderMasterInsertService {

    private static final Logger logger = LoggerFactory.getLogger(BillsOrderMasterInsertServiceImpl.class);

    private final BillsOrderMasterRepository billsOrderMasterRepository;
    private final BillsOrderDetailsRepository billsOrderDetailsRepository;
    private final AppUserRepository appUserRepository;
    private final EmployeeMasterRepository employeeMasterRepository;
    private final PaymentTermsMasterRepository paymentTermsMasterRepository;
    private final TruckMasterRepository truckMasterRepository;
    private final DriverMasterRepository driverMasterRepository;
    private final SequenceNoMasterRepository sequenceNoMasterRepository;
    private final JdbcTemplate jdbcTemplate;
    private final BillsOrderWhatsAppService whatsAppService;

    @Value("${app.whatsapp.enabled:true}")
    private boolean whatsAppEnabled;

    public BillsOrderMasterInsertServiceImpl(
            BillsOrderMasterRepository billsOrderMasterRepository,
            BillsOrderDetailsRepository billsOrderDetailsRepository,
            AppUserRepository appUserRepository,
            EmployeeMasterRepository employeeMasterRepository,
            PaymentTermsMasterRepository paymentTermsMasterRepository,
            TruckMasterRepository truckMasterRepository,
            DriverMasterRepository driverMasterRepository,
            SequenceNoMasterRepository sequenceNoMasterRepository,
            JdbcTemplate jdbcTemplate,
            BillsOrderWhatsAppService whatsAppService) {
        this.billsOrderMasterRepository = billsOrderMasterRepository;
        this.billsOrderDetailsRepository = billsOrderDetailsRepository;
        this.appUserRepository = appUserRepository;
        this.employeeMasterRepository = employeeMasterRepository;
        this.paymentTermsMasterRepository = paymentTermsMasterRepository;
        this.truckMasterRepository = truckMasterRepository;
        this.driverMasterRepository = driverMasterRepository;
        this.sequenceNoMasterRepository = sequenceNoMasterRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.whatsAppService = whatsAppService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillsOrderMasterResponseDto insertBillsOrderMaster(
            BillsOrderMasterInsertDto dto,
            Integer companyId) {

        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("🔵 START: BillsOrderMaster INSERT/UPDATE Operation");
        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("Company ID: {}", companyId);
        logger.info("Operation Type: {}", (dto.getId() == null || dto.getId() == 0) ? "INSERT (New)" : "UPDATE (Existing)");
        logger.info("─── DTO DATA RECEIVED ───");
        logDtoData(dto);
        logger.info("─── END DTO DATA ───");

        try {
            logger.info("Step 1/10: Validating BillsOrderDetails...");
            validateBillsOrderDetails(dto);
            logger.info("✓ Step 1: Validation passed");

            logger.info("Step 2/10: Preparing reference ID validations...");
            // Null checking for primitive types mimicking the SP logic
            Integer userRefId = (dto.getUserRefId() != null && dto.getUserRefId() > 0) ? dto.getUserRefId() : null;
            Integer employeeRefId = (dto.getEmployeeRefId() != null && dto.getEmployeeRefId() > 0) ? dto.getEmployeeRefId() : null;
            Integer truckRefId = (dto.getTruckRefid() != null && dto.getTruckRefid() > 0) ? dto.getTruckRefid() : null;
            Integer driverRefId = (dto.getDriverRefid() != null && dto.getDriverRefid() > 0) ? dto.getDriverRefid() : null;
            Integer saleMasterRefId = (dto.getSaleMasterRefId() != null && dto.getSaleMasterRefId() > 0) ? dto.getSaleMasterRefId() : null;

            logger.info("  • UserRefId (after null check): {}", userRefId);
            logger.info("  • EmployeeRefId (after null check): {}", employeeRefId);
            logger.info("  • TruckRefId (after null check): {}", truckRefId);
            logger.info("  • DriverRefId (after null check): {}", driverRefId);
            logger.info("  • SaleMasterRefId (after null check): {}", saleMasterRefId);

            logger.info("Step 3/10: Validating Foreign Key references...");
            // Foreign Key Validations
            if (userRefId != null) {
                boolean exists = appUserRepository.existsByIdAndCompanyRefIdAndActive(userRefId, companyId, 1);
                logger.info("  • User ID {} exists: {}", userRefId, exists);
                if (!exists) {
                    return buildErrorResponse("Login User Not Found Issue id" + userRefId);
                }
            }

            if (dto.getPaymentTermsRefid() != null && dto.getPaymentTermsRefid() > 0) {
                boolean exists = paymentTermsMasterRepository.existsByIdAndCompanyRefIdAndActive(dto.getPaymentTermsRefid(), companyId, 1);
                logger.info("  • PaymentTerms ID {} exists: {}", dto.getPaymentTermsRefid(), exists);
                if (!exists) {
                    return buildErrorResponse("Payment Terms Not Found Issue id" + dto.getPaymentTermsRefid());
                }
            }

            if (employeeRefId != null) {
                boolean exists = employeeMasterRepository.existsByIdAndCompanyRefIdAndActive(employeeRefId, companyId, 1);
                logger.info("  • Employee ID {} exists: {}", employeeRefId, exists);
                if (!exists) {
                    return buildErrorResponse("Employee Not Found Issue id" + employeeRefId);
                }
            }

            if (truckRefId != null) {
                boolean exists = truckMasterRepository.existsByIdAndCompanyRefIdAndActive(truckRefId, companyId, 1);
                logger.info("  • Truck ID {} exists: {}", truckRefId, exists);
                if (!exists) {
                    return buildErrorResponse("Truck Not Found Issue id" + truckRefId);
                }
            }

            if (driverRefId != null) {
                boolean exists = driverMasterRepository.existsByIdAndCompanyRefIdAndActive(driverRefId, companyId, 1);
                logger.info("  • Driver ID {} exists: {}", driverRefId, exists);
                if (!exists) {
                    return buildErrorResponse("Driver Not Found Issue id" + driverRefId);
                }
            }
            logger.info("✓ Step 3: All FK references validated");

            logger.info("Step 4/10: Determining INSERT vs UPDATE...");
            boolean isNewRecord = (dto.getId() == null || dto.getId() == 0);
            logger.info("  • isNewRecord: {}", isNewRecord);

            BillsOrderMaster masterEntity;
            String billNoDisplay = "";

            if (!isNewRecord) {
                logger.info("Step 5/10: UPDATE MODE - Processing existing record ID: {}", dto.getId());
                // UPDATE Process
                logger.info("  • Calling updateSaleOrderMasterFlags...");
                updateSaleOrderMasterFlags(dto);

                logger.info("  • Fetching existing master record...");
                masterEntity = billsOrderMasterRepository.findById(dto.getId())
                        .orElseThrow(() -> new IllegalArgumentException("BillsOrderMaster not found for id: " + dto.getId()));
                
                masterEntity.setModifiedDate(LocalDateTime.now());
                masterEntity.setModifiedBy("system");
                logger.info("✓ Step 5: Record prepared for update");
            } else {
                logger.info("Step 5/10: INSERT MODE - Creating new record");
                // INSERT Process
                masterEntity = new BillsOrderMaster();
                masterEntity.setActive(1);
                masterEntity.setCreatedDate(LocalDateTime.now());
                masterEntity.setCreatedBy("system");
                masterEntity.setModifiedDate(LocalDateTime.now());
                masterEntity.setModifiedBy("system");
                masterEntity.setCNumber(0);
                masterEntity.setCNumberDisplay("");
                logger.info("✓ Step 5: New entity created");
            }

            logger.info("Step 6/10: Mapping DTO fields to Entity...");
            // Map standard fields
            masterEntity.setCompanyRefId(companyId);
            masterEntity.setUserRefId(userRefId);
            masterEntity.setEmployeeRefId(employeeRefId);
            masterEntity.setLastEmployeeRefId(employeeRefId);
            masterEntity.setSupplierRefId(dto.getSupplierRefId());
            masterEntity.setSaleMasterRefId(saleMasterRefId);
            masterEntity.setFileupload(dto.getFileupload() != null ? dto.getFileupload() : 0);
            masterEntity.setPaymentTermsRefid(dto.getPaymentTermsRefid());
            masterEntity.setDescription(dto.getDescription());
            // Set when the PO was raised from a workshop job order, so the job
            // can list every PO opened against it - one repair buys from
            // several vendors, so there is normally more than one.
            masterEntity.setJobOrderMasterRefId(dto.getJobOrderMasterRefId());
            masterEntity.setJobOrderNo(dto.getJobOrderNo());
            masterEntity.setSaleDate(dto.getSaleDate() != null ? dto.getSaleDate().atStartOfDay() : LocalDateTime.now());
            masterEntity.setSaleType(dto.getSaleType() != null ? dto.getSaleType() : "");
            masterEntity.setCoinage(dto.getCoinage() != null ? dto.getCoinage() : 0.0f);
            masterEntity.setGrossAmount(dto.getGrossAmount() != null ? dto.getGrossAmount() : 0.0f);
            masterEntity.setTaxAmount(dto.getTaxAmount() != null ? dto.getTaxAmount() : 0.0f);
            masterEntity.setDiscountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : 0.0f);
            masterEntity.setRemarks(dto.getRemarks());
            masterEntity.setPlusAmount(dto.getPlusAmount() != null ? dto.getPlusAmount() : 0.0f);
            masterEntity.setMinusAmount(dto.getMinusAmount() != null ? dto.getMinusAmount() : 0.0f);
            masterEntity.setAmount(dto.getAmount() != null ? dto.getAmount() : 0.0f);
            masterEntity.setInvoiceNo(dto.getInvoiceNo());
            masterEntity.setInvoiceDate(dto.getInvoiceDate() != null ? dto.getInvoiceDate().atStartOfDay() : LocalDateTime.now());
            masterEntity.setTruckRefid(truckRefId);
            masterEntity.setDriverRefid(driverRefId);
            masterEntity.setPStatus(dto.getPStatus() != null ? dto.getPStatus() : 0);
            masterEntity.setOffVessal(dto.getOffVessal() != null ? dto.getOffVessal() : "");
            masterEntity.setLodingVessal(dto.getLodingVessal() != null ? dto.getLodingVessal() : "");
            masterEntity.setCheckloadingVessel(dto.getCheckloadingVessel() != null ? dto.getCheckloadingVessel() : 0);
            masterEntity.setCheckoffgVessel(dto.getCheckoffgVessel() != null ? dto.getCheckoffgVessel() : 0);
            
            masterEntity.setCurrencyValue(dto.getCurrencyValue() != null ? dto.getCurrencyValue() : 0.0f);
            masterEntity.setActualAmount(dto.getActualAmount() != null ? dto.getActualAmount() : 0.0f);
            masterEntity.setBillStatus(dto.getBillStatus());
            masterEntity.setPayTo(dto.getPayTo());
            masterEntity.setDueDate(dto.getDueDate());
            logger.info("✓ Step 6: All fields mapped to entity");

            logger.info("Step 7/10: Saving Master Record to database...");
            // Save Master Record
            masterEntity = billsOrderMasterRepository.save(masterEntity);
            Integer newMasterId = masterEntity.getId();
            logger.info("✓ Step 7: Master Record saved with ID: {}", newMasterId);

            logger.info("Step 8/10: Processing {} Detail Records...", dto.getBillsOrderDetails().size());

            // Matched by id rather than replaced wholesale: a line already received
            // into stock carries StockPushedDate, and deleting it to insert a fresh
            // row in its place would erase that fact on the very next save of the
            // order, silently reopening the line to a second push. Anything the
            // incoming list drops (the user removed a row in the grid) is deleted
            // explicitly below instead, once it is known who is still wanted.
            Map<Integer, BillsOrderDetails> existingDetailsById = new java.util.LinkedHashMap<>();
            if (!isNewRecord) {
                for (BillsOrderDetails existing
                        : billsOrderDetailsRepository.findByBillsOrderMasterRefId(dto.getId())) {
                    existingDetailsById.put(existing.getId(), existing);
                }
            }

            List<BillsOrderDetails> detailsList = new ArrayList<>();
            for (int i = 0; i < dto.getBillsOrderDetails().size(); i++) {
                BillsOrderDetailsInsertDto detailDto = dto.getBillsOrderDetails().get(i);

                Integer existingId = (detailDto.getId() != null && detailDto.getId() > 0)
                        ? detailDto.getId() : null;
                BillsOrderDetails detail = existingId != null
                        ? existingDetailsById.remove(existingId) : null;
                boolean isNewLine = detail == null;
                if (isNewLine) {
                    detail = new BillsOrderDetails();
                    detail.setCreatedDate(LocalDateTime.now());
                }

                detail.setBillsOrderMasterRefId(newMasterId);
                detail.setAccountMasterRefId(detailDto.getAccountMasterRefId());
                detail.setMrp(detailDto.getMrp() != null ? detailDto.getMrp() : 0.0f);
                detail.setPurchaseRate(detailDto.getPurchaseRate() != null ? detailDto.getPurchaseRate() : 0.0f);
                detail.setItemQty(detailDto.getItemQty() != null ? detailDto.getItemQty() : 0.0f);
                detail.setDiscPer(detailDto.getDiscPer() != null ? detailDto.getDiscPer() : 0.0f);
                detail.setDiscAmount(detailDto.getDiscAmount() != null ? detailDto.getDiscAmount() : 0.0f);
                detail.setLandingCost(detailDto.getLandingCost() != null ? detailDto.getLandingCost() : 0.0f);
                detail.setTaxPercent(detailDto.getTaxPercent() != null ? detailDto.getTaxPercent() : 0.0f);
                detail.setTaxAmount(detailDto.getTaxAmount() != null ? detailDto.getTaxAmount() : 0.0f);
                detail.setSalesRate(detailDto.getSalesRate() != null ? detailDto.getSalesRate() : 0.0f);
                detail.setNetSalesRate(detailDto.getNetSalesRate() != null ? detailDto.getNetSalesRate() : 0.0f);
                detail.setAmount(detailDto.getAmount() != null ? detailDto.getAmount() : 0.0f);
                detail.setModifiedDate(LocalDateTime.now());
                detail.setRemarksD(detailDto.getRemarksD());
                detail.setCurrencyValue(detailDto.getCurrencyValue() != null ? detailDto.getCurrencyValue() : 0.0f);
                detail.setActualAmount(detailDto.getActualAmount() != null ? detailDto.getActualAmount() : 0.0f);
                detail.setProductRefId(detailDto.getProductRefId() != null ? detailDto.getProductRefId() : 0);
                detail.setQuoteValue(detailDto.getQuoteValue() != null ? detailDto.getQuoteValue() : 0.0f);
                detail.setSerialNo(detailDto.getSerialNo() != null ? detailDto.getSerialNo() : "");
                // Kept null rather than defaulted to 0: 0 is not a product, and the
                // stock-in loop skips nulls but would treat a zero as something to look up.
                detail.setInventoryProductRefId(
                        detailDto.getInventoryProductRefId() != null && detailDto.getInventoryProductRefId() > 0
                                ? detailDto.getInventoryProductRefId()
                                : null);

                detailsList.add(detail);
                logger.info("  • Detail[{}] prepared for save - AccountId: {}, Amount: {}, new={}",
                    (i+1), detail.getAccountMasterRefId(), detail.getAmount(), isNewLine);
            }

            // Whatever is still in this map was on the order before and is absent
            // from what was just submitted - removed by the user in the grid.
            if (!existingDetailsById.isEmpty()) {
                logger.info("  • Removing {} line(s) dropped from the grid: ids={}",
                        existingDetailsById.size(), existingDetailsById.keySet());
                billsOrderDetailsRepository.deleteAll(existingDetailsById.values());
            }

            billsOrderDetailsRepository.saveAll(detailsList);
            logger.info("✓ Step 8: All {} detail records saved", detailsList.size());

            logger.info("Step 9/10: Processing Sequence Number...");
            // Sequence Number Logic
            if (isNewRecord) {
                Integer maxSeq = sequenceNoMasterRepository.findMaxBillsOrderSequenceNo(companyId);
                logger.info("  • Current Max Sequence: {}", maxSeq);

                Integer newSeq = (maxSeq == null || maxSeq == 0) ? 1 : maxSeq + 1;
                logger.info("  • New Sequence to assign: {}", newSeq);

                masterEntity.setCNumber(newSeq);
                billNoDisplay = String.format("PO%09d", newSeq);
                masterEntity.setCNumberDisplay(billNoDisplay);
                logger.info("  • Generated Bill Display Number: {}", billNoDisplay);

                billsOrderMasterRepository.save(masterEntity);
                logger.info("  • Master record updated with sequence number");

                // Update SequenceNoMaster
                SequenceNoMaster seqMaster = sequenceNoMasterRepository
                    .findByCompanyRefIdAndSequenceName(companyId, "BillsOrderMaster")
                    .orElseGet(() -> {
                        SequenceNoMaster newS = new SequenceNoMaster();
                        newS.setCompanyRefId(companyId);
                        newS.setSequenceName("BillsOrderMaster");
                        return newS;
                    });
                seqMaster.setSequenceNo(newSeq);
                sequenceNoMasterRepository.save(seqMaster);
                logger.info("  • SequenceNoMaster updated for company: {}", companyId);

                logger.info("Step 10/10: Calling updateSaleOrderMasterFlags for new INSERT...");
                // Update SaleOrderMaster Flags
                updateSaleOrderMasterFlags(dto);
                logger.info("✓ Step 10: Update flags completed");
            }
            else {
                billNoDisplay = masterEntity.getCNumberDisplay();
                logger.info("Step 10/10: UPDATE mode - No sequence update needed, using existing display: {}", billNoDisplay);
            }

            logger.info("Final Step: WhatsApp Notification...");
            // WhatsApp Notification
            if (isNewRecord && whatsAppEnabled) {
                try {
                    logger.info("  • WhatsApp enabled and new record - sending notification...");
                    whatsAppService.sendBillOrderNotification(dto, newMasterId, companyId);
                    logger.info("  • WhatsApp notification sent successfully");
                } catch (Exception ex) {
                    logger.error("❌ Failed to send WhatsApp notification", ex);
                }
            } else {
                logger.info("  • WhatsApp notification skipped (isNewRecord={}, whatsAppEnabled={})",
                    isNewRecord, whatsAppEnabled);
            }

            logger.info("═══════════════════════════════════════════════════════════════");
            logger.info("✅ OPERATION COMPLETED SUCCESSFULLY");
            logger.info("═══════════════════════════════════════════════════════════════");
            logger.info("Result Summary:");
            logger.info("  • Bill ID: {}", newMasterId);
            logger.info("  • Bill Display No: {}", billNoDisplay);
            logger.info("  • Operation: {}", isNewRecord ? "INSERT" : "UPDATE");

            return BillsOrderMasterResponseDto.builder()
                    .result(1)
                    .message(isNewRecord ? "Inserted Successfully" : "Updated Successfully")
                    .billNo(billNoDisplay)
                    .saleTime(LocalDateTime.now())
                    .id(newMasterId)
                    .build();

        } catch (IllegalArgumentException ex) {
            logger.error("❌ VALIDATION ERROR: {}", ex.getMessage());
            return buildErrorResponse(ex.getMessage());
        } catch (Exception ex) {
            logger.error("❌ UNEXPECTED ERROR in insertBillsOrderMaster", ex);
            logger.error("Exception Type: {}", ex.getClass().getName());
            logger.error("Exception Message: {}", ex.getMessage());
            logger.error("Stack Trace:", ex);
            return buildErrorResponse("Error: " + (ex.getMessage() != null ? ex.getMessage() : "Unknown error"));
        }
    }

    private BillsOrderMasterResponseDto buildErrorResponse(String message) {
        return BillsOrderMasterResponseDto.builder()
                .result(0)
                .message(message)
                .billNo("")
                .saleTime(LocalDateTime.now())
                .id(0)
                .build();
    }

    @Override
    public void validateBillsOrderDetails(BillsOrderMasterInsertDto billsOrderMasterDto) {
        if (billsOrderMasterDto.getBillsOrderDetails() == null ||
            billsOrderMasterDto.getBillsOrderDetails().isEmpty()) {
            throw new IllegalArgumentException("At least one bill order detail is required");
        }

        long itemsWithoutAccount = billsOrderMasterDto.getBillsOrderDetails().stream()
                .filter(detail -> detail.getAccountMasterRefId() == null ||
                                detail.getAccountMasterRefId() == 0)
                .count();

        if (itemsWithoutAccount > 0) {
            throw new IllegalArgumentException(
                    "Please enter the Account Code for all items. Found " + itemsWithoutAccount + " items without account.");
        }
    }

    /**
     * Declared on IBillsOrderMasterInsertService, so it can be called on its own
     * rather than only from insertBillsOrderMaster. It writes through
     * JdbcTemplate, and the pool hands out connections with autocommit off, so
     * an external call without a transaction would discard the update instead of
     * saving it. Internal calls already run inside the caller's transaction and
     * join it here.
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleOrderMasterFlags(BillsOrderMasterInsertDto dto) {
        logger.info("─── SALE ORDER MASTER FLAG UPDATE ───");

        if (dto.getSaleMasterRefId() == null || dto.getSaleMasterRefId() == 0) {
            logger.info("⏭️  SaleMasterRefId is null or 0, skipping SaleOrderMaster flag update");
            return;
        }

        String description = dto.getDescription();
        if (description == null || description.isEmpty()) {
            logger.info("⏭️  Description is null or empty, skipping SaleOrderMaster flag update");
            return;
        }

        Integer saleMasterRefId = dto.getSaleMasterRefId();
        String descriptionUpper = description.toUpperCase().trim();

        logger.info("Parameters for flag update:");
        logger.info("  • SaleMasterRefId: {}", saleMasterRefId);
        logger.info("  • Description: '{}' (original: '{}')", descriptionUpper, description);

        String updateQuery = buildFlagUpdateQuery(descriptionUpper, saleMasterRefId);

        if (updateQuery != null) {
            logger.info("Update Query Built for Description '{}': {}", descriptionUpper, updateQuery);

            try {
                // Check if SaleOrderMaster record exists first
                String checkQuery = "SELECT COUNT(*) FROM SaleOrderMaster WHERE Id = " + saleMasterRefId;
                logger.debug("Checking if SaleOrderMaster exists with query: {}", checkQuery);

                Integer recordCount = jdbcTemplate.queryForObject(checkQuery, Integer.class);
                logger.info("SaleOrderMaster Record Count for ID {}: {}", saleMasterRefId, recordCount);

                if (recordCount == null || recordCount == 0) {
                    logger.warn("❌ SaleOrderMaster record NOT FOUND for ID: {} - Description: '{}' - Cannot proceed with update",
                        saleMasterRefId, descriptionUpper);
                    return;
                }

                logger.info("✓ SaleOrderMaster record EXISTS - Proceeding with UPDATE");
                logger.info("Executing Query: {}", updateQuery);

                int rowsUpdated = jdbcTemplate.update(updateQuery);
                logger.info("Query Execution Result: {} rows affected", rowsUpdated);

                if (rowsUpdated > 0) {
                    logger.info("✅ SUCCESS: Updated SaleOrderMaster ID={} for description '{}': {} rows affected",
                        saleMasterRefId, descriptionUpper, rowsUpdated);
                } else {
                    // Log current values to help diagnose why WHERE clause didn't match
                    String selectQuery = "SELECT PortCPop, LiveCPop, ForwardingCPop, BoatCPop, PermitCPop, MMHECPop, AFpoCPop, SFWpoCPop, BoatCPop1, PFPPCPop1 FROM SaleOrderMaster WHERE Id = " + saleMasterRefId;
                    logger.warn("⚠️  No rows updated (WHERE condition not met). Current column values for ID {}:", saleMasterRefId);
                    logger.warn("    Query to check current values: {}", selectQuery);
                    logger.warn("    This means the record exists but the flag columns may already have values other than 0 or 1");
                    logger.warn("    Expected condition: Flags should be IN (0,1) to be updated to 2");
                }
            } catch (Exception ex) {
                logger.error("❌ ERROR updating SaleOrderMaster flags for ID={}, description='{}'",
                    saleMasterRefId, descriptionUpper, ex);
            }
        } else {
            logger.info("⏭️  No update query built for description: '{}' - Not in switch case", descriptionUpper);
        }

        logger.info("─── END SALE ORDER MASTER FLAG UPDATE ───");
    }

    private String buildFlagUpdateQuery(String description, Integer saleMasterRefId) {
        switch (description) {
            case "PORT CHARGES":
                // Update when PortCPop or LiveCPop is 0 or 1
                return "UPDATE SaleOrderMaster SET PortCPop = 2, LiveCPop = 2 WHERE Id = " + saleMasterRefId + " AND (PortCPop IN (0,1) OR LiveCPop IN (0,1))";
            case "LIVE CHARGES":
                return "UPDATE SaleOrderMaster SET LiveCPop = 2 WHERE Id = " + saleMasterRefId + " AND LiveCPop IN (0,1)";
            case "CUSTOM CLEARANCE":
            case "CUSTOMER CLEARANCE":
                return "UPDATE SaleOrderMaster SET ForwardingCPop = 2 WHERE Id = " + saleMasterRefId + " AND ForwardingCPop IN (0,1)";
            case "BOAT CHARGES":
                return "UPDATE SaleOrderMaster SET BoatCPop = 2 WHERE Id = " + saleMasterRefId + " AND BoatCPop IN (0,1)";

            case "PERMIT CHARGES":
            case "INWARD PERMIT CHARGES":
                return "UPDATE SaleOrderMaster " +
                        "SET PermitCPop = CASE WHEN PermitCPop IN (0,1) THEN 2 ELSE PermitCPop END, " +
                        "    PFPPCPop1 = CASE WHEN PFPPCPop1 IN (0,1) THEN 2 ELSE PFPPCPop1 END " +
                        "WHERE Id = " + saleMasterRefId;
            case "MMHE CHARGES":
                return "UPDATE SaleOrderMaster SET MMHECPop = 2 WHERE Id = " + saleMasterRefId + " AND MMHECPop IN (0,1)";
            case "AIR FREIGHT EXPORT CHARGES":
                return "UPDATE SaleOrderMaster SET AFpoCPop = 2 WHERE Id = " + saleMasterRefId + " AND AFpoCPop IN (0,1)";
            case "STORAGE FEE":
            case "FREIGHT CHARGES":
                return "UPDATE SaleOrderMaster SET SFWpoCPop = 2 WHERE Id = " + saleMasterRefId + " AND SFWpoCPop IN (0,1)";
            case "CRANE & WHARFMARK CHARGES":
                return "UPDATE SaleOrderMaster SET BoatCPop1 = 2 WHERE Id = " + saleMasterRefId + " AND BoatCPop1 IN (0,1)";
            case "PFP & PAC CHARGES":
                // Also update when column is 0 (as requested)
                return "UPDATE SaleOrderMaster SET PFPPCPop1 = 2 WHERE Id = " + saleMasterRefId + " AND PFPPCPop1 IN (0,1)";
            default:
                return null;
        }
    }

    private void logDtoData(BillsOrderMasterInsertDto dto) {
        logger.info("Master Record Fields:");
        logger.info("  ID: {} (0=New Insert, >0=Update)", dto.getId() != null ? dto.getId() : "NULL");
        logger.info("  Description: '{}'", dto.getDescription());
        logger.info("  SaleMasterRefId: {}", dto.getSaleMasterRefId());
        logger.info("  SupplierRefId: {}", dto.getSupplierRefId());
        logger.info("  UserRefId: {}", dto.getUserRefId());
        logger.info("  EmployeeRefId: {}", dto.getEmployeeRefId());
        logger.info("  InvoiceNo: '{}'", dto.getInvoiceNo());
        logger.info("  InvoiceDate: {}", dto.getInvoiceDate());
        logger.info("  SaleDate: {}", dto.getSaleDate());
        logger.info("  GrossAmount: {}", dto.getGrossAmount());
        logger.info("  TaxAmount: {}", dto.getTaxAmount());
        logger.info("  DiscountAmount: {}", dto.getDiscountAmount());
        logger.info("  Amount: {}", dto.getAmount());
        logger.info("  PaymentTermsRefId: {}", dto.getPaymentTermsRefid());
        logger.info("  TruckRefId: {}", dto.getTruckRefid());
        logger.info("  DriverRefId: {}", dto.getDriverRefid());
        logger.info("  BillStatus: '{}'", dto.getBillStatus());
        logger.info("  PayTo: '{}'", dto.getPayTo());
        logger.info("  OffVessel: '{}'", dto.getOffVessal());
        logger.info("  LoadingVessel: '{}'", dto.getLodingVessal());

        if (dto.getBillsOrderDetails() != null) {
            logger.info("Detail Records Count: {}", dto.getBillsOrderDetails().size());
            for (int i = 0; i < dto.getBillsOrderDetails().size(); i++) {
                BillsOrderDetailsInsertDto detail = dto.getBillsOrderDetails().get(i);
                logger.info("  Detail[{}]:", i + 1);
                logger.info("    • AccountMasterRefId: {}", detail.getAccountMasterRefId());
                logger.info("    • ProductRefId: {}", detail.getProductRefId());
                logger.info("    • Amount: {}", detail.getAmount());
                logger.info("    • Quantity: {}", detail.getItemQty());
                logger.info("    • TaxAmount: {}", detail.getTaxAmount());
            }
        } else {
            logger.warn("⚠️  No detail records provided!");
        }
    }
}
