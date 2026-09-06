package my.maleva.api.module.paymentrecept.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * ReceiptSaveRequest DTO
 * Equivalent to legacy .NET ReceiptModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptSaveRequest {

    @JsonAlias({"Id", "id"})
    @JsonProperty("id")
    private Integer id;

    @JsonAlias({"CompanyRefId", "companyRefId", "Comid", "comid"})
    @JsonProperty("companyRefId")
    private Integer companyRefId;

    @JsonAlias({"UserRefId", "userRefId"})
    @JsonProperty("userRefId")
    private Integer userRefId;

    @JsonAlias({"EmployeeRefId", "employeeRefId"})
    @JsonProperty("employeeRefId")
    private Integer employeeRefId;

    @JsonAlias({"LastEmployeeRefId", "lastEmployeeRefId"})
    @JsonProperty("lastEmployeeRefId")
    private Integer lastEmployeeRefId;

    @JsonAlias({"CustomerRefId", "customerRefId", "customerId", "CustomerId"})
    @JsonProperty("customerRefId")
    private Integer customerRefId;

    @JsonAlias({"BankRefId", "bankRefId", "bankId", "BankId"})
    @JsonProperty("bankRefId")
    private Integer bankRefId;

    @JsonAlias({"BankName1", "bankName1", "BankName", "bankName"})
    @JsonProperty("bankName")
    private String bankName;

    @JsonAlias({"AccountNo", "accountNo"})
    @JsonProperty("accountNo")
    private String accountNo;

    @JsonAlias({"ReceiptDate", "receiptDate"})
    @JsonProperty("receiptDate")
    private String receiptDate;

    @JsonAlias({"SReceiptDate", "sReceiptDate"})
    @JsonProperty("sReceiptDate")
    private String sReceiptDate;

    @JsonAlias({"Amount", "amount"})
    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonAlias({"CurrencyValue", "currencyValue"})
    @JsonProperty("currencyValue")
    private Double currencyValue;

    @JsonAlias({"ActualNetAmount", "actualNetAmount"})
    @JsonProperty("actualNetAmount")
    private Double actualNetAmount;

    @JsonAlias({"BankCharges", "bankCharges"})
    @JsonProperty("bankCharges")
    private Double bankCharges;

    @JsonAlias({"Remarks", "remarks"})
    @JsonProperty("remarks")
    private String remarks;

    @JsonAlias({"RefNumber", "refNumber"})
    @JsonProperty("refNumber")
    private String refNumber;

    @JsonAlias({"CNumber", "cNumber"})
    @JsonProperty("cNumber")
    private Integer cNumber;

    @JsonAlias({"CNumberDisplay", "cNumberDisplay"})
    @JsonProperty("cNumberDisplay")
    private String cNumberDisplay;

    @JsonAlias({"PVStatus", "pvStatus"})
    @JsonProperty("pvStatus")
    private Integer pvStatus;

    @JsonAlias({"Fileupload", "fileupload", "fileUpload", "FileUpload"})
    @JsonProperty("fileUpload")
    private Integer fileUpload;

    @JsonAlias({"TinNo", "tinNo"})
    @JsonProperty("tinNo")
    private String tinNo;

    @JsonAlias({"SSTNo", "sstNo"})
    @JsonProperty("sstNo")
    private String sstNo;

    @JsonAlias({"MsicCode", "msicCode"})
    @JsonProperty("msicCode")
    private String msicCode;

    @JsonAlias({"ServiceTaxType", "serviceTaxType"})
    @JsonProperty("serviceTaxType")
    private String serviceTaxType;

    @JsonAlias({"ReceiptDetails", "receiptDetails", "details", "Details"})
    @JsonProperty("receiptDetails")
    private List<ReceiptBillDto> receiptDetails;
}
