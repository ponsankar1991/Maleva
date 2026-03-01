package my.maleva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * RTIDetailsDto
 * Data Transfer Object for RTIDetails API layer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RTIDetailsDto {

    private Integer id;

    @NotNull(message = "RTI Master Reference ID is required")
    private Integer rtiMasterRefId;

    @NotNull(message = "Sale Order Master Reference ID is required")
    private Integer saleOrderMasterRefId;

    @Min(value = 0, message = "Salary must be 0 or greater")
    private Double salary;

    @Size(max = 300, message = "PPIC cannot exceed 300 characters")
    private String ppic;

    @Size(max = 300, message = "DPIC cannot exceed 300 characters")
    private String dpic;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private Integer pwdType;

    private LocalDateTime pickupDateD;

    private LocalDateTime deliveryDateD;

    @Size(max = 250, message = "Origin cannot exceed 250 characters")
    private String originD;

    @Size(max = 250, message = "Destination cannot exceed 250 characters")
    private String destinationD;

    @Size(max = 2000, message = "Pickup Address cannot exceed 2000 characters")
    private String pickupAddressD;

    @Size(max = 2000, message = "Delivery Address cannot exceed 2000 characters")
    private String deliveryAddressD;

    @Size(max = 5000, message = "Pickup Address Timelist cannot exceed 5000 characters")
    private String pickupAddressTimelistD;

    @Size(max = 500, message = "Pickup Address Quantity cannot exceed 500 characters")
    private String pickupAddressQuantityD;

    @Size(max = 500, message = "Delivery Address Quantity cannot exceed 500 characters")
    private String deliveryAddressQuantityD;

    @Size(max = 500, message = "Delivery Address Datelist cannot exceed 500 characters")
    private String deliveryAddressdatelistD;
}

