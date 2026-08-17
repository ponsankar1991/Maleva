package my.maleva.api.module.joborder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOrderResponseDto {

    private Integer id;
    
    private Integer companyRefId;
    
    private Integer cNumber;
    
    private String cNumberDisplay;

    private Integer employeeRefId;
    private String employeeName;

    private Integer truckMasterRefId;
    private String truckName;
    private String truckNumber;

    private Integer driverMasterRefId;
    private String driverName;

    private String vendorName;

    private Integer jobTypeRefId;
    private String jobTypeName;

    private String problemName;
    private String productUse;
    private String remarks;

    private Integer statusRefId;
    private String statusName;

    private Integer priorityRefId;
    private String priorityName;

    private BigDecimal odometerReading;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;

    private LocalDateTime jobDate;
    private LocalDateTime expectedCompletionDate;
    private LocalDateTime completedDate;

    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer modifiedBy;
    private LocalDateTime modifiedDate;

    private Boolean isActive;

    private Integer requestedBy;

    private java.util.List<JobOrderDetailResponseDto> details;
}
