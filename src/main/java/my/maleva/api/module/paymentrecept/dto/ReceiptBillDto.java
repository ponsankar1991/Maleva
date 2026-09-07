package my.maleva.api.module.paymentrecept.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    // The PascalCase getters below exist for output only; on input the
    // aliases let a legacy-shaped row ({"SaleMasterRefId":..,"Amount":..})
    // bind as well as the camelCase one the React screen sends. Without them
    // Jackson rejected "Amount" as an unknown field and the save answered 500.
    @JsonAlias("CompanyRefId")
    private Integer companyRefId;
    @JsonAlias("SDId")
    private Integer sdId;
    @JsonAlias("SDId1")
    private Integer sdId1;
    @JsonAlias("ReceiptRefId")
    private Integer receiptRefId;
    @JsonAlias("Amount")
    private BigDecimal amount;
    @JsonAlias("SaleCreditMasterRefId")
    private Integer saleCreditMasterRefId;
    @JsonAlias("SaleCreditAmount")
    private BigDecimal saleCreditAmount;
    @JsonAlias("Id")
    private Integer id;
    @JsonAlias("CustomerName")
    private String customerName;
    @JsonAlias("SaleMasterRefId")
    private Integer saleMasterRefId;
    @JsonAlias({"CustomeropenRefId", "customerOpenRefId", "CustomerOpenRefId"})
    private Integer customeropenRefId;
    @JsonAlias("BillNo")
    private String billNo;
    @JsonAlias("BillDate")
    private LocalDateTime billDate;
    @JsonAlias("SBillDate")
    private String sBillDate;
    @JsonAlias("BillAmount")
    private BigDecimal billAmount;
    @JsonAlias("Receipt")
    private BigDecimal receipt;
    @JsonAlias("Balance")
    private BigDecimal balance;
    @JsonAlias("CurrencyValue")
    private BigDecimal currencyValue;
    @JsonAlias("ActualAmount")
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
