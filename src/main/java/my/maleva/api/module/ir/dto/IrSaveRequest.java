package my.maleva.api.module.ir.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Insert when {@code id} is null or 0, update otherwise.
 *
 * <p>Three things the caller cannot set, on purpose:
 * <ul>
 *   <li>{@code departmentName} - resolved server-side from the UserRoles enum,
 *       so the snapshot can never disagree with the id it was taken from.</li>
 *   <li>{@code active} - only the delete endpoint changes it.</li>
 *   <li>{@code filePath} - owned by /api/attachments, so re-saving the form
 *       cannot blank out documents uploaded after the record was created.</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrSaveRequest {

    /** Null or 0 inserts; anything else updates that row. */
    private Integer id;

    @NotNull(message = "companyRefId is required")
    private Integer companyRefId;

    @NotNull(message = "irDate is required")
    private LocalDateTime irDate;

    @NotNull(message = "irStatusRefId is required")
    private Integer irStatusRefId;

    @NotBlank(message = "description is required")
    private String description;

    @Size(max = 1000, message = "reason must be 1000 characters or fewer")
    private String reason;

    @NotNull(message = "departmentRefId is required")
    private Integer departmentRefId;

    @Size(max = 100)
    private String vesselName;

    private Integer truckRefId;

    /** Free text for a hired lorry with no TruckMaster row. Ignored when truckRefId is set. */
    @Size(max = 100)
    private String truckNo;

    private Integer employeeRefId;

    /** Ignored when employeeRefId is set. */
    @Size(max = 100)
    private String employeeName;

    private Integer driverRefId;

    /** The outside-driver case: a typed name with no DriverMaster row behind it. */
    @Size(max = 100)
    private String driverName;

    @PositiveOrZero(message = "actualAmount must not be negative")
    private Integer actualAmount;
}
