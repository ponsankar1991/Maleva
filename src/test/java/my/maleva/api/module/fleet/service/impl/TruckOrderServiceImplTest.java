package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.dto.OrderableTruckDto;
import my.maleva.api.module.fleet.dto.TruckAvailabilityDto;
import my.maleva.api.module.fleet.dto.TruckDayCapacityDto;
import my.maleva.api.module.fleet.dto.TruckOrderCalendarResponse;
import my.maleva.api.module.fleet.dto.TruckOrderDto;
import my.maleva.api.module.fleet.dto.request.TruckOrderSaveRequest;
import my.maleva.api.module.fleet.dto.request.TruckOrderSearchRequest;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.entity.TruckOrder;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.fleet.repository.TruckOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * The booking-type rules and the free / taken counting, pinned without a
 * database. The queries themselves (Active = 1, MalevaTruck = 1, the range) live
 * in JPQL and are covered by TruckOrderServiceImplIT.
 */
@ExtendWith(MockitoExtension.class)
class TruckOrderServiceImplTest {

    private static final Integer COMPANY = 6;
    private static final LocalDate DAY = LocalDate.of(2026, 9, 20);

    @Mock private TruckOrderRepository orders;
    @Mock private TruckMasterRepository trucks;
    @Mock private CustomerRepository customers;
    @Mock private JdbcTemplate jdbc;

    private TruckOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TruckOrderServiceImpl(orders, trucks, customers, jdbc);
        // Every list and availability call names the customers on the orders.
        lenient().when(customers.findOptions(COMPANY)).thenReturn(List.of());
    }

    // ------------------------------------------------------------- fixtures

    /** {@code truckType} is the free text TruckMaster stores; the size is read from it. */
    private static TruckMaster truck(int id, String name, String truckType) {
        return TruckMaster.builder()
                .id(id).companyRefId(COMPANY).active(1).malevaTruck(1).orderableTruck(1)
                .truckName(name).truckType(truckType)
                .build();
    }

    /** The same truck, off the road. {@code until} null means "no return date". */
    private static TruckMaster truckInWorkshop(int id, String name, String truckType, LocalDate until) {
        TruckMaster truck = truck(id, name, truckType);
        truck.setTruckStatus("WORKSHOP");
        truck.setWorkshopUntil(until);
        return truck;
    }

    private static TruckOrder order(int id, Integer truckId, String type, LocalDate day) {
        return TruckOrder.builder()
                .id(id).companyRefId(COMPANY).truckRefId(truckId).bookingType(type)
                .orderDate(day).status("Pending").active(1)
                .cNumber(id).cNumberDisplay(String.format("ORD%09d", id))
                .build();
    }

    /** An order with a recorded load, e.g. 8 PLT. */
    private static TruckOrder loadedOrder(int id, Integer truckId, LocalDate day,
                                          String quantity, String unit) {
        TruckOrder order = order(id, truckId, "OWN", day);
        order.setQuantity(new BigDecimal(quantity));
        order.setQuantityUnit(unit);
        return order;
    }

    private static TruckOrderSaveRequest request(String type, Integer truckId) {
        return TruckOrderSaveRequest.builder()
                .companyRefId(COMPANY).bookingType(type).truckRefId(truckId)
                .orderDate(DAY).status("Pending")
                .build();
    }

    /** Truck 10 = JQX 7151 exists, the counter hands out 7, and a save returns the row with an id. */
    private void savesWork(long otherOrdersOnTruck) {
        lenient().when(trucks.existsByIdAndCompanyRefIdAndActive(10, COMPANY, 1)).thenReturn(true);
        lenient().when(trucks.findById(10)).thenReturn(Optional.of(truck(10, "JQX 7151", "40FT SIDE CURTAIN")));
        lenient().when(orders.countClashes(eq(COMPANY), eq(10), eq(DAY), anyInt())).thenReturn(otherOrdersOnTruck);
        lenient().when(jdbc.queryForList(anyString(), eq(Integer.class), eq(COMPANY), eq("TruckOrderMaster")))
                .thenReturn(List.of(7));
        lenient().when(orders.saveAndFlush(any(TruckOrder.class))).thenAnswer(invocation -> {
            TruckOrder saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(99);
            }
            return saved;
        });
    }

    // ----------------------------------------------------------------- OWN

    @Test
    void ownOnAFreeTruckIsSavedWithTheNextNumber() {
        savesWork(0);

        TruckOrderDto saved = service.save(request("OWN", 10), "tester");

        assertEquals("OWN", saved.getBookingType());
        assertEquals(10, saved.getTruckRefId());
        assertEquals("ORD000000007", saved.getCNumberDisplay());
    }

    @Test
    void blankBookingTypeMeansOwn() {
        savesWork(0);

        assertEquals("OWN", service.save(request(null, 10), "tester").getBookingType());
    }

    @Test
    void ownOnATakenTruckIsRejectedNamingTheTruckAndTheDay() {
        savesWork(1);

        InvalidRequestException error = assertThrows(InvalidRequestException.class,
                () -> service.save(request("OWN", 10), "tester"));

        assertEquals("No truck available: JQX 7151 is already booked on 20 Sep 2026.", error.getMessage());
        verify(orders, never()).saveAndFlush(any(TruckOrder.class));
    }

    // -------------------------------------------------------------- SHARED

    @Test
    void sharedOnATakenTruckIsSaved() {
        savesWork(1);

        TruckOrderDto saved = service.save(request("SHARED", 10), "tester");

        assertEquals("SHARED", saved.getBookingType());
        assertEquals(10, saved.getTruckRefId());
    }

    @Test
    void sharedOnAFreeTruckIsRejected() {
        savesWork(0);

        InvalidRequestException error = assertThrows(InvalidRequestException.class,
                () -> service.save(request("SHARED", 10), "tester"));

        assertEquals("JQX 7151 is free on 20 Sep 2026 - book it as own.", error.getMessage());
    }

    // ------------------------------------------------------------- OUTSIDE

    @Test
    void outsideWithoutATruckNameIsRejected() {
        savesWork(0);
        TruckOrderSaveRequest request = request("OUTSIDE", null);
        request.setOutsideTruckName("   ");

        assertEquals("Outside truck name is required.",
                assertThrows(InvalidRequestException.class, () -> service.save(request, "tester")).getMessage());
    }

    @Test
    void outsideStoresNoTruckAndSkipsTheClashRule() {
        savesWork(5);
        TruckOrderSaveRequest request = request("OUTSIDE", 10);
        request.setOutsideTruckName(" ABC 1234 ");
        request.setOutsideSupplierName("XYZ Transport");

        TruckOrderDto saved = service.save(request, "tester");

        assertEquals("OUTSIDE", saved.getBookingType());
        assertNull(saved.getTruckRefId(), "an outside order must not hold one of our trucks");
        assertEquals("ABC 1234", saved.getOutsideTruckName());
        assertEquals("XYZ Transport", saved.getOutsideSupplierName());
        verify(orders, never()).countClashes(any(), any(), any(), any());
    }

    @Test
    void ownOrderSheddsOutsideFields() {
        savesWork(0);
        TruckOrderSaveRequest request = request("OWN", 10);
        request.setOutsideTruckName("left over from the form");

        assertNull(service.save(request, "tester").getOutsideTruckName());
    }

    @Test
    void editingOwnToOutsideKeepsTheOrderNumber() {
        savesWork(0);
        TruckOrder existing = order(5, 10, "OWN", DAY);
        lenient().when(orders.findByIdAndCompanyRefIdAndActive(5, COMPANY, 1)).thenReturn(Optional.of(existing));

        TruckOrderSaveRequest request = request("OUTSIDE", null);
        request.setId(5);
        request.setOutsideTruckName("ABC 1234");

        TruckOrderDto saved = service.save(request, "tester");

        assertEquals("OUTSIDE", saved.getBookingType());
        assertEquals("ORD000000005", saved.getCNumberDisplay());
        assertNull(saved.getTruckRefId());
    }

    @Test
    void anUnknownBookingTypeIsRejected() {
        assertThrows(InvalidRequestException.class, () -> service.save(request("BORROWED", 10), "tester"));
    }

    @Test
    void anUnknownSizeClassIsRejected() {
        TruckOrderSaveRequest request = request("OWN", 10);
        request.setTruckSizeClass("99FT");

        assertThrows(InvalidRequestException.class, () -> service.save(request, "tester"));
    }

    @Test
    void theSizeSearchedForIsStoredAsItsCode() {
        savesWork(0);
        TruckOrderSaveRequest request = request("OWN", 10);
        request.setTruckSizeClass("40 ft");

        assertEquals("40FT", service.save(request, "tester").getTruckSizeClass());
    }

    // --------------------------------------------- customer and quantity

    @Test
    void theCustomerAndTheQuantityAreStored() {
        savesWork(0);
        lenient().when(customers.findByIdAndCompanyRefId(77, COMPANY)).thenReturn(Optional.of(
                Customer.builder().id(77).companyRefId(COMPANY).customerName("ACME SHIPPING").build()));

        TruckOrderSaveRequest request = request("OWN", 10);
        request.setCustomerRefId(77);
        request.setQuantity(new BigDecimal("12"));
        request.setQuantityUnit("PLT");

        TruckOrderDto saved = service.save(request, "tester");

        assertEquals(77, saved.getCustomerRefId());
        assertEquals("ACME SHIPPING", saved.getCustomerName());
        assertEquals(new BigDecimal("12"), saved.getQuantity());
        assertEquals("PLT", saved.getQuantityUnit());
    }

    /** The unit name resolves as well as its code, so either may be posted. */
    @Test
    void theUnitIsStoredAsItsCode() {
        savesWork(0);
        TruckOrderSaveRequest request = request("OWN", 10);
        request.setQuantity(new BigDecimal("2.5"));
        request.setQuantityUnit("pallet");

        assertEquals("PLT", service.save(request, "tester").getQuantityUnit());
    }

    @Test
    void aQuantityWithoutAUnitIsRejected() {
        savesWork(0);
        TruckOrderSaveRequest request = request("OWN", 10);
        request.setQuantity(new BigDecimal("12"));

        assertEquals("Choose what the quantity is counted in.",
                assertThrows(InvalidRequestException.class,
                        () -> service.save(request, "tester")).getMessage());
    }

    @Test
    void aUnitWithoutAQuantityIsRejected() {
        savesWork(0);
        TruckOrderSaveRequest request = request("OWN", 10);
        request.setQuantityUnit("PLT");

        assertTrue(assertThrows(InvalidRequestException.class,
                () -> service.save(request, "tester")).getMessage().startsWith("Enter a quantity"));
    }

    @Test
    void aZeroOrNegativeQuantityIsRejected() {
        savesWork(0);
        TruckOrderSaveRequest request = request("OWN", 10);
        request.setQuantity(BigDecimal.ZERO);
        request.setQuantityUnit("PLT");

        assertEquals("Quantity must be more than zero.",
                assertThrows(InvalidRequestException.class,
                        () -> service.save(request, "tester")).getMessage());
    }

    @Test
    void anUnknownUnitIsRejected() {
        savesWork(0);
        TruckOrderSaveRequest request = request("OWN", 10);
        request.setQuantity(new BigDecimal("3"));
        request.setQuantityUnit("BOXES");

        assertThrows(InvalidRequestException.class, () -> service.save(request, "tester"));
    }

    @Test
    void aCustomerOfAnotherCompanyIsRejected() {
        savesWork(0);
        lenient().when(customers.findByIdAndCompanyRefId(999, COMPANY)).thenReturn(Optional.empty());

        TruckOrderSaveRequest request = request("OWN", 10);
        request.setCustomerRefId(999);

        assertEquals("Customer not found.",
                assertThrows(InvalidRequestException.class,
                        () -> service.save(request, "tester")).getMessage());
    }

    /** Nobody is named on plenty of orders; 0 means the same as nothing. */
    @Test
    void noCustomerIsFine() {
        savesWork(0);
        TruckOrderSaveRequest request = request("OWN", 10);
        request.setCustomerRefId(0);

        assertNull(service.save(request, "tester").getCustomerRefId());
        verify(customers, never()).findByIdAndCompanyRefId(eq(0), any());
    }

    // -------------------------------------------------------- availability

    private void fleetOfFour() {
        lenient().when(trucks.findOrderableTrucks(COMPANY)).thenReturn(List.of(
                truck(10, "JQX 7151", "40FT SIDE CURTAIN"),
                // Stored with the letter O, as three trucks on the live data are.
                truck(11, "BPR 7151", "4O FT  BOX TRUCK"),
                truck(12, "QD 7151", "20 FT SIDE CURTAIN"),
                truck(13, "GOLD 7151 ", "")));
    }

    @Test
    void ownAndSharedTakeATruckOnceAndOutsideIsNeverCounted() {
        fleetOfFour();
        when(orders.findLiveInRange(COMPANY, DAY, DAY)).thenReturn(List.of(
                order(1, 10, "OWN", DAY),
                order(2, 10, "SHARED", DAY),
                order(3, null, "OUTSIDE", DAY)));

        TruckAvailabilityDto result = service.availability(COMPANY, DAY, null, null);

        assertEquals(4, result.getTotalTrucks());
        assertEquals(1, result.getTakenCount());
        assertEquals(3, result.getFreeCount());
        assertEquals(2, result.getTakenTrucks().get(0).getOrders().size());
        assertEquals(1, result.getOutsideOrders().size());
        assertTrue(result.getFreeTrucks().stream().noneMatch(t -> t.getId() == 10));
    }

    @Test
    void theOrderBeingEditedDoesNotTakeItsOwnTruck() {
        fleetOfFour();
        when(orders.findLiveInRange(COMPANY, DAY, DAY)).thenReturn(List.of(order(1, 10, "OWN", DAY)));

        TruckAvailabilityDto result = service.availability(COMPANY, DAY, null, 1);

        assertEquals(0, result.getTakenCount());
        assertEquals(4, result.getFreeCount());
    }

    @Test
    void aSizeNarrowsTheFleetAndUnclassifiedTrucksOnlyCountForAnySize() {
        fleetOfFour();
        when(orders.findLiveInRange(COMPANY, DAY, DAY)).thenReturn(List.of(order(1, 12, "OWN", DAY)));

        TruckAvailabilityDto forty = service.availability(COMPANY, DAY, "40FT", null);
        assertEquals(2, forty.getTotalTrucks());
        assertEquals(0, forty.getTakenCount(), "a 20 FT booking does not take a 40 FT truck");
        assertEquals("40FT", forty.getSizeClass());

        TruckAvailabilityDto any = service.availability(COMPANY, DAY, "", null);
        assertEquals(4, any.getTotalTrucks());
        assertEquals(1, any.getTakenCount());
        assertNull(any.getSizeClass());
    }

    @Test
    void noFreeTruckIsReportedAsZeroFree() {
        lenient().when(trucks.findOrderableTrucks(COMPANY))
                .thenReturn(List.of(truck(10, "JQX 7151", "40FT SIDE CURTAIN")));
        when(orders.findLiveInRange(COMPANY, DAY, DAY)).thenReturn(List.of(order(1, 10, "OWN", DAY)));

        TruckAvailabilityDto result = service.availability(COMPANY, DAY, "40FT", null);

        assertEquals(0, result.getFreeCount());
        assertTrue(result.getFreeTrucks().isEmpty());
        assertEquals("JQX 7151", result.getTakenTrucks().get(0).getTruckName());
    }

    @Test
    void availabilityNeedsADate() {
        assertThrows(InvalidRequestException.class, () -> service.availability(COMPANY, null, null, null));
    }

    // ------------------------------------------------------------ the route

    /** Stored one way, so "  singapore " and "SINGAPORE" are one place to the search. */
    @Test
    void theRouteIsStoredTrimmedAndInCapitals() {
        savesWork(0);
        TruckOrderSaveRequest request = request("OWN", 10);
        request.setOrigin("  singapore ");
        request.setDestination("pasir   gudang");

        TruckOrderDto saved = service.save(request, "tester");

        assertEquals("SINGAPORE", saved.getOrigin());
        assertEquals("PASIR GUDANG", saved.getDestination(), "inner spaces collapse to one");
    }

    @Test
    void aBlankRouteIsNotRecorded() {
        savesWork(0);
        TruckOrderSaveRequest request = request("OWN", 10);
        request.setOrigin("   ");

        TruckOrderDto saved = service.save(request, "tester");

        assertNull(saved.getOrigin());
        assertNull(saved.getDestination());
    }

    @Test
    void placesAreTheNamesAlreadyInUseMostUsedFirst() {
        lenient().when(jdbc.queryForList(anyString(), eq(String.class), eq(COMPANY), eq(COMPANY), eq(COMPANY), eq(COMPANY)))
                .thenReturn(List.of("SINGAPORE", "PTP", "WESTPORT"));

        assertEquals(List.of("SINGAPORE", "PTP", "WESTPORT"), service.places(COMPANY));
    }

    @Test
    void placesNeedACompany() {
        assertThrows(InvalidRequestException.class, () -> service.places(0));
    }

    // --------------------------------------------------- how full a truck is

    @Test
    void theLoadOnATruckIsAddedUpInPalletSpaces() {
        lenient().when(trucks.findOrderableTrucks(COMPANY))
                .thenReturn(List.of(truck(10, "JQX 7151", "40 FT SIDE CURTAIN")));
        when(orders.findLiveInRange(COMPANY, DAY, DAY)).thenReturn(List.of(
                loadedOrder(1, 10, DAY, "8", "PLT"),      // 8.0
                loadedOrder(2, 10, DAY, "9", "IBC"),      // 9.0
                loadedOrder(3, 10, DAY, "4", "DRUM"),     // 1.0
                loadedOrder(4, 10, DAY, "10", "PKG")));   // 1.0

        TruckAvailabilityDto.TakenTruck truck =
                service.availability(COMPANY, DAY, null, null).getTakenTrucks().get(0);

        assertEquals(24, truck.getPalletCapacity(), "a 40 ft truck holds 24 spaces by default");
        assertEquals(0, new BigDecimal("19.0").compareTo(truck.getUsedSpaces()),
                "8 pallets + 9 IBC + 4 drums + 10 packages = 19 spaces, got " + truck.getUsedSpaces());
        assertEquals(0, truck.getOrdersWithoutQuantity());
    }

    /**
     * An order with no quantity is unmeasured, not empty. Counting it as zero
     * would show a confident total built on missing data.
     */
    @Test
    void ordersWithNoQuantityAreReportedRatherThanCountedAsZero() {
        lenient().when(trucks.findOrderableTrucks(COMPANY))
                .thenReturn(List.of(truck(10, "JQX 7151", "40 FT SIDE CURTAIN")));
        when(orders.findLiveInRange(COMPANY, DAY, DAY)).thenReturn(List.of(
                loadedOrder(1, 10, DAY, "8", "PLT"),
                order(2, 10, "SHARED", DAY),
                order(3, 10, "SHARED", DAY)));

        TruckAvailabilityDto.TakenTruck truck =
                service.availability(COMPANY, DAY, null, null).getTakenTrucks().get(0);

        assertEquals(0, new BigDecimal("8").compareTo(truck.getUsedSpaces()));
        assertEquals(2, truck.getOrdersWithoutQuantity(), "two orders said nothing about their load");
    }

    /** "1x40FT" means the whole vehicle, so it fills whatever that truck holds. */
    @Test
    void aWholeTruckBookingFillsTheTruck() {
        lenient().when(trucks.findOrderableTrucks(COMPANY))
                .thenReturn(List.of(truck(10, "JQX 7151", "40 FT SIDE CURTAIN")));
        when(orders.findLiveInRange(COMPANY, DAY, DAY))
                .thenReturn(List.of(loadedOrder(1, 10, DAY, "1", "TRUCK")));

        TruckAvailabilityDto.TakenTruck truck =
                service.availability(COMPANY, DAY, null, null).getTakenTrucks().get(0);

        assertEquals(0, new BigDecimal("24").compareTo(truck.getUsedSpaces()));
    }

    @Test
    void aTrucksOwnCapacityBeatsTheFigureForItsSize() {
        TruckMaster odd = truck(10, "JQX 7151", "40 FT SIDE CURTAIN");
        odd.setPalletCapacity(18);
        lenient().when(trucks.findOrderableTrucks(COMPANY)).thenReturn(List.of(odd));
        when(orders.findLiveInRange(COMPANY, DAY, DAY))
                .thenReturn(List.of(loadedOrder(1, 10, DAY, "2", "PLT")));

        assertEquals(18, service.availability(COMPANY, DAY, null, null)
                .getTakenTrucks().get(0).getPalletCapacity());
    }

    /** A long loader carries one machine; pallet spaces mean nothing on it. */
    @Test
    void aLongLoaderHasNoPalletCapacity() {
        lenient().when(trucks.findOrderableTrucks(COMPANY))
                .thenReturn(List.of(truck(38, "UMS 7151", "LONG LOADER")));
        when(orders.findLiveInRange(COMPANY, DAY, DAY)).thenReturn(List.of());

        assertNull(service.availability(COMPANY, DAY, null, null)
                .getFreeTrucks().get(0).getPalletCapacity());
    }

    @Test
    void freeTrucksAlsoCarryTheirCapacitySoTheDialogCanShowIt() {
        lenient().when(trucks.findOrderableTrucks(COMPANY)).thenReturn(List.of(
                truck(10, "JQX 7151", "40 FT SIDE CURTAIN"),
                truck(21, "GOLD 7151", "5 TON BOX")));
        when(orders.findLiveInRange(COMPANY, DAY, DAY)).thenReturn(List.of());

        List<OrderableTruckDto> free = service.availability(COMPANY, DAY, null, null).getFreeTrucks();
        assertEquals(24, free.get(0).getPalletCapacity());
        assertEquals(6, free.get(1).getPalletCapacity());
    }

    /** Over the limit is still saved: the dispatcher decides, the screen only warns. */
    @Test
    void anOverloadedTruckIsNeverRefusedBytheServer() {
        savesWork(1);
        TruckOrderSaveRequest request = request("SHARED", 10);
        request.setQuantity(new BigDecimal("99"));
        request.setQuantityUnit("PLT");

        assertEquals("SHARED", service.save(request, "tester").getBookingType());
    }

    // ------------------------------------------------------------- workshop

    @Test
    void aTruckInTheWorkshopIsNotOfferedAndIsCountedSeparately() {
        lenient().when(trucks.findOrderableTrucks(COMPANY)).thenReturn(List.of(
                truck(10, "JQX 7151", "40 FT SIDE CURTAIN"),
                truckInWorkshop(11, "BPR 7151", "40 FT SIDE CURTAIN", DAY.plusDays(3))));
        when(orders.findLiveInRange(COMPANY, DAY, DAY)).thenReturn(List.of());

        TruckAvailabilityDto result = service.availability(COMPANY, DAY, null, null);

        assertEquals(1, result.getTotalTrucks(), "the workshop truck is not part of today's fleet");
        assertEquals(1, result.getFreeCount());
        assertEquals(1, result.getWorkshopTrucks());
        assertTrue(result.getFreeTrucks().stream().noneMatch(t -> t.getId() == 11));
    }

    /** Due back on the 23rd means bookable from the 24th, with nobody switching anything. */
    @Test
    void aTruckIsOfferedAgainOnceItsReturnDateHasPassed() {
        lenient().when(trucks.findOrderableTrucks(COMPANY)).thenReturn(List.of(
                truckInWorkshop(11, "BPR 7151", "40 FT SIDE CURTAIN", DAY)));
        lenient().when(orders.findLiveInRange(any(), any(), any())).thenReturn(List.of());

        assertEquals(0, service.availability(COMPANY, DAY, null, null).getTotalTrucks());
        assertEquals(1, service.availability(COMPANY, DAY.plusDays(1), null, null).getTotalTrucks());
    }

    @Test
    void aWorkshopTruckWithNoReturnDateStaysOffTheRoad() {
        lenient().when(trucks.findOrderableTrucks(COMPANY)).thenReturn(List.of(
                truckInWorkshop(11, "BPR 7151", "40 FT SIDE CURTAIN", null)));
        lenient().when(orders.findLiveInRange(any(), any(), any())).thenReturn(List.of());

        assertEquals(0, service.availability(COMPANY, DAY.plusYears(1), null, null).getTotalTrucks());
    }

    @Test
    void savingOntoATruckInTheWorkshopIsRefusedAndSaysUntilWhen() {
        savesWork(0);
        TruckMaster off = truckInWorkshop(10, "JQX 7151", "40 FT SIDE CURTAIN", DAY.plusDays(2));
        lenient().when(trucks.findById(10)).thenReturn(Optional.of(off));

        InvalidRequestException error = assertThrows(InvalidRequestException.class,
                () -> service.save(request("OWN", 10), "tester"));

        assertEquals("JQX 7151 is in the workshop until 22 Sep 2026.", error.getMessage());
        verify(orders, never()).saveAndFlush(any(TruckOrder.class));
    }

    @Test
    void aSoldTruckCanNeverBeBooked() {
        savesWork(0);
        TruckMaster sold = truck(10, "JQX 7151", "40 FT SIDE CURTAIN");
        sold.setTruckStatus("SOLD");
        lenient().when(trucks.findById(10)).thenReturn(Optional.of(sold));

        assertEquals("JQX 7151 is marked sold and cannot be booked.",
                assertThrows(InvalidRequestException.class,
                        () -> service.save(request("OWN", 10), "tester")).getMessage());
    }

    /** An unreadable status must not take the calendar down; the truck is on the road. */
    @Test
    void anUnknownStatusReadsAsActive() {
        lenient().when(trucks.findOrderableTrucks(COMPANY)).thenReturn(List.of(
                truck(10, "JQX 7151", "40 FT SIDE CURTAIN")));
        TruckMaster odd = truck(11, "BPR 7151", "40 FT SIDE CURTAIN");
        odd.setTruckStatus("PARKED?");
        lenient().when(trucks.findOrderableTrucks(COMPANY)).thenReturn(List.of(odd));
        when(orders.findLiveInRange(COMPANY, DAY, DAY)).thenReturn(List.of());

        assertEquals(1, service.availability(COMPANY, DAY, null, null).getTotalTrucks());
    }

    // ------------------------------------------------------ calendar counters

    @Test
    @SuppressWarnings("unchecked")
    void searchCountsEveryDateOfTheRangeOverTheWholeFleet() {
        fleetOfFour();
        LocalDate to = DAY.plusDays(2);
        when(orders.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());
        when(trucks.findByCompanyRefId(COMPANY)).thenReturn(List.of());
        when(orders.findLiveInRange(COMPANY, DAY, to)).thenReturn(List.of(
                order(1, 10, "OWN", DAY),
                order(2, 11, "OWN", DAY),
                order(3, 11, "SHARED", DAY),
                order(4, null, "OUTSIDE", DAY),
                order(5, 12, "OWN", DAY.plusDays(1))));

        TruckOrderCalendarResponse response = service.search(TruckOrderSearchRequest.builder()
                .companyRefId(COMPANY).fromDate(DAY).toDate(to).build());

        List<TruckDayCapacityDto> days = response.getDays();
        assertEquals(3, days.size());

        assertEquals(DAY, days.get(0).getDate());
        assertEquals(4, days.get(0).getTotalTrucks());
        assertEquals(2, days.get(0).getTakenTrucks());
        assertEquals(2, days.get(0).getFreeTrucks());
        assertEquals(1, days.get(0).getOutsideOrders());

        assertEquals(0, days.get(0).getWorkshopTrucks(), "nothing is off the road in this fixture");
        assertEquals(1, days.get(1).getTakenTrucks());
        assertEquals(0, days.get(2).getTakenTrucks());
        assertEquals(4, days.get(2).getFreeTrucks());
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchLeavesTheCountersOutForARangeLongerThanTwoMonths() {
        when(orders.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());
        when(trucks.findByCompanyRefId(COMPANY)).thenReturn(List.of());

        TruckOrderCalendarResponse response = service.search(TruckOrderSearchRequest.builder()
                .companyRefId(COMPANY).fromDate(LocalDate.of(2026, 1, 1)).toDate(LocalDate.of(2026, 3, 31)).build());

        assertTrue(response.getDays().isEmpty());
        verify(orders, never()).findLiveInRange(any(), any(), any());
    }

    private static <T> org.mockito.stubbing.OngoingStubbing<T> when(T call) {
        return org.mockito.Mockito.when(call);
    }
}
