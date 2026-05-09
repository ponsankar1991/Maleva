package my.maleva.api.module.transaction.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    // Company ID - matches both frontend field names
    @JsonAlias({"Comid", "comId", "ComId"})
    private Integer comId;

    // Filter by customer
    @JsonAlias({"CustomerId", "customerId"})
    private Integer customerId;

    // Filter by job/job type
    @JsonAlias({"Jobid", "jobId", "JobId"})
    private Integer jobId;

    // Date range filters
    @JsonAlias({"Fromdate", "fromDate", "FromDate"})
    private LocalDate fromDate;
    @JsonAlias({"Todate", "toDate", "ToDate"})
    private LocalDate toDate;

    // Filter flags for date range
    @JsonAlias({"Pickupdate", "pickupDate", "PickupDate"})
    private Boolean pickupDate;  // If true, filter by PickupDate
    @JsonAlias({"ETA", "eta"})
    private Boolean eta;          // If true, filter by ETA/OETA
    @JsonAlias({"ETAType", "etaType"})
    private Integer etaType;      // 0=ETA, 1=OETA, 2=Both

    // Additional filters
    @JsonAlias({"DeliveryDone", "deliveryDone"})
    private Boolean deliveryDone; // Exclude delivery completed status
    @JsonAlias({"SPort", "sPort"})
    private String sPort;         // Source/origin port search
    @JsonAlias({"Search", "search"})
    private String search;        // Vessel name search

    // Additional date fields
    @JsonAlias({"Expdate", "expDate"})
    private String expDate;       // Expiry date
    @JsonAlias({"SFromDate", "sFromDate"})
    private String sFromDate;     // Search from date
    @JsonAlias({"ExpApadBonam", "expApadBonam"})
    private String expApadBonam;  // Expiry Apad Bonam

    // Additional fields
    @JsonAlias({"Id", "id"})
    private Integer id;           // Record ID
    @JsonAlias({"Cons", "cons"})
    private Boolean cons;         // Consolidation flag
}

