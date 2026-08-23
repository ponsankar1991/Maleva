package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** One row of the toll entry list. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TollEntryListItemDto {

    private Integer id;
    private Integer cNumber;
    private String cNumberDisplay;

    private LocalDate saleDate;

    private Integer truckRefId;
    private String truckName;

    /** Header total, as stored. */
    private Double amount;

    /** How many toll transactions sit under this entry. */
    private Integer detailCount;

    private String remarks;
    private String filePath;
}
