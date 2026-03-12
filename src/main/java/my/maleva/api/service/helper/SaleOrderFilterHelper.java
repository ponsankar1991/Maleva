package my.maleva.api.service.helper;

import my.maleva.api.dto.SaleOrderFilterDTO;
import my.maleva.api.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SaleOrderFilterHelper - Helper class for building and validating SaleOrder filters
 * Provides utility methods for filter parameter validation and processing
 * 
 * Separates filter logic from service layer for better readability and maintainability
 * 
 * @author Enterprise Java Team
 * @version 1.0
 * @since 2026-03-12
 */
@Component
public class SaleOrderFilterHelper {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderFilterHelper.class);

    private static final Integer DEFAULT_COMPANY_ID = 6;
    private static final String ISNULL_DEFAULT_DATE = "1900-01-01";

    /**
     * Validate filter parameters and set defaults where needed
     * 
     * @param filter the SaleOrderFilterDTO to validate
     * @throws IllegalArgumentException if critical parameters are missing
     */
    public void validateFilter(SaleOrderFilterDTO filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter cannot be null");
        }

        if (filter.getComid() == null || filter.getComid() <= 0) {
            logger.warn("Company ID is null or invalid, using default: {}", DEFAULT_COMPANY_ID);
            filter.setComid(DEFAULT_COMPANY_ID);
        }

        // Ensure date fields have valid values
        if (filter.getFromdate() == null) {
            logger.debug("FromDate is null, setting to today");
            filter.setFromdate(LocalDate.now());
        }

        if (filter.getTodate() == null) {
            logger.debug("ToDate is null, setting to today");
            filter.setTodate(LocalDate.now());
        }

        logger.debug("Filter validation completed - Company: {}, FromDate: {}, ToDate: {}",
                filter.getComid(), filter.getFromdate(), filter.getTodate());
    }

    /**
     * Check if customer filter is active (Id != 0)
     * 
     * @param filter the filter to check
     * @return true if customer filtering is enabled
     */
    public boolean isCustomerFilterActive(SaleOrderFilterDTO filter) {
        return filter != null && filter.getId() != null && filter.getId() != 0;
    }

    /**
     * Check if job master filter is active (JId != 0)
     * 
     * @param filter the filter to check
     * @return true if job master filtering is enabled
     */
    public boolean isJobMasterFilterActive(SaleOrderFilterDTO filter) {
        return filter != null && filter.getJId() != null && filter.getJId() != 0;
    }

    /**
     * Check if employee filter is active (Employeeid != 0)
     * 
     * @param filter the filter to check
     * @return true if employee filtering is enabled
     */
    public boolean isEmployeeFilterActive(SaleOrderFilterDTO filter) {
        return filter != null && filter.getEmployeeid() != null && filter.getEmployeeid() != 0;
    }

    /**
     * Check if status list filter is active (statusList != null and not empty)
     * 
     * @param filter the filter to check
     * @return true if status list filtering is enabled
     */
    public boolean isStatusListFilterActive(SaleOrderFilterDTO filter) {
        return filter != null && filter.getStatusList() != null && !filter.getStatusList().trim().isEmpty();
    }

    /**
     * Check if single status filter is active (Statusid != 0)
     * 
     * @param filter the filter to check
     * @return true if single status filtering is enabled
     */
    public boolean isStatusIdFilterActive(SaleOrderFilterDTO filter) {
        return filter != null && filter.getStatusid() != null && filter.getStatusid() != 0;
    }

    /**
     * Check if remarks filter is active (Remarks == 1 or Remarks == 2)
     * 
     * @param filter the filter to check
     * @return true if remarks filtering is enabled
     */
    public boolean isRemarksFilterActive(SaleOrderFilterDTO filter) {
        return filter != null && filter.getRemarks() != null &&
                (filter.getRemarks() == 1 || filter.getRemarks() == 2);
    }

    /**
     * Check if vessel name filters are active
     * 
     * @param filter the filter to check
     * @return true if any vessel name filtering is enabled
     */
    public boolean isVesselNameFilterActive(SaleOrderFilterDTO filter) {
        return filter != null && (
                (filter.getOffvesselname() != null && !filter.getOffvesselname().trim().isEmpty()) ||
                (filter.getLoadingvesselname() != null && !filter.getLoadingvesselname().trim().isEmpty())
        );
    }

    /**
     * Check if search filter is active (Search != "" and Search != null)
     * 
     * @param filter the filter to check
     * @return true if search filtering is enabled
     */
    public boolean isSearchFilterActive(SaleOrderFilterDTO filter) {
        return filter != null && filter.getSearch() != null && !filter.getSearch().trim().isEmpty();
    }

    /**
     * Check if ETA filter is active (ETA == true)
     * 
     * @param filter the filter to check
     * @return true if ETA filtering is enabled
     */
    public boolean isEtaFilterActive(SaleOrderFilterDTO filter) {
        return filter != null && filter.getEta() != null && filter.getEta();
    }

    /**
     * Check if date filter is active (when not using search)
     * Checks for ETA, Pickup, or default SaleDate filtering
     * 
     * @param filter the filter to check
     * @return true if any date filtering is enabled
     */
    public boolean isDateFilterActive(SaleOrderFilterDTO filter) {
        return filter != null && !isSearchFilterActive(filter);
    }

    /**
     * Parse comma-separated status list into integer array
     * Used for building IN clause conditions
     * 
     * @param statusList comma-separated status IDs (e.g., "1,2,3")
     * @return array of status IDs
     */
    public Integer[] parseStatusList(String statusList) {
        if (statusList == null || statusList.trim().isEmpty()) {
            return new Integer[0];
        }

        try {
            return Arrays.stream(statusList.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toArray(Integer[]::new);
        } catch (NumberFormatException e) {
            logger.warn("Error parsing status list: {}, returning empty array", statusList);
            return new Integer[0];
        }
    }

    /**
     * Convert LocalDate to LocalDateTime at start of day for date range queries
     * 
     * @param date the LocalDate to convert
     * @return LocalDateTime at 00:00:00
     */
    public LocalDateTime getDateRangeStart(LocalDate date) {
        if (date == null) {
            return null;
        }
        return DateTimeUtil.toStartOfDay(date);
    }

    /**
     * Convert LocalDate to LocalDateTime at end of day for date range queries
     * 
     * @param date the LocalDate to convert
     * @return LocalDateTime at 23:59:59
     */
    public LocalDateTime getDateRangeEnd(LocalDate date) {
        if (date == null) {
            return null;
        }
        return DateTimeUtil.toEndOfDay(date);
    }

    /**
     * Log filter details for debugging
     * 
     * @param filter the filter to log
     */
    public void logFilterDetails(SaleOrderFilterDTO filter) {
        if (filter == null) {
            logger.debug("Filter is null");
            return;
        }

        StringBuilder sb = new StringBuilder("SaleOrderFilter Details: ");
        sb.append("Company=").append(filter.getComid());
        
        if (isCustomerFilterActive(filter)) {
            sb.append(", Customer=").append(filter.getId());
        }
        if (isJobMasterFilterActive(filter)) {
            sb.append(", JobMaster=").append(filter.getJId());
        }
        if (isEmployeeFilterActive(filter)) {
            sb.append(", Employee=").append(filter.getEmployeeid());
        }
        if (isStatusListFilterActive(filter)) {
            sb.append(", StatusList=").append(filter.getStatusList());
        }
        if (isStatusIdFilterActive(filter)) {
            sb.append(", StatusId=").append(filter.getStatusid());
        }
        if (isSearchFilterActive(filter)) {
            sb.append(", Search=").append(filter.getSearch());
        }
        if (isDateFilterActive(filter)) {
            sb.append(", FromDate=").append(filter.getFromdate())
              .append(", ToDate=").append(filter.getTodate());
        }

        logger.debug(sb.toString());
    }

    /**
     * Format filter object to readable string for logging
     * 
     * @param filter the filter object
     * @return formatted filter string
     */
    public String formatFilterForLogging(SaleOrderFilterDTO filter) {
        if (filter == null) {
            return "Filter: null";
        }

        return String.format(
                "Filter[Company=%d, Customer=%d, JobMaster=%d, Employee=%d, " +
                "Search=%s, FromDate=%s, ToDate=%s, DashboardStatus=%d]",
                filter.getComid(),
                filter.getId() != null ? filter.getId() : 0,
                filter.getJId() != null ? filter.getJId() : 0,
                filter.getEmployeeid() != null ? filter.getEmployeeid() : 0,
                filter.getSearch() != null ? filter.getSearch() : "",
                filter.getFromdate(),
                filter.getTodate(),
                filter.getDashboardStatus() != null ? filter.getDashboardStatus() : 0
        );
    }

    /**
     * Determine if the employee filter should use sub-employee lookup
     * (when DashboardStatus == 2, includes subordinate employees)
     * 
     * @param filter the filter to check
     * @return true if sub-employee lookup is needed
     */
    public boolean shouldIncludeSubEmployees(SaleOrderFilterDTO filter) {
        return filter != null && 
               filter.getDashboardStatus() != null && 
               filter.getDashboardStatus() == 2;
    }

    /**
     * Get ETA filter type description
     * 
     * @param etaType the ETA type code (1=OETA, 2=ETA, other=Both)
     * @return description of ETA type
     */
    public String getEtaTypeDescription(Integer etaType) {
        if (etaType == null) {
            return "BOTH";
        }
        switch (etaType) {
            case 1: return "OETA (Outbound ETA)";
            case 2: return "ETA (Estimated Time of Arrival)";
            default: return "BOTH (ETA and OETA)";
        }
    }
}

