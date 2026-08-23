package my.maleva.api.module.fleet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One toll transaction on a toll entry.
 *
 * These arrive as a batch pasted or imported from the toll operator's statement,
 * which is why the fields are the operator's own vocabulary rather than ours.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TollEntryDetailRequest {

    @NotNull(message = "Entry amount is required")
    private Double entryAmount;

    /** Card balance after the transaction, as printed on the statement. */
    private Double entryBalance;

    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;

    private LocalDateTime entryTime;

    /** Uppercased by the stored procedure on the way in. */
    @Size(max = 200)
    private String transType;

    private Integer vehicleClass;

    @Size(max = 200) private String entrySP;
    @Size(max = 200) private String entryLocation;
    @Size(max = 200) private String exitSP;
    @Size(max = 200) private String exitLocation;
    @Size(max = 200) private String transNo;
    @Size(max = 200) private String transactionId;
    @Size(max = 200) private String vehicleNumber;
    @Size(max = 200) private String mfgNumber;
}
