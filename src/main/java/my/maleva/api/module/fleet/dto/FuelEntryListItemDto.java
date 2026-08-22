package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** One row of the fuel entry list. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelEntryListItemDto {

    private Integer id;
    private Integer cNumber;
    private String cNumberDisplay;

    private LocalDate saleDate;

    private Integer truckRefId;
    private String truckName;
    private Integer driverRefId;
    private String driverName;

    private String remarks;
    private String filePath;

    private Double pRate;

    /** Actual litres from the receipt, and the money for them. */
    private Double aliter;
    private Double aAmount;

    /** Patron litres - what the fuel patron billed. */
    private Double pliter;
    private Double pAmount;

    /** GPS litres. */
    private Double gliter;
    private Double gAmount;

    /** Patron minus GPS - the loss the screen exists to show. */
    private Double diffLiter;
    private Double diffAmount;

    /** True when diffLiter is a loss, so the UI paints it red. */
    private boolean adverse;

    /** 1 when the row came from the driver app, 0 from the web screen. */
    private Integer fStatus;
}
