package my.maleva.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "MainSetting")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "ChequePOPUP", nullable = false)
    private Integer chequePopup;

    @Column(name = "ReorderPOPUP", nullable = false)
    private Integer reorderPopup;

    @Column(name = "SaleDiscountAfterTax", nullable = false)
    private Integer saleDiscountAfterTax;

    @Column(name = "CashierLogin", nullable = false)
    private Integer cashierLogin;

    @Column(name = "CommonCompany", nullable = false)
    private Integer commonCompany;

    @Column(name = "SaveDislogPurchase", nullable = false)
    private Integer saveDislogPurchase;

    @Column(name = "SaveDislogPurchaseOrder", nullable = false)
    private Integer saveDislogPurchaseOrder;

    @Column(name = "SaveDislogPurchasereturn", nullable = false)
    private Integer saveDislogPurchasereturn;

    @Column(name = "SameProductSameLine", nullable = false)
    private Integer sameProductSameLine;

    @Column(name = "Product_Purchase", nullable = false)
    private Integer productPurchase;

    @Column(name = "Product_Sale", nullable = false)
    private Integer productSale;

    @Column(name = "WholeSaleRate", nullable = false)
    private Integer wholeSaleRate;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "SaleBillyesNo", nullable = false)
    private Integer saleBillyesNo;

    @Column(name = "LandingCostCompare", nullable = false)
    private Integer landingCostCompare;

    @Column(name = "DuplicateCopy", length = 100)
    private String duplicateCopy;

    @Column(name = "ExpDatePOPUP", nullable = false)
    private Integer expDatePopup;

    @Column(name = "ExpDateBeforeDays", nullable = false)
    private Integer expDateBeforeDays;

    @Column(name = "SaveDialogSale", nullable = false)
    private Integer saveDialogSale;

    @Column(name = "ReceiptBill", nullable = false)
    private Integer receiptBill;

    @Column(name = "PaymentBill", nullable = false)
    private Integer paymentBill;

    @Column(name = "BillPrintClosingBalance", nullable = false)
    private Integer billPrintClosingBalance;

    @Column(name = "ChequePOPDays", nullable = false)
    private Integer chequePopDays;

    @Column(name = "SupplierPaymentViewDialog", nullable = false)
    private Integer supplierPaymentViewDialog;

    @Column(name = "CustomerReceiptViewDialog", nullable = false)
    private Integer customerReceiptViewDialog;

    @Column(name = "CashSaveViewDialog", nullable = false)
    private Integer cashSaveViewDialog;

    @Column(name = "BankSaveViewDialog", nullable = false)
    private Integer bankSaveViewDialog;

    @Column(name = "PurchaseItemmasterSave", nullable = false)
    private Integer purchaseItemmasterSave;

    @Column(name = "StockTransferShowSaveDialog", nullable = false)
    private Integer stockTransferShowSaveDialog;

    @Column(name = "CommonCompanyDiffStock", nullable = false)
    private Integer commonCompanyDiffStock;

    @Column(name = "SupplierCommonCompany", nullable = false)
    private Integer supplierCommonCompany;

    @Column(name = "SupplierCommonCompanyCommonBalance", nullable = false)
    private Integer supplierCommonCompanyCommonBalance;

    @Column(name = "CustomerCommonCompany", nullable = false)
    private Integer customerCommonCompany;

    @Column(name = "CustomerCommonCompanyCommonBalance", nullable = false)
    private Integer customerCommonCompanyCommonBalance;

    @Column(name = "CustomerMulitipleAllow", nullable = false)
    private Integer customerMulitipleAllow;

    @Column(name = "SupplierMulitipleAllow", nullable = false)
    private Integer supplierMulitipleAllow;

    @Column(name = "BatchWiseStock", nullable = false)
    private Integer batchWiseStock;

    @Column(name = "EstimateBilling", nullable = false)
    private Integer estimateBilling;

    @Column(name = "TextilesSerialNowiseBilling", nullable = false)
    private Integer textilesSerialNowiseBilling;

    @Column(name = "AlwaysBatchCreatedAllItem", nullable = false)
    private Integer alwaysBatchCreatedAllItem;

    @Column(name = "BatchNoPerfix", length = 50)
    private String batchNoPerfix;

    @Column(name = "BatchNoDigit", nullable = false)
    private Integer batchNoDigit;

    @Column(name = "MultipleUOMBilling", nullable = false)
    private Integer multipleUOMBilling;

    @Column(name = "NomsQtyName", length = 50)
    private String nomsQtyName;

    @Column(name = "BatchNoName", length = 50)
    private String batchNoName;

    @Column(name = "PONo", nullable = false)
    private Integer poNo;

    @Column(name = "PODate", nullable = false)
    private Integer poDate;

    @Column(name = "DCNo", nullable = false)
    private Integer dcNo;

    @Column(name = "DCDate", nullable = false)
    private Integer dcDate;

    @Column(name = "LRNo", nullable = false)
    private Integer lrNo;

    @Column(name = "LRDate", nullable = false)
    private Integer lrDate;

    @Column(name = "VehicleNo", nullable = false)
    private Integer vehicleNo;

    @Column(name = "TransportName", nullable = false)
    private Integer transportName;

    @Column(name = "Through", nullable = false)
    private Integer through;

    @Column(name = "CourierName", nullable = false)
    private Integer courierName;

    @Column(name = "CourierNo", nullable = false)
    private Integer courierNo;

    @Column(name = "DriverName", nullable = false)
    private Integer driverName;

    @Column(name = "BillSaleType", nullable = false)
    private Integer billSaleType;

    @Column(name = "WorkingDate", nullable = false)
    private Integer workingDate;

    @Column(name = "MirrorTableOnline")
    private Integer mirrorTableOnline;

    @Column(name = "MComid")
    private Integer mComid;

    @Column(name = "ItemwiseCRMPoint")
    private Integer itemwiseCRMPoint;

    @Column(name = "SaleSubMaster")
    private Integer saleSubMaster;

    @Column(name = "SmallBillPrint")
    private Integer smallBillPrint;

    @Column(name = "A4BillPrint")
    private Integer a4BillPrint;

    @Column(name = "ProductNameTamil")
    private Integer productNameTamil;

    @Column(name = "CustomerNameTamil")
    private Integer customerNameTamil;

    @Column(name = "DayWiseSingleBill")
    private Integer dayWiseSingleBill;
}
