package my.maleva.api.module.paymentrecept.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptBillDto {
    private Integer companyRefId;
    private Integer sdId;
    private Integer sdId1;
    private Integer receiptRefId;
    private BigDecimal amount;
    private Integer saleCreditMasterRefId;
    private BigDecimal saleCreditAmount;
    private Integer id;
    private String customerName;
    private Integer saleMasterRefId;
    private Integer customeropenRefId;
    private String billNo;
    private LocalDateTime billDate;
    private String sBillDate;
    private BigDecimal billAmount;
    private BigDecimal receipt;
    private BigDecimal balance;
    private BigDecimal currencyValue;
    private BigDecimal actualAmount;

    // PascalCase getters for legacy frontend / JQXGrid compatibility
    @JsonProperty("SaleMasterRefId")
    public Integer getSaleMasterRefIdPascal() { return saleMasterRefId; }

    @JsonProperty("CustomeropenRefId")
    public Integer getCustomeropenRefIdPascal() { return customeropenRefId; }

    @JsonProperty("BillNo")
    public String getBillNoPascal() { return billNo; }

    @JsonProperty("BillDate")
    public LocalDateTime getBillDatePascal() { return billDate; }

    @JsonProperty("SBillDate")
    public String getSBillDatePascal() { return sBillDate; }

    @JsonProperty("BillAmount")
    public BigDecimal getBillAmountPascal() { return billAmount; }

    @JsonProperty("Receipt")
    public BigDecimal getReceiptPascal() { return receipt; }

    @JsonProperty("Balance")
    public BigDecimal getBalancePascal() { return balance; }

    @JsonProperty("Amount")
    public BigDecimal getAmountPascal() { return amount; }

    @JsonProperty("ActualAmount")
    public BigDecimal getActualAmountPascal() { return actualAmount; }

    @JsonProperty("CurrencyValue")
    public BigDecimal getCurrencyValuePascal() { return currencyValue; }

    @JsonProperty("CompanyRefId")
    public Integer getCompanyRefIdPascal() { return companyRefId; }

    @JsonProperty("CustomerName")
    public String getCustomerNamePascal() { return customerName; }

    @JsonProperty("SDId")
    public Integer getSdIdPascal() { return sdId; }

    @JsonProperty("SDId1")
    public Integer getSdId1Pascal() { return sdId1; }

    @JsonProperty("ReceiptRefId")
    public Integer getReceiptRefIdPascal() { return receiptRefId; }

    @JsonProperty("SaleCreditMasterRefId")
    public Integer getSaleCreditMasterRefIdPascal() { return saleCreditMasterRefId; }

    @JsonProperty("SaleCreditAmount")
    public BigDecimal getSaleCreditAmountPascal() { return saleCreditAmount; }
}
