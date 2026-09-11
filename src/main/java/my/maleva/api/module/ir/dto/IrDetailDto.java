package my.maleva.api.module.ir.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One IR, as the form and the grid read it. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrDetailDto {

    private Integer id;
    private Integer companyRefId;

    private LocalDateTime irDate;

    private Integer irStatusRefId;
    private String statusCode;
    private String statusName;
    private String statusColor;

    private String description;
    private String reason;

    private Integer departmentRefId;
    private String departmentName;

    private String vesselName;

    private Integer truckRefId;
    private String truckNo;

    private Integer employeeRefId;
    private String employeeName;

    private Integer driverRefId;
    private String driverName;

    private Integer actualAmount;

    /** Who filed the report: the employee id, and the name for display. */
    private Integer createEmployeeRefId;
    private String createEmployeeName;

    /** Comma-joined paths as stored; the screen lists files through /api/attachments. */
    private String filePath;

    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
}
