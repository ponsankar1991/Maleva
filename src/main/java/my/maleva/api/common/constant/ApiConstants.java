package my.maleva.api.common.constant;

/**
 * API Constants - Centralized constant definitions
 * 
 * Provides single source of truth for:
 * - Error messages
 * - Success messages
 * - Endpoint paths
 * - Validation messages
 * - Default values
 * 
 * Benefits:
 * - Easier maintenance and updates
 * - Consistent messaging across application
 * - Reduced code duplication
 * - Centralized configuration
 * 
 * @since 2.0
 */
public final class ApiConstants {

    // ==================== Suppress Constructor ====================
    private ApiConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }

    // ==================== API Endpoints ====================
    public static final class Endpoints {
        private Endpoints() {}
        
        public static final String API_V1 = "/api/v1";
        public static final String TRUCK_COMBO = "/truck-combo";
        public static final String TRUCK_COMBO_FULL = API_V1 + TRUCK_COMBO;
    }

    // ==================== HTTP Status Messages ====================
    public static final class Messages {
        private Messages() {}
        
        // Success Messages
        public static final String SUCCESS = "Success";
        public static final String TRUCK_COMBO_RETRIEVED = "Truck combo list retrieved successfully";
        public static final String RESOURCE_CREATED = "Resource created successfully";
        public static final String RESOURCE_UPDATED = "Resource updated successfully";
        public static final String RESOURCE_DELETED = "Resource deleted successfully";
        
        // Error Messages - Validation
        public static final String COMPANY_ID_REQUIRED = "Company ID is required and cannot be null";
        public static final String COMPANY_ID_INVALID = "Company ID is required and must be a positive integer";
        public static final String TRUCK_TYPE_INVALID = "Truck type must be between 1 and 50 characters";
        public static final String INVALID_REQUEST = "Invalid request parameters";
        public static final String VALIDATION_FAILED = "Request validation failed";
        
        // Error Messages - General
        public static final String RESOURCE_NOT_FOUND = "Requested resource not found";
        public static final String UNAUTHORIZED = "Unauthorized access";
        public static final String FORBIDDEN = "Forbidden - insufficient permissions";
        public static final String INTERNAL_ERROR = "Internal server error";
        public static final String SERVICE_UNAVAILABLE = "Service temporarily unavailable";
    }

    // ==================== Logging Templates ====================
    public static final class LogTemplates {
        private LogTemplates() {}
        
        // Request Logging
        public static final String REQUEST_TEMPLATE = "{} - Request: {}";
        public static final String REQUEST_WITH_PARAMS = "{} - companyId={}, type={}";
        public static final String EXECUTION_TIME = "Execution time: {} ms";
        
        // Success Logging
        public static final String SUCCESS_TEMPLATE = "Success: {}";
        public static final String RESOURCES_FOUND = "Found {} resource(s)";
        
        // Error Logging
        public static final String VALIDATION_ERROR = "Validation error: {}";
        public static final String BUSINESS_ERROR = "Business logic error: {}";
        public static final String SYSTEM_ERROR = "System error: {}";
        public static final String ERROR_WITH_EXCEPTION = "Error: {} - Exception: {}";
    }

    // ==================== Validation Messages ====================
    public static final class Validation {
        private Validation() {}
        
        public static final String COMPANY_ID_NOT_NULL = "Company ID is required and cannot be null";
        public static final String COMPANY_ID_POSITIVE = "Company ID must be a positive integer";
        public static final String TRUCK_TYPE_SIZE = "Truck type must be between 1 and 50 characters";
        public static final String REQUEST_BODY_REQUIRED = "Request body is required";
    }

    // ==================== Security & Authorization ====================
    public static final class Security {
        private Security() {}
        
        public static final String ROLE_ADMIN = "ROLE_ADMIN";
        public static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";
        public static final String ROLE_USER = "ROLE_USER";
        
        public static final String PREAUTHORIZE_COMBO_READ = 
            "hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_USER')";
        public static final String PREAUTHORIZE_ADMIN_ONLY = 
            "hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')";
    }

    // ==================== Default Values ====================
    public static final class Defaults {
        private Defaults() {}
        
        public static final int PAGE_SIZE_DEFAULT = 100;
        public static final int PAGE_SIZE_MIN = 1;
        public static final int PAGE_SIZE_MAX = 1000;
        
        public static final int TIMEOUT_SECONDS = 30;
        public static final int CACHE_TTL_MINUTES = 15;
        
        public static final String CHARSET = "UTF-8";
        public static final String MEDIA_TYPE = "application/json";
    }

    // ==================== CORS Configuration ====================
    public static final class Cors {
        private Cors() {}
        
        public static final String[] ALLOWED_ORIGINS = {"*"};
        public static final long MAX_AGE = 3600;
        public static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};
        public static final String[] ALLOWED_HEADERS = {"*"};
    }

    // ==================== Database ====================
    public static final class Database {
        private Database() {}
        
        public static final int ACTIVE_FLAG = 1;
        public static final int INACTIVE_FLAG = 0;
        public static final String DEFAULT_SORT_BY = "name";
    }

    // ==================== Field Lengths ====================
    public static final class FieldLengths {
        private FieldLengths() {}
        
        public static final int TRUCK_TYPE_MIN = 1;
        public static final int TRUCK_TYPE_MAX = 50;
        public static final int TRUCK_NAME_MIN = 1;
        public static final int TRUCK_NAME_MAX = 100;
        public static final int DESCRIPTION_MAX = 500;
    }
}

