package my.maleva.api.module.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * One payment, loaded back into the screen.
 *
 * <p>{@code paymentDetails} is not merely the saved lines: it is the supplier's
 * full outstanding list with this payment's amounts filled back in, which is
 * what the grid needs in order to let the clerk move money between documents.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEditDto {

    private Integer id;

    private Integer companyRefId;

    private Integer supplierRefId;

    private Integer bankRefId;

    private Integer employeeRefId;

    private Integer userRefId;

    private LocalDateTime paymentDate;

    /** {@code dd/MM/yyyy}, for the date picker. */
    private String sPaymentDate;

    private Integer cNumber;

    private String cNumberDisplay;

    private String refNumber;

    private String payTo;

    private String description;

    private String remarks;

    private String paymentStatus;

    private Integer pvStatus;

    private BigDecimal amount;

    private Float bankCharges;

    private Float currencyValue;

    private Float actualAmount;

    private String tinNo;

    private String sstNo;

    private String msicCode;

    private String serviceTaxType;

    private String bankName;

    private String accountNo;

    private String qneCode;

    private String qneId;

    private List<SupplierBillDto> paymentDetails;
}
