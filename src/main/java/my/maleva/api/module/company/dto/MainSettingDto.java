package my.maleva.api.module.company.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainSettingDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    private Boolean chequePopup;

    @NotNull
    private Boolean reorderPopup;

    @NotNull
    private Boolean saleDiscountAfterTax;

    @NotNull
    private Boolean cashierLogin;

    @NotNull
    private Boolean commonCompany;

    @NotNull
    private Boolean saveDislogPurchase;

    @NotNull
    private Boolean saveDislogPurchaseOrder;

    @NotNull
    private Boolean saveDislogPurchasereturn;

    @NotNull
    private Boolean sameProductSameLine;

    @NotNull
    private Boolean productPurchase;

    @NotNull
    private Boolean productSale;

    @NotNull
    private Boolean wholeSaleRate;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotNull
    @Size(max = 50)
    private String modifiedBy;

    @NotNull
    private Boolean saleBillyesNo;

    @NotNull
    private Boolean landingCostCompare;

    @Size(max = 100)
    private String duplicateCopy;

    @NotNull
    private Boolean expDatePopup;

    @NotNull
    private Integer expDateBeforeDays;

    @NotNull
    private Boolean saveDialogSale;

    @NotNull
    private Boolean receiptBill;

    @NotNull
    private Boolean paymentBill;

    @NotNull
    private Boolean billPrintClosingBalance;

    @NotNull
    private Integer chequePopDays;

    @NotNull
    private Boolean supplierPaymentViewDialog;

    @NotNull
    private Boolean customerReceiptViewDialog;

    @NotNull
    private Boolean cashSaveViewDialog;

    @NotNull
    private Boolean bankSaveViewDialog;

    @NotNull
    private Boolean purchaseItemmasterSave;

    @NotNull
    private Boolean stockTransferShowSaveDialog;

    @NotNull
    private Boolean commonCompanyDiffStock;

    @NotNull
    private Boolean supplierCommonCompany;

    @NotNull
    private Boolean supplierCommonCompanyCommonBalance;

    @NotNull
    private Boolean customerCommonCompany;

    @NotNull
    private Boolean customerCommonCompanyCommonBalance;

    @NotNull
    private Boolean customerMulitipleAllow;

    @NotNull
    private Boolean supplierMulitipleAllow;

    @NotNull
    private Boolean batchWiseStock;

    @NotNull
    private Boolean estimateBilling;

    @NotNull
    private Boolean textilesSerialNowiseBilling;

    @NotNull
    private Boolean alwaysBatchCreatedAllItem;

    @Size(max = 50)
    private String batchNoPerfix;

    @NotNull
    private Integer batchNoDigit;

    @NotNull
    private Boolean multipleUOMBilling;

    @Size(max = 50)
    private String nomsQtyName;

    @Size(max = 50)
    private String batchNoName;

    @NotNull
    private Boolean poNo;

    @NotNull
    private Boolean poDate;

    @NotNull
    private Boolean dcNo;

    @NotNull
    private Boolean dcDate;

    @NotNull
    private Boolean lrNo;

    @NotNull
    private Boolean lrDate;

    @NotNull
    private Boolean vehicleNo;

    @NotNull
    private Boolean transportName;

    @NotNull
    private Boolean through;

    @NotNull
    private Boolean courierName;

    @NotNull
    private Boolean courierNo;

    @NotNull
    private Boolean driverName;

    @NotNull
    private Boolean billSaleType;

    @NotNull
    private Boolean workingDate;

    private Integer mirrorTableOnline;

    private Integer mComid;

    private Boolean itemwiseCRMPoint;

    private Boolean saleSubMaster;

    private Boolean smallBillPrint;

    private Boolean a4BillPrint;

    private Boolean productNameTamil;

    private Boolean customerNameTamil;

    private Boolean dayWiseSingleBill;
}
