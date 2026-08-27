package my.maleva.api.module.billing.bill.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A bill reloaded for editing: header, its lines, and the pre-formatted
 * dd/MM/yyyy date strings the screen's date pickers read
 * ({@code sSaleDate}, {@code sInvoiceDate}, {@code sDueDate}), which the
 * legacy query supplied alongside the raw dates.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillMasterEditDto {

    private Integer id;
    private Integer companyRefId;
    private Integer userRefId;
    private Integer employeeRefId;
    private Integer supplierRefId;

    private LocalDateTime saleDate;
    private String sSaleDate;
    private String invoiceNo;
    private LocalDateTime invoiceDate;
    private String sInvoiceDate;
    private LocalDate dueDate;
    private String sDueDate;

    private String saleType;
    private String billStatus;
    private String cNumberDisplay;
    private Integer cNumber;

    private Float coinage;
    private Float grossAmount;
    private Float taxAmount;
    private Float discountAmount;
    private Float plusAmount;
    private Float minusAmount;
    private Float amount;

    private String remarks;
    private String description;
    private Integer paymentTermsRefid;
    private Float currencyValue;
    private Float currencyValue1;
    private Float actualAmount;
    private Integer truckRefid;
    private Integer driverRefid;
    private Integer billsOrderMasterRefId;
    private Integer active;

    private String qneCode;
    private String qneId;

    private List<BillDetailsEditDto> billDetails;
}
