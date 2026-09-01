package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A truck the calendar will take orders for.
 *
 * Not every truck in TruckMaster qualifies. The legacy screen carried the subset
 * as a literal array of 14 plates inside truckordermaster.js; the rule is
 * {@code TruckMaster.OrderableTruck = 1} now, so the fleet is maintained as data
 * rather than by editing and redeploying JavaScript.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderableTruckDto {

    private Integer id;
    private String truckName;
    private String truckType;
}
