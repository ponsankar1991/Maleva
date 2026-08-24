package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** One row of the levi entry list, with its lookups already resolved to names. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassEntryListItemDto {

    private Integer id;

    /** Sequence number behind {@link #cNumberDisplay}. */
    private Integer cNumber;

    /** Printed levi number, e.g. {@code LE000000241}. */
    private String cNumberDisplay;

    private LocalDate saleDate;

    private Integer truckRefId;
    private String truckName;

    private Integer driverRefId;
    private String driverName;

    private Integer rtiRefId;
    /** The RTI's printed number, which is what the grid shows. */
    private String rtiNumber;

    private String enterLink;
    private String exitLink;

    private Double amount;
    private String remarks;
    private String filePath;
}
