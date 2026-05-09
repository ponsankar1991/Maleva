package my.maleva.api.module.billing.billorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for CheckInvoiceNo/PaymentVoucherCombo response
 * Equivalent to .NET PaymentVouchercombo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInvoiceNoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String invoiceNo;
}

