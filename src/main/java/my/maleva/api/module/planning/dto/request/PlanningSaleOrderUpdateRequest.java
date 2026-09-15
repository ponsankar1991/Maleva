package my.maleva.api.module.planning.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Body of POST /api/planing/update-dates - the Planning screen's Update window.
 *
 * Carries only what that window edits. A blank date means its box was unticked and clears the
 * column. Totals, line items, customer, status and every other sale order field are absent on
 * purpose, so this save cannot change them.
 */
@Data
@NoArgsConstructor
public class PlanningSaleOrderUpdateRequest {

    @NotNull
    @Positive
    private Integer saleOrderId;

    @NotNull
    @Positive
    private Integer companyId;

    /** Written to LastEmployeeRefId; null or 0 clears it. */
    private Integer employeeId;

    private String pickupDate;

    private String deliveryDate;

    @Size(max = 200)
    private String origin;

    @Size(max = 200)
    private String destination;

    /** Location master id when origin was picked from the list; null or 0 when typed. */
    private Integer originRefId;

    private Integer destinationRefId;

    @Size(max = 100)
    private String quantity;

    @Size(max = 100)
    private String totalWeight;

    private String wareHouseEnterDate;

    private String wareHouseExitDate;

    @Size(max = 2000)
    private String wareHouseAddress;

    /**
     * The pickup stops on the form, in order. A stop with an id updates that SaleOrderPickup
     * row; a stop without one is inserted. Null leaves the job's pickup stops untouched.
     */
    @Valid
    private List<Stop> pickups;

    @Valid
    private List<Stop> deliveries;

    /**
     * Stops the user deleted on the form. Deletion is stated, never inferred from what is
     * missing: a stop another user added while the form was open is not on it and must stay.
     */
    private List<Integer> removedPickupIds;

    private List<Integer> removedDeliveryIds;

    @Data
    @NoArgsConstructor
    public static class Stop {

        /** SaleOrderPickup.Id / SaleOrderDelivery.Id; null or 0 for a new stop. */
        private Integer id;

        @Size(max = 2000)
        private String address;

        /** Blank when the stop's date box is unticked. */
        private String time;

        @Size(max = 100)
        private String weight;

        @Size(max = 100)
        private String quantity;
    }
}
