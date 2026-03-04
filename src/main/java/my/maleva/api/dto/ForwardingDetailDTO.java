package my.maleva.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ForwardingDetailDTO - DTO for forwarding details in a sale order
 * Represents forwarding/logistics information for items in transit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForwardingDetailDTO {

    private Integer id;

    @NotNull(message = "Sale Order Master Reference ID is required")
    private Integer saleOrderMasterRefId;

    @Size(max = 100, message = "Forwarding Date must not exceed 100 characters")
    private String forwardingDate;

    private String forwardingName;

    @Size(max = 200, message = "Enter Reference must not exceed 200 characters")
    private String enterRef;

    @Size(max = 100, message = "SMK Number must not exceed 100 characters")
    private String smkNo;

    private Integer sealByRefId;

    @Size(max = 100, message = "Seal Amount must not exceed 100 characters")
    private String sealAmount;

    private Integer breakSealByRefId;

    @Size(max = 100, message = "Break Seal Amount must not exceed 100 characters")
    private String breakSealAmount;

    @Size(max = 200, message = "Exit Reference must not exceed 200 characters")
    private String exitRef;

    @Size(max = 100, message = "Quantity must not exceed 100 characters")
    private String quantity;

    private Integer s1;

    private Integer s2;

    private Integer rowNumber;
}

