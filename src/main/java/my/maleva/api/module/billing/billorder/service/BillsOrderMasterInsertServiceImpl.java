package my.maleva.api.module.billing.billorder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterInsertDto;
import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterResponseDto;
import my.maleva.api.module.billing.billorder.dto.BillsOrderDetailsInsertDto;
import my.maleva.api.module.billing.billorder.repository.BillsOrderMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service implementation for BillsOrderMaster insert/update operations
 * Equivalent to .NET ISupplierServices.InsertBillsOrderMaster method
 *
 * Responsibilities:
 * 1. Validate BillsOrderDetails (all items must have AccountMasterRefId)
 * 2. Update SaleOrderMaster flags based on charge description type
 * 3. Serialize DTO to JSON for SP consumption
 * 4. Execute stored procedure (SP_BillsOrderMaster)
 * 5. Send WhatsApp notifications on successful new inserts
 */
@Service
public class BillsOrderMasterInsertServiceImpl implements IBillsOrderMasterInsertService {

    private static final Logger logger = LoggerFactory.getLogger(BillsOrderMasterInsertServiceImpl.class);

    private final BillsOrderMasterRepository billsOrderMasterRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final BillsOrderWhatsAppService whatsAppService;

    @Value("${app.whatsapp.enabled:true}")
    private boolean whatsAppEnabled;

    public BillsOrderMasterInsertServiceImpl(
            BillsOrderMasterRepository billsOrderMasterRepository,
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            BillsOrderWhatsAppService whatsAppService) {
        this.billsOrderMasterRepository = billsOrderMasterRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.whatsAppService = whatsAppService;
    }

    /**
     * {@inheritDoc}
     *
     * Process flow:
     * 1. Validate all bill order details have AccountMasterRefId set
     * 2. Update related SaleOrderMaster records based on charge type
     * 3. Convert DTO to properly formatted JSON
     * 4. Call stored procedure to persist data
     * 5. Send WhatsApp notification if new record created
     */
    @Override
    @Transactional
    public BillsOrderMasterResponseDto insertBillsOrderMaster(
            BillsOrderMasterInsertDto billsOrderMasterDto,
            Integer companyId) {

        logger.info("Starting BillsOrderMaster insert for Company: {}", companyId);

        try {
            // Step 1: Validate all details have AccountMasterRefId
            validateBillsOrderDetails(billsOrderMasterDto);
            logger.debug("✓ Validation passed - all items have AccountMasterRefId");

            // Step 2: Update SaleOrderMaster flags based on description
            // Only for new records (id = 0) to avoid redundant updates
            boolean isNewRecord = billsOrderMasterDto.getId() == null || billsOrderMasterDto.getId() == 0;
            if (isNewRecord) {
                updateSaleOrderMasterFlags(billsOrderMasterDto);
                logger.debug("✓ SaleOrderMaster flags updated");
            }

            // Step 3: Serialize to JSON with boundindex for SP row numbering
            String jsonData = wrapDtoWithBoundIndex(billsOrderMasterDto);
            logger.debug("✓ DTO serialized to JSON with boundindex");

            // Step 3.1: Clean JSON like .NET code does
            // .NET: details.Replace("\"null\"", "\"\"").Replace("null", "\"\"").Replace("'", "")
            jsonData = jsonData.replace("\"null\"", "\"\"").replace("null", "\"\"").replace("'", "");
            logger.debug("JSON after null cleanup: {}", jsonData);

            // Step 4: Execute stored procedure
            logger.debug("Executing SP_BillsOrderMaster...");
            BillsOrderMasterResponseDto response = callStoredProcedure(jsonData, companyId);

            // Step 5: Handle response and send notifications
            // Always log the response details for debugging
            logger.info("SP Response - result: {}, msg: {}, billNo: {}, id: {}",
                response.getResult(), response.getMessage(), response.getBillNo(), response.getId());

            if (response.isSuccess()) {
                logger.info("✓ BillsOrderMaster inserted successfully - ID: {}, BillNo: {}",
                    response.getId(), response.getBillNo());

                // Send WhatsApp notification for new records
                if (isNewRecord && whatsAppEnabled) {
                    try {
                        whatsAppService.sendBillOrderNotification(
                            billsOrderMasterDto,
                            response.getId(),
                            companyId
                        );
                        logger.debug("✓ WhatsApp notification sent");
                    } catch (Exception ex) {
                        logger.error("! Failed to send WhatsApp notification", ex);
                        // Don't fail the entire transaction for notification error
                    }
                }

                return response;
            } else {
                logger.warn("✗ BillsOrderMaster insert failed - result: {}, msg: {}",
                    response.getResult(), response.getMessage());
                return response;
            }
        } catch (IllegalArgumentException ex) {
            logger.error("✗ Validation error: {}", ex.getMessage());
            return BillsOrderMasterResponseDto.builder()
                    .result(0)
                    .message(ex.getMessage())
                    .build();
        } catch (Exception ex) {
            logger.error("✗ Unexpected error in insertBillsOrderMaster", ex);
            return BillsOrderMasterResponseDto.builder()
                    .result(0)
                    .message("Error: " + (ex.getMessage() != null ? ex.getMessage() : "Unknown error"))
                    .build();
        }
    }

    /**
     * Validate that all BillsOrderDetails have AccountMasterRefId set
     * Equivalent to .NET: if (BillsOrderDetails.Where(c => c.AccountMasterRefId == 0).Count() != 0)
     */
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
     * Update SaleOrderMaster flags based on charge description type
     * Equivalent to .NET if-statements checking Description and SaleMasterRefId
     *
     * Maps Description to update SQL queries based on charge type
     */
    @Transactional
    public void updateSaleOrderMasterFlags(BillsOrderMasterInsertDto billsOrderMasterDto) {
        // Validate prerequisites
        if (billsOrderMasterDto.getSaleMasterRefId() == null ||
            billsOrderMasterDto.getSaleMasterRefId() == 0) {
            logger.debug("No SaleMasterRefId - skipping SaleOrderMaster flag update");
            return;
        }

        String description = billsOrderMasterDto.getDescription();
        if (description == null || description.isEmpty()) {
            logger.debug("No description - skipping SaleOrderMaster flag update");
            return;
        }

        Integer saleMasterRefId = billsOrderMasterDto.getSaleMasterRefId();
        String updateQuery = buildFlagUpdateQuery(description.toUpperCase().trim(), saleMasterRefId);

        if (updateQuery != null) {
            try {
                int rowsUpdated = jdbcTemplate.update(updateQuery);
                logger.info("Updated SaleOrderMaster for description '{}': {} rows affected",
                    description, rowsUpdated);
            } catch (Exception ex) {
                logger.error("Error updating SaleOrderMaster flags for description: {}", description, ex);
                // Don't throw - this is a secondary operation
            }
        }
    }

    /**
     * Build the SQL UPDATE query based on charge description type
     * Equivalent to .NET switch-case statements
     */
    private String buildFlagUpdateQuery(String description, Integer saleMasterRefId) {
        String updateQuery = null;

        switch (description) {
            case "PORT CHARGES":
                // .NET code has BOTH updates - PortCPop AND LiveCPop
                updateQuery = "UPDATE SaleOrderMaster SET PortCPop = 2, LiveCPop = 2 WHERE Id = " + saleMasterRefId + " AND (PortCPop = 1 OR LiveCPop = 1)";
                break;
            case "LIVE CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET LiveCPop = 2 WHERE Id = " + saleMasterRefId + " AND LiveCPop = 1";
                break;
            case "CUSTOM CLEARANCE":
            case "CUSTOMER CLEARANCE":
                updateQuery = "UPDATE SaleOrderMaster SET ForwardingCPop = 2 WHERE Id = " + saleMasterRefId + " AND ForwardingCPop = 1";
                break;
            case "BOAT CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET BoatCPop = 2 WHERE Id = " + saleMasterRefId + " AND BoatCPop = 1";
                break;
            case "PERMIT CHARGES":
            case "INWARD PERMIT CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET PermitCPop = 2 WHERE Id = " + saleMasterRefId + " AND PermitCPop = 1";
                break;
            case "MMHE CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET MMHECPop = 2 WHERE Id = " + saleMasterRefId + " AND MMHECPop = 1";
                break;
            case "AIR FREIGHT EXPORT CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET AFpoCPop = 2 WHERE Id = " + saleMasterRefId + " AND AFpoCPop = 1";
                break;
            case "STORAGE FEE":
            case "FREIGHT CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET SFWpoCPop = 2 WHERE Id = " + saleMasterRefId + " AND SFWpoCPop = 1";
                break;
            case "CRANE & WHARFMARK CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET BoatCPop1 = 2 WHERE Id = " + saleMasterRefId + " AND BoatCPop1 = 1";
                break;
            case "PFP & PAC CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET PFPPCPop1 = 2 WHERE Id = " + saleMasterRefId + " AND PFPPCPop1 = 1";
                break;
            default:
                logger.debug("No flag update for description: {}", description);
        }

        return updateQuery;
    }

    /**
     * Call the stored procedure SP_BillsOrderMaster
     * Input Parameters:
     *   @master NVARCHAR(MAX)  - JSON serialized BillsOrderMasterInsertDto with boundindex
     *   @Comid INT             - Company ID
     *
     * Returns result set with columns: Result, msg, BillNo, SaleTime, id
     */
    private BillsOrderMasterResponseDto callStoredProcedure(String jsonData, Integer companyId) {
        logger.info("Calling SP_BillsOrderMaster with JSON: {}", jsonData);
        try {
            return jdbcTemplate.execute(new CallableStatementCreator() {
                @Override
                public CallableStatement createCallableStatement(Connection con) throws SQLException {
                    // SP signature: SP_BillsOrderMaster(@master, @Comid)
                    CallableStatement cs = con.prepareCall("{call SP_BillsOrderMaster(?, ?)}");
                    cs.setString(1, jsonData);    // @master parameter
                    cs.setInt(2, companyId);      // @Comid parameter
                    logger.debug("Executing SP with params - json length: {}, comid: {}", jsonData.length(), companyId);
                    return cs;
                }
            }, cs -> {
                cs.execute();
                var resultSet = cs.getResultSet();

                if (resultSet != null && resultSet.next()) {
                    int result = resultSet.getInt("Result");
                    String msg = resultSet.getString("msg");
                    String billNo = resultSet.getString("BillNo");
                    int id = resultSet.getInt("id");

                    logger.debug("SP Result - result: {}, msg: {}, billNo: {}, id: {}", result, msg, billNo, id);

                    return BillsOrderMasterResponseDto.builder()
                            .result(result)
                            .message(msg)
                            .billNo(billNo)
                            .saleTime(resultSet.getTimestamp("SaleTime") != null ?
                                    resultSet.getTimestamp("SaleTime").toLocalDateTime() :
                                    LocalDateTime.now())
                            .id(id)
                            .build();
                } else {
                    logger.warn("No result set returned from SP");
                    return BillsOrderMasterResponseDto.builder()
                            .result(0)
                            .message("No result returned from stored procedure")
                            .build();
                }
            });
        } catch (Exception ex) {
            logger.error("Error executing SP_BillsOrderMaster", ex);
            return BillsOrderMasterResponseDto.builder()
                    .result(0)
                    .message("Error calling stored procedure: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * Wrap the DTO with boundindex field required by SP OPENJSON parser
     * Converts to JSON array format: [{boundindex: 1, ...dto fields, BillsOrderDetails: [...]}]
     *
     * CRITICAL: Converts field names to PascalCase to match SQL Server OPENJSON schema
     * Example: "cNumber" → "CNumber", "pStatus" → "PStatus", "sInvoiceDate" → "SInvoiceDate"
     *
     * Also normalizes values:
     * - Empty strings "" → null for nullable fields
     * - Empty strings "" → 0 for numeric fields
     *
     * The stored procedure expects an array because OPENJSON processes:
     * SELECT ROW_NUMBER() OVER(ORDER BY SNo) AS tempid ...
     * FROM OPENJSON(@master) WITH (SNo int '$.boundindex', CNumber int '$.CNumber', ...)
     *
     * @param billsOrderMasterDto The DTO to serialize
     * @return JSON array string ready for stored procedure input
     * @throws Exception if serialization fails
     */
    private String wrapDtoWithBoundIndex(BillsOrderMasterInsertDto billsOrderMasterDto) throws Exception {
        try {
            logger.info("Converting BillsOrderMaster DTO to JSON array for SP_BillsOrderMaster");

            // Step 1: Create wrapper object with boundindex
            com.fasterxml.jackson.databind.node.ObjectNode wrapperNode =
                objectMapper.createObjectNode();

            // Step 2: Add boundindex (always 1 for single record insert)
            wrapperNode.put("boundindex", 1);

            // Step 3: Convert DTO to JsonNode first
            com.fasterxml.jackson.databind.JsonNode dtoNode =
                objectMapper.convertValue(billsOrderMasterDto, com.fasterxml.jackson.databind.JsonNode.class);

            // Step 4: Create new object with PascalCase fields and normalized values
            com.fasterxml.jackson.databind.node.ObjectNode normalizedNode =
                objectMapper.createObjectNode();

            // Copy boundindex
            normalizedNode.put("boundindex", 1);

            // Step 5: Map camelCase fields to PascalCase with value normalization
            java.util.Iterator<String> fieldNames = dtoNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                com.fasterxml.jackson.databind.JsonNode fieldValue = dtoNode.get(fieldName);

                // Convert field name to PascalCase (capitalize first letter)
                String pascalCaseField = toPascalCase(fieldName);

                // Normalize value (convert empty strings to null/0)
                com.fasterxml.jackson.databind.JsonNode normalizedValue = normalizeValue(fieldValue, fieldName);

                // Add to normalized node
                normalizedNode.set(pascalCaseField, normalizedValue);
            }

            // Step 6: Create array wrapper
            com.fasterxml.jackson.databind.node.ArrayNode arrayNode =
                objectMapper.createArrayNode();
            arrayNode.add(normalizedNode);

            // Step 7: Serialize and log
            String jsonArray = objectMapper.writeValueAsString(arrayNode);
            logger.info("✓ Successfully converted to JSON array ({} characters)", jsonArray.length());
            logger.debug("✓ Field names converted to PascalCase");
            logger.debug("✓ Empty strings normalized to null/0");
            logger.trace("JSON Array for SP: {}", jsonArray);

            return jsonArray;

        } catch (Exception ex) {
            logger.error("✗ Error wrapping DTO with boundindex: {}", ex.getMessage(), ex);
            throw new Exception("Failed to serialize BillsOrderMaster DTO to JSON array: " + ex.getMessage(), ex);
        }
    }

    /**
     * Convert camelCase field name to PascalCase
     * Examples:
     * - "cNumber" → "CNumber"
     * - "pStatus" → "PStatus"
     * - "sInvoiceDate" → "SInvoiceDate"
     * - "id" → "Id"
     * - "billsOrderDetails" → "BillsOrderDetails"
     */
    private String toPascalCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        // Capitalize first letter
        return Character.toUpperCase(camelCase.charAt(0)) + camelCase.substring(1);
    }

    /**
     * Normalize field values for SQL Server compatibility
     * - Empty strings → null for string fields
     * - Empty strings → 0 for numeric fields
     * - Keep proper values as-is
     */
    private com.fasterxml.jackson.databind.JsonNode normalizeValue(
            com.fasterxml.jackson.databind.JsonNode value, String fieldName) {

        // List of numeric fields that should be 0 if empty
        String[] numericFields = {
            "id", "sdId", "companyRefId", "fileupload", "userRefId", "employeeRefId",
            "supplierRefId", "cNumber", "coinage", "grossAmount", "taxAmount", "discountAmount",
            "plusAmount", "minusAmount", "amount", "active", "truckRefid", "driverRefid",
            "saleMasterRefId", "pStatus", "currencyValue", "actualAmount", "paymentTermsRefid",
            "checkloadingVessel", "checkoffgVessel"
        };

        // Check if value is empty string
        if (value != null && value.isTextual() && value.asText().isEmpty()) {
            // For numeric fields, return 0
            for (String numField : numericFields) {
                if (fieldName.equalsIgnoreCase(numField)) {
                    return objectMapper.valueToTree(0);
                }
            }
            // For string fields, return null
            return objectMapper.getNodeFactory().nullNode();
        }

        // Return value as-is if not empty
        return value;
    }
}

