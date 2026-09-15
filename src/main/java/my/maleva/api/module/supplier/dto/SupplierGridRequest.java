package my.maleva.api.module.supplier.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The SupplierView grid's search — bound from query parameters.
 *
 * <p>Every name starts with two lower-case letters on purpose. Spring binds a
 * query parameter to the bean property Lombok's getter implies, and
 * {@code getSName()} would be the property {@code SName}: a {@code sName}
 * parameter would silently bind to nothing. Hence {@code symbolName}.
 */
@Data
@NoArgsConstructor
public class SupplierGridRequest {

    private Integer companyId;

    /** ALL or blank means every type; otherwise an exact SupplierType, as legacy SelectSupplier. */
    private String type;

    /** Free text matched anywhere in the grid's text columns, or an exact supplier Id. */
    private String keyword;

    // Per-column filters: "contains", case-insensitive under the table collation.
    private String accountCode;
    private String qneCode;
    private String supplierName;
    private String supplierType;
    private String symbolName;
    private String termsName;
    private String address1;
    private String address2;
    private String city;
    private String mobileNo;
    private String gstNo;

    /** "active" (Active = 1), "inactive" (Active = 0), or blank for both. Deleted rows never show. */
    private String active;

    /** One of the grid's column keys, or "id". Anything else falls back to the default. */
    private String sortBy;

    /** "asc" or "desc" (default). */
    private String sortDir;

    private Integer page;
    private Integer size;
}
