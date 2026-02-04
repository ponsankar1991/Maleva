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
public class DoMasterDto {
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

    @Size(max = 1000)
    private String pickupAddress;

    @Size(max = 1000)
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

    private Integer saleOrderMasterNo;
}
