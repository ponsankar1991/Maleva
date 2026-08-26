package my.maleva.api.module.fleet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TruckMasterDto - DTO for TruckMaster
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckMasterDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    @NotBlank(message = "Truck Name is required")
    @Size(max = 200, message = "Truck Name must not exceed 200 characters")
    private String truckName;

    @NotBlank(message = "Truck Number is required")
    @Size(max = 100, message = "Truck Number must not exceed 100 characters")
    private String truckNumber;

    @NotBlank(message = "C Number Display is required")
    @Size(max = 300, message = "C Number Display must not exceed 300 characters")
    private String cNumberDisplay;

    @NotNull(message = "C Number is required")
    private Integer cNumber;

    @NotBlank(message = "Truck Type is required")
    @Size(max = 100, message = "Truck Type must not exceed 100 characters")
    private String truckType;

    @Size(max = 50, message = "Latitude must not exceed 50 characters")
    private String latitude;

    @Size(max = 50, message = "Longitude must not exceed 50 characters")
    private String longitude;

    @NotNull(message = "Active status is required")
    private Integer active;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private String modifiedBy;

    private LocalDate rotexMyExp;

    private LocalDate rotexSGExp;

    private LocalDate puspacomExp;

    private LocalDate insuranceExp;

    private LocalDate bonamExp;

    private LocalDate apadExp;

    private LocalDate rotexMyExp1;

    private LocalDate rotexSGExp1;

    private LocalDate puspacomExp1;

    @Size(max = 100, message = "Truck Number 1 must not exceed 100 characters")
    private String truckNumber1;

    private LocalDate serviceExp;

    private LocalDate alignmentExp;

    private LocalDate greeceExp;

    @NotNull(message = "Account Reference ID is required")
    private Integer accountRefid;

    private LocalDate gearOilExp;

    private LocalDate ptpStickerExp;

    private LocalDate alignmentLast;

    private LocalDate greeceLast;

    private LocalDate gearOilLast;

    private LocalDate serviceLast;

    @Size(max = 500, message = "SID Exp must not exceed 500 characters")
    private String sidExp;

    @Size(max = 300, message = "Vehicle Type must not exceed 300 characters")
    private String vehicleType;

    @Size(max = 50, message = "Last Service KM must not exceed 50 characters")
    private String lastServiceKM;

    @Size(max = 50, message = "Next Service KM must not exceed 50 characters")
    private String nextServiceKM;

    @Size(max = 50, message = "Next Odometer KM must not exceed 50 characters")
    private String nextodometerKm;

    private LocalDate batteryDate;

    // 1 = Maleva-owned truck, 0 = outside/subcontractor truck
    private Integer malevaTruck;

    // Account code from AccountsGroupMaster (optional)
    private String accountCode;
}

