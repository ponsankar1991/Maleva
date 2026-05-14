package my.maleva.api.module.purchase.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Custom implementation of PurchaseMaster repository methods
 * Uses raw JDBC to execute SP_PurchaseMaster logic directly without calling the stored procedure
 * Replaces entire SP logic with direct Java/JDBC implementation
 */
@Service
public class PurchaseMasterRepositoryCustomImpl {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseMasterRepositoryCustomImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    public List<Object[]> executeInsertPurchaseMaster(String jsonDetails, Integer companyId) {
        logger.info("Executing SP_PurchaseMaster logic directly with JDBC (NO SP call)");

        List<Object[]> results = new ArrayList<>();
        Connection conn = null;

        try {
            conn = jdbcTemplate.getDataSource().getConnection();
            conn.setAutoCommit(false); // Start transaction


            // Parse JSON to list of maps
            List<Map<String, Object>> masterRecords = objectMapper.readValue(
                jsonDetails,
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
            );

            Integer lastId = 0;
            String lastBillNo = "";
            int resultCode = 0;
            String message = "";

            try {
                // Process each master record (equivalent to SP WHILE loop)
                for (Map<String, Object> master : masterRecords) {
                    logger.debug("Processing master record: {}", master);

                    Integer id = safeToInt(master.get("Id"));
                    Integer companyRefId = safeToInt(master.get("CompanyRefId"), companyId);
                    Integer userRefId = safeToInt(master.get("UserRefId"));
                    Integer employeeRefId = safeToInt(master.get("EmployeeRefId"));
                    Integer supplierRefId = safeToInt(master.get("SupplierRefId"));
                    LocalDate saleDate = safeToLocalDate(master.get("SaleDate"));
                    String saleType = safeToString(master.get("SaleType"));
                    Double grossAmount = safeToDouble(master.get("GrossAmount"));
                    Double taxAmount = safeToDouble(master.get("TaxAmount"));
                    Double discountAmount = safeToDouble(master.get("DiscountAmount"));
                    String remarks = safeToString(master.get("Remarks"));
                    Double plusAmount = safeToDouble(master.get("PlusAmount"));
                    Double minusAmount = safeToDouble(master.get("MinusAmount"));
                    Double coinage = safeToDouble(master.get("Coinage"));
                    Double amount = safeToDouble(master.get("Amount"));
                    String invoiceNo = safeToString(master.get("InvoiceNo"));
                    LocalDate invoiceDate = safeToLocalDate(master.get("InvoiceDate"));
                    Integer truckRefId = safeToInt(master.get("TruckRefId"));
                    Integer driverRefId = safeToInt(master.get("DriverRefId"));
                    Integer paymentTermsRefId = safeToInt(master.get("PaymentTermsRefId"));
                    String description = safeToString(master.get("Description"));
                    Double currencyValue = safeToDouble(master.get("CurrencyValue"));
                    Double actualAmount = safeToDouble(master.get("ActualAmount"));
                    String serialNo = safeToString(master.get("SerialNo"));
                    Integer purchaseOrderMasterRefId = safeToInt(master.get("PurchaseOrderMasterRefId"));

                    int purchaseMasterId = 0;
                    String generatedCNumberDisplay = "";
                    Integer generatedCNumber = 0;

                    // =====================================================================
                    // VALIDATION: Check foreign key references exist (matching SP logic)
                    // =====================================================================
                    // SP: If @UserRefId <> 0 BEGIN check if exists in AppUser...
                    if (userRefId != null && userRefId != 0) {
                        Integer userCheck = validateUserExists(conn, companyId, userRefId);
                        if (userCheck == 0) {
                            throw new SQLException("Login User Not Found - Id: " + userRefId);
                        }
                        logger.debug("User validation passed: UserRefId = {}", userRefId);
                    }

                    // SP: If @EmployeeRefId <> 0 BEGIN check if exists in EmployeeMaster...
                    if (employeeRefId != null && employeeRefId != 0) {
                        Integer empCheck = validateEmployeeExists(conn, companyId, employeeRefId);
                        if (empCheck == 0) {
                            throw new SQLException("Employee Not Found - Id: " + employeeRefId);
                        }
                        logger.debug("Employee validation passed: EmployeeRefId = {}", employeeRefId);
                    }

                    // SP: If @TruckRefid <> 0 BEGIN check if exists in TruckMaster...
                    if (truckRefId != null && truckRefId != 0) {
                        Integer truckCheck = validateTruckExists(conn, companyId, truckRefId);
                        if (truckCheck == 0) {
                            throw new SQLException("Truck Not Found - Id: " + truckRefId);
                        }
                        logger.debug("Truck validation passed: TruckRefId = {}", truckRefId);
                    }

                    // SP: If @DriverRefid <> 0 BEGIN check if exists in DriverMaster...
                    if (driverRefId != null && driverRefId != 0) {
                        Map<String, Object> driverCheckResult = validateDriverExistsWithDiagnostics(conn, companyId, driverRefId);
                        Integer driverCheck = (Integer) driverCheckResult.get("exists");
                        String diagnostics = (String) driverCheckResult.get("diagnostics");

                        if (driverCheck == 0) {
                            String errorMsg = "Driver Not Found - Id: " + driverRefId +
                                            "\nDiagnostics: " + diagnostics;
                            logger.error("❌ Driver Validation Failed: {}", errorMsg);
                            throw new SQLException(errorMsg);
                        }
                        logger.debug("Driver validation passed: DriverRefId = {}", driverRefId);
                    }

                    // SP: If @PaymentTermsRefid <> 0 BEGIN check if exists in PaymentTermsMaster...
                    if (paymentTermsRefId != null && paymentTermsRefId != 0) {
                        Map<String, Object> paymentCheckResult = validatePaymentTermsWithDiagnostics(conn, companyId, paymentTermsRefId);
                        Integer paymentCheck = (Integer) paymentCheckResult.get("exists");
                        String diagnostics = (String) paymentCheckResult.get("diagnostics");

                        if (paymentCheck == 0) {
                            String errorMsg = "Payment Terms Not Found - Id: " + paymentTermsRefId +
                                            "\nDiagnostics: " + diagnostics;
                            logger.error("❌ Payment Terms Validation Failed: {}", errorMsg);
                            throw new SQLException(errorMsg);
                        }
                        logger.debug("Payment Terms validation passed: PaymentTermsRefId = {} - {}", paymentTermsRefId, diagnostics);
                    }

                    // =====================================================================
                    // For NEW records: Generate sequence BEFORE insert (SP requirement: CNumberDisplay is NOT NULL)
                    // =====================================================================
                    if (id == 0) {
                        logger.debug("Generating sequence number for new PurchaseMaster");
                        Map<String, Object> seqResult = generateSequenceNumberBeforeInsert(conn, companyRefId);
                        generatedCNumber = (Integer) seqResult.get("sequenceNo");
                        generatedCNumberDisplay = (String) seqResult.get("displayNumber");
                        logger.debug("Generated CNumber: {}, CNumberDisplay: {}", generatedCNumber, generatedCNumberDisplay);
                    }

                    // =====================================================================
                    // INSERT or UPDATE PurchaseMaster (equivalent to SP logic)
                    // =====================================================================
                    if (id == 0) {
                        // INSERT new record with generated sequence numbers
                        logger.debug("Inserting new PurchaseMaster record with CNumberDisplay: {}", generatedCNumberDisplay);

                        String insertSql = """
                            INSERT INTO PurchaseMaster
                            (CompanyRefId, UserRefId, EmployeeRefId, LastEmployeeRefId, SupplierRefId, PaymentTermsRefId, Description,
                             SaleDate, SaleType, Coinage, GrossAmount, TaxAmount, DiscountAmount, Remarks, PlusAmount, MinusAmount,
                             Amount, Active, Created_Date, Created_By, Modified_Date, Modified_By, InvoiceNo, InvoiceDate,
                             TruckRefId, DriverRefId, PurchaseOrderMasterRefId, CurrencyValue, ActualAmount, SerialNo, CNumber, CNumberDisplay)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), ?, GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """;

                        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                            String currentUser = getCurrentUser();
                            pstmt.setInt(1, companyRefId);

                            // ...existing code...
                            setIntOrNull(pstmt, 2, userRefId);        // UserRefId
                            setIntOrNull(pstmt, 3, employeeRefId);    // EmployeeRefId
                            setIntOrNull(pstmt, 4, employeeRefId);    // LastEmployeeRefId = EmployeeRefId
                            pstmt.setInt(5, supplierRefId);           // SupplierRefId (required)
                            setIntOrNull(pstmt, 6, paymentTermsRefId);// PaymentTermsRefId
                            pstmt.setString(7, description);
                            pstmt.setDate(8, saleDate != null ? java.sql.Date.valueOf(saleDate) : null);
                            pstmt.setString(9, saleType);
                            pstmt.setDouble(10, coinage);
                            pstmt.setDouble(11, grossAmount);
                            pstmt.setDouble(12, taxAmount);
                            pstmt.setDouble(13, discountAmount);
                            pstmt.setString(14, remarks);
                            pstmt.setDouble(15, plusAmount);
                            pstmt.setDouble(16, minusAmount);
                            pstmt.setDouble(17, amount);
                            pstmt.setInt(18, 1); // Active
                            pstmt.setString(19, currentUser); // Created_By
                            pstmt.setString(20, currentUser); // Modified_By
                            pstmt.setString(21, invoiceNo);
                            pstmt.setDate(22, invoiceDate != null ? java.sql.Date.valueOf(invoiceDate) : null);
                            setIntOrNull(pstmt, 23, truckRefId);              // TruckRefId
                            setIntOrNull(pstmt, 24, driverRefId);            // DriverRefId
                            setIntOrNull(pstmt, 25, purchaseOrderMasterRefId);// PurchaseOrderMasterRefId
                            pstmt.setDouble(26, currencyValue != null ? currencyValue : 0.0);
                            pstmt.setDouble(27, actualAmount != null ? actualAmount : 0.0);
                            pstmt.setString(28, serialNo);
                            pstmt.setInt(29, generatedCNumber); // CNumber - generated before insert
                            pstmt.setString(30, generatedCNumberDisplay); // CNumberDisplay - generated before insert

                            int rowsAffected = pstmt.executeUpdate();

                            if (rowsAffected == 0) {
                                throw new SQLException("Insert failed: No rows affected");
                            }

                            // ✅ CRITICAL FIX: Properly retrieve IDENTITY value from SQL Server
                            // Use getGeneratedKeys() which works for SQL Server IDENTITY columns
                            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    purchaseMasterId = generatedKeys.getInt(1);
                                    lastId = purchaseMasterId;
                                    lastBillNo = generatedCNumberDisplay; // Store for return
                                    logger.info("✅ Inserted PurchaseMaster with ID: {}, BillNo: {}", purchaseMasterId, generatedCNumberDisplay);
                                } else {
                                    throw new SQLException("Could not retrieve generated ID from INSERT");
                                }
                            }
                        }

                    } else {
                        // UPDATE existing record
                        logger.debug("Updating existing PurchaseMaster record with ID: {}", id);
                        
                        purchaseMasterId = id;
                        lastId = id;

                        String updateSql = """
                            UPDATE PurchaseMaster SET
                                UserRefId = ?, EmployeeRefId = ?, LastEmployeeRefId = ?, SupplierRefId = ?, SaleDate = ?,
                                SaleType = ?, GrossAmount = ?, TaxAmount = ?, DiscountAmount = ?,
                                Remarks = ?, PlusAmount = ?, MinusAmount = ?, Coinage = ?,
                                Amount = ?, InvoiceNo = ?, InvoiceDate = ?, TruckRefId = ?,
                                DriverRefId = ?, PaymentTermsRefId = ?, Description = ?,
                                CurrencyValue = ?, ActualAmount = ?, SerialNo = ?,
                                PurchaseOrderMasterRefId = ?, Modified_Date = GETDATE(), Modified_By = ?
                            WHERE Id = ?
                            """;

                        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                            String currentUser = getCurrentUser();
                            setIntOrNull(pstmt, 1, userRefId);        // UserRefId
                            setIntOrNull(pstmt, 2, employeeRefId);    // EmployeeRefId
                            setIntOrNull(pstmt, 3, employeeRefId);    // LastEmployeeRefId = EmployeeRefId
                            pstmt.setInt(4, supplierRefId);           // SupplierRefId
                            pstmt.setDate(5, saleDate != null ? java.sql.Date.valueOf(saleDate) : null);
                            pstmt.setString(6, saleType);
                            pstmt.setDouble(7, grossAmount);
                            pstmt.setDouble(8, taxAmount);
                            pstmt.setDouble(9, discountAmount);
                            pstmt.setString(10, remarks);
                            pstmt.setDouble(11, plusAmount);
                            pstmt.setDouble(12, minusAmount);
                            pstmt.setDouble(13, coinage);
                            pstmt.setDouble(14, amount);
                            pstmt.setString(15, invoiceNo);
                            pstmt.setDate(16, invoiceDate != null ? java.sql.Date.valueOf(invoiceDate) : null);
                            setIntOrNull(pstmt, 17, truckRefId);             // TruckRefId
                            setIntOrNull(pstmt, 18, driverRefId);           // DriverRefId
                            setIntOrNull(pstmt, 19, paymentTermsRefId);     // PaymentTermsRefId
                            pstmt.setString(20, description);
                            pstmt.setDouble(21, currencyValue != null ? currencyValue : 0.0);
                            pstmt.setDouble(22, actualAmount != null ? actualAmount : 0.0);
                            pstmt.setString(23, serialNo);
                            setIntOrNull(pstmt, 24, purchaseOrderMasterRefId);// PurchaseOrderMasterRefId
                            pstmt.setString(25, currentUser); // Modified_By
                            pstmt.setInt(26, id);

                            pstmt.executeUpdate();
                        }

                        // =====================================================================
                        // DELETE all existing PurchaseDetails (Edit Process - SP line: delete from PurchaseDetails where PurchaseMasterRefId=@Id)
                        // =====================================================================
                        logger.debug("Deleting all existing PurchaseDetails for PurchaseMaster ID: {}", id);
                        try (Statement stmt = conn.createStatement()) {
                            stmt.executeUpdate("DELETE FROM PurchaseDetails WHERE PurchaseMasterRefId = " + id);
                        }
                    }

                    // =====================================================================
                    // Handle PurchaseDetails
                    // SP LOGIC: INSERT all detail rows from JSON (no UPDATE)
                    // When updating master (Id > 0), old details were already deleted above
                    // =====================================================================
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> purchaseDetails = (List<Map<String, Object>>) master.get("PurchaseDetails");

                    if (purchaseDetails != null && !purchaseDetails.isEmpty()) {
                        logger.info("✅ Inserting {} PurchaseDetail records for PurchaseMaster ID: {}",
                            purchaseDetails.size(), purchaseMasterId);

                        // ✅ FIX: REMOVED Id from INSERT - let SQL Server IDENTITY auto-generate it
                        // Issue: Cannot insert explicit value for identity column when IDENTITY_INSERT is OFF
                        String insertDetailSql = """
                            INSERT INTO PurchaseDetails
                            (PurchaseMasterRefId, ProductMasterRefId, MRP, PurchaseRate, ItemQty,
                             DiscPer, DiscAmount, LandingCost, TaxPercent, TaxAmount, SalesRate,
                             NetSalesRate, Amount, CurrencyValue, ActualAmount, RemarksD,
                             ProductName, ProductCode, Created_Date, Modified_Date)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())
                            """;
                        
                        int detailsInserted = 0;
                        int detailIndex = 0;

                        for (Map<String, Object> detail : purchaseDetails) {
                            detailIndex++;
                            try (PreparedStatement pstmt = conn.prepareStatement(insertDetailSql, Statement.RETURN_GENERATED_KEYS)) {
                                Integer detailProductMasterId = safeToInt(detail.get("ProductMasterRefId"));
                                Double detailMrp = safeToDouble(detail.get("MRP"));
                                Double detailPurchaseRate = safeToDouble(detail.get("PurchaseRate"));
                                Double detailItemQty = safeToDouble(detail.get("ItemQty"));
                                Double detailDiscPer = safeToDouble(detail.get("DiscPer"));
                                Double detailDiscAmount = safeToDouble(detail.get("DiscAmount"));
                                Double detailLandingCost = safeToDouble(detail.get("LandingCost"));
                                Double detailTaxPercent = safeToDouble(detail.get("TaxPercent"));
                                Double detailTaxAmount = safeToDouble(detail.get("TaxAmount"));
                                Double detailSalesRate = safeToDouble(detail.get("SalesRate"));
                                Double detailNetSalesRate = safeToDouble(detail.get("NetSalesRate"));
                                Double detailAmount = safeToDouble(detail.get("Amount"));
                                Double detailCurrencyValue = safeToDouble(detail.get("CurrencyValue"));
                                Double detailActualAmount = safeToDouble(detail.get("ActualAmount"));
                                String detailRemarksD = safeToString(detail.get("RemarksD"));
                                String detailProductName = safeToString(detail.get("ProductName"));
                                String detailProductCode = safeToString(detail.get("ProductCode"));

                                logger.info("Detail #{}: PurchaseMasterId={}, ProductMasterId={}, Qty={}, Amount={}, Code={}",
                                    detailIndex, purchaseMasterId, detailProductMasterId, detailItemQty, detailAmount, detailProductCode);

                                // ✅ CRITICAL FIX: Set ALL parameters correctly
                                pstmt.setInt(1, purchaseMasterId);                              // PurchaseMasterRefId
                                setIntOrNull(pstmt, 2, detailProductMasterId);                 // ProductMasterRefId (✅ WAS MISSING!)
                                setDoubleOrNull(pstmt, 3, detailMrp);
                                setDoubleOrNull(pstmt, 4, detailPurchaseRate);
                                setDoubleOrNull(pstmt, 5, detailItemQty);
                                setDoubleOrNull(pstmt, 6, detailDiscPer);
                                setDoubleOrNull(pstmt, 7, detailDiscAmount);
                                setDoubleOrNull(pstmt, 8, detailLandingCost);
                                setDoubleOrNull(pstmt, 9, detailTaxPercent);
                                setDoubleOrNull(pstmt, 10, detailTaxAmount);
                                setDoubleOrNull(pstmt, 11, detailSalesRate);
                                setDoubleOrNull(pstmt, 12, detailNetSalesRate);
                                setDoubleOrNull(pstmt, 13, detailAmount);
                                setDoubleOrNull(pstmt, 14, detailCurrencyValue);
                                setDoubleOrNull(pstmt, 15, detailActualAmount);
                                // Parameters 16-18: String fields
                                pstmt.setString(16, detailRemarksD.isEmpty() ? null : detailRemarksD);
                                pstmt.setString(17, detailProductName.isEmpty() ? null : detailProductName);
                                pstmt.setString(18, detailProductCode.isEmpty() ? null : detailProductCode);

                                // ✅ Execute the INSERT statement
                                int rowsAffected;
                                try {
                                    rowsAffected = pstmt.executeUpdate();
                                } catch (SQLException e) {
                                    logger.error("SQL EXCEPTION during detail insert: {}", e.getMessage());
                                    throw e;
                                }

                                // SQL Server JDBC can return -1 when the statement executes successfully but row count is not available
                                // So we treat both > 0 AND -1 as success
                                if (rowsAffected > 0 || rowsAffected == -1) {
                                    detailsInserted++;
                                    logger.info("✅ PurchaseDetail inserted successfully. Total: {}/{}", detailsInserted, purchaseDetails.size());
                                } else {
                                    logger.warn("⚠️ PurchaseDetail insert returned 0 rows affected");
                                }
                            }
                        }

                        logger.info("✅ Successfully inserted {} out of {} PurchaseDetail records",
                            detailsInserted, purchaseDetails.size());

                        if (detailsInserted != purchaseDetails.size()) {
                            throw new SQLException("Failed to insert all detail records: expected " +
                                purchaseDetails.size() + " but inserted " + detailsInserted);
                        }
                    }
                }

                // Success - commit transaction
                conn.commit();
                logger.info("✅ TRANSACTION COMMITTED successfully with SERIALIZABLE isolation level");
                resultCode = 1;
                message = "";

                // Get or generate the BillNo
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT CNumberDisplay FROM PurchaseMaster WHERE Id = " + lastId);
                    if (rs.next()) {
                        lastBillNo = rs.getString(1);
                        logger.info("✅ Retrieved CNumberDisplay from DB: {} for ID: {}", lastBillNo, lastId);
                    }
                }

                // Build result array
                Object[] resultRow = {resultCode, message, lastBillNo, new Timestamp(System.currentTimeMillis()), lastId};
                results.add(resultRow);

                logger.info("SP_PurchaseMaster logic executed successfully - ID: {}, BillNo: {}", lastId, lastBillNo);

            }


            catch (Exception e) {
                conn.rollback();
                logger.error("❌ ERROR processing PurchaseMaster records - ROLLING BACK TRANSACTION", e);
                logger.error("❌ If you see ID gaps after this, it's due to SQL Server IDENTITY allocation during failed insert attempt");
                resultCode = 0;
                message = e.getMessage();
                Object[] resultRow = {resultCode, message, "", new Timestamp(System.currentTimeMillis()), lastId};
                results.add(resultRow);
            }

        } catch (Exception e) {
            logger.error("Error executing SP_PurchaseMaster logic with JDBC", e);
            Object[] resultRow = {0, e.getMessage(), "", new Timestamp(System.currentTimeMillis()), 0};
            results.add(resultRow);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing database connection", e);
                }
            }
        }

        return results;
    }


    private Map<String, Object> generateSequenceNumberBeforeInsert(Connection conn, Integer companyRefId) throws SQLException {
        logger.debug("Generating sequence number BEFORE insert (for CNumberDisplay NOT NULL requirement)");

        Map<String, Object> result = new HashMap<>();

        // =====================================================================
        // Step 1: Get current max sequence from SequenceNoMaster table
        // =====================================================================
        Integer currentMaxSequence = 0;
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT ISNULL(MAX(SequenceNo), 0) FROM SequenceNoMaster WHERE CompanyRefId = " + companyRefId + " AND SequenceName = 'PurchaseMaster'"
            );
            if (rs.next()) {
                currentMaxSequence = rs.getInt(1);
            }
        }

        int newSequenceNo;

        // =====================================================================
        // Step 2: Update or Insert in SequenceNoMaster
        // =====================================================================
        if (currentMaxSequence == 0) {
            // No existing sequence record, create new one with SequenceNo = 1
            newSequenceNo = 1;
            
            try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO SequenceNoMaster (CompanyRefId, SequenceName, SequenceNo) VALUES (?, ?, ?)")) {
                pstmt.setInt(1, companyRefId);
                pstmt.setString(2, "PurchaseMaster");
                pstmt.setInt(3, newSequenceNo);
                pstmt.executeUpdate();
            }
            logger.debug("Inserted new SequenceNoMaster record with SequenceNo: {}", newSequenceNo);
        } else {
            // Increment existing sequence
            newSequenceNo = currentMaxSequence + 1;
            
            try (PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE SequenceNoMaster SET SequenceNo = ? WHERE CompanyRefId = ? AND SequenceName = 'PurchaseMaster'")) {
                pstmt.setInt(1, newSequenceNo);
                pstmt.setInt(2, companyRefId);
                pstmt.executeUpdate();
            }
            logger.debug("Updated SequenceNoMaster with new SequenceNo: {}", newSequenceNo);
        }

        // Generate CNumberDisplay: 'PM' + RIGHT('000000000' + SequenceNo, 9)
        String cNumberDisplay = String.format("PM%09d", newSequenceNo);
        
        result.put("sequenceNo", newSequenceNo);
        result.put("displayNumber", cNumberDisplay);
        
        logger.debug("Generated sequence BEFORE insert: CNumber={}, CNumberDisplay={}", newSequenceNo, cNumberDisplay);
        return result;
    }



    // =====================================================================
    // Helper methods for safe type conversions
    // =====================================================================

    private Integer safeToInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.isEmpty()) return 0;
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private Integer safeToInt(Object value, Integer defaultValue) {
        Integer result = safeToInt(value);
        return result != 0 ? result : (defaultValue != null ? defaultValue : 0);
    }

    private Double safeToDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.isEmpty()) return 0.0;
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private String safeToString(Object value) {
        if (value == null) return "";
        if (value instanceof String) {
            String str = ((String) value).trim();
            return str.equalsIgnoreCase("null") ? "" : str;
        }
        return value.toString().trim();
    }

    private LocalDate safeToLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        if (value instanceof String) {
            String str = safeToString(value);
            if (str.isEmpty()) return null;
            try {
                return LocalDate.parse(str);
            } catch (Exception e) {
                return null;
            }
        }
        if (value instanceof java.sql.Date) return ((java.sql.Date) value).toLocalDate();
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime().toLocalDate();
        return null;
    }

    /**
     * Get current user from Spring Security context
     * Falls back to system username if not available
     * Matches SP behavior: (suser_name())
     */
    private String getCurrentUser() {
        try {
            // Try to get from Spring Security context
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() != null) {
                Object principal = auth.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                }
                if (principal instanceof String) {
                    return (String) principal;
                }
            }
        } catch (Exception e) {
            logger.debug("Could not get user from Security context", e);
        }

        // Fallback to system property
        String user = System.getProperty("user.name");
        return (user != null && !user.isEmpty()) ? user : "SYSTEM";
    }

    /**
     * Helper: Set INT parameter or NULL
     * If value is null or 0, sets SQL NULL (matching SP logic where 0 means no value)
     */
    private void setIntOrNull(PreparedStatement pstmt, int paramIndex, Integer value) throws SQLException {
        if (value == null || value == 0) {
            pstmt.setNull(paramIndex, java.sql.Types.INTEGER);
        } else {
            pstmt.setInt(paramIndex, value);
        }
    }

    /**
     * Helper: Set DOUBLE parameter or NULL
     * Only sets NULL if value is actually null (not 0.0)
     * ✅ 0.0 is a valid amount value and should be inserted as 0, not NULL
     */
    private void setDoubleOrNull(PreparedStatement pstmt, int paramIndex, Double value) throws SQLException {
        if (value == null) {
            pstmt.setNull(paramIndex, java.sql.Types.DOUBLE);
        } else {
            pstmt.setDouble(paramIndex, value);
        }
    }

    /**
     * Validate User exists in AppUser table (matching SP validation)
     * SP: If @UserRefId <> 0 BEGIN set @countcheck = isnull((select Id from AppUser where CompanyRefId=@Comid and Id=@UserRefId and Active=1),0)...
     */
    private Integer validateUserExists(Connection conn, Integer companyId, Integer userRefId) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT ISNULL(COUNT(*), 0) FROM AppUser WHERE CompanyRefId = " + companyId + 
                " AND Id = " + userRefId + " AND Active = 1"
            );
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Validate Employee exists in EmployeeMaster table (matching SP validation)
     * SP: If @EmployeeRefId <> 0 BEGIN set @countcheck = isnull((select Id from EmployeeMaster where CompanyRefId=@Comid and Id=@EmployeeRefId and Active=1),0)...
     */
    private Integer validateEmployeeExists(Connection conn, Integer companyId, Integer employeeRefId) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT ISNULL(COUNT(*), 0) FROM EmployeeMaster WHERE CompanyRefId = " + companyId + 
                " AND Id = " + employeeRefId + " AND Active = 1"
            );
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Validate Truck exists in TruckMaster table (matching SP validation)
     * SP: If @TruckRefid <> 0 BEGIN set @countcheck = isnull((select Id from TruckMaster where CompanyRefId=@Comid and Id=@TruckRefid and Active=1),0)...
     */
    private Integer validateTruckExists(Connection conn, Integer companyId, Integer truckRefId) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT ISNULL(COUNT(*), 0) FROM TruckMaster WHERE CompanyRefId = " + companyId + 
                " AND Id = " + truckRefId + " AND Active = 1"
            );
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }


    /**
     * Validate Driver exists in DriverMaster table with detailed diagnostics
     * SP: If @DriverRefid <> 0 BEGIN set @countcheck = isnull((select Id from DriverMaster where CompanyRefId=@Comid and Id=@DriverRefid and Active=1),0)...
     *
     * ✅ ENHANCED VERSION: Provides detailed diagnostic info on why validation fails
     * Returns both validation result AND diagnostic information for better error messages
     */
    private Map<String, Object> validateDriverExistsWithDiagnostics(Connection conn, Integer companyId, Integer driverRefId) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        StringBuilder diagnostics = new StringBuilder();

        // Check 1: Does DriverMaster record exist at all with this ID?
        Integer totalCount = 0;
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT ISNULL(COUNT(*), 0) FROM DriverMaster WHERE Id = " + driverRefId
            );
            if (rs.next()) {
                totalCount = rs.getInt(1);
            }
        }

        if (totalCount == 0) {
            diagnostics.append("❌ Driver with ID ").append(driverRefId).append(" does not exist in database");
            result.put("exists", 0);
            result.put("diagnostics", diagnostics.toString());
            return result;
        }
        diagnostics.append("✅ Driver ID ").append(driverRefId).append(" exists. ");

        // Check 2: Does it belong to the correct company?
        Integer companyCount = 0;
        try (Statement stmt = conn.createStatement()) {
            String query = "SELECT ISNULL(COUNT(*), 0) FROM DriverMaster WHERE Id = " + driverRefId +
                          " AND CompanyRefId = " + companyId;
            logger.debug("Checking company ownership: {}", query);
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                companyCount = rs.getInt(1);
            }
        }

        if (companyCount == 0) {
            diagnostics.append("❌ Driver doesn't belong to Company ").append(companyId);
            result.put("exists", 0);
            result.put("diagnostics", diagnostics.toString());
            return result;
        }
        diagnostics.append("✅ Company ").append(companyId).append(" verified. ");

        // Check 3: Is it ACTIVE (Active = 1)?
        Integer activeCount = 0;
        try (Statement stmt = conn.createStatement()) {
            String query = "SELECT ISNULL(COUNT(*), 0) FROM DriverMaster WHERE Id = " + driverRefId +
                          " AND CompanyRefId = " + companyId + " AND Active = 1";
            logger.debug("Checking active status: {}", query);
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                activeCount = rs.getInt(1);
            }
        }

        if (activeCount == 0) {
            // Get actual status
            Integer actualStatus = 0;
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT ISNULL(Active, -1) FROM DriverMaster WHERE Id = " + driverRefId
                );
                if (rs.next()) {
                    actualStatus = rs.getInt(1);
                }
            }
            diagnostics.append("❌ Driver is not ACTIVE (Current Active status: ").append(actualStatus).append(", required: 1)");
            result.put("exists", 0);
            result.put("diagnostics", diagnostics.toString());
            return result;
        }
        diagnostics.append("✅ Driver is ACTIVE");

        // All checks passed
        result.put("exists", 1);
        result.put("diagnostics", diagnostics.toString());
        logger.info("✅ All Driver Validations Passed: {}", diagnostics);
        return result;
    }

    /**
     * Validate PaymentTerms exists in PaymentTermsMaster table with detailed diagnostics
     * SP: If @PaymentTermsRefid <> 0 BEGIN set @countcheck = isnull((select Id from PaymentTermsMaster where CompanyRefId=@Comid and Id=@PaymentTermsRefid and Active=1),0)...
     * 
     * ✅ NEW ENHANCED VERSION: Provides detailed diagnostic info on why validation fails
     * Returns both validation result AND diagnostic information for better error messages
     */
    private Map<String, Object> validatePaymentTermsWithDiagnostics(Connection conn, Integer companyId, Integer paymentTermsRefId) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        StringBuilder diagnostics = new StringBuilder();

        // Check 1: Does PaymentTermsMaster record exist at all with this ID?
        Integer totalCount = 0;
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT ISNULL(COUNT(*), 0) FROM PaymentTermsMaster WHERE Id = " + paymentTermsRefId
            );
            if (rs.next()) {
                totalCount = rs.getInt(1);
            }
        }

        if (totalCount == 0) {
            diagnostics.append("❌ PaymentTermsMaster ID ").append(paymentTermsRefId)
                      .append(" does not exist in database at all. ");
            // Provide available IDs for this company
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT Id, TermsName, Active FROM PaymentTermsMaster WHERE CompanyRefId = " + companyId + " ORDER BY Id"
                );
                diagnostics.append("Available PaymentTerms for Company ").append(companyId).append(": ");
                boolean hasRecords = false;
                while (rs.next()) {
                    hasRecords = true;
                    diagnostics.append("[Id=").append(rs.getInt(1))
                              .append(", Name='").append(rs.getString(2))
                              .append("', Active=").append(rs.getInt(3)).append("] ");
                }
                if (!hasRecords) {
                    diagnostics.append("NONE - No PaymentTerms records exist for this company!");
                }
            }
            result.put("exists", 0);
            result.put("diagnostics", diagnostics.toString());
            return result;
        }

        // Check 2: Does it belong to the correct company?
        Integer companyMatchCount = 0;
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT ISNULL(COUNT(*), 0), MAX(CompanyRefId) FROM PaymentTermsMaster WHERE Id = " + paymentTermsRefId
            );
            if (rs.next()) {
                companyMatchCount = rs.getInt(1);
                Integer actualCompanyId = rs.getInt(2);
                if (companyMatchCount > 0 && !actualCompanyId.equals(companyId)) {
                    diagnostics.append("⚠️  PaymentTermsMaster ID ").append(paymentTermsRefId)
                              .append(" exists but belongs to Company ").append(actualCompanyId)
                              .append(", not Company ").append(companyId).append(". ");
                }
            }
        }

        if (companyMatchCount == 0) {
            diagnostics.append("❌ PaymentTermsMaster ID ").append(paymentTermsRefId)
                      .append(" does not belong to Company ").append(companyId).append(". ");
        }

        // Check 3: Is it active?
        Integer activeCount = 0;
        Integer activeStatus = 0;
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT ISNULL(COUNT(*), 0), MAX(Active) FROM PaymentTermsMaster WHERE Id = " + paymentTermsRefId + 
                " AND CompanyRefId = " + companyId
            );
            if (rs.next()) {
                activeCount = rs.getInt(1);
                activeStatus = rs.getInt(2);
                if (activeCount > 0 && activeStatus != 1) {
                    diagnostics.append("❌ PaymentTermsMaster ID ").append(paymentTermsRefId)
                              .append(" exists and belongs to this company but is INACTIVE (Active=")
                              .append(activeStatus).append("). ");
                }
            }
        }

        // Final validation: Count records that pass ALL checks
        Integer validCount = 0;
        String termsName = "";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT ISNULL(COUNT(*), 0), MAX(TermsName) FROM PaymentTermsMaster WHERE CompanyRefId = " + companyId + 
                " AND Id = " + paymentTermsRefId + " AND Active = 1"
            );
            if (rs.next()) {
                validCount = rs.getInt(1);
                termsName = rs.getString(2) != null ? rs.getString(2) : "N/A";
            }
        }

        if (validCount > 0) {
            diagnostics.append("✅ VALID: PaymentTermsMaster '").append(termsName)
                      .append("' (ID=").append(paymentTermsRefId).append(") is active for Company ")
                      .append(companyId).append(".");
            result.put("exists", 1);
        } else {
            if (diagnostics.length() == 0) {
                diagnostics.append("❌ PaymentTermsMaster ID ").append(paymentTermsRefId)
                          .append(" failed validation checks for Company ").append(companyId).append(".");
            }
            result.put("exists", 0);
        }

        result.put("diagnostics", diagnostics.toString());
        return result;
    }







}
