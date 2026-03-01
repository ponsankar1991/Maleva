package my.maleva.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SaleMasterDto
 * Data Transfer Object for SaleMaster API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleMasterDto {

    private Integer id;
    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;
    private Integer userRefId;
    private Integer employeeRefId;
    @NotNull(message = "Customer Reference ID is required")
    private Integer customerRefId;
    @NotNull(message = "Job Master Reference ID is required")
    private Integer jobMasterRefId;
    private Integer agentCompanyRefId;
    private Integer agentMasterRefId;
    @NotNull(message = "Sale Date is required")
    private LocalDateTime saleDate;
    @NotBlank(message = "Bill Type is required")
    @Size(max = 50, message = "Bill Type cannot exceed 50 characters")
    private String billType;
    @NotBlank(message = "Sale Type is required")
    @Size(max = 50, message = "Sale Type cannot exceed 50 characters")
    private String saleType;
    @NotBlank(message = "C Number Display is required")
    @Size(max = 300, message = "C Number Display cannot exceed 300 characters")
    private String cNumberDisplay;
    @NotNull(message = "C Number is required")
    private Integer cNumber;
    private Double coinage;
    private Double grossAmount;
    private Double taxAmount;
    private Double discountAmount;
    private Double plusAmount;
    private Double minusAmount;
    @NotNull(message = "Amount is required")
    private Double amount;
    @Size(max = 300, message = "Remarks cannot exceed 300 characters")
    private String remarks;
    private Integer active;
    private LocalDateTime createdDate;
    @Size(max = 50, message = "Created By cannot exceed 50 characters")
    private String createdBy;
    private LocalDateTime modifiedDate;
    @Size(max = 50, message = "Modified By cannot exceed 50 characters")
    private String modifiedBy;
    @Size(max = 200, message = "Offvessel Name cannot exceed 200 characters")
    private String offvesselname;
    @Size(max = 200, message = "Loading Vessel Name cannot exceed 200 characters")
    private String loadingvesselname;
    @Size(max = 200, message = "S Port cannot exceed 200 characters")
    private String sPort;
    @Size(max = 200, message = "Vessel cannot exceed 200 characters")
    private String vessel;
    @Size(max = 200, message = "Commodity cannot exceed 200 characters")
    private String commodity;
    private LocalDateTime eta;
    private LocalDateTime etb;
    private LocalDateTime etd;
    private Integer docNo;
    private Integer saleOrderMasterNo;
    private Integer truckRefid;
    private Integer driverRefid;
    @Size(max = 100, message = "AWB No cannot exceed 100 characters")
    private String awbNo;
    @Size(max = 100, message = "BL Copy cannot exceed 100 characters")
    private String blCopy;
    @Size(max = 100, message = "Quantity cannot exceed 100 characters")
    private String quantity;
    @Size(max = 100, message = "Total Weight cannot exceed 100 characters")
    private String totalWeight;
    private Integer jStatus;
    private Integer oStatus;
    private Integer forkliftbyRefid;
    private Integer sealbyRefid;
    private Integer sealbreakbyRefid;
    private LocalDateTime pickupDate;
    private LocalDateTime deliveryDate;
    @Size(max = 2000, message = "Pickup Address cannot exceed 2000 characters")
    private String pickupAddress;
    @Size(max = 2000, message = "Delivery Address cannot exceed 2000 characters")
    private String deliveryAddress;
    @Size(max = 50, message = "Forwarding cannot exceed 50 characters")
    private String forwarding;
    @Size(max = 200, message = "Origin cannot exceed 200 characters")
    private String origin;
    @Size(max = 200, message = "Destination cannot exceed 200 characters")
    private String destination;
    @Size(max = 50, message = "Zb cannot exceed 50 characters")
    private String zb;
    private LocalDateTime oeta;
    private LocalDateTime oetb;
    private LocalDateTime oetd;
    private Integer oAgentCompanyRefId;
    private Integer oAgentMasterRefId;
    @Size(max = 500, message = "DO Description cannot exceed 500 characters")
    private String doDescription;
    @Size(max = 200, message = "SCN cannot exceed 200 characters")
    private String scn;
    @Size(max = 200, message = "Truck Size cannot exceed 200 characters")
    private String truckSize;
    private Integer lastEmployeeRefId;
    private LocalDateTime wareHouseEnterDate;
    private LocalDateTime wareHouseExitDate;
    @Size(max = 2000, message = "Warehouse Address cannot exceed 2000 characters")
    private String wareHouseAddress;
    private Integer boardingOfficerRefid;
    private Integer boardingOfficer1Refid;
    private Double boardingAmount;
    private Double boardingAmount1;
    @Size(max = 200, message = "Forwarding Enter Ref cannot exceed 200 characters")
    private String forwardingEnterRef;
    @Size(max = 200, message = "Forwarding Exit Ref cannot exceed 200 characters")
    private String forwardingExitRef;
    @Size(max = 200, message = "Port Charges Ref cannot exceed 200 characters")
    private String portChargesRef;
    private Double portCharges;
    private Double sealAmount;
    private Double breakSealAmount;
    private Double currencyValue;
    private Double actualNetAmount;
    @Size(max = 300, message = "Remarks1 cannot exceed 300 characters")
    private String remarks1;
    @Size(max = 200)
    private String forwardingEnterRef2;
    @Size(max = 200)
    private String forwardingExitRef2;
    @Size(max = 200)
    private String forwardingEnterRef3;
    @Size(max = 200)
    private String forwardingExitRef3;
    @Size(max = 50)
    private String forwarding2;
    @Size(max = 50)
    private String forwarding3;
    @Size(max = 50)
    private String zb2;
    @Size(max = 200)
    private String zbRef;
    @Size(max = 200)
    private String zbRef2;
    private Double sealAmount2;
    private Double breakSealAmount2;
    private Double sealAmount3;
    private Double breakSealAmount3;
    private Integer sealbyRefid2;
    private Integer sealbreakbyRefid2;
    private Integer sealbyRefid3;
    private Integer sealbreakbyRefid3;
    @Size(max = 200)
    private String lscn;
    @Size(max = 200)
    private String cargo;
    @Size(max = 100)
    private String ptw;
    @Size(max = 200)
    private String oVessel;
    @Size(max = 200)
    private String oPort;
    private LocalDateTime boardingStartTime;
    private LocalDateTime boardingEndTime;
    @Size(max = 50)
    private String driverStatus;
    @Size(max = 200)
    private String forwardingSMKNo;
    @Size(max = 200)
    private String forwardingSMKNo2;
    @Size(max = 200)
    private String forwardingSMKNo3;
    @Size(max = 50)
    private String qneCode;
    @Size(max = 50)
    private String qneId;
    private Integer symbolRefId;
    @Size(max = 100)
    private String eInvoiceUid;
    @Size(max = 100)
    private String eInvoiceSUid;
    @Size(max = 100)
    private String eInvoiceLongId;
    private LocalDateTime eInvoicePushDT;
    @Size(max = 50)
    private String eInvoiceStatus;
    private LocalDateTime eInvoicePushVDT;
}

