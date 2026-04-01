package my.maleva.api.module.saleorder.util;

/**
 * Centralizes sale-order constants so business rules and response messaging stay
 * consistent across the module.
 */
public final class SaleOrderApiConstants {

    public static final String DEFAULT_AUDIT_USER = "SYSTEM";
    public static final String DEFAULT_BILL_TYPE = "STANDARD";
    public static final String DEFAULT_SALE_TYPE = "STANDARD";
    public static final String SEQUENCE_NAME_PREFIX = "SaleOrderMaster";
    public static final String ORDER_NUMBER_PATTERN = "%09d";

    public static final int ACTIVE_STATUS = 1;
    public static final int INACTIVE_STATUS = 0;
    public static final int DEFAULT_STATUS_CODE = 200;
    public static final int CREATED_STATUS_CODE = 201;
    public static final int BAD_REQUEST_STATUS_CODE = 400;
    public static final int NOT_FOUND_STATUS_CODE = 404;
    public static final int INTERNAL_SERVER_ERROR_STATUS_CODE = 500;
    public static final int COMPLETED_JOB_STATUS = 8;

    public static final String CREATE_OPERATION = "CREATE";
    public static final String UPDATE_OPERATION = "UPDATE";
    public static final String DELETE_OPERATION = "DELETE";

    public static final String MESSAGE_CREATE_SUCCESS = "Sale order created successfully";
    public static final String MESSAGE_UPDATE_SUCCESS = "Sale order updated successfully";
    public static final String MESSAGE_DELETE_SUCCESS = "Sale order deleted successfully";
    public static final String MESSAGE_SELECT_SUCCESS = "Success";
    public static final String MESSAGE_COMPANY_REQUIRED = "Company ID is required and must be greater than zero";
    public static final String MESSAGE_FILTER_REQUIRED = "Filter payload is required";
    public static final String MESSAGE_FROM_DATE_INVALID = "From date cannot be after to date";
    public static final String MESSAGE_ORDER_NOT_FOUND = "Sale order not found with ID: %d";
    public static final String MESSAGE_SAVE_FAILED = "Failed to save sale order";
    public static final String MESSAGE_UPDATE_FAILED = "Failed to update sale order";
    public static final String MESSAGE_DELETE_FAILED = "Failed to delete sale order";
    public static final String MESSAGE_CNUMBER_REQUIRED = "C Number is required for update and must be positive";
    public static final String MESSAGE_COMPANY_REF_REQUIRED = "Company Reference ID is required and must be positive";
    public static final String MESSAGE_CUSTOMER_REF_REQUIRED = "Customer Reference ID is required and must be positive";
    public static final String MESSAGE_SALE_DATE_REQUIRED = "Sale date is required";
    public static final String MESSAGE_SEQUENCE_GENERATION_FAILED = "Failed to generate sale order sequence";
    public static final String MESSAGE_STATUS_LIST_INVALID = "Status list contains invalid values";
    public static final String MESSAGE_JOB_STATUS_REQUIRED = "Job status is required and must be positive";
    public static final String MESSAGE_JOB_STATUS_INVALID = "Job status is invalid for this company";
    public static final String MESSAGE_EDIT_LOOKUP_REQUIRED = "Either sale-order ID or sale-order number is required";
    public static final String MESSAGE_COMPANY_ID_REQUIRED = "Company ID is required and must be positive";
    public static final String MESSAGE_SALE_ORDER_NO_NOT_FOUND = "Invalid sale-order number";
    public static final String API_DETAILS_SELECT_SALE_ORDER = "Api Details: SaleOrder_SelectSaleOrder";
    public static final String RESPONSE_DATA3_SALE_F5_VIEW = "SaleF5View";

    private SaleOrderApiConstants() {
    }
}
