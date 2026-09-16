package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Which of our trucks are free on one day, answered while an order is being
 * entered.
 *
 * <p>{@link #freeCount} of 0 is what makes the dialog say "no truck available"
 * and offer Share, Outside truck or Cancel. {@link #takenTrucks} carries each
 * taken truck's orders so the dispatcher can see what a Share would ride with.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckAvailabilityDto {

    private LocalDate orderDate;

    /** The size class asked about, or null for any size. */
    private String sizeClass;

    /** Bookable trucks on this day - a truck in the workshop is not one of them. */
    private Integer totalTrucks;
    private Integer takenCount;
    private Integer freeCount;

    /** How many of the fleet are off the road that day, so the smaller total makes sense. */
    private Integer workshopTrucks;

    private List<OrderableTruckDto> freeTrucks;

    private List<TakenTruck> takenTrucks;

    /** Live OUTSIDE orders on the day; listed, never counted. */
    private List<TruckOrderDto> outsideOrders;

    /** A truck that already holds one or more OWN / SHARED orders on the day. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TakenTruck {
        private Integer id;
        private String truckName;
        private String sizeClass;
        private List<TruckOrderDto> orders;

        /** Pallet spaces the truck holds; null for a long loader. */
        private Integer palletCapacity;

        /**
         * Pallet spaces the orders already on it add up to.
         *
         * <p>Only the orders that actually recorded a quantity are in this sum -
         * see {@link #ordersWithoutQuantity}. It is an estimate for the
         * dispatcher to judge, and nothing refuses a booking because of it.
         */
        private java.math.BigDecimal usedSpaces;

        /**
         * How many of the orders on this truck have no quantity recorded.
         *
         * <p>Reported so the screen can say the total is incomplete rather than
         * show a confident figure built on missing data.
         */
        private Integer ordersWithoutQuantity;
    }
}
