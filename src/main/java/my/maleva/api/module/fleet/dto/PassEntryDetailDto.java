package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One levi entry, as the edit form and the print action need it.
 *
 * Replaces the legacy {@code select A.*} responses, which shipped every column
 * of the row - audit stamps, {@code LastEmployeeRefId}, {@code UserRefId} - to a
 * form that used eight of them.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassEntryDetailDto {

    private Integer id;
    private Integer companyRefId;

    private Integer cNumber;
    private String cNumberDisplay;

    private LocalDate saleDate;

    private Integer truckRefId;
    private String truckName;

    private Integer driverRefId;
    private String driverName;

    private Integer rtiRefId;
    private String rtiNumber;

    private Integer employeeRefId;

    private String enterLink;
    private String exitLink;

    private Double amount;
    private String remarks;

    /**
     * Attachment paths recorded on the row. The levi screen files its documents
     * under the {@code LeviEntry} attachment folder and lists them from there,
     * so this is usually blank on existing rows.
     */
    private String filePath;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
