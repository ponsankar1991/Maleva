package my.maleva.api.module.fleet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * A fuel entry being created or updated.
 *
 * PAmount, GAmount and the four difference fields are deliberately absent: the
 * legacy screen computed them in JavaScript and posted them, so anyone could
 * send whatever numbers they liked. The server recomputes those from the litres
 * and the rate. AAmount is different - it is the receipt total and is keyed in.
 *
 * @see my.maleva.api.module.fleet.service.FuelVarianceCalculator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelEntrySaveRequest {

    /** Null or 0 creates a new entry; anything else updates that entry. */
    private Integer id;

    @NotNull(message = "Company is required")
    private Integer companyRefId;

    @NotNull(message = "Truck is required")
    private Integer truckRefId;

    private Integer driverRefId;

    private Integer employeeRefId;

    @NotNull(message = "Fuel date is required")
    private LocalDate saleDate;

    @NotNull(message = "Actual litres are required")
    @PositiveOrZero(message = "Actual litres cannot be negative")
    private Double aliter;

    /**
     * The amount on the fuel receipt. Keyed in, not derived from the litres and
     * the rate - the two routinely disagree, and the receipt is what was paid.
     */
    @PositiveOrZero(message = "Receipt amount cannot be negative")
    private Double aAmount;

    /** Patron litres - what the fuel patron billed. */
    @PositiveOrZero(message = "Patron litres cannot be negative")
    private Double pliter;

    @PositiveOrZero(message = "GPS litres cannot be negative")
    private Double gliter;

    @NotNull(message = "Rate is required")
    @PositiveOrZero(message = "Rate cannot be negative")
    private Double pRate;

    @Size(max = 2000, message = "Remarks must not exceed 2000 characters")
    private String remarks;

    private String filePath;

    /** 1 when the entry comes from the driver app, 0 from the web screen. */
    private Integer fStatus;
}
