package my.maleva.api.module.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of the Planning Update window, in the shapes the planning grid already shows (dates as
 * "yyyy-MM-dd HH:mm", empty when cleared; packageType as "Quantity/TotalWeight"), so the screen
 * can put them on the job's grid lines without reloading the plan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningSaleOrderUpdateResponse {

    private boolean ok;

    private String message;

    private Integer saleOrderId;

    private String pickupDate;

    private String deliveryDate;

    private String origin;

    private String destination;

    private String quantity;

    private String totalWeight;

    private String packageType;

    private String wareHouseEnterDate;

    private String wareHouseExitDate;

    private String wareHouseAddress;

    private String pickupAddress;

    private String deliveryAddress;

    private String pickupQuantityList;

    private String deliveryQuantityList;

    private int pickupCount;

    private int deliveryCount;
}
