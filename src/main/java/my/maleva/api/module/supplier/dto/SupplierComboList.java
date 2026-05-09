package my.maleva.api.module.supplier.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SupplierComboList - Simple DTO for supplier combo/dropdown lists
 * Contains ID and AccountName (Supplier Name + Mobile No) for lightweight API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierComboList {
    private Integer id;
    private String supplierName;
    private String accountName; // Combined: SupplierName + '-' + MobileNo
}

