package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.dto.ExpiryAlertDto;
import my.maleva.api.module.fleet.dto.MaintenanceDashboardDto;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.joborder.repository.JobOrderMasterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

/** The thresholds are the whole point of this screen, so they are pinned here. */
@ExtendWith(MockitoExtension.class)
class FleetExpiryServiceImplTest {

    private static final Integer COMPANY = 6;

    @Mock
    private TruckMasterRepository truckMasterRepository;

    @Mock
    private DriverMasterRepository driverMasterRepository;

    @Mock
    private JobOrderMasterRepository jobOrderMasterRepository;

    private MaintenanceDashboardDto run(List<TruckMaster> trucks, List<DriverMaster> drivers) {
        lenient().when(truckMasterRepository
                        .findByCompanyRefIdAndActiveAndMalevaTruck(eq(COMPANY), eq(1), eq(1)))
                .thenReturn(trucks);
        lenient().when(driverMasterRepository.findByCompanyRefIdAndActive(eq(COMPANY), eq(1)))
                .thenReturn(drivers);
        lenient().when(jobOrderMasterRepository.findOpenJobs(eq(COMPANY), org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(List.of());
        return new FleetExpiryServiceImpl(truckMasterRepository, driverMasterRepository, jobOrderMasterRepository)
                .getDashboard(COMPANY, null, null);
    }

    /** A truck whose insurance expires the given number of days from today. */
    private TruckMaster truckWithInsuranceIn(int days) {
        return TruckMaster.builder()
                .id(1).companyRefId(COMPANY).active(1)
                .truckName("VIPS 7151").truckNumber("VIPS7151")
                .insuranceExp(LocalDate.now().plusDays(days))
                .build();
    }

    @Test
    void showsSomethingDueInsideTheTenDayHorizon() {
        MaintenanceDashboardDto result = run(List.of(truckWithInsuranceIn(8)), List.of());

        assertEquals(1, result.getAlerts().size());
        assertEquals("WARNING", result.getAlerts().get(0).getSeverity());
        assertEquals(8, result.getAlerts().get(0).getDaysRemaining());
    }

    @Test
    void hidesSomethingDueBeyondTheHorizon() {
        // Twenty days out is not a problem yet, and showing it would bury the
        // things that are.
        MaintenanceDashboardDto result = run(List.of(truckWithInsuranceIn(20)), List.of());

        assertTrue(result.getAlerts().isEmpty());
        assertEquals(0, result.getWarningCount());
    }

    @Test
    void marksTheLastFiveDaysAsCritical() {
        assertEquals("CRITICAL", run(List.of(truckWithInsuranceIn(5)), List.of())
                .getAlerts().get(0).getSeverity());
        assertEquals("CRITICAL", run(List.of(truckWithInsuranceIn(0)), List.of())
                .getAlerts().get(0).getSeverity());
        assertEquals("WARNING", run(List.of(truckWithInsuranceIn(6)), List.of())
                .getAlerts().get(0).getSeverity());
    }

    @Test
    void stillShowsWhatHasAlreadyExpired() {
        MaintenanceDashboardDto result = run(List.of(truckWithInsuranceIn(-3)), List.of());

        assertEquals("EXPIRED", result.getAlerts().get(0).getSeverity());
        assertEquals(-3, result.getAlerts().get(0).getDaysRemaining());
        assertEquals(1, result.getExpiredCount());
    }

    @Test
    void ignoresThePlaceholderDate() {
        // Rows where the field was never filled in carry 1900-01-01. Treating
        // those as expired would swamp the screen.
        TruckMaster truck = TruckMaster.builder()
                .id(1).companyRefId(COMPANY).active(1).truckName("VIPS 7151")
                .insuranceExp(LocalDate.of(1900, 1, 1))
                .build();

        assertTrue(run(List.of(truck), List.of()).getAlerts().isEmpty());
    }

    @Test
    void ignoresColumnsThatWereNeverSet() {
        TruckMaster truck = TruckMaster.builder()
                .id(1).companyRefId(COMPANY).active(1).truckName("VIPS 7151")
                .build();

        assertTrue(run(List.of(truck), List.of()).getAlerts().isEmpty());
    }

    @Test
    void ranksTheMostOverdueFirst() {
        TruckMaster truck = TruckMaster.builder()
                .id(1).companyRefId(COMPANY).active(1).truckName("VIPS 7151")
                .insuranceExp(LocalDate.now().plusDays(9))
                .serviceExp(LocalDate.now().minusDays(4))
                .alignmentExp(LocalDate.now().plusDays(2))
                .build();

        List<ExpiryAlertDto> alerts = run(List.of(truck), List.of()).getAlerts();

        assertEquals(3, alerts.size());
        assertEquals("Service", alerts.get(0).getCategory());
        assertEquals("Alignment", alerts.get(1).getCategory());
        assertEquals("Insurance", alerts.get(2).getCategory());
    }

    @Test
    void countsEachTruckOnceHoweverManyItemsItHas() {
        TruckMaster truck = TruckMaster.builder()
                .id(1).companyRefId(COMPANY).active(1).truckName("VIPS 7151")
                .insuranceExp(LocalDate.now().plusDays(2))
                .serviceExp(LocalDate.now().plusDays(3))
                .build();

        MaintenanceDashboardDto result = run(List.of(truck), List.of());

        assertEquals(2, result.getAlerts().size());
        assertEquals(1, result.getTrucksNeedingAttention());
    }

    @Test
    void picksUpDriverLicencesAndPortPasses() {
        DriverMaster driver = DriverMaster.builder()
                .id(7).companyRefId(COMPANY).active(1)
                .driverName("SIVANYANAM").licenseNo("001205-05-0285")
                .licenseExp(LocalDate.now().plusDays(4))
                .ptpPort(LocalDate.now().plusDays(9))
                .build();

        MaintenanceDashboardDto result = run(List.of(), List.of(driver));

        assertEquals(2, result.getAlerts().size());
        assertEquals(1, result.getDriversNeedingAttention());
        assertEquals("Licence", result.getAlerts().get(0).getCategory());
        assertEquals("PORT_PASS", result.getAlerts().get(1).getGroup());
    }

    @Test
    void groupsTheCountsByCategory() {
        TruckMaster first = TruckMaster.builder()
                .id(1).companyRefId(COMPANY).active(1).truckName("A")
                .serviceExp(LocalDate.now().plusDays(2)).build();
        TruckMaster second = TruckMaster.builder()
                .id(2).companyRefId(COMPANY).active(1).truckName("B")
                .serviceExp(LocalDate.now().plusDays(3))
                .insuranceExp(LocalDate.now().plusDays(4)).build();

        MaintenanceDashboardDto result = run(List.of(first, second), List.of());

        assertEquals(2, result.getByCategory().get("Service"));
        assertEquals(1, result.getByCategory().get("Insurance"));
    }

    @Test
    void onlyQueriesMalevaOwnedTrucksNotSubcontractorVehicles() {
        // TruckMaster holds both: MalevaTruck = 1 for our own trucks, 0 for a
        // subcontractor's vehicle kept in the same table. On the live data
        // that is 69 of 71 active trucks. A subcontractor's documents are not
        // ours to renew, so this dashboard must never even ask for them.
        run(List.of(), List.of());

        org.mockito.Mockito.verify(truckMasterRepository)
                .findByCompanyRefIdAndActiveAndMalevaTruck(COMPANY, 1, 1);
        org.mockito.Mockito.verify(truckMasterRepository, org.mockito.Mockito.never())
                .findByCompanyRefIdAndActive(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsACriticalWindowWiderThanTheHorizon() {
        assertThrows(InvalidRequestException.class, () ->
                new FleetExpiryServiceImpl(truckMasterRepository, driverMasterRepository, jobOrderMasterRepository)
                        .getDashboard(COMPANY, 5, 10));
    }

    @Test
    void requiresACompany() {
        assertThrows(InvalidRequestException.class, () ->
                new FleetExpiryServiceImpl(truckMasterRepository, driverMasterRepository, jobOrderMasterRepository)
                        .getDashboard(null, null, null));
    }
}
