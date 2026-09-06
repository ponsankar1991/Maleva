package my.maleva.api.module.paymentrecept.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One document settled by a receipt, for the expandable row of the view grid
 * (legacy {@code ReceiptDetailViewModel}). {@code saleRefId} is the parent
 * receipt id, which is what legacy called it.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptViewDetailDto {
    private Integer detailId;
    private Integer saleRefId;
    private Integer saleMasterRefId;
    private Integer customerOpenRefId;
    /** Invoice number, blank for an opening-balance line. */
    private String saleNo;
    /** dd/MM/yyyy, blank for an opening-balance line. */
    private String sSaleDate;
    private String dCustomerName;
    private BigDecimal receiptAmount;
}
