package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** One stored toll transaction. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TollEntryDetailRowDto {

    private Integer id;
    private Integer tollEntryMasterRefId;

    private Double entryAmount;
    private Double entryBalance;

    private LocalDate entryDate;
    private LocalDateTime entryTime;

    private String transType;
    private Integer vehicleClass;

    private String entrySP;
    private String entryLocation;
    private String exitSP;
    private String exitLocation;

    private String transNo;
    private String transactionId;
    private String vehicleNumber;
    private String mfgNumber;
}
