package my.maleva.api.module.fleet.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A truck order being created or updated.
 *
 * The order number is absent on purpose. The legacy dialog showed one from
 * /TruckMaster/GetNextOrderNumber and posted it back as CNumberDisplay, but
 * SP_TruckOrderMaster threw it away and numbered the row itself, so the value
 * on screen was never the value that got saved. The server still assigns it.
 *
 * Which fields are required depends on {@code bookingType}, so that check lives
 * in the service rather than in annotations here: OWN and SHARED need a truck,
 * OUTSIDE needs an outside truck name instead.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckOrderSaveRequest {

    /** Null or 0 creates; anything else updates that order. */
    private Integer id;

    @NotNull(message = "Company is required")
    private Integer companyRefId;

    /** OWN, SHARED or OUTSIDE. Blank means OWN. */
    private String bookingType;

    /** Required for OWN and SHARED; ignored and stored as null for OUTSIDE. */
    private Integer truckRefId;

    /** Required for OUTSIDE; ignored for OWN and SHARED. */
    @Size(max = 100, message = "Outside truck name must not exceed 100 characters")
    private String outsideTruckName;

    /** Optional, OUTSIDE only. */
    @Size(max = 200, message = "Outside supplier must not exceed 200 characters")
    private String outsideSupplierName;

    /** The size the dispatcher searched for (e.g. 40FT). Optional; reporting only. */
    private String truckSizeClass;

    /** Who the order is for. Optional; 0 is treated as "nobody named". */
    private Integer customerRefId;

    /**
     * How much cargo. Optional, but a quantity and a unit travel together: one
     * without the other is rejected rather than half-stored.
     */
    @Positive(message = "Quantity must be more than zero.")
    @Digits(integer = 16, fraction = 2, message = "Quantity may have at most 2 decimal places")
    private BigDecimal quantity;

    /** PLT, PKG, IBC, BIN, DRUM, CTN, TON, CBM or TRUCK. */
    private String quantityUnit;

    /** Who entered it. 0 is stored as NULL, as the procedure did. */
    private Integer employeeRefId;

    @NotNull(message = "Please select Order Date.")
    private LocalDate orderDate;

    /** One of Pending, Confirmed, In Transit, Delivered. Defaults to Pending. */
    @Size(max = 30, message = "Status must not exceed 30 characters")
    private String status;

    @Size(max = 300, message = "Remarks must not exceed 300 characters")
    private String remarks;
}
