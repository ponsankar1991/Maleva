package my.maleva.api.module.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * InvoiceResponseDTO
 * Response DTO for invoice queries
 * Combines master and detail information for comprehensive invoice view
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponseDTO {

    // Master invoice information
    private Integer id;
    private Integer companyRefId;
    private Integer customerRefId;
    private String customerName;
    private String invoiceNumber;
    private String cNumberDisplay;
    private LocalDateTime saleDate;
    private String saleType;
    private String billType;

    // Amounts
    private Double grossAmount;
    private Double taxAmount;
    private Double discountAmount;
    private Double amount;

    // Vessel and logistics information
    private String offVesselName;
    private String loadingVesselName;
    private String sPort;
    private String oPort;

    // Dates
    private LocalDateTime eta;
    private LocalDateTime etb;
    private LocalDateTime etd;
    private LocalDateTime oEta;
    private LocalDateTime oEtb;
    private LocalDateTime oEtd;

    // QNE Integration
    private String qneCode;
    private String qneId;

    // E-Invoice
    private String eInvoiceUid;
    private String eInvoiceLongId;

    // Status
    private Integer active;
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime modifiedDate;
    private String modifiedBy;

    // Line items
    private List<SaleDetailsDto> details;
}

