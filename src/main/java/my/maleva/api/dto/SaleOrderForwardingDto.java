package my.maleva.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SaleOrderForwardingDto - DTO for SaleOrderForwarding
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderForwardingDto {

    private Integer id;

    @NotNull(message = "Sale Order Master Reference ID is required")
    private Integer saleOrderMasterRefId;

    private LocalDateTime forwardingDate;

    @Size(max = 200, message = "Forwarding Name must not exceed 200 characters")
    private String forwardingName;

    @Size(max = 200, message = "Enter Ref must not exceed 200 characters")
    private String enterRef;

    @Size(max = 200, message = "SMK No must not exceed 200 characters")
    private String smkNo;

    private Integer sealByRefId;

    private BigDecimal sealAmount;

    private Integer breakSealByRefId;

    private BigDecimal breakSealAmount;

    @Size(max = 200, message = "Exit Ref must not exceed 200 characters")
    private String exitRef;

    private BigDecimal quantity;

    @Size(max = 200, message = "S1 must not exceed 200 characters")
    private String s1;

    @Size(max = 200, message = "S2 must not exceed 200 characters")
    private String s2;

    @NotNull(message = "Row Number is required")
    private Integer rowNumber;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;
}

