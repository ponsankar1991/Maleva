package my.maleva.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PickupDetailDTO - DTO for pickup details in a sale order
 * Represents pickup information for delivery
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickupDetailDTO {

    private Integer id;

    @NotNull(message = "Sale Order Master Reference ID is required")
    private Integer saleOrderMasterRefId;

    @Size(max = 2000, message = "Pickup Address must not exceed 2000 characters")
    private String pickupAddress;

    @Size(max = 100, message = "Pickup Time must not exceed 100 characters")
    private String pickupTime;

    @Size(max = 100, message = "Pickup Weight must not exceed 100 characters")
    private String pickupWeaight;

    @Size(max = 100, message = "Pickup Quantity must not exceed 100 characters")
    private String pickupQuantity;

    private Integer rowNumber;
}

