package my.maleva.api.module.invoice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The full save payload for a sale invoice - the shape {@code SP_SaleMaster}
 * reads out of its {@code @master} JSON argument.
 *
 * <p>Every field the procedure names in its {@code OPENJSON ... WITH} block is
 * here, because the procedure re-writes the whole document on each save. A
 * field left out of this DTO is a column that silently becomes NULL or 0 on
 * every edit.
 *
 * <p>Fields the procedure reads but never writes are intentionally absent:
 * {@code CNumber} and {@code CNumberDisplay} are allocated server-side from
 * SequenceNoMaster and are commented out of the procedure's UPDATE, and
 * {@code SaleType} is forced to {@code 'CREDIT'} on both the INSERT and the
 * UPDATE regardless of what is sent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleInvoiceRequestDTO {

    /** 0 creates a new invoice; non-zero re-writes that invoice in place. */
    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    @Positive(message = "Company Reference ID must be positive")
    private Integer companyRefId;

    private Integer userRefId;

    private Integer employeeRefId;

    @NotNull(message = "Customer Reference ID is required")
    @Positive(message = "Customer Reference ID must be positive")
    private Integer customerRefId;

    @NotNull(message = "Job Master Reference ID is required")
    @Positive(message = "Job Master Reference ID must be positive")
    private Integer jobMasterRefId;

    private Integer agentCompanyRefId;

    private Integer agentMasterRefId;

    private Integer oAgentCompanyRefId;

    private Integer oAgentMasterRefId;

    @NotNull(message = "Sale Date is required")
    private LocalDate saleDate;

    /** Only written when the invoice is created; the procedure's UPDATE skips it. */
    private String billType;

    // ── amounts ────────────────────────────────────────────────────────────

    private Double grossAmount;

    private Double taxAmount;

    private Double discountAmount;

    private Double plusAmount;

    private Double minusAmount;

    private Double coinage;

    private Double amount;

    private Double currencyValue;

    private Double actualNetAmount;

    private Integer symbolRefId;

    // ── text ───────────────────────────────────────────────────────────────

    private String remarks;

    private String remarks1;

    private String doDescription;

    private String offVesselName;

    private String loadingVesselName;

    private String truckSize;

    private String sPort;

    private String oPort;

    private String scn;

    private String lscn;

    private String vessel;

    private String oVessel;

    private String commodity;

    private String cargo;

    private String awbNo;

    private String blCopy;

    private String quantity;

    private String totalWeight;

    private String ptw;

    private String origin;

    private String destination;

    // ── vessel schedule ────────────────────────────────────────────────────

    private LocalDateTime eta;

    private LocalDateTime etb;

    private LocalDateTime etd;

    private LocalDateTime oEta;

    private LocalDateTime oEtb;

    private LocalDateTime oEtd;

    // ── movement ───────────────────────────────────────────────────────────

    private LocalDateTime pickupDate;

    private LocalDateTime deliveryDate;

    private LocalDateTime wareHouseEnterDate;

    private LocalDateTime wareHouseExitDate;

    private String pickupAddress;

    private String deliveryAddress;

    private String wareHouseAddress;

    // ── references and status ──────────────────────────────────────────────

    /** Only written when the invoice is created; the procedure's UPDATE skips it. */
    private Integer docNo;

    private Integer saleOrderMasterNo;

    private Integer truckRefId;

    private Integer driverRefId;

    private Integer jStatus;

    private Integer oStatus;

    // ── operations staff ───────────────────────────────────────────────────

    private Integer forkliftByRefId;

    private Integer sealByRefId;

    private Integer sealBreakByRefId;

    private Integer sealByRefId2;

    private Integer sealBreakByRefId2;

    private Integer sealByRefId3;

    private Integer sealBreakByRefId3;

    private Integer boardingOfficerRefId;

    private Integer boardingOfficer1RefId;

    private Double boardingAmount;

    private Double boardingAmount1;

    // ── forwarding / seal / port charges ───────────────────────────────────

    private String forwarding;

    private String forwarding2;

    private String forwarding3;

    private String forwardingEnterRef;

    private String forwardingExitRef;

    private String forwardingEnterRef2;

    private String forwardingExitRef2;

    private String forwardingEnterRef3;

    private String forwardingExitRef3;

    private String forwardingSmkNo;

    private String forwardingSmkNo2;

    private String forwardingSmkNo3;

    private String portChargesRef;

    private Double portCharges;

    private Double sealAmount;

    private Double breakSealAmount;

    private Double sealAmount2;

    private Double breakSealAmount2;

    private Double sealAmount3;

    private Double breakSealAmount3;

    private String zb;

    private String zb2;

    private String zbRef;

    private String zbRef2;

    // ── children ───────────────────────────────────────────────────────────

    /** Line items. An empty list deletes every line on an edit. */
    private List<SaleInvoiceDetailRequestDTO> details;

    /**
     * The sale orders this invoice covers. The procedure stamps
     * {@code SaleOrderMaster.InvoiceNo} for each and writes one
     * SaleMasterReference row per entry; an edit clears both first.
     */
    private List<Integer> saleOrderRefIds;
}
