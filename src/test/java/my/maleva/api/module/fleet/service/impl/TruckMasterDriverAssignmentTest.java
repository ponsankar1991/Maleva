package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.module.fleet.dto.TruckDriverContactDto;
import my.maleva.api.module.fleet.dto.TruckMasterDto;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.mapper.TruckMasterMapper;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Assigning the driver who normally drives a truck.
 *
 * <p>The pairing is stored once, on DriverMaster.TruckRefId. These tests pin the
 * rules that keep it single: one truck has one default driver, and moving a
 * driver from another truck is allowed.
 */
@ExtendWith(MockitoExtension.class)
class TruckMasterDriverAssignmentTest {

    private static final Integer COMPANY = 6;
    private static final Integer TRUCK = 11;

    @Mock private TruckMasterRepository repository;
    @Mock private DriverMasterRepository driverMasterRepository;
    @Mock private TruckMasterMapper mapper;

    @InjectMocks private TruckMasterServiceImpl service;

    private TruckMaster truck() {
        return TruckMaster.builder().id(TRUCK).companyRefId(COMPANY).active(1).truckName("BPR 7151").build();
    }

    private DriverMaster driver(int id, String name, Integer truckRefId) {
        return DriverMaster.builder().id(id).companyRefId(COMPANY).driverName(name).truckRefId(truckRefId).build();
    }

    private void truckExists() {
        lenient().when(repository.findById(TRUCK)).thenReturn(Optional.of(truck()));
        lenient().when(mapper.toDto(any(TruckMaster.class)))
                .thenReturn(TruckMasterDto.builder().id(TRUCK).companyRefId(COMPANY).truckName("BPR 7151").build());
    }

    @Test
    void assigningADriverPointsThatDriverAtTheTruck() {
        truckExists();
        DriverMaster raju = driver(3, "RAJU", null);
        lenient().when(driverMasterRepository.findByCompanyRefIdAndTruckRefId(COMPANY, TRUCK)).thenReturn(List.of());
        lenient().when(driverMasterRepository.findById(3)).thenReturn(Optional.of(raju));

        service.assignDriver(TRUCK, 3);

        assertEquals(TRUCK, raju.getTruckRefId());
        verify(driverMasterRepository).save(raju);
    }

    /** One truck, one default driver: the previous holder is released. */
    @Test
    void assigningADifferentDriverReleasesThePreviousOne() {
        truckExists();
        DriverMaster old = driver(2, "MUTHU", TRUCK);
        DriverMaster raju = driver(3, "RAJU", null);
        lenient().when(driverMasterRepository.findByCompanyRefIdAndTruckRefId(COMPANY, TRUCK)).thenReturn(List.of(old));
        lenient().when(driverMasterRepository.findById(3)).thenReturn(Optional.of(raju));

        service.assignDriver(TRUCK, 3);

        assertNull(old.getTruckRefId(), "the old driver should no longer hold this truck");
        assertEquals(TRUCK, raju.getTruckRefId());
    }

    /** Re-saving the same driver must not release them a moment later. */
    @Test
    void assigningTheSameDriverAgainKeepsTheLink() {
        truckExists();
        DriverMaster raju = driver(3, "RAJU", TRUCK);
        lenient().when(driverMasterRepository.findByCompanyRefIdAndTruckRefId(COMPANY, TRUCK)).thenReturn(List.of(raju));
        lenient().when(driverMasterRepository.findById(3)).thenReturn(Optional.of(raju));

        service.assignDriver(TRUCK, 3);

        assertEquals(TRUCK, raju.getTruckRefId());
    }

    @Test
    void clearingLeavesTheTruckWithNoDriver() {
        truckExists();
        DriverMaster raju = driver(3, "RAJU", TRUCK);
        lenient().when(driverMasterRepository.findByCompanyRefIdAndTruckRefId(COMPANY, TRUCK)).thenReturn(List.of(raju));

        service.assignDriver(TRUCK, null);

        assertNull(raju.getTruckRefId());
        verify(driverMasterRepository, never()).findById(any());
    }

    /** 0 is what an empty dropdown posts; it means the same as nothing. */
    @Test
    void zeroMeansNoDriver() {
        truckExists();
        lenient().when(driverMasterRepository.findByCompanyRefIdAndTruckRefId(COMPANY, TRUCK)).thenReturn(List.of());

        service.assignDriver(TRUCK, 0);

        verify(driverMasterRepository, never()).findById(any());
    }

    @Test
    void aDriverOfAnotherCompanyIsRefused() {
        truckExists();
        DriverMaster stranger = DriverMaster.builder().id(9).companyRefId(99).driverName("OTHER CO").build();
        lenient().when(driverMasterRepository.findByCompanyRefIdAndTruckRefId(COMPANY, TRUCK)).thenReturn(List.of());
        lenient().when(driverMasterRepository.findById(9)).thenReturn(Optional.of(stranger));

        assertThrows(IllegalArgumentException.class, () -> service.assignDriver(TRUCK, 9));
        assertNull(stranger.getTruckRefId());
    }

    // ------------------------------------------------------ truck status

    @Test
    void puttingATruckInTheWorkshopKeepsTheReturnDate() {
        TruckMaster truck = truck();
        lenient().when(repository.findById(TRUCK)).thenReturn(Optional.of(truck));
        lenient().when(repository.save(any(TruckMaster.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(mapper.toDto(any(TruckMaster.class)))
                .thenReturn(TruckMasterDto.builder().id(TRUCK).companyRefId(COMPANY).build());
        lenient().when(driverMasterRepository.findByCompanyRefIdAndTruckRefId(COMPANY, TRUCK))
                .thenReturn(List.of());

        service.updateStatus(TRUCK, "WORKSHOP", LocalDate.of(2026, 9, 22));

        assertEquals("WORKSHOP", truck.getTruckStatus());
        assertEquals(LocalDate.of(2026, 9, 22), truck.getWorkshopUntil());
    }

    /** A leftover return date must not bring a sold or active truck back. */
    @Test
    void leavingTheWorkshopClearsTheReturnDate() {
        TruckMaster truck = truck();
        truck.setTruckStatus("WORKSHOP");
        truck.setWorkshopUntil(LocalDate.of(2026, 9, 22));
        lenient().when(repository.findById(TRUCK)).thenReturn(Optional.of(truck));
        lenient().when(repository.save(any(TruckMaster.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(mapper.toDto(any(TruckMaster.class)))
                .thenReturn(TruckMasterDto.builder().id(TRUCK).companyRefId(COMPANY).build());
        lenient().when(driverMasterRepository.findByCompanyRefIdAndTruckRefId(COMPANY, TRUCK))
                .thenReturn(List.of());

        service.updateStatus(TRUCK, "ACTIVE", LocalDate.of(2026, 9, 22));

        assertEquals("ACTIVE", truck.getTruckStatus());
        assertNull(truck.getWorkshopUntil());
    }

    @Test
    void anUnknownStatusIsRefusedAndChangesNothing() {
        TruckMaster truck = truck();
        lenient().when(repository.findById(TRUCK)).thenReturn(Optional.of(truck));

        assertThrows(RuntimeException.class, () -> service.updateStatus(TRUCK, "PARKED", null));
        assertNull(truck.getTruckStatus());
        verify(repository, never()).save(any(TruckMaster.class));
    }

    // --------------------------------------------- the contact window

    @Test
    void theFleetListPairsEachTruckWithItsDriverAndNumber() {
        TruckMaster bpr = TruckMaster.builder().id(TRUCK).companyRefId(COMPANY).active(1)
                .truckName("BPR 7151 ").truckNumber("BPR7151").truckType("40 FT SIDE CURTAIN").build();
        TruckMaster ums = TruckMaster.builder().id(38).companyRefId(COMPANY).active(1)
                .truckName("UMS 7151").truckNumber("UMS7151").truckType("LONG LOADER").build();
        DriverMaster raju = driver(3, "RAJU", TRUCK);
        raju.setMobileNo("012-3456789");

        lenient().when(repository.findOrderableTrucks(COMPANY)).thenReturn(List.of(bpr, ums));
        lenient().when(driverMasterRepository.findByCompanyRefIdAndTruckRefIdIsNotNull(COMPANY))
                .thenReturn(List.of(raju));

        List<TruckDriverContactDto> fleet = service.getFleetDrivers(COMPANY);

        assertEquals(2, fleet.size());
        TruckDriverContactDto first = fleet.get(0);
        assertEquals("BPR 7151", first.getTruckName(), "the stored plate has a trailing space");
        assertEquals("40FT", first.getSizeClass());
        assertEquals("RAJU", first.getDriverName());
        assertEquals("012-3456789", first.getDriverMobileNo());

        // A truck with nobody assigned is listed, with the gaps showing.
        TruckDriverContactDto second = fleet.get(1);
        assertEquals("SPECIAL", second.getSizeClass());
        assertNull(second.getDriverRefId());
        assertNull(second.getDriverMobileNo());
    }

    @Test
    void anUnknownTruckIsRefused() {
        lenient().when(repository.findById(404)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.assignDriver(404, 3));
    }
}
