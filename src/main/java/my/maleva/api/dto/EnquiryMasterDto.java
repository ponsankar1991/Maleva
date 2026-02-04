package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnquiryMasterDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    private Integer userRefId;
    private Integer employeeRefId;

    @NotNull
    private Integer customerRefId;

    @NotNull
    private Integer jobMasterRefId;

    private Integer agentCompanyRefId;
    private Integer agentMasterRefId;

    @NotNull
    private LocalDateTime saleDate;

    @NotBlank
    @Size(max = 50)
    private String billType;

    @NotBlank
    @Size(max = 50)
    private String saleType;

    @NotBlank
    @Size(max = 300)
    private String cNumberDisplay;

    @NotNull
    private Integer cNumber;

    @NotNull
    private Float coinage;

    @NotNull
    private Float grossAmount;

    @NotNull
    private Float taxAmount;

    @NotNull
    private Float discountAmount;

    @NotNull
    private Float plusAmount;

    @NotNull
    private Float minusAmount;

    @NotNull
    private Float amount;

    @Size(max = 300)
    private String remarks;

    @NotNull
    private Integer active;

    private LocalDateTime createdDate;

    @NotBlank
    @Size(max = 50)
    private String createdBy;

    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 50)
    private String modifiedBy;

    @Size(max = 200)
    private String offvesselname;

    @Size(max = 200)
    private String loadingvesselname;

    @Size(max = 200)
    private String sport;

    @Size(max = 200)
    private String vessel;

    @Size(max = 200)
    private String commodity;

    private LocalDateTime eta;
    private LocalDateTime etb;
    private LocalDateTime etd;

    private Integer docNo;
    private Integer invoiceNo;
    private Integer truckRefid;
    private Integer driverRefid;

    @Size(max = 100)
    private String awbNo;

    @Size(max = 100)
    private String blCopy;

    @Size(max = 100)
    private String quantity;

    @Size(max = 100)
    private String totalWeight;

    private Integer jStatus;
    private Integer oStatus;
    private Integer forkliftbyRefid;
    private Integer sealbyRefid;
    private Integer sealbreakbyRefid;

    private LocalDateTime pickupDate;
    private LocalDateTime deliveryDate;

    @Size(max = 2000)
    private String pickupAddress;

    @Size(max = 2000)
    private String deliveryAddress;

    @Size(max = 50)
    private String forwarding;

    @Size(max = 200)
    private String origin;

    @Size(max = 200)
    private String destination;

    @Size(max = 50)
    private String zb;

    private LocalDateTime oeta;
    private LocalDateTime oetb;
    private LocalDateTime oetd;

    private Integer oAgentCompanyRefId;
    private Integer oAgentMasterRefId;

    @Size(max = 500)
    private String doDescription;

    @Size(max = 200)
    private String scn;

    @Size(max = 200)
    private String truckSize;

    private Integer lastEmployeeRefId;

    private LocalDateTime wareHouseEnterDate;
    private LocalDateTime wareHouseExitDate;

    @Size(max = 2000)
    private String wareHouseAddress;

    private Integer boardingOfficerRefid;
    private Integer boardingOfficer1Refid;

    @NotNull
    private Float boardingAmount;

    @NotNull
    private Float boardingAmount1;

    @Size(max = 200)
    private String forwardingEnterRef;

    @Size(max = 200)
    private String forwardingExitRef;

    @Size(max = 200)
    private String portChargesRef;

    @NotNull
    private Float portCharges;

    @NotNull
    private Float sealAmount;

    @NotNull
    private Float breakSealAmount;

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

    @NotNull
    private Float sealAmount2;

    @NotNull
    private Float breakSealAmount2;

    @NotNull
    private Float sealAmount3;

    @NotNull
    private Float breakSealAmount3;

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
    private String ovessel;

    @Size(max = 200)
    private String oport;

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

    private Float currencyValue;
    private Float actualNetAmount;

    @Size(max = 300)
    private String remarks1;

    private LocalDateTime completedDate;

    @Size(max = 500)
    private String forwarding1S1;

    @Size(max = 500)
    private String forwarding1S2;

    @Size(max = 500)
    private String forwarding2S1;

    @Size(max = 500)
    private String forwarding2S2;

    @Size(max = 500)
    private String forwarding3S1;

    @Size(max = 500)
    private String forwarding3S2;

    @Size(max = 500)
    private String trucksize2;

    private Integer originRefId;
    private Integer destinationRefId;

    private LocalDateTime forwardingDate;
    private LocalDateTime forwarding2Date;
    private LocalDateTime forwarding3Date;

    @Size(max = 200)
    private String jobStatus;
}
