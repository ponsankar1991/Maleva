package my.maleva.api.module.ceodashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "Request object for filtering CEO Dashboard data")
public class DashboardFilterRequestDto {
    
    @Schema(description = "List of Company IDs to filter by", example = "[1, 2]")
    private List<Integer> companyRefIds;
    
    @Schema(description = "List of Branch IDs to filter by", example = "[10, 11]")
    private List<Integer> branchRefIds;
    
    @Schema(description = "Start date for the data range", example = "2026-01-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @PastOrPresent(message = "From Date cannot be in the future")
    private LocalDate fromDate;
    
    @Schema(description = "End date for the data range", example = "2026-12-31")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
    
    @Schema(description = "List of Customer IDs to specifically filter", example = "[5001, 5002]")
    private List<Integer> customerRefIds;
    
    @Schema(description = "List of Sales Person IDs to filter by performance", example = "[12]")
    private List<Integer> salesPersonRefIds;
    
    @Schema(description = "Filter by Business Type", example = "Export")
    private String businessType;
    
    @Schema(description = "Filter by Trade Type", example = "FCL")
    private String tradeType;
    
    @Schema(description = "Filter by Job Type", example = "Forwarding")
    private String jobType;
    
    @Schema(description = "Filter by Service Type", example = "Door-to-Door")
    private String serviceType;
}
