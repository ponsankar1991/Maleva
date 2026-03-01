package my.maleva.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SaleOrderDeliveryDto - DTO for SaleOrderDelivery
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderDeliveryDto {

    private Integer id;

    @NotNull(message = "Sale Order Master Reference ID is required")
    private Integer saleOrderMasterRefId;

    @NotBlank(message = "Delivery Address is required")
    @Size(max = 2000, message = "Delivery Address must not exceed 2000 characters")
    private String deliveryAddress;

    private LocalDateTime deliveryTime;

    @Size(max = 100, message = "Delivery Weight must not exceed 100 characters")
    private String deliveryWeight;

    @Size(max = 100, message = "Delivery Quantity must not exceed 100 characters")
    private String deliveryQuantity;

    private LocalDateTime createdDate;
}

