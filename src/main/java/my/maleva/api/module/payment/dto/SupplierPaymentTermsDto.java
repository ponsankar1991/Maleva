package my.maleva.api.module.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Supplier Payment Terms query response
 * Returns: { id, supplierName, tDays, active }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierPaymentTermsDto {

    private Integer id;

    private String supplierName;

    private Integer tDays;

    private Integer active;

    private String termsName;
}

