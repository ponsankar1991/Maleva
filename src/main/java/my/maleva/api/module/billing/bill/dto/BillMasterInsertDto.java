package my.maleva.api.module.billing.bill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bill save payload — the Java port of the legacy {@code SP_BillMaster} input
 * model. Id 0 (or null) inserts; anything else updates that bill.
 *
 * <p>{@code cNumber}/{@code cNumberDisplay} are ignored on insert: the screen
 * sends zeros and the server assigns the real document number from
 * SequenceNoMaster, so two clerks saving at once cannot collide.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillMasterInsertDto {

    private Integer id;

    private Integer companyRefId;
    private Integer userRefId;
    private Integer employeeRefId;

    @NotNull(message = "Supplier is required")
    private Integer supplierRefId;

    @NotNull(message = "Bill date is required")
    private LocalDateTime saleDate;

    private String invoiceNo;
    private LocalDateTime invoiceDate;
    private LocalDate dueDate;

    private String saleType;
    private String billStatus;
    private String description;
    private String remarks;

    @NotNull(message = "Payment terms are required")
    private Integer paymentTermsRefid;

    private Integer truckRefid;
    private Integer driverRefid;

    private Float coinage;
    private Float grossAmount;
    private Float taxAmount;
    private Float discountAmount;
    private Float plusAmount;
    private Float minusAmount;
    private Float amount;

    /** Supplier currency rate against the base currency. */
    private Float currencyValue;
    /** Rate the clerk typed on screen; drives the per-line conversion. */
    private Float currencyValue1;
    private Float actualAmount;

    /** Set when the bill was converted from a purchase order. */
    private Integer billsOrderMasterRefId;

    private List<BillDetailsInsertDto> billDetails;
}
