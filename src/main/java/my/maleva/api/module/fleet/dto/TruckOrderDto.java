package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * One truck order, as the calendar and the edit dialog both read it.
 *
 * The legacy screen had two payload shapes for the same row - the list query
 * and EditTruckOrderById returned identical columns - so there is one DTO here.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckOrderDto {

    private Integer id;
    private Integer companyRefId;

    private Integer cNumber;

    /** The printed order number, e.g. {@code ORD002600005}. */
    private String cNumberDisplay;

    /** Whole-day booking; the column carries no time. */
    private LocalDate orderDate;

    private Integer truckRefId;
    private String truckName;

    private Integer employeeRefId;

    private String status;
    private String remarks;
}
