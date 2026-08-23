package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * One toll entry with its transactions, for the edit form.
 *
 * The legacy EditTollEntry returned a flat join and stitched master to details
 * in C# with a Dapper multi-mapper and a dictionary. Nesting them here means
 * the shape says what it is.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TollEntryDetailDto {

    private Integer id;
    private Integer companyRefId;
    private String cNumberDisplay;
    private Integer cNumber;

    private LocalDate saleDate;

    private Integer truckRefId;
    private String truckName;
    private Integer employeeRefId;

    private Double amount;
    private String remarks;
    private String filePath;

    private List<TollEntryDetailRowDto> details;
}
