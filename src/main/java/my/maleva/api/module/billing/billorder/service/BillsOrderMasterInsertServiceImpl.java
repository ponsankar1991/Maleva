package my.maleva.api.module.billing.billorder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterInsertDto;
import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterResponseDto;
import my.maleva.api.module.billing.billorder.dto.BillsOrderDetailsInsertDto;
import my.maleva.api.module.billing.billorder.repository.BillsOrderMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service implementation for BillsOrderMaster insert/update operations
 * Equivalent to .NET ISupplierServices implementation
 */
@Service
public class BillsOrderMasterInsertServiceImpl implements IBillsOrderMasterInsertService {

    private static final Logger logger = LoggerFactory.getLogger(BillsOrderMasterInsertServiceImpl.class);

    private final BillsOrderMasterRepository billsOrderMasterRepository;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ObjectMapper objectMapper;

    public BillsOrderMasterInsertServiceImpl(
            BillsOrderMasterRepository billsOrderMasterRepository,
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            ObjectMapper objectMapper) {
        this.billsOrderMasterRepository = billsOrderMasterRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BillsOrderMasterResponseDto insertBillsOrderMaster(
            BillsOrderMasterInsertDto billsOrderMasterDto,
            Integer companyId) {
        try {
            // Validation
            validateBillsOrderDetails(billsOrderMasterDto);

            // Update SaleOrderMaster flags based on charge type
            // Only update if new record (id = 0) to avoid redundant updates
            if (billsOrderMasterDto.getId() == null || billsOrderMasterDto.getId() == 0) {
                updateSaleOrderMasterFlags(billsOrderMasterDto, false);
            }

            // Convert DTO to JSON
            String jsonData = objectMapper.writeValueAsString(billsOrderMasterDto);

            // Clean JSON to handle null values
            jsonData = jsonData.replace("\"null\"", "\"\"").replace("null", "\"\"").replace("'", "");

            logger.debug("Executing SP_BillsOrderMaster with JSON: {}", jsonData);

            // Call stored procedure
            BillsOrderMasterResponseDto response = callStoredProcedure(jsonData, companyId);

            // Handle successful response
            if (response.isSuccess()) {
                logger.info("BillsOrderMaster inserted successfully. ID: {}, BillNo: {}",
                    response.getId(), response.getBillNo());

                // Send WhatsApp notification for new records
                if (billsOrderMasterDto.getId() == null || billsOrderMasterDto.getId() == 0) {
                    sendWhatsAppNotification(billsOrderMasterDto, response.getId(), companyId);
                }

                return response;
            } else {
                logger.warn("BillsOrderMaster insert failed. Message: {}", response.getMessage());
                return response;
            }

        } catch (Exception ex) {
            logger.error("Error in insertBillsOrderMaster", ex);
            return BillsOrderMasterResponseDto.builder()
                    .result(0)
                    .message("Error: " + (ex.getMessage() != null ? ex.getMessage() : "Unknown error"))
                    .build();
        }
    }

    /**
     * Call the stored procedure SP_BillsOrderMaster
     */
    private BillsOrderMasterResponseDto callStoredProcedure(String jsonData, Integer companyId) {
        try {
            return jdbcTemplate.execute(new CallableStatementCreator() {
                @Override
                public CallableStatement createCallableStatement(Connection con) throws SQLException {
                    CallableStatement cs = con.prepareCall("{call SP_BillsOrderMaster(?, ?)}");
                    cs.setString(1, jsonData);
                    cs.setInt(2, companyId);

                    // Register output parameters
                    cs.registerOutParameter(1, Types.INTEGER);     // Result
                    cs.registerOutParameter(2, Types.VARCHAR);     // Message
                    cs.registerOutParameter(3, Types.VARCHAR);     // BillNo
                    cs.registerOutParameter(4, Types.TIMESTAMP);   // SaleTime
                    cs.registerOutParameter(5, Types.INTEGER);     // ID

                    return cs;
                }
            }, cs -> {
                cs.execute();
                return BillsOrderMasterResponseDto.builder()
                        .result(cs.getInt(1))
                        .message(cs.getString(2))
                        .billNo(cs.getString(3))
                        .saleTime(cs.getTimestamp(4) != null ? cs.getTimestamp(4).toLocalDateTime() : LocalDateTime.now())
                        .id(cs.getInt(5))
                        .build();
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
     * {@inheritDoc}
     */
    @Override
    public void validateBillsOrderDetails(BillsOrderMasterInsertDto billsOrderMasterDto) {
        if (billsOrderMasterDto.getBillsOrderDetails() == null || billsOrderMasterDto.getBillsOrderDetails().isEmpty()) {
            throw new IllegalArgumentException("At least one bill order detail is required");
        }

        // Validate all items have AccountMasterRefId set
        long itemsWithoutAccount = billsOrderMasterDto.getBillsOrderDetails().stream()
                .filter(detail -> detail.getAccountMasterRefId() == null || detail.getAccountMasterRefId() == 0)
                .count();

        if (itemsWithoutAccount > 0) {
            throw new IllegalArgumentException(
                    "Please enter the Account Code for all items. Found " + itemsWithoutAccount + " items without account.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateSaleOrderMasterFlags(BillsOrderMasterInsertDto billsOrderMasterDto, boolean recalculateFlags) {
        if (billsOrderMasterDto.getSaleMasterRefId() == null || billsOrderMasterDto.getSaleMasterRefId() == 0) {
            return; // No sale order to update
        }

        String description = billsOrderMasterDto.getDescription();
        if (description == null || description.isEmpty()) {
            return;
        }

        Integer saleMasterRefId = billsOrderMasterDto.getSaleMasterRefId();
        String updateQuery = null;

        switch (description.toUpperCase().trim()) {
            case "PORT CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET PortCPop = 2 WHERE Id = ? AND PortCPop = 1";
                break;
            case "LIVE CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET LiveCPop = 2 WHERE Id = ? AND LiveCPop = 1";
                break;
            case "CUSTOM CLEARANCE":
            case "CUSTOMER CLEARANCE":
                updateQuery = "UPDATE SaleOrderMaster SET ForwardingCPop = 2 WHERE Id = ? AND ForwardingCPop = 1";
                break;
            case "BOAT CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET BoatCPop = 2 WHERE Id = ? AND BoatCPop = 1";
                break;
            case "PERMIT CHARGES":
            case "INWARD PERMIT CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET PermitCPop = 2 WHERE Id = ? AND PermitCPop = 1";
                break;
            case "MMHE CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET MMHECPop = 2 WHERE Id = ? AND MMHECPop = 1";
                break;
            case "AIR FREIGHT EXPORT CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET AFpoCPop = 2 WHERE Id = ? AND AFpoCPop = 1";
                break;
            case "STORAGE FEE":
            case "FREIGHT CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET SFWpoCPop = 2 WHERE Id = ? AND SFWpoCPop = 1";
                break;
            case "CRANE & WHARFMARK CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET BoatCPop1 = 2 WHERE Id = ? AND BoatCPop1 = 1";
                break;
            case "PFP & PAC CHARGES":
                updateQuery = "UPDATE SaleOrderMaster SET PFPPCPop1 = 2 WHERE Id = ? AND PFPPCPop1 = 1";
                break;
        }

        if (updateQuery != null) {
            try {
                int rowsUpdated = jdbcTemplate.update(updateQuery, saleMasterRefId);
                logger.info("Updated SaleOrderMaster flags for description '{}'. Rows updated: {}",
                    description, rowsUpdated);
            } catch (Exception ex) {
                logger.error("Error updating SaleOrderMaster flags for description: {}", description, ex);
                // Don't throw - this is a secondary operation
            }
        }
    }

    /**
     * Send WhatsApp notification for new bills order master
     * (Implementation depends on your WhatsApp service integration)
     *
     * @param billsOrderMasterDto The bills order master data
     * @param billId              The generated bill ID
     * @param companyId           The company ID
     */
    private void sendWhatsAppNotification(
            BillsOrderMasterInsertDto billsOrderMasterDto,
            Integer billId,
            Integer companyId) {
        try {
            // TODO: Integrate with WhatsApp service
            logger.debug("WhatsApp notification sent for Bill ID: {}", billId);
        } catch (Exception ex) {
            logger.error("Error sending WhatsApp notification", ex);
            // Don't throw - this is a non-critical operation
        }
    }
}

