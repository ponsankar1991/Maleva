package my.maleva.api.module.fleet.service;

import my.maleva.api.module.fleet.dto.MaintenanceDashboardDto;

/**
 * Which trucks and drivers have paperwork or servicing coming due.
 *
 * A truck carries fourteen expiry dates and a driver twelve more, spread across
 * columns rather than rows, so nothing in the system could answer "what needs
 * attention this week" without opening every record. This flattens them.
 */
public interface FleetExpiryService {

    /**
     * The maintenance dashboard.
     *
     * <p>Only items already expired or due within {@code horizonDays} are
     * reported - something due in three weeks is not a problem yet and would
     * only bury the things that are.
     *
     * @param horizonDays  how far ahead to look; defaults to 10
     * @param criticalDays inside this many days an alert is critical rather than
     *                     a warning; defaults to 5
     */
    MaintenanceDashboardDto getDashboard(Integer companyRefId, Integer horizonDays, Integer criticalDays);
}
