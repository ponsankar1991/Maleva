package my.maleva.api.module.inventory.recon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Move a job off the recon shelf and into repair, in-house or at a vendor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendForRepairRequestDto {

    @NotBlank(message = "Repair mode is required - IN_HOUSE or VENDOR")
    private String repairMode;

    /** Required for VENDOR, and rejected for IN_HOUSE. */
    private Integer vendorRefId;

    @Size(max = 50, message = "Vendor Doc No cannot exceed 50 characters")
    private String vendorDocNo;

    private LocalDateTime expectedDate;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    @NotBlank(message = "Modified By is required")
    @Size(max = 50, message = "Modified By cannot exceed 50 characters")
    private String modifiedBy;
}
