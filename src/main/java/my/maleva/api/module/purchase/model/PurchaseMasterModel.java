package my.maleva.api.module.purchase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PurchaseMasterModel - Exact replica of .NET PurchaseMasterModel
 * Used for EditPurchaseMaster operation to maintain compatibility with .NET response structure
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseMasterModel {

    // Primary identifiers
    private int id; // (int, not null)
    private int sdId; // (int, not null)
    private int companyRefId; // (int, not null)

    // User and employee references
    private Integer userRefId; // (int, nullable)
    private Integer employeeRefId; // (int, nullable)

    // Invoice information
    private String invoiceNo; // (varchar, nullable)
    private LocalDateTime invoiceDate; // (datetime, not null)
    private String sInvoiceDate; // (varchar, not null)
    private int supplierRefId; // (int, not null)

    // Sale information
    private LocalDateTime saleDate; // (datetime, not null)
    private String sSaleDate; // (varchar, not null)
    private String saleType; // (varchar(50), not null)

    // Display and numbering
    private String cNumberDisplay; // (varchar(300), not null)
    private int cNumber; // (int, not null)

    // Financial amounts (using float to match .NET Single)
    private float coinage; // (real, not null)
    private float grossAmount; // (real, not null)
    private float taxAmount; // (real, not null)
    private float discountAmount; // (real, not null)
    private String remarks; // (varchar(300), not null)

    // Additional amounts
    private float plusAmount; // (real, not null)
    private float minusAmount; // (real, not null)
    private float amount; // (real, not null)

    // Status and audit
    private int active; // (int, not null)
    private LocalDateTime createdDate; // (datetime, not null)
    private String createdBy; // (varchar(50), not null)
    private LocalDateTime modifiedDate; // (datetime, not null)
    private String modifiedBy; // (varchar(50), not null)

    // Transport references
    private int truckRefId; // (int, nullable)
    private int driverRefId; // (int, nullable)

    // Additional fields
    private String description; // (varchar(50), not null)
    private String serialNo; // (varchar(50), not null)
    private Integer paymentTermsRefId; // (int, nullable) - changed to Integer to handle null values

    // Currency and amounts
    private float currencyValue; // (real, not null)
    private float actualAmount; // (real, not null)

    // Purchase order reference
    private Integer purchaseOrderMasterRefId; // (int, nullable)

    // Nested details
    private List<PurchaseDetailsModel> purchaseDetails;
}
