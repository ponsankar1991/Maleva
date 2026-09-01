package my.maleva.api.module.fleet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * A truck order being created or updated.
 *
 * The order number is absent on purpose. The legacy dialog showed one from
 * /TruckMaster/GetNextOrderNumber and posted it back as CNumberDisplay, but
 * SP_TruckOrderMaster threw it away and numbered the row itself, so the value
 * on screen was never the value that got saved. The server still assigns it.
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

    @NotNull(message = "Please select a Truck.")
    private Integer truckRefId;

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
