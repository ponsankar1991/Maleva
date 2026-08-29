package my.maleva.api.module.purchase.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.purchase.dto.*;
import my.maleva.api.module.purchase.entity.PurchaseMaster;
import my.maleva.api.module.purchase.mapper.PurchaseMasterMapper;
import my.maleva.api.module.purchase.repository.PurchaseMasterRepository;
import my.maleva.api.module.purchase.repository.impl.PurchaseMasterRepositoryCustomImpl;
import my.maleva.api.module.purchase.service.PurchaseMasterService;
import my.maleva.api.module.purchase.model.PurchaseMasterModel;
import my.maleva.api.module.purchase.model.PurchaseDetailsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Service implementation for PurchaseMaster
 * Implements SP_PurchaseMaster stored procedure logic
 */
@Service
public class PurchaseMasterServiceImpl implements PurchaseMasterService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseMasterServiceImpl.class);

    @Autowired
    private PurchaseMasterRepository purchaseMasterRepository;

    @Autowired
    private PurchaseMasterMapper mapper;

    @Autowired
    private PurchaseMasterRepositoryCustomImpl purchaseMasterCustomRepository;

    @Autowired
    private SequenceNoMasterRepository sequenceNoMasterRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<PurchaseMasterDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all PurchaseMaster records for company: {}", companyRefId);
        return purchaseMasterRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseMasterDto> getActiveByCompanyId(Integer companyRefId) {
        logger.info("Fetching active PurchaseMaster records for company: {}", companyRefId);
        return purchaseMasterRepository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PurchaseMasterDto> getById(Integer id) {
        logger.info("Fetching PurchaseMaster by ID: {}", id);
        return purchaseMasterRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public PurchaseMasterDto create(PurchaseMasterDto dto) {
        logger.info("Creating new PurchaseMaster for company: {}", dto.getCompanyRefId());

        PurchaseMaster entity = mapper.toEntity(dto);
        entity.setActive(1);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());

        // Generate next CNumber based on SP_PurchaseMaster logic
        Integer nextCNumber = getNextCNumber(dto.getCompanyRefId());
        entity.setCNumber(nextCNumber);
        String cNumberDisplay = generateCNumberDisplay(nextCNumber);
        entity.setCNumberDisplay(cNumberDisplay);

        PurchaseMaster saved = purchaseMasterRepository.save(entity);
        logger.info("PurchaseMaster created successfully with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public PurchaseMasterDto update(Integer id, PurchaseMasterDto dto) {
        logger.info("Updating PurchaseMaster with ID: {}", id);
        PurchaseMaster entity = purchaseMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseMaster not found with ID: " + id));

        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());

        PurchaseMaster updated = purchaseMasterRepository.save(entity);
        logger.info("PurchaseMaster updated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting PurchaseMaster with ID: {}", id);
        if (!purchaseMasterRepository.existsById(id)) {
            logger.warn("PurchaseMaster not found with ID: {}", id);
            return false;
        }
        purchaseMasterRepository.deleteById(id);
        logger.info("PurchaseMaster deleted successfully with ID: {}", id);
        return true;
    }

    @Override
    public Optional<PurchaseMasterDto> getByInvoiceNo(Integer companyRefId, String invoiceNo) {
        logger.info("Fetching PurchaseMaster by invoice number: {}", invoiceNo);
        return purchaseMasterRepository.findByCompanyRefIdAndInvoiceNo(companyRefId, invoiceNo)
                .map(mapper::toDto);
    }

    @Override
    public List<PurchaseMasterDto> getBySupplier(Integer companyRefId, Integer supplierRefId) {
        logger.info("Fetching PurchaseMaster for supplier: {}", supplierRefId);
        return purchaseMasterRepository.findByCompanyRefIdAndSupplierRefId(companyRefId, supplierRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseMasterDto> getBySaleType(Integer companyRefId, String saleType) {
        logger.info("Fetching PurchaseMaster by sale type: {}", saleType);
        return purchaseMasterRepository.findByCompanyRefIdAndSaleType(companyRefId, saleType)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseMasterDto> getByDateRange(Integer companyRefId, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching PurchaseMaster between dates: {} to {}", startDate, endDate);
        return purchaseMasterRepository.findByCompanyRefIdAndSaleDateBetween(companyRefId, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseMasterDto> getByEmployee(Integer companyRefId, Integer employeeRefId) {
        logger.info("Fetching PurchaseMaster for employee: {}", employeeRefId);
        return purchaseMasterRepository.findByCompanyRefIdAndEmployeeRefId(companyRefId, employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Checking if CNumber exists: {}", cNumber);
        return purchaseMasterRepository.existsByCompanyRefIdAndCNumber(companyRefId, cNumber);
    }

    @Override
    public Optional<PurchaseMasterDto> getByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Fetching PurchaseMaster by CNumber: {}", cNumber);
        return purchaseMasterRepository.findByCompanyRefIdAndCNumber(companyRefId, cNumber)
                .map(mapper::toDto);
    }

    @Override
    public Optional<PurchaseMasterDto> getByPurchaseOrderRef(Integer purchaseOrderMasterRefId) {
        logger.info("Fetching PurchaseMaster by purchase order reference: {}", purchaseOrderMasterRefId);
        return purchaseMasterRepository.findByPurchaseOrderMasterRefId(purchaseOrderMasterRefId)
                .map(mapper::toDto);
    }

    @Override
    public long countByCompanyId(Integer companyRefId) {
        logger.info("Counting PurchaseMaster records for company: {}", companyRefId);
        return purchaseMasterRepository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompanyId(Integer companyRefId) {
        logger.info("Counting active PurchaseMaster records for company: {}", companyRefId);
        return purchaseMasterRepository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    @Transactional
    public PurchaseMasterDto activate(Integer id) {
        logger.info("Activating PurchaseMaster with ID: {}", id);
        PurchaseMaster entity = purchaseMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseMaster not found with ID: " + id));
        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        PurchaseMaster updated = purchaseMasterRepository.save(entity);
        logger.info("PurchaseMaster activated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public PurchaseMasterDto deactivate(Integer id) {
        logger.info("Deactivating PurchaseMaster with ID: {}", id);
        PurchaseMaster entity = purchaseMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseMaster not found with ID: " + id));
        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        PurchaseMaster updated = purchaseMasterRepository.save(entity);
        logger.info("PurchaseMaster deactivated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    public Integer getNextCNumber(Integer companyRefId) {
        logger.info("Generating next CNumber for company: {}", companyRefId);
        // This logic is based on SP_PurchaseMaster stored procedure
        // It retrieves the maximum CNumber from SequenceNoMaster for 'PurchaseMaster'
        // and increments it by 1
        List<PurchaseMaster> purchases = purchaseMasterRepository.findByCompanyRefId(companyRefId);
        if (purchases.isEmpty()) {
            return 1;
        }
        Integer maxCNumber = purchases.stream()
                .map(PurchaseMaster::getCNumber)
                .max(Integer::compareTo)
                .orElse(0);
        return maxCNumber + 1;
    }

    @Override
    public String generateCNumberDisplay(Integer cNumber) {
        logger.info("Generating CNumberDisplay for CNumber: {}", cNumber);
        // Format: PM + 9 digit zero-padded number (e.g., PM000000001)
        return String.format("PM%09d", cNumber);
    }

    @Override
    public BigDecimal checkEditAmount(Integer companyId, Integer purchaseId) {
        logger.info("Calculating total payment amount for purchase ID: {} in company: {}", purchaseId, companyId);

        // Input validation
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException("Company ID must be positive");
        }
        if (purchaseId == null || purchaseId <= 0) {
            throw new InvalidRequestException("Purchase ID must be positive");
        }

        // Verify purchase exists and is active
        Optional<PurchaseMaster> purchase = purchaseMasterRepository.findById(purchaseId);
        if (purchase.isEmpty()) {
            throw new EntityNotFoundException("Purchase order not found with ID: " + purchaseId);
        }
        if (!purchase.get().getCompanyRefId().equals(companyId)) {
            throw new InvalidRequestException("Purchase order does not belong to the specified company");
        }
        if (purchase.get().getActive() == 2) {
            throw new InvalidRequestException("Purchase order is inactive");
        }

        BigDecimal totalAmount = purchaseMasterRepository.checkEditAmount(purchaseId, companyId);
        logger.info("Total payment amount calculated: {} for purchase ID: {}", totalAmount, purchaseId);

        return totalAmount;
    }

    @Override
    @Transactional
    public boolean softDelete(Integer id, Integer companyId) {
        logger.info("Soft deleting PurchaseMaster with ID: {} for company: {}", id, companyId);

        // Validate inputs
        if (id == null || id <= 0) {
            throw new InvalidRequestException("Purchase Master ID must be positive");
        }
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException("Company ID must be positive");
        }

        // Check if record exists and belongs to the company
        Optional<PurchaseMaster> purchaseOpt = purchaseMasterRepository.findById(id);
        if (purchaseOpt.isEmpty()) {
            logger.warn("PurchaseMaster not found with ID: {}", id);
            return false;
        }

        PurchaseMaster purchase = purchaseOpt.get();
        if (!purchase.getCompanyRefId().equals(companyId)) {
            throw new InvalidRequestException("Purchase order does not belong to the specified company");
        }

        // Perform soft delete by setting Active=2
        purchase.setActive(2);
        purchase.setModifiedDate(LocalDateTime.now());
        purchaseMasterRepository.save(purchase);

        logger.info("PurchaseMaster soft deleted successfully with ID: {}", id);
        return true;
    }

    @Override
    public String getMaxPurchaseMasterNo(Integer companyId) {
        logger.info("Getting max PurchaseMaster number for company: {}", companyId);

        // Input validation
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException("Company ID must be positive");
        }

        // Get the maximum sequence number from SequenceNoMaster for 'PurchaseMaster'
        // Equivalent to: SELECT ISNULL(MAX(SequenceNo)+1,1) FROM SequenceNoMaster WHERE CompanyRefId = companyId AND SequenceName='PurchaseMaster'
        Integer maxSequenceNo = sequenceNoMasterRepository.findMaxPurchaseMasterSequenceNo(companyId);
        Integer nextSequenceNo = (maxSequenceNo == null ? 0 : maxSequenceNo) + 1;

        // Format as "PM" + 9-digit zero-padded number
        String formattedNumber = String.format("PM%09d", nextSequenceNo);

        logger.info("Generated max PurchaseMaster number: {} for company: {}", formattedNumber, companyId);
        return formattedNumber;
    }

    @Override
    public List<String> getDistinctDescriptions(Integer companyId) {
        logger.info("Getting distinct descriptions for company: {}", companyId);

        // Input validation
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException("Company ID must be positive");
        }

        // Get distinct descriptions from PurchaseMaster
        List<String> descriptions = purchaseMasterRepository.findDistinctDescriptionsByCompanyId(companyId);

        logger.info("Retrieved {} distinct descriptions for company: {}", descriptions.size(), companyId);
        return descriptions;
    }

    @Override
    public InsertPurchaseMasterResponseDto insertPurchaseMaster(List<PurchaseMasterDto> purchaseMasters, Integer companyId) {
        logger.info("Inserting PurchaseMaster records for company: {} with {} items", companyId, purchaseMasters.size());

        // Input validation
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException("Company ID must be positive");
        }
        if (purchaseMasters == null || purchaseMasters.isEmpty()) {
            throw new InvalidRequestException("PurchaseMaster list cannot be null or empty");
        }

        try {
            // Create a list of maps with boundindex added and PascalCase field names
            // Following the exact same approach as the .NET code that's working
            List<Map<String, Object>> jsonList = new ArrayList<>();
            for (int i = 0; i < purchaseMasters.size(); i++) {
                PurchaseMasterDto dto = purchaseMasters.get(i);

                // Manually build the map with PascalCase field names to match stored procedure
                // Using helper methods to ensure safe type conversion and prevent empty string errors
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("boundindex", i + 1);
                itemMap.put("Id", convertToInt(dto.getId()));
                itemMap.put("CompanyRefId", convertToInt(dto.getCompanyRefId(), companyId));
                itemMap.put("UserRefId", convertToInt(dto.getUserRefId()));  // 0 if null
                itemMap.put("EmployeeRefId", convertToInt(dto.getEmployeeRefId()));  // 0 if null
                itemMap.put("SupplierRefId", convertToInt(dto.getSupplierRefId()));  // 0 if null
                itemMap.put("SaleDate", convertToString(dto.getSaleDate()));
                itemMap.put("SaleType", convertToString(dto.getSaleType()));
                itemMap.put("GrossAmount", convertToDouble(dto.getGrossAmount()));
                itemMap.put("TaxAmount", convertToDouble(dto.getTaxAmount()));
                itemMap.put("DiscountAmount", convertToDouble(dto.getDiscountAmount()));
                itemMap.put("Remarks", convertToString(dto.getRemarks()));
                itemMap.put("PlusAmount", convertToDouble(dto.getPlusAmount()));
                itemMap.put("MinusAmount", convertToDouble(dto.getMinusAmount()));
                itemMap.put("Coinage", convertToDouble(dto.getCoinage()));
                itemMap.put("Amount", convertToDouble(dto.getAmount()));
                itemMap.put("InvoiceNo", convertToString(dto.getInvoiceNo()));
                itemMap.put("InvoiceDate", convertToString(dto.getInvoiceDate()));
                // ✅ FIXED: Correct PascalCase field names with proper capitalization
                // Changed from "TruckRefid" to "TruckRefId", "DriverRefid" to "DriverRefId", etc.
                itemMap.put("TruckRefId", convertToInt(dto.getTruckRefId()));  // 0 if null
                itemMap.put("DriverRefId", convertToInt(dto.getDriverRefId()));  // 0 if null
                itemMap.put("PaymentTermsRefId", convertToInt(dto.getPaymentTermsRefId()));  // 0 if null
                itemMap.put("Description", convertToString(dto.getDescription()));
                itemMap.put("CNumberDisplay", convertToString(dto.getCNumberDisplay()));
                itemMap.put("CNumber", convertToInt(dto.getCNumber()));
                itemMap.put("CurrencyValue", convertToDouble(dto.getCurrencyValue()));
                itemMap.put("ActualAmount", convertToDouble(dto.getActualAmount()));
                itemMap.put("SerialNo", convertToString(dto.getSerialNo()));
                itemMap.put("PurchaseOrderMasterRefId", convertToInt(dto.getPurchaseOrderMasterRefId()));  // 0 if null
                itemMap.put("PurchaseDetails", buildPurchaseDetailsJson(dto.getPurchaseDetails()));

                jsonList.add(itemMap);
            }

            // Serialize the list to JSON
            String jsonDetails = objectMapper.writeValueAsString(jsonList);

            // Clean up the JSON - ONLY remove single quotes
            // DO NOT replace null with empty string - the SP handles null values properly
            // Numeric fields are already normalized to 0 in normalizeNumericFields()
            // Date fields and other nullable fields should remain as null (not empty string)
            jsonDetails = jsonDetails.replace("'", "");

            // Convert literal "null" string values to null (for legacy data)
            jsonDetails = jsonDetails.replace("\"null\"", "null");

            logger.debug("Prepared JSON details for stored procedure: {}", jsonDetails);

            // ✅ FIXED: Use raw JDBC implementation to avoid Hibernate type inference issues
            // Execute the stored procedure with custom JDBC handler
            List<Object[]> results = purchaseMasterCustomRepository.executeInsertPurchaseMaster(jsonDetails, companyId);

            // Process the result (SP returns: Result, msg, BillNo, SaleTime, id)
            if (results != null && !results.isEmpty()) {
                Object[] result = results.get(0);
                logger.debug("Stored procedure result array length: {}", result.length);

                // Parse result fields: Result, msg, BillNo, SaleTime, id
                // ✅ Use safe type conversion methods to avoid Hibernate character type mapping issues
                Integer resultCode = safeToInteger(result.length > 0 ? result[0] : null);
                String msg = safeToString(result.length > 1 ? result[1] : null);
                String billNo = safeToString(result.length > 2 ? result[2] : null);
                Integer id = safeToInteger(result.length > 4 ? result[4] : null);

                // Success case: Result = 1
                if (resultCode == 1) {
                    logger.info("PurchaseMaster inserted successfully with ID: {}, BillNo: {}", id, billNo);
                    return InsertPurchaseMasterResponseDto.builder()
                            .ok(true)
                            .message("PurchaseMasterCreateSuccess")
                            .name(billNo)  // BillNo is returned as the account name
                            .id(id)
                            .build();
                } else {
                    logger.warn("PurchaseMaster insertion failed: resultCode={}, msg={}", resultCode, msg);
                    return InsertPurchaseMasterResponseDto.builder()
                            .ok(false)
                            .message(msg != null && !msg.isEmpty() ? msg : "PurchaseMaster insertion failed")
                            .build();
                }
            } else {
                logger.warn("No result returned from stored procedure");
                return InsertPurchaseMasterResponseDto.builder()
                        .ok(false)
                        .message("No result returned from stored procedure")
                        .build();
            }

        } catch (JsonProcessingException e) {
            logger.error("Error serializing PurchaseMaster DTOs to JSON", e);
            return InsertPurchaseMasterResponseDto.builder()
                    .ok(false)
                    .message("Error processing purchase master data: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("Error executing InsertPurchaseMaster stored procedure", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";

            return InsertPurchaseMasterResponseDto.builder()
                    .ok(false)
                    .message("Error inserting purchase master records: " + errorMessage)
                    .build();
        }
    }

    /**
     * Helper method to build PurchaseDetails JSON array
     * Converts PurchaseDetailsDto list to JSON format expected by stored procedure
     */
     /**
      * Build purchase details as a List of Maps (NOT as a JSON string)
      * ✅ Returns: List<Map> which will be serialized once by outer mapper
      * ✅ FIXED: Converts camelCase field names to PascalCase for stored procedure compatibility
      * ❌ WRONG: Returning JSON string causes double encoding
      * @param purchaseDetails List of detail DTOs
      * @return List of maps ready for JSON serialization
      */
     private List<Map<String, Object>> buildPurchaseDetailsJson(List<?> purchaseDetails) {
         try {
             if (purchaseDetails == null || purchaseDetails.isEmpty()) {
                 return new ArrayList<>();  // ✅ Return empty list, not "[]" string
             }

             List<Map<String, Object>> detailsList = new ArrayList<>();
             for (Object detail : purchaseDetails) {
                 Map<String, Object> detailMap = objectMapper.convertValue(detail, new HashMap<String, Object>().getClass());

                 // Normalize numeric fields to 0 instead of null (matching .NET behavior)
                 // These fields are: Id, SDId, PurchaseMasterRefId, ProductMasterRefId, etc.
                 normalizeNumericFields(detailMap);

                 // ✅ FIXED: Convert camelCase field names to PascalCase for stored procedure
                 // The stored procedure expects exact PascalCase names
                 detailMap = convertFieldNamesToPascalCase(detailMap);

                 detailsList.add(detailMap);
             }

             // ✅ IMPORTANT: Return the List directly, NOT as JSON string
             // The outer objectMapper.writeValueAsString() will handle serialization
             // This prevents double encoding that causes "PurchaseDetails": "[{...}]" (wrong)
             // Instead of "PurchaseDetails": [{...}] (correct)
             return detailsList;
         } catch (Exception e) {
             logger.error("Error building purchase details list", e);
             return new ArrayList<>();  // ✅ Return empty list on error
         }
     }

     /**
      * Convert camelCase field names to PascalCase
      * ✅ FIXED: Ensures field names match what the stored procedure expects
      * Examples: sdId → SDId, purchaseRate → PurchaseRate, itemQty → ItemQty
      */
     private Map<String, Object> convertFieldNamesToPascalCase(Map<String, Object> detailMap) {
         Map<String, Object> pascalCaseMap = new HashMap<>();

         // Map of camelCase to PascalCase field names
         Map<String, String> fieldNameMapping = new HashMap<>();
         fieldNameMapping.put("id", "Id");
         fieldNameMapping.put("sdId", "SDId");  // ✅ Special case: SDId not SdId
         fieldNameMapping.put("purchaseMasterRefId", "PurchaseMasterRefId");
         fieldNameMapping.put("productMasterRefId", "ProductMasterRefId");
         fieldNameMapping.put("mrp", "MRP");  // ✅ Special case: MRP all caps
         fieldNameMapping.put("purchaseRate", "PurchaseRate");
         fieldNameMapping.put("itemQty", "ItemQty");
         fieldNameMapping.put("discPer", "DiscPer");
         fieldNameMapping.put("discAmount", "DiscAmount");
         fieldNameMapping.put("landingCost", "LandingCost");
         fieldNameMapping.put("taxPercent", "TaxPercent");
         fieldNameMapping.put("taxAmount", "TaxAmount");
         fieldNameMapping.put("salesRate", "SalesRate");
         fieldNameMapping.put("netSalesRate", "NetSalesRate");
         fieldNameMapping.put("amount", "Amount");
         fieldNameMapping.put("currencyValue", "CurrencyValue");
         fieldNameMapping.put("actualAmount", "ActualAmount");
         fieldNameMapping.put("remarksD", "RemarksD");
         fieldNameMapping.put("productCode", "ProductCode");
         fieldNameMapping.put("productName", "ProductName");
         fieldNameMapping.put("uom", "UOM");  // ✅ Special case: UOM all caps
         fieldNameMapping.put("createdDate", "CreatedDate");
         fieldNameMapping.put("modifiedDate", "ModifiedDate");

         // Apply mapping
         for (Map.Entry<String, Object> entry : detailMap.entrySet()) {
             String key = entry.getKey();
             String pascalCaseKey = fieldNameMapping.getOrDefault(key, key);
             pascalCaseMap.put(pascalCaseKey, entry.getValue());
         }

         return pascalCaseMap;
     }

     /**
      * Ensures numeric fields match the .NET stored procedure expectations
      * Fields converted: Id, SDId, PurchaseMasterRefId, ProductMasterRefId, etc.
      * ✅ FIXED: ProductMasterRefId now properly handles null by converting to 0
      */
     private void normalizeNumericFields(Map<String, Object> detailMap) {
         // List of numeric field names that should be 0 if null
         // ✅ IMPORTANT: ProductMasterRefId is now Integer (nullable), so it needs special handling
         // If it's null, convert to 0 to prevent "Cannot insert NULL" database error
         String[] numericFields = {
             "id", "sdId", "purchaseMasterRefId", "productMasterRefId",
             "mrp", "purchaseRate", "itemQty", "discPer", "discAmount", "landingCost",
             "taxPercent", "taxAmount", "salesRate", "netSalesRate", "amount",
             "currencyValue", "actualAmount"
         };

         for (String fieldName : numericFields) {
             if (detailMap.containsKey(fieldName)) {
                 Object value = detailMap.get(fieldName);
                 // Convert null or empty values to 0
                 if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                     detailMap.put(fieldName, 0);
                 }
                 // Also handle string representations of null
                 else if (value instanceof String && "null".equalsIgnoreCase(((String) value).trim())) {
                     detailMap.put(fieldName, 0);
                 }
             }
         }
     }

    @Override
    public SelectSparePartsViewResponseDto selectSparePartsView(SelectSparePartsViewRequestDto request) {
        logger.info("Fetching spare parts report view for company: {} with filters: supplier={}, employee={}, driver={}, truck={}, product={}, search={}", 
                   request.getCompanyId(), request.getSupplierId(), request.getEmployeeId(), 
                   request.getDriverId(), request.getTruckId(), request.getProductId(), 
                   request.getSearch());

        try {
            // Input validation
            if (request.getCompanyId() == null || request.getCompanyId() <= 0) {
                throw new InvalidRequestException("Company ID must be positive");
            }

            // Set default values for optional filter parameters
            Integer supplierId = request.getSupplierId() != null ? request.getSupplierId() : 0;
            Integer employeeId = request.getEmployeeId() != null ? request.getEmployeeId() : 0;
            Integer driverId = request.getDriverId() != null ? request.getDriverId() : 0;
            Integer truckId = request.getTruckId() != null ? request.getTruckId() : 0;
            Integer productId = request.getProductId() != null ? request.getProductId() : 0;
            String search = request.getSearch() != null ? request.getSearch().trim() : "";
            Integer invoiceCheck = request.getInvoiceCheck() != null ? request.getInvoiceCheck() : 0;
            String fromDate = request.getFromDate() != null ? request.getFromDate() : "";
            String toDate = request.getToDate() != null ? request.getToDate() : "";

            // Execute the query
            List<Object[]> results = purchaseMasterRepository.selectSparePartsView(
                    request.getCompanyId(),
                    supplierId,
                    employeeId,
                    driverId,
                    truckId,
                    productId,
                    search,
                    invoiceCheck,
                    fromDate,
                    toDate
            );

            logger.info("Retrieved {} spare parts report records", results.size());

            // Map the Object[] results to SparePartsReportViewDto objects
            List<SparePartsReportViewDto> reportData = results.stream()
                    .map(this::mapToSparePartsReportViewDto)
                    .collect(Collectors.toList());

            // Return successful response
            return SelectSparePartsViewResponseDto.builder()
                    .ok(true)
                    .message("Success")
                    .data(reportData)
                    .build();

        } catch (InvalidRequestException e) {
            logger.error("Invalid request for SelectSparePartsView: {}", e.getMessage());
            return SelectSparePartsViewResponseDto.builder()
                    .ok(false)
                    .message(e.getMessage())
                    .data(List.of())
                    .build();
        } catch (Exception e) {
            logger.error("Error fetching spare parts report view", e);
            return SelectSparePartsViewResponseDto.builder()
                    .ok(false)
                    .message("Error retrieving spare parts report: " + e.getMessage())
                    .data(List.of())
                    .build();
        }
    }

    // Deliberately NOT @Transactional: this method catches and wraps its own
    // errors, and inside a transaction a repository failure marks it
    // rollback-only — the caught error is then replaced at commit by an opaque
    // "Transaction silently rolled back" 500. The reads ride the repository's
    // own transactions.
    @Override
    public SelectPurchaseMasterResponseDto selectPurchaseMaster(SelectPurchaseMasterRequestDto request) {
        logger.info("Fetching purchase master records - Company: {}, Supplier: {}, Employee: {}, Driver: {}, Truck: {}, Product: {}",
                request.getCompanyId(), request.getSupplierId(), request.getEmployeeId(),
                request.getDriverId(), request.getTruckId(), request.getProductId());

        try {
            // Validate mandatory parameters
            if (request.getCompanyId() == null || request.getCompanyId() <= 0) {
                logger.warn("Invalid company ID: {}", request.getCompanyId());
                return SelectPurchaseMasterResponseDto.builder()
                        .ok(false)
                        .message("Company ID must be a positive number")
                        .data(List.of())
                        .build();
            }

            // Normalize filter parameters (null becomes 0, null strings become empty)
            Integer supplierId = request.getSupplierId() != null ? request.getSupplierId() : 0;
            Integer employeeId = request.getEmployeeId() != null ? request.getEmployeeId() : 0;
            Integer driverId = request.getDriverId() != null ? request.getDriverId() : 0;
            Integer truckId = request.getTruckId() != null ? request.getTruckId() : 0;
            Integer productId = request.getProductId() != null ? request.getProductId() : 0;
            String search = request.getSearch() != null ? request.getSearch().trim() : "";
            Integer invoiceCheck = request.getInvoiceCheck() != null ? request.getInvoiceCheck() : 0;
            String fromDate = request.getFromDate() != null ? request.getFromDate().trim() : "";
            String toDate = request.getToDate() != null ? request.getToDate().trim() : "";

            logger.debug("Normalized parameters - Search: '{}', InvoiceCheck: {}, FromDate: '{}', ToDate: '{}'",
                    search, invoiceCheck, fromDate, toDate);

            // Query master records
            List<Object[]> masterRecords = purchaseMasterRepository.selectPurchaseMaster(
                    request.getCompanyId(), supplierId, employeeId, driverId,
                    truckId, productId, search, invoiceCheck, fromDate, toDate
            );

            // Query detail records (simplified with just company and optional filters)
            List<Object[]> detailRecords = purchaseMasterRepository.selectPurchaseDetails(
                    request.getCompanyId(), supplierId, productId
            );

            // Log query results
            logger.info("Query returned {} master records and {} detail records",
                    masterRecords.size(), detailRecords.size());

            // Map results to DTOs
            List<PurchaseMasterHistoryDto> masterList = masterRecords.stream()
                    .map(this::mapToPurchaseMasterHistoryDto)
                    .collect(Collectors.toList());

            List<PurchaseDetailsHistoryDto> detailList = detailRecords.stream()
                    .map(this::mapToPurchaseDetailsHistoryDto)
                    .collect(Collectors.toList());

            // Create combined view
            SelectPurchaseMasterViewDto viewDto = SelectPurchaseMasterViewDto.builder()
                    .purchaseMaster(masterList)
                    .purchaseDetails(detailList)
                    .build();

            logger.info("Successfully mapped {} master and {} detail records to DTOs",
                    masterList.size(), detailList.size());

            return SelectPurchaseMasterResponseDto.builder()
                    .ok(true)
                    .message("Purchase Master records retrieved successfully")
                    .data(List.of(viewDto))
                    .build();

        } catch (Exception e) {
            logger.error("Error retrieving purchase master records for company: {}", request.getCompanyId(), e);
            return SelectPurchaseMasterResponseDto.builder()
                    .ok(false)
                    .message("Error retrieving purchase master records: " + e.getMessage())
                    .data(List.of())
                    .build();
        }
    }

    /**
     * Maps an Object[] array from selectPurchaseMaster master query to PurchaseMasterHistoryDto
     * Object array columns: Id, EmployeeName, BillDate, InvoiceNo, InvoiceDate, BillNoDisplay,
     * BillTime, SupplierName, NetAmt, SaleType, BillNo, TruckName, DriverName
     */
    private PurchaseMasterHistoryDto mapToPurchaseMasterHistoryDto(Object[] row) {
        return PurchaseMasterHistoryDto.builder()
                .id(row[0] != null ? ((Number) row[0]).intValue() : null)
                .employeeName((String) row[1])
                .billDate((String) row[2])
                .invoiceNo((String) row[3])
                .invoiceDate((String) row[4])
                .billNoDisplay((String) row[5])
                .billTime((String) row[6])
                .supplierName((String) row[7])
                .netAmount(row[8] != null ? ((Number) row[8]).doubleValue() : null)
                .saleType((String) row[9])
                .billNo(row[10] != null ? ((Number) row[10]).intValue() : null)
                .truckName((String) row[11])
                .driverName((String) row[12])
                .build();
    }

    /**
     * Maps an Object[] array from selectPurchaseDetails query to PurchaseDetailsHistoryDto
     * Object array columns: DiscountAmt, DiscountPercent, ItemQty, MRP, ProductName,
     * SaleRate, PurchaseMasterRefId, TaxAmt, TaxPercent, ProductCode, SAmount, RemarksD
     */
    private PurchaseDetailsHistoryDto mapToPurchaseDetailsHistoryDto(Object[] row) {
        // PurchaseMasterRefId is at index 6 (PM.PurchaseMasterRefId as SaleRefId)
        Integer purchaseMasterId = row[6] != null ? ((Number) row[6]).intValue() : null;
        
        return PurchaseDetailsHistoryDto.builder()
                .discountAmount(row[0] != null ? ((Number) row[0]).doubleValue() : null)
                .discountPercent(row[1] != null ? ((Number) row[1]).doubleValue() : null)
                .itemQty(row[2] != null ? ((Number) row[2]).doubleValue() : null)
                .mrp(row[3] != null ? ((Number) row[3]).doubleValue() : null)
                .productName((String) row[4])
                .saleRate(row[5] != null ? ((Number) row[5]).doubleValue() : null)
                .saleRefId(purchaseMasterId)
                .purchaseMasterRefId(purchaseMasterId)  // Set both fields to same value
                .taxAmount(row[7] != null ? ((Number) row[7]).doubleValue() : null)
                .taxPercent(row[8] != null ? ((Number) row[8]).doubleValue() : null)
                .productCode((String) row[9])
                .amount(row[10] != null ? ((Number) row[10]).doubleValue() : null)
                .remarksD((String) row[11])
                .build();
    }

    /**
     * Maps an Object[] array from the native query result to SparePartsReportViewDto
     * Object array columns: Id, EmployeeName, BillDate, InvoiceNo, InvoiceDate, BillNoDisplay, 
     * BillTime, SupplierName, NetAmt, SaleType, BillNo, TruckName, DriverName, ItemQty, 
     * SalesRate, Amount, RemarksD, ProductCode, ProductName, SerialNo
     */
    private SparePartsReportViewDto mapToSparePartsReportViewDto(Object[] row) {
        return SparePartsReportViewDto.builder()
                .id(row[0] != null ? ((Number) row[0]).intValue() : null)
                .employeeName((String) row[1])
                .billDate((String) row[2])
                .invoiceNo((String) row[3])
                .invoiceDate((String) row[4])
                .billNoDisplay((String) row[5])
                .billTime((String) row[6])
                .supplierName((String) row[7])
                .netAmt(row[8] != null ? ((Number) row[8]).doubleValue() : null)
                .saleType((String) row[9])
                // row[10] is BillNo (not used in DTO but present in result set)
                .truckName((String) row[11])
                .driverName((String) row[12])
                .itemQty(row[13] != null ? ((Number) row[13]).intValue() : null)
                .salesRate(row[14] != null ? ((Number) row[14]).doubleValue() : null)
                .amount(row[15] != null ? ((Number) row[15]).doubleValue() : null)
                .remarksD((String) row[16])
                .productCode((String) row[17])
                .productName((String) row[18])
                .serialNo((String) row[19])
                .build();
    }

    // NOT @Transactional for the same reason as selectPurchaseMaster: the
    // catch wraps the error, which a transaction would replace with an opaque
    // rollback 500.
    @Override
    public EditPurchaseMasterResponseDto editPurchaseMaster(EditPurchaseMasterRequestDto request) {
        logger.info("Fetching purchase master for edit - Company: {}, ID: {}, PurchaseMasterNo: {}",
                request.getCompanyId(), request.getId(), request.getPurchaseMasterNo());

        try {
            // Validate mandatory parameters
            if (request.getCompanyId() == null || request.getCompanyId() <= 0) {
                logger.warn("Invalid company ID: {}", request.getCompanyId());
                return EditPurchaseMasterResponseDto.builder()
                        .ok(false)
                        .message("Company ID must be a positive number")
                        .data(List.of())
                        .build();
            }

            // Resolve ID if purchaseMasterNo is provided
            Integer resolvedId = request.getId();
            if ((resolvedId == null || resolvedId == 0) && request.getPurchaseMasterNo() != null && request.getPurchaseMasterNo() != 0) {
                logger.debug("Resolving ID from PurchaseMasterNo: {}", request.getPurchaseMasterNo());
                resolvedId = purchaseMasterRepository.findIdByCNumberAndCompany(
                        request.getCompanyId(), 
                        request.getPurchaseMasterNo()
                );
                
                if (resolvedId == null || resolvedId == 0) {
                    logger.warn("Invalid Purchase Master No: {}", request.getPurchaseMasterNo());
                    return EditPurchaseMasterResponseDto.builder()
                            .ok(false)
                            .message("Invalid PurchaseMaster No !!!") 
                            .data(List.of())
                            .build();
                }
            }

            // Validate resolved ID
            if (resolvedId == null || resolvedId <= 0) {
                logger.warn("No valid Purchase Master ID provided");
                return EditPurchaseMasterResponseDto.builder()
                        .ok(false)
                        .message("Purchase Master ID is required")
                        .data(List.of())
                        .build();
            }

            // Query master with all details
            List<Object[]> resultSet = purchaseMasterRepository.findEditPurchaseMaster(
                    resolvedId, 
                    request.getCompanyId()
            );

            // Check if results found
            if (resultSet == null || resultSet.isEmpty()) {
                logger.warn("No purchase master found - ID: {}, Company: {}", resolvedId, request.getCompanyId());
                return EditPurchaseMasterResponseDto.builder()
                        .ok(false)
                        .message("Invalid PurchaseMaster No !!! .")
                        .data(List.of())
                        .build();
            }

            // Map results to a single PurchaseMasterModel with details
            PurchaseMasterModel masterModel = mapToEditPurchaseMasterModel(resultSet);

            logger.info("Successfully fetched purchase master for edit - ID: {}, Details Count: {}",
                    masterModel.getId(), masterModel.getPurchaseDetails().size());

            return EditPurchaseMasterResponseDto.builder()
                    .ok(true)
                    .message("Purchase Master retrieved successfully")
                    .data(List.of(masterModel))
                    .build();

        } catch (Exception e) {
            logger.error("Error retrieving purchase master for edit - Company: {}, ID: {}",
                    request.getCompanyId(), request.getId(), e);
            return EditPurchaseMasterResponseDto.builder()
                    .ok(false)
                    .message("Error retrieving purchase master: " + e.getMessage())
                    .data(List.of())
                    .build();
        }
    }

    /**
     * Maps result set from findEditPurchaseMaster query to PurchaseMasterModel with all details
     * Object array columns: 
     * Master: 0-28 (Id through DriverRefid)
     * Detail: 29+ (SDId, ProductMasterRefId, MRP, etc.)
     * 
     * Handles multiple detail rows for the same master record
     */
    private PurchaseMasterModel mapToEditPurchaseMasterModel(List<Object[]> resultSet) {
        if (resultSet.isEmpty()) {
            return null;
        }

        // Use first row for master data (all rows have same master info)
        Object[] firstRow = resultSet.get(0);
        
        PurchaseMasterModel masterModel = PurchaseMasterModel.builder()
                .id(firstRow[0] != null ? ((Number) firstRow[0]).intValue() : null)
                .companyRefId(firstRow[1] != null ? ((Number) firstRow[1]).intValue() : null)
                .userRefId(firstRow[2] != null ? ((Number) firstRow[2]).intValue() : null)
                .employeeRefId(firstRow[3] != null ? ((Number) firstRow[3]).intValue() : null)
                .invoiceNo((String) firstRow[4])
                .invoiceDate(convertToLocalDateTime((java.sql.Timestamp) firstRow[5]))
                .sInvoiceDate(firstRow[5] != null ? ((java.sql.Timestamp) firstRow[5]).toLocalDateTime().toString() : null)
                .supplierRefId(firstRow[7] != null ? ((Number) firstRow[7]).intValue() : null)
                .saleDate(convertToLocalDateTime((java.sql.Timestamp) firstRow[8]))
                .sSaleDate(firstRow[8] != null ? ((java.sql.Timestamp) firstRow[8]).toLocalDateTime().toString() : null)
                .saleType((String) firstRow[10])
                .cNumberDisplay((String) firstRow[11])
                .cNumber(firstRow[12] != null ? ((Number) firstRow[12]).intValue() : null)
                .coinage(firstRow[13] != null ? ((Number) firstRow[13]).floatValue() : null)
                .grossAmount(firstRow[14] != null ? ((Number) firstRow[14]).floatValue() : null)
                .taxAmount(firstRow[15] != null ? ((Number) firstRow[15]).floatValue() : null)
                .discountAmount(firstRow[16] != null ? ((Number) firstRow[16]).floatValue() : null)
                .plusAmount(firstRow[17] != null ? ((Number) firstRow[17]).floatValue() : null)
                .minusAmount(firstRow[18] != null ? ((Number) firstRow[18]).floatValue() : null)
                .amount(firstRow[19] != null ? ((Number) firstRow[19]).floatValue() : null)
                .remarks((String) firstRow[20])
                .active(firstRow[21] != null ? ((Number) firstRow[21]).intValue() : null)
                .createdDate(convertToLocalDateTime((java.sql.Timestamp) firstRow[22]))
                .createdBy((String) firstRow[23])
                .modifiedDate(convertToLocalDateTime((java.sql.Timestamp) firstRow[24]))
                .modifiedBy((String) firstRow[25])
                .description((String) firstRow[26])
                .paymentTermsRefId(firstRow[27] != null ? ((Number) firstRow[27]).intValue() : null)
                .serialNo((String) firstRow[28])
                .truckRefId(firstRow[29] != null ? ((Number) firstRow[29]).intValue() : null)
                .driverRefId(firstRow[30] != null ? ((Number) firstRow[30]).intValue() : null)
                .build();

        // Map all detail rows
        List<PurchaseDetailsModel> detailsList = resultSet.stream()
                .map(row -> mapToEditPurchaseDetailsModel(row))
                .collect(Collectors.toList());

        masterModel.setPurchaseDetails(detailsList);
        return masterModel;
    }

    /**
     * Maps a single row from the detail query to PurchaseDetailsModel
     * Object array columns (starting from detail index 31):
     * SDId, ProductMasterRefId, MRP, PurchaseRate, ItemQty, DiscPer, DiscAmount, 
     * LandingCost, TaxPercent, TaxAmount, SalesRate, NetSalesRate, Amount, RemarksD,
     * ProductCode, ProductName, UOM
     */
    private PurchaseDetailsModel mapToEditPurchaseDetailsModel(Object[] row) {
        return PurchaseDetailsModel.builder()
                .id(row[31] != null ? ((Number) row[31]).intValue() : null)
                .sdId(row[32] != null ? ((Number) row[32]).intValue() : null)
                .purchaseMasterRefId(row[33] != null ? ((Number) row[33]).intValue() : null)
                .productMasterRefId(row[34] != null ? ((Number) row[34]).intValue() : null)
                .mrp(row[35] != null ? ((Number) row[35]).floatValue() : null)
                .purchaseRate(row[36] != null ? ((Number) row[36]).floatValue() : null)
                .itemQty(row[37] != null ? ((Number) row[37]).floatValue() : null)
                .discPer(row[38] != null ? ((Number) row[38]).floatValue() : 0.0f)
                .discAmount(row[39] != null ? ((Number) row[39]).floatValue() : 0.0f)
                .landingCost(row[40] != null ? ((Number) row[40]).floatValue() : null)
                .taxPercent(row[41] != null ? ((Number) row[41]).floatValue() : null)
                .taxAmount(row[42] != null ? ((Number) row[42]).floatValue() : null)
                .salesRate(row[43] != null ? ((Number) row[43]).floatValue() : null)
                .netSalesRate(row[44] != null ? ((Number) row[44]).floatValue() : 0.0f)
                .amount(row[45] != null ? ((Number) row[45]).floatValue() : null)
                .currencyValue(row[46] != null ? ((Number) row[46]).floatValue() : null)
                .actualAmount(row[47] != null ? ((Number) row[47]).floatValue() : null)
                .remarksD((String) row[48])
                .productCode((String) row[49])
                .productName((String) row[50])
                .uom((String) row[51])
                .build();
    }

    /**
     * Safe conversion from Object to Integer
     * ✅ Handles: null → 0, empty string → 0, Number types → intValue, String → parseInt
     * ✅ Prevents: Hibernate character type mapping errors
     * @param value The value to convert (can be null, Integer, Number, String, etc.)
     * @return 0 if null/empty/error, otherwise the integer value
     */
    private Integer safeToInteger(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.isEmpty()) {
                return 0;
            }
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                logger.warn("Failed to convert string '{}' to integer, using 0", str);
                return 0;
            }
        }
        return 0;
    }

    /**
     * Safe conversion from Object to String
     * ✅ Handles: null → "", empty string → "", any object → toString()
     * ✅ Prevents: NullPointerException and character type mapping errors
     * @param value The value to convert
     * @return Empty string if null, otherwise the string value
     */
    private String safeToString(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString().trim();
    }

    /**
     * Helper method to safely convert java.sql.Timestamp to LocalDate
     */
    private LocalDate convertToLocalDate(java.sql.Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime().toLocalDate() : null;
    }

    /**
     * Helper method to safely convert java.sql.Timestamp to LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(java.sql.Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    /**
     * Helper method to safely convert any value to Integer
     * ✅ Handles: null → 0, empty string "" → 0, valid integers → passthrough
     * ✅ Prevents: "value does not contain a character: ''" error from Hibernate
     * @param value The value to convert (can be null, Integer, String, or Number)
     * @return 0 if null/empty, otherwise the integer value
     */
    private Integer convertToInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.isEmpty()) {
                return 0;  // ✅ Convert empty string to 0, not null
            }
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                logger.warn("Failed to convert string '{}' to integer, using 0", str);
                return 0;
            }
        }
        return 0;
    }

    /**
     * Helper method to safely convert any value to Integer with default
     * ✅ Same as convertToInt() but returns provided default if null
     * @param value The value to convert
     * @param defaultValue The default value if value is null
     * @return The converted integer or defaultValue if null
     */
    private Integer convertToInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue != null ? defaultValue : 0;
        }
        return convertToInt(value);
    }

    /**
     * Helper method to safely convert any value to Double
     * ✅ Handles: null → 0.0, empty string "" → 0.0, valid doubles → passthrough
     * @param value The value to convert (can be null, Number, String, etc.)
     * @return 0.0 if null/empty, otherwise the double value
     */
    private Double convertToDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.isEmpty()) {
                return 0.0;
            }
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                logger.warn("Failed to convert string '{}' to double, using 0.0", str);
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * Helper method to safely convert any value to String
     * ✅ Handles: null → "", LocalDate → yyyy-MM-dd, LocalDateTime → yyyy-MM-dd, other objects → toString()
     * ✅ Prevents: NullPointerException and type conversion errors
     * @param value The value to convert
     * @return Empty string if null, otherwise the string value
     */
    private String convertToString(Object value) {
        if (value == null) {
            return "";  // ✅ Use empty string, not null, for SQL Server
        }
        
        // ✅ Handle LocalDate - convert to yyyy-MM-dd format
        if (value instanceof LocalDate) {
            return ((LocalDate) value).toString();
        }
        
        // ✅ Handle LocalDateTime - extract date part (yyyy-MM-dd)
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate().toString();
        }
        
        String str = value.toString().trim();
        if (str.equalsIgnoreCase("null")) {
            return "";  // ✅ Convert literal "null" strings to empty
        }
        return str;
    }
}
