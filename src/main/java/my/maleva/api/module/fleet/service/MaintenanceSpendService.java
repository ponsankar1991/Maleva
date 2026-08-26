package my.maleva.api.module.fleet.service;

import my.maleva.api.module.fleet.dto.MaintenanceSpendDto;

import java.time.LocalDate;

public interface MaintenanceSpendService {

    /**
     * Spending analytics for the maintenance dashboard: job orders (truck and
     * job-type wise), bill orders (description wise) and pass costs (AutoPass,
     * Toll, Levi) between the two dates inclusive.
     */
    MaintenanceSpendDto getSpend(Integer companyRefId, LocalDate fromDate, LocalDate toDate);
}
