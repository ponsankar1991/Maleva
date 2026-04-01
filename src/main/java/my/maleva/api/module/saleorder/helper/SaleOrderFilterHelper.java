package my.maleva.api.module.saleorder.helper;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.saleorder.dto.SaleOrderFilterDTO;
import my.maleva.api.module.saleorder.util.SaleOrderApiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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

    /**
     * Validate filter parameters and set defaults where needed
     * 
     * @param filter the SaleOrderFilterDTO to validate
     * @throws InvalidRequestException if critical parameters are missing
     */
    public void validateFilter(SaleOrderFilterDTO filter) {
        if (filter == null) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_FILTER_REQUIRED);
        }

        if (filter.getComid() == null || filter.getComid() <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_COMPANY_REQUIRED);
        }

        if (filter.getFromdate() == null) {
            filter.setFromdate(LocalDate.now());
        }

        if (filter.getTodate() == null) {
            filter.setTodate(LocalDate.now());
        }

        if (filter.getFromdate().isAfter(filter.getTodate())) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_FROM_DATE_INVALID);
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
     * Check if search filter is active (Search != "" and Search != null)
     * 
     * @param filter the filter to check
     * @return true if search filtering is enabled
     */
    public boolean isSearchFilterActive(SaleOrderFilterDTO filter) {
        return filter != null && filter.getSearch() != null && !filter.getSearch().trim().isEmpty();
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
}

