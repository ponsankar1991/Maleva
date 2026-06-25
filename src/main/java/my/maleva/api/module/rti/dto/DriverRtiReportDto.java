package my.maleva.api.module.rti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverRtiReportDto {
    private Long id;
    private Integer comid;
    private Integer driverId;
    private Integer truckId;

    private String cNumberDisplay;
    private LocalDateTime saleDate;
    private String sSaleDate;

    private BigDecimal sleepingAmount;
    private String destination;
    private BigDecimal pickupAmount;
    private BigDecimal dropAmount;
    private BigDecimal exitAmount;
    private BigDecimal emptyDeliveryAmount;
    private BigDecimal manpwAmount;
    private BigDecimal amount;

    private String driverName;
    private String truckName;

    private Integer pckHandling;
    private Integer punctuality;
    private Integer documentSub;

    private LocalDateTime createdDate;
    private BigDecimal salary;
}

