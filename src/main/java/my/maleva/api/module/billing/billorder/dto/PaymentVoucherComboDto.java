package my.maleva.api.module.billing.billorder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PaymentVoucherComboDto - DTO for Payment Voucher Combo/Dropdown list
 * Used to fetch InvoiceNo and AccountName from BillsOrderMaster
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucherComboDto {
    private String accountName;
    private String invoiceNo;
}

