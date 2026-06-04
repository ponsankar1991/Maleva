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

        logger.info("Starting BillsOrderMaster insert for Company: {}", companyId);

        try {
            validateBillsOrderDetails(dto);

            // Null checking for primitive types mimicking the SP logic
            Integer userRefId = (dto.getUserRefId() != null && dto.getUserRefId() > 0) ? dto.getUserRefId() : null;
            Integer employeeRefId = (dto.getEmployeeRefId() != null && dto.getEmployeeRefId() > 0) ? dto.getEmployeeRefId() : null;
            Integer truckRefId = (dto.getTruckRefid() != null && dto.getTruckRefid() > 0) ? dto.getTruckRefid() : null;
            Integer driverRefId = (dto.getDriverRefid() != null && dto.getDriverRefid() > 0) ? dto.getDriverRefid() : null;
            Integer saleMasterRefId = (dto.getSaleMasterRefId() != null && dto.getSaleMasterRefId() > 0) ? dto.getSaleMasterRefId() : null;

            // Foreign Key Validations
            if (userRefId != null) {
                if (!appUserRepository.existsByIdAndCompanyRefIdAndActive(userRefId, companyId, 1)) {
                    return buildErrorResponse("Login User Not Found Issue id" + userRefId);
                }
            }

            if (dto.getPaymentTermsRefid() != null && dto.getPaymentTermsRefid() > 0) {
                if (!paymentTermsMasterRepository.existsByIdAndCompanyRefIdAndActive(dto.getPaymentTermsRefid(), companyId, 1)) {
                    return buildErrorResponse("Payment Terms Not Found Issue id" + dto.getPaymentTermsRefid());
                }
            }

            if (employeeRefId != null) {
                if (!employeeMasterRepository.existsByIdAndCompanyRefIdAndActive(employeeRefId, companyId, 1)) {
                    return buildErrorResponse("Employee Not Found Issue id" + employeeRefId);
                }
            }

            if (truckRefId != null) {
                if (!truckMasterRepository.existsByIdAndCompanyRefIdAndActive(truckRefId, companyId, 1)) {
                    return buildErrorResponse("Truck Not Found Issue id" + truckRefId);
                }
            }

            if (driverRefId != null) {
                if (!driverMasterRepository.existsByIdAndCompanyRefIdAndActive(driverRefId, companyId, 1)) {
                    return buildErrorResponse("Driver Not Found Issue id" + driverRefId);
                }
            }

            boolean isNewRecord = (dto.getId() == null || dto.getId() == 0);
            BillsOrderMaster masterEntity;
            String billNoDisplay = "";

            if (!isNewRecord) {
                // UPDATE Process
                billsOrderDetailsRepository.deleteByBillsOrderMasterRefId(dto.getId());
                
                masterEntity = billsOrderMasterRepository.findById(dto.getId())
                        .orElseThrow(() -> new IllegalArgumentException("BillsOrderMaster not found for id: " + dto.getId()));
                
                masterEntity.setModifiedDate(LocalDateTime.now());
                masterEntity.setModifiedBy("system");
            } else {
                // INSERT Process
                masterEntity = new BillsOrderMaster();
                masterEntity.setActive(1);
                masterEntity.setCreatedDate(LocalDateTime.now());
                masterEntity.setCreatedBy("system");
                masterEntity.setModifiedDate(LocalDateTime.now());
                masterEntity.setModifiedBy("system");
                masterEntity.setCNumber(0);
                masterEntity.setCNumberDisplay("");
            }

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

            // Save Master Record
            masterEntity = billsOrderMasterRepository.save(masterEntity);
            Integer newMasterId = masterEntity.getId();

            // Insert Details
            List<BillsOrderDetails> detailsList = new ArrayList<>();
            for (BillsOrderDetailsInsertDto detailDto : dto.getBillsOrderDetails()) {
                BillsOrderDetails detail = new BillsOrderDetails();
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
                detail.setCreatedDate(LocalDateTime.now());
                detail.setModifiedDate(LocalDateTime.now());
                detail.setRemarksD(detailDto.getRemarksD());
                detail.setCurrencyValue(detailDto.getCurrencyValue() != null ? detailDto.getCurrencyValue() : 0.0f);
                detail.setActualAmount(detailDto.getActualAmount() != null ? detailDto.getActualAmount() : 0.0f);
                detail.setProductRefId(detailDto.getProductRefId() != null ? detailDto.getProductRefId() : 0);
                detail.setQuoteValue(detailDto.getQuoteValue() != null ? detailDto.getQuoteValue() : 0.0f);
                detail.setSerialNo(detailDto.getSerialNo() != null ? detailDto.getSerialNo() : "");
                
                detailsList.add(detail);
            }
            billsOrderDetailsRepository.saveAll(detailsList);

            // Sequence Number Logic
            if (isNewRecord) {
                Integer maxSeq = sequenceNoMasterRepository.findMaxBillsOrderSequenceNo(companyId);
                Integer newSeq = (maxSeq == null || maxSeq == 0) ? 1 : maxSeq + 1;
                
                masterEntity.setCNumber(newSeq);
                billNoDisplay = String.format("PO%09d", newSeq);
                masterEntity.setCNumberDisplay(billNoDisplay);
                
                billsOrderMasterRepository.save(masterEntity);

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

                // Update SaleOrderMaster Flags
                updateSaleOrderMasterFlags(dto);
            } else {
                billNoDisplay = masterEntity.getCNumberDisplay();
            }

            // WhatsApp Notification
            if (isNewRecord && whatsAppEnabled) {
                try {
                    whatsAppService.sendBillOrderNotification(dto, newMasterId, companyId);
                } catch (Exception ex) {
                    logger.error("! Failed to send WhatsApp notification", ex);
                }
            }

            return BillsOrderMasterResponseDto.builder()
                    .result(1)
                    .message(isNewRecord ? "Inserted Successfully" : "Updated Successfully")
                    .billNo(billNoDisplay)
                    .saleTime(LocalDateTime.now())
                    .id(newMasterId)
                    .build();

        } catch (IllegalArgumentException ex) {
            logger.error("✗ Validation error: {}", ex.getMessage());
            return buildErrorResponse(ex.getMessage());
        } catch (Exception ex) {
            logger.error("✗ Unexpected error in insertBillsOrderMaster", ex);
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

    public void updateSaleOrderMasterFlags(BillsOrderMasterInsertDto dto) {
        if (dto.getSaleMasterRefId() == null || dto.getSaleMasterRefId() == 0) {
            return;
        }

        String description = dto.getDescription();
        if (description == null || description.isEmpty()) {
            return;
        }

        Integer saleMasterRefId = dto.getSaleMasterRefId();
        String updateQuery = buildFlagUpdateQuery(description.toUpperCase().trim(), saleMasterRefId);

        if (updateQuery != null) {
            try {
                int rowsUpdated = jdbcTemplate.update(updateQuery);
                logger.info("Updated SaleOrderMaster for description '{}': {} rows affected",
                    description, rowsUpdated);
            } catch (Exception ex) {
                logger.error("Error updating SaleOrderMaster flags", ex);
            }
        }
    }

    private String buildFlagUpdateQuery(String description, Integer saleMasterRefId) {
        switch (description) {
            case "PORT CHARGES":
                return "UPDATE SaleOrderMaster SET PortCPop = 2, LiveCPop = 2 WHERE Id = " + saleMasterRefId + " AND (PortCPop = 1 OR LiveCPop = 1)";
            case "LIVE CHARGES":
                return "UPDATE SaleOrderMaster SET LiveCPop = 2 WHERE Id = " + saleMasterRefId + " AND LiveCPop = 1";
            case "CUSTOM CLEARANCE":
            case "CUSTOMER CLEARANCE":
                return "UPDATE SaleOrderMaster SET ForwardingCPop = 2 WHERE Id = " + saleMasterRefId + " AND ForwardingCPop = 1";
            case "BOAT CHARGES":
                return "UPDATE SaleOrderMaster SET BoatCPop = 2 WHERE Id = " + saleMasterRefId + " AND BoatCPop = 1";
            case "PERMIT CHARGES":
            case "INWARD PERMIT CHARGES":
                return "UPDATE SaleOrderMaster SET PermitCPop = 2 WHERE Id = " + saleMasterRefId + " AND PermitCPop = 1";
            case "MMHE CHARGES":
                return "UPDATE SaleOrderMaster SET MMHECPop = 2 WHERE Id = " + saleMasterRefId + " AND MMHECPop = 1";
            case "AIR FREIGHT EXPORT CHARGES":
                return "UPDATE SaleOrderMaster SET AFpoCPop = 2 WHERE Id = " + saleMasterRefId + " AND AFpoCPop = 1";
            case "STORAGE FEE":
            case "FREIGHT CHARGES":
                return "UPDATE SaleOrderMaster SET SFWpoCPop = 2 WHERE Id = " + saleMasterRefId + " AND SFWpoCPop = 1";
            case "CRANE & WHARFMARK CHARGES":
                return "UPDATE SaleOrderMaster SET BoatCPop1 = 2 WHERE Id = " + saleMasterRefId + " AND BoatCPop1 = 1";
            case "PFP & PAC CHARGES":
                return "UPDATE SaleOrderMaster SET PFPPCPop1 = 2 WHERE Id = " + saleMasterRefId + " AND PFPPCPop1 = 1";
            default:
                return null;
        }
    }
}
