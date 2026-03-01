package my.maleva.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SaleOrderPickupDto - DTO for SaleOrderPickup
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderPickupDto {

    private Integer id;

    @NotNull(message = "Sale Order Master Reference ID is required")
    private Integer saleOrderMasterRefId;

    @Size(max = 2000, message = "Pickup Address must not exceed 2000 characters")
    private String pickupAddress;

    private LocalDateTime pickupTime;

    @Size(max = 100, message = "Pickup Weight must not exceed 100 characters")
    private String pickupWeight;

    @Size(max = 100, message = "Pickup Quantity must not exceed 100 characters")
    private String pickupQuantity;

    private LocalDateTime createdDate;
}

