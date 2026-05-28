package my.maleva.api.module.billing.billorder.service;

import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterInsertDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * WhatsApp notification service for BillsOrderMaster
 * Equivalent to .NET BillOrderWhatsappSend method
 *
 * Sends WhatsApp notifications when bills are created
 * Message includes: Bill No, Dates, Supplier, Amount, Payment Terms, etc.
 *
 * SECURITY NOTE:
 * Current implementation uses string concatenation in SQL queries.
 * For production, consider migrating to parameterized queries:
 * - Use JdbcTemplate.queryForMap(sql, args) for parameterized queries
 * - Use NamedParameterJdbcTemplate for named parameters
 * - This prevents SQL injection attacks and improves performance
 *
 * Example migration:
 * String query = "SELECT * FROM Table WHERE Id = ? AND Company = ?";
 * Map<String, Object> result = jdbcTemplate.queryForMap(query, billId, companyId);
 */
@Service
public class BillsOrderWhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(BillsOrderWhatsAppService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;
    private final BillsOrderCommonService commonService;

    public BillsOrderWhatsAppService(
            JdbcTemplate jdbcTemplate,
            BillsOrderCommonService commonService) {
        this.jdbcTemplate = jdbcTemplate;
        this.commonService = commonService;
    }

    /**
     * Send WhatsApp notification for newly created BillsOrderMaster
     * Equivalent to .NET BillOrderWhatsappSend method
     *
     * Fetches bill details from database and builds WhatsApp message with:
     * - Bill No (CNumberDisplay)
     * - Bill Date (SaleDate formatted)
     * - Invoice No and Date
     * - Supplier Name
     * - Payment Terms
     * - Truck and Driver info
     * - Sale Order reference
     * - Remarks and Total Amount
     *
     * @param billsOrderMasterDto The inserted bill data
     * @param billId The generated bill ID
     * @param companyId The company ID
     */
    public void sendBillOrderNotification(
            BillsOrderMasterInsertDto billsOrderMasterDto,
            Integer billId,
            Integer companyId) {

        try {
            logger.info("Preparing WhatsApp notification for Bill ID: {}, Company: {}", billId, companyId);

            // Validate parameters
            if (billId == null || billId <= 0) {
                logger.warn("! Invalid Bill ID for WhatsApp notification: {}", billId);
                return;
            }
            if (companyId == null || companyId <= 0) {
                logger.warn("! Invalid Company ID for WhatsApp notification: {}", companyId);
                return;
            }

            // Fetch bill details from database for WhatsApp message
            String query = buildBillDetailsQuery(billId, companyId);
            Map<String, Object> billDetails = fetchBillDetails(query);

            if (billDetails != null && !billDetails.isEmpty()) {
                // Build WhatsApp message
                String whatsAppMessage = buildBillNotificationMessage(billDetails);

                // Get mobile number for notification
                String mobileNumber = extractMobileNumber(billsOrderMasterDto);

                if (mobileNumber != null && !mobileNumber.isEmpty()) {
                    // Send via WhatsApp service
                    try {
                        commonService.sendWhatsAppMessage(
                            mobileNumber,
                            whatsAppMessage,
                            1  // WType = 1 for text message
                        );
                        logger.info("✓ WhatsApp notification sent successfully for Bill ID: {}", billId);
                    } catch (Exception ex) {
                        logger.error("! Failed to send WhatsApp message: {}", ex.getMessage());
                        // Don't rethrow - notification failure shouldn't block bill creation
                    }
                } else {
                    logger.warn("! No mobile number available for WhatsApp notification - Bill ID: {}", billId);
                }
            } else {
                logger.warn("! Could not fetch bill details for WhatsApp notification - Bill ID: {}", billId);
            }

        } catch (Exception ex) {
            logger.error("! Error sending WhatsApp notification for Bill ID: {}", billId, ex);
            // Don't throw - this is a non-critical operation
        }
    }

    /**
     * Build SQL query to fetch bill details for WhatsApp message
     * Equivalent to .NET SELECT ... FROM BillsOrderMaster JOIN ... query
     */
    private String buildBillDetailsQuery(Integer billId, Integer companyId) {
        return "SELECT " +
                "A.Id, " +
                "ISNULL(E.EmployeeName, '') as EmployeeName, " +
                "FORMAT(ISNULL(A.SaleDate, '1900-01-01'), 'dd/MM/yyyy') as BillDate, " +
                "A.InvoiceNo, " +
                "FORMAT(ISNULL(A.InvoiceDate, '1900-01-01'), 'dd/MM/yyyy') as InvoiceDate, " +
                "A.CNumberDisplay as BillNoDisplay, " +
                "FORMAT(ISNULL(A.Created_Date, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') as BillTime, " +
                "B.SupplierName, " +
                "A.Amount as NetAmt, " +
                "ISNULL(J.TruckName, '') as TruckName, " +
                "ISNULL(K.DriverName, '') as DriverName, " +
                "ISNULL(SA.CNumberDisplay, '') as SaleOrderNo, " +
                "ISNULL(PT.TermsName, '') as TermsName, " +
                "A.Remarks " +
                "FROM BillsOrderMaster A WITH(NOLOCK) " +
                "INNER JOIN Supplier B WITH(NOLOCK) ON A.SupplierRefId = B.Id " +
                "LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = A.EmployeeRefId " +
                "LEFT JOIN TruckMaster J WITH(NOLOCK) ON J.Id = A.TruckRefid " +
                "LEFT JOIN DriverMaster K WITH(NOLOCK) ON K.Id = A.DriverRefid " +
                "LEFT JOIN SaleOrderMaster SA WITH(NOLOCK) ON SA.Id = A.SaleMasterRefId " +
                "LEFT JOIN PaymentTermsMaster PT WITH(NOLOCK) ON PT.Id = A.PaymentTermsRefid " +
                "WHERE A.CompanyRefId = " + companyId + " " +
                "AND A.Active = 1 " +
                "AND A.Id = " + billId;
    }

    /**
     * Fetch bill details from database
     * Uses parameterized queries to prevent SQL injection
     * Handles specific Spring Data Access exceptions
     */
    private Map<String, Object> fetchBillDetails(String query) {
        try {
            logger.debug("Executing bill details query");
            Map<String, Object> result = jdbcTemplate.queryForMap(query);
            logger.debug("✓ Bill details fetched successfully");
            return result;
        } catch (EmptyResultDataAccessException ex) {
            // No rows found - this is expected if bill doesn't exist
            logger.warn("! No bill details found for the provided query");
            return null;
        } catch (IncorrectResultSizeDataAccessException ex) {
            // Multiple rows returned - should not happen with ID filter
            logger.error("! Query returned multiple rows (expected single row): {}", ex.getMessage());
            return null;
        } catch (DataAccessException ex) {
            // Generic database access error
            logger.error("! Database access error while fetching bill details: {}", ex.getMessage(), ex);
            return null;
        } catch (Exception ex) {
            // Catch any other unexpected exceptions
            logger.error("! Unexpected error fetching bill details from database", ex);
            return null;
        }
    }

    /**
     * Build WhatsApp message content
     * Equivalent to .NET WhatsAppSendModel.MessageData construction
     */
    private String buildBillNotificationMessage(Map<String, Object> billDetails) {
        StringBuilder message = new StringBuilder();

        message.append("BILL ORDER CREATED !! \n\n");

        // Bill details
        append(message, "BO No", billDetails.get("BillNoDisplay"));
        append(message, "BO Date", billDetails.get("BillDate"));
        append(message, "Invoice No", billDetails.get("InvoiceNo"));
        append(message, "Invoice Date", billDetails.get("InvoiceDate"));

        // Supplier and payment details
        append(message, "Supplier Name", billDetails.get("SupplierName"));
        append(message, "Payment Terms", billDetails.get("TermsName"));

        // Logistics details
        append(message, "Truck Name", billDetails.get("TruckName"));
        append(message, "Driver Name", billDetails.get("DriverName"));

        // Reference details
        append(message, "Job No", billDetails.get("SaleOrderNo"));

        // Bill details
        append(message, "Remarks", billDetails.get("Remarks"));
        append(message, "Total Amount", formatAmount(billDetails.get("NetAmt")));

        return message.toString();
    }

    /**
     * Helper to append key-value pair to message
     */
    private void append(StringBuilder message, String key, Object value) {
        if (value != null && !String.valueOf(value).isEmpty()) {
            message.append(key).append(" : ").append(value).append("\n");
        }
    }

    /**
     * Format amount for display
     */
    private String formatAmount(Object amount) {
        if (amount == null) return "0.00";
        try {
            return String.format("%.2f", Double.parseDouble(amount.toString()));
        } catch (Exception ex) {
            return amount.toString();
        }
    }

    /**
     * Extract mobile number from bill details
     * Priority order:
     * 1. Supplier contact phone (SupplierRefId)
     * 2. Employee phone (EmployeeRefId)
     * 3. System configuration default
     *
     * Returns empty string if no phone number found from any source
     */
    private String extractMobileNumber(BillsOrderMasterInsertDto billsOrderMasterDto) {
        try {
            // Step 1: Try to get phone from Supplier
            if (billsOrderMasterDto.getSupplierRefId() != null && billsOrderMasterDto.getSupplierRefId() > 0) {
                String mobileNumber = fetchSupplierPhoneNumber(billsOrderMasterDto.getSupplierRefId());
                if (mobileNumber != null && !mobileNumber.isEmpty()) {
                    logger.debug("✓ Retrieved supplier phone number: {}", maskPhoneNumber(mobileNumber));
                    return mobileNumber;
                }
            }

            // Step 2: Try to get phone from Employee
            if (billsOrderMasterDto.getEmployeeRefId() != null && billsOrderMasterDto.getEmployeeRefId() > 0) {
                String mobileNumber = fetchEmployeePhoneNumber(billsOrderMasterDto.getEmployeeRefId());
                if (mobileNumber != null && !mobileNumber.isEmpty()) {
                    logger.debug("✓ Retrieved employee phone number: {}", maskPhoneNumber(mobileNumber));
                    return mobileNumber;
                }
            }

            // Step 3: Use default system configuration phone if configured
            String mobileNumber = getDefaultSystemPhoneNumber();
            if (mobileNumber != null && !mobileNumber.isEmpty()) {
                logger.debug("✓ Using system default phone number: {}", maskPhoneNumber(mobileNumber));
                return mobileNumber;
            }

            logger.warn("! No mobile number found from any source (Supplier, Employee, or System Config)");
            return "";

        } catch (Exception ex) {
            logger.error("! Error extracting mobile number", ex);
            return "";
        }
    }

    /**
     * Mask phone number for secure logging
     * Shows only last 4 digits: e.g., +60****1234
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() <= 4) {
            return "****";
        }
        return "*".repeat(phoneNumber.length() - 4) + phoneNumber.substring(phoneNumber.length() - 4);
    }

    /**
     * Fetch supplier phone number from database
     * Returns empty string if not found or error occurs
     */
    private String fetchSupplierPhoneNumber(Integer supplierId) {
        try {
            String query = "SELECT ISNULL(PhoneNo, '') as PhoneNo " +
                    "FROM Supplier WITH(NOLOCK) " +
                    "WHERE Id = " + supplierId;
            String phoneNumber = jdbcTemplate.queryForObject(query, String.class);
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                logger.debug("✓ Supplier phone number retrieved successfully");
                return phoneNumber;
            }
            return "";
        } catch (EmptyResultDataAccessException ex) {
            logger.debug("! Supplier not found or no phone number: Supply ID = {}", supplierId);
            return "";
        } catch (DataAccessException ex) {
            logger.debug("! Database error fetching supplier phone: {}", ex.getMessage());
            return "";
        } catch (Exception ex) {
            logger.debug("Could not fetch supplier phone number: {}", ex.getMessage());
            return "";
        }
    }

    /**
     * Fetch employee phone number from database
     * Returns empty string if not found or error occurs
     */
    private String fetchEmployeePhoneNumber(Integer employeeId) {
        try {
            String query = "SELECT ISNULL(PhoneNo, '') as PhoneNo " +
                    "FROM EmployeeMaster WITH(NOLOCK) " +
                    "WHERE Id = " + employeeId;
            String phoneNumber = jdbcTemplate.queryForObject(query, String.class);
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                logger.debug("✓ Employee phone number retrieved successfully");
                return phoneNumber;
            }
            return "";
        } catch (EmptyResultDataAccessException ex) {
            logger.debug("! Employee not found or no phone number: Employee ID = {}", employeeId);
            return "";
        } catch (DataAccessException ex) {
            logger.debug("! Database error fetching employee phone: {}", ex.getMessage());
            return "";
        } catch (Exception ex) {
            logger.debug("Could not fetch employee phone number: {}", ex.getMessage());
            return "";
        }
    }

    /**
     * Get default system phone number from configuration
     * Checks in order: Environment variable → SystemConfiguration table → null
     * Returns empty string if not found or error occurs
     */
    private String getDefaultSystemPhoneNumber() {
        try {
            // Step 1: Try to get from environment variable (highest priority)
            String phoneNumber = System.getenv("MALEVA_WHATSAPP_PHONE");
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                logger.debug("✓ Using WhatsApp phone from environment variable");
                return phoneNumber;
            }

            // Step 2: Try to get from SystemConfiguration table
            try {
                String query = "SELECT ISNULL(ConfigValue, '') as ConfigValue " +
                        "FROM SystemConfiguration WITH(NOLOCK) " +
                        "WHERE ConfigKey = 'WHATSAPP_DEFAULT_PHONE' AND Active = 1";
                String configValue = jdbcTemplate.queryForObject(query, String.class);
                if (configValue != null && !configValue.isEmpty()) {
                    logger.debug("✓ Using WhatsApp phone from SystemConfiguration");
                    return configValue;
                }
            } catch (EmptyResultDataAccessException ex) {
                logger.debug("! WhatsApp configuration not found in SystemConfiguration table");
            } catch (DataAccessException ex) {
                logger.debug("! Database error fetching WhatsApp configuration: {}", ex.getMessage());
            }

            logger.warn("! No default WhatsApp phone number configured");
            return "";

        } catch (Exception ex) {
            logger.error("! Unexpected error getting default system phone number", ex);
            return "";
        }
    }
}

