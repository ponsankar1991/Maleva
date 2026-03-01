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
 * TollEntryDetailsDto - DTO for TollEntryDetails
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TollEntryDetailsDto {

    private Integer id;

    @NotNull(message = "Toll Entry Master Reference ID is required")
    private Integer tollEntryMasterRefId;

    @NotNull(message = "Entry Amount is required")
    private Float entryAmount;

    @NotNull(message = "Entry Date is required")
    private LocalDateTime entryDate;

    @NotNull(message = "Entry Time is required")
    private LocalDateTime entryTime;

    @NotBlank(message = "Transaction Type is required")
    @Size(max = 200, message = "Transaction Type must not exceed 200 characters")
    private String transType;

    @NotNull(message = "Entry Balance is required")
    private Float entryBalance;

    private Integer vehicleClass;

    @Size(max = 200, message = "Exit SP must not exceed 200 characters")
    private String exitSP;

    @Size(max = 200, message = "Exit Location must not exceed 200 characters")
    private String exitLocation;

    @Size(max = 200, message = "Entry SP must not exceed 200 characters")
    private String entrySP;

    @Size(max = 200, message = "Entry Location must not exceed 200 characters")
    private String entryLocation;

    @Size(max = 200, message = "Transaction No must not exceed 200 characters")
    private String transNo;

    @Size(max = 200, message = "Transaction ID must not exceed 200 characters")
    private String transactionID;

    @Size(max = 200, message = "Vehicle Number must not exceed 200 characters")
    private String vehicleNumber;

    @Size(max = 200, message = "MFG Number must not exceed 200 characters")
    private String mfgNumber;
}

