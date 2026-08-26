package my.maleva.api.module.joborder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * One purchase order raised against a workshop job order.
 *
 * A repair buys parts from several vendors, so a job normally lists more than
 * one of these - enough of each to recognise it and open it, not the whole PO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOrderPurchaseOrderDto {

    private Integer id;
    /** PO number as it reads on screen, e.g. BO000000123. */
    private String poNumber;
    private LocalDate poDate;
    private String supplierName;
    private String description;
    private Double amount;
    private String status;
    /** How many repair lines of this job the PO covers. */
    private Long lineCount;
}
