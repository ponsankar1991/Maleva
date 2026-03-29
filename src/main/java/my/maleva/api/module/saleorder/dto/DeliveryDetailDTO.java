package my.maleva.api.module.saleorder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeliveryDetailDTO - DTO for delivery details in a sale order
 * Represents delivery information for ordered items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDetailDTO {

    private Integer id;

    @NotNull(message = "Sale Order Master Reference ID is required")
    private Integer saleOrderMasterRefId;

    @Size(max = 2000, message = "Delivery Address must not exceed 2000 characters")
    private String deliveryAddress;

    @Size(max = 100, message = "Delivery Time must not exceed 100 characters")
    private String deliveryTime;

    @Size(max = 100, message = "Delivery Weight must not exceed 100 characters")
    private String deliveryWeight;

    @Size(max = 100, message = "Delivery Quantity must not exceed 100 characters")
    private String deliveryQuantity;

    private Integer rowNumber;
}

