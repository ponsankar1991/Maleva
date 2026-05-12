package my.maleva.api.module.invoice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SaleInvoiceRequestDTO
 * Request DTO for creating/updating sale invoices
 * This is a more specific DTO than SaleMasterDto for invoice operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleInvoiceRequestDTO {

    @NotNull(message = "Company Reference ID is required")
    @Positive(message = "Company Reference ID must be positive")
    private Integer companyRefId;

    private Integer userRefId;

    private Integer employeeRefId;

    @NotNull(message = "Customer Reference ID is required")
    @Positive(message = "Customer Reference ID must be positive")
    private Integer customerRefId;

    @NotNull(message = "Job Master Reference ID is required")
    @Positive(message = "Job Master Reference ID must be positive")
    private Integer jobMasterRefId;

    private Integer agentCompanyRefId;

    private Integer agentMasterRefId;

    @NotNull(message = "Sale Date is required")
    private LocalDateTime saleDate;

    @NotNull(message = "Bill Type is required")
    @NotEmpty(message = "Bill Type cannot be empty")
    private String billType;

    @NotNull(message = "Sale Type is required")
    @NotEmpty(message = "Sale Type cannot be empty")
    private String saleType;

    private String remarks;

    private String remarks1;

    private String offVesselName;

    private String loadingVesselName;

    private String sPort;

    private String oPort;

    private LocalDateTime eta;

    private LocalDateTime etb;

    private LocalDateTime etd;

    private LocalDateTime oEta;

    private LocalDateTime oEtb;

    private LocalDateTime oEtd;

    private LocalDateTime pickupDate;

    private LocalDateTime deliveryDate;

    private Double grossAmount;

    private Double taxAmount;

    private Double discountAmount;

    private Double plusAmount;

    private Double minusAmount;

    // Line items/details
    private List<SaleInvoiceDetailRequestDTO> details;
}

