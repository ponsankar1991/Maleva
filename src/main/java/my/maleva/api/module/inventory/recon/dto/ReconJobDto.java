package my.maleva.api.module.inventory.recon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A recon job with its cost lines.
 *
 * Carries the resolved truck, vendor and product names alongside the ids, so
 * the screens do not have to fetch four more lookups to render one row.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconJobDto {

    private Integer id;
    private Integer companyRefId;
    private String reconNo;

    private Integer productRefId;
    private String productCode;
    private String productName;
    private Integer assetRefId;
    private String serialNo;

    /** Where the unit was removed from. */
    private Integer removedFromTruckRefId;
    private String removedFromTruckName;
    private Integer removedOnJobOrderRefId;
    private LocalDateTime removedDate;
    private String removedBy;
    private String faultDescription;

    /** What was fitted in its place. */
    private Integer replacedByProductRefId;
    private String replacedBySerialNo;
    private String replacedByCondition;

    private String repairMode;
    private Integer vendorRefId;
    private String vendorName;
    private String vendorDocNo;
    private LocalDateTime sentDate;
    private LocalDateTime expectedDate;
    private LocalDateTime receivedDate;

    private String status;

    private BigDecimal labourCost;
    private BigDecimal partsCost;
    private BigDecimal vendorCost;
    private BigDecimal otherCost;
    private BigDecimal totalCost;
    private BigDecimal resultingUnitCost;
    private BigDecimal newPartCost;

    /** newPartCost minus totalCost; null when no new-part cost was recorded. */
    private BigDecimal saving;

    /** Days from removal to return, or days open so far while still running. */
    private Integer daysInRecon;

    /** State of the unit itself, e.g. AWAITING_RECON or AVAILABLE. */
    private String assetStatus;
    private String assetCondition;
    private Integer assetReconCount;

    private String remarks;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String modifiedBy;

    private List<ReconCostDto> costs;
}
