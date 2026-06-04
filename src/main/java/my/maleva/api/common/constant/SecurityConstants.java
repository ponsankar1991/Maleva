package my.maleva.api.common.constant;

/**
 * Security Constants - Centralized @PreAuthorize expressions
 *
 * Provides single source of truth for authorization rules across the application.
 * Uses Spring Security's @PreAuthorize annotation with role/authority-based checks.
 *
 * Benefits:
 * - Consistent authorization patterns across all controllers
 * - Easier maintenance and updates to security policies
 * - Reduced code duplication
 * - Prevents typos in role names
 * - Enables batch refactoring of authorization rules
 *
 * Usage Example:
 * @PreAuthorize(SecurityConstants.ROLE_ADMIN_SUPERADMIN_100)
 * public ResponseEntity<...> someMethod() { ... }
 *
 * @since 2.1
 */
public final class SecurityConstants {

    private SecurityConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }

    // ==================== Individual Role Patterns ====================

    // Super Admin & Admin only
    public static final String ROLE_ADMIN_SUPERADMIN =
        "hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN')";

    // Most common: Super Admin, Admin, and ROLE_100
    public static final String ROLE_ADMIN_SUPERADMIN_100 =
            "hasAuthority('ROLE_SUPERADMIN') or " +
                    "hasAuthority('ROLE_ADMIN') or " +
                    "hasAuthority('ROLE_100') or " +
                    "hasAuthority('ROLE_200') or " +
                    "hasAuthority('ROLE_300') or " +
                    "hasAuthority('ROLE_400') or " +
                    "hasAuthority('ROLE_500') or " +
                    "hasAuthority('ROLE_600') or " +
                    "hasAuthority('ROLE_700') or " +
                    "hasAuthority('ROLE_800') or " +
                    "hasAuthority('ROLE_900') or " +
                    "hasAuthority('ROLE_1000') or " +
                    "hasAuthority('ROLE_1100') or " +
                    "hasAuthority('ROLE_1200') or " +
                    "hasAuthority('ROLE_1300')";



    // Extended: Super Admin, Admin, ROLE_100, and ROLE_200
    public static final String ROLE_ADMIN_SUPERADMIN_100_200 =
        "hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100') or hasAuthority('ROLE_200')";

    // ==================== Specialized Role Combinations ====================

    // Tax Master (multiple roles)
    public static final String ROLE_TAX_MASTER_ACCESS =
        "hasAnyAuthority('ROLE_SUPERADMIN','ROLE_ADMIN','ROLE_CUSTOMERSERVICE','ROLE_OPERATIONADMIN','ROLE_BOARDINGOFFICER','ROLE_WAREHOUSE','ROLE_DRIVER','ROLE_HR','ROLE_ACCOUNTS','ROLE_PAYABLE','ROLE_RECEIVABLE','ROLE_MAINTENANCE','ROLE_USER')";

    // Job Type Master (hasAnyRole pattern)
    public static final String ROLE_JOB_TYPE_MASTER =
        "hasAnyRole('SUPERADMIN','ADMIN')";

    // Truck Combo Controller pattern
    public static final String ROLE_TRUCK_COMBO =
            "hasAuthority('ROLE_SUPERADMIN') or " +
                    "hasAuthority('ROLE_ADMIN') or " +
                    "hasAuthority('ROLE_100') or " +
                    "hasAuthority('ROLE_200') or " +
                    "hasAuthority('ROLE_300') or " +
                    "hasAuthority('ROLE_400') or " +
                    "hasAuthority('ROLE_500') or " +
                    "hasAuthority('ROLE_600') or " +
                    "hasAuthority('ROLE_700') or " +
                    "hasAuthority('ROLE_800') or " +
                    "hasAuthority('ROLE_900') or " +
                    "hasAuthority('ROLE_1000') or " +
                    "hasAuthority('ROLE_1100') or " +
                    "hasAuthority('ROLE_1200') or " +
                    "hasAuthority('ROLE_1300')";

    // Product Master (mixed pattern with duplicates normalized)
    public static final String ROLE_PRODUCT_MASTER =
        "hasAnyRole('ADMIN','SUPERADMIN') or hasAnyAuthority('ROLE_ADMIN','ROLE_SUPERADMIN','ROLE_100','ROLE_200')";

    // ==================== Permission Levels ====================

    // Super admin only
    public static final String ROLE_SUPERADMIN_ONLY =
        "hasAuthority('ROLE_SUPERADMIN')";

    // Admin or Super admin
    public static final String ROLE_ADMIN_OR_SUPERADMIN =
        "hasAnyAuthority('ROLE_ADMIN','ROLE_SUPERADMIN')";

    // User or Admin or Super admin
    public static final String ROLE_USER_ADMIN_SUPERADMIN =
        "hasAnyAuthority('ROLE_USER','ROLE_ADMIN','ROLE_SUPERADMIN')";

    // ==================== Public/Permit All ====================

    // NOTE: Use @PermitAll annotation directly instead of string constant
    // For endpoints that need no authentication
    // Example: @PermitAll public ResponseEntity<...> publicEndpoint() { ... }
}

