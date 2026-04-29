package my.maleva.api.module.transaction.dto;

import lombok.*;
import java.time.LocalDate;

/**
 * Search/Filter model for Pre-Alert Report queries
 * Equivalent to TransactionViewModel in C# implementation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PreAlertSearchModel {

    // Company ID
    private Integer comId;

    // Filter by customer
    private Integer customerId;

    // Filter by job/job type
    private Integer jobId;

    // Date range filters
    private LocalDate fromDate;
    private LocalDate toDate;

    // Filter flags for date range
    private Boolean pickupDate;  // If true, filter by PickupDate
    private Boolean eta;          // If true, filter by ETA/OETA
    private Integer etaType;      // 0=ETA, 1=OETA, 2=Both

    // Additional filters
    private Boolean deliveryDone; // Exclude delivery completed status
    private String sPort;         // Source/origin port search
    private String oPort;         // Origin port search (alternative)
    private String search;        // Vessel name search

    // Pagination (optional)
    private Integer pageNo;
    private Integer pageSize;

    // Sorting
    private String sortBy;  // Default: SaleDate, Alternative: DETA (Date ETA)
    private String sortOrder; // ASC or DESC
}

