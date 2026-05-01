package my.maleva.api.module.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * InvoiceSearchFilterDTO
 * Query parameters for advanced invoice search and filtering
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSearchFilterDTO {

    // Required filters
    private Integer companyId;

    // Optional filters
    private Integer customerId;
    private Integer employeeId;
    private Integer jobId;
    private Integer statusId;

    // Date range filters
    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    // Vessel filters
    private String offVesselName;
    private String loadingVesselName;

    // Search filters
    private String search;         // Can be invoice number or job number
    private Boolean searchInInvoice;  // true = search in invoice numbers, false = job numbers

    // Remarks filter (0=all, 1=with remarks, 2=without)
    private Integer remarksFilter;

    // Date filter type (1=OETA, 2=ETA, 0=both)
    private Integer etaType;
    private Boolean etaFilter;
    private Boolean pickupFilter;

    // Status filters
    private Boolean excludeCompleted;
    private Boolean onlyUnpushedQne;

    // Bill type and sale type
    private String billType;
    private String saleType;

    // Pagination
    private Integer page;
    private Integer size;
    private String sort;  // Format: "field,direction" (e.g., "id,desc")
}

