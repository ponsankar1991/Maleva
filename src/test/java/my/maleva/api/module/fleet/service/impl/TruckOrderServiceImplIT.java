package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.dto.OrderableTruckDto;
import my.maleva.api.module.fleet.dto.TruckOrderCalendarResponse;
import my.maleva.api.module.fleet.dto.TruckOrderDto;
import my.maleva.api.module.fleet.dto.request.TruckOrderSaveRequest;
import my.maleva.api.module.fleet.dto.request.TruckOrderSearchRequest;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.fleet.service.TruckOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The truck order port, exercised against MalevanewDemo.
 *
 * <p>Everything runs inside the test's own transaction and is rolled back, so
 * the write cases leave no rows behind and the SequenceNoMaster counter is put
 * back where it was.
 *
 * <p>Fixed values are live rows: TruckOrderMaster holds four orders, all company
 * 6, all Pending, running 21 March to 10 April 2026 and numbered ORD002600001
 * upwards.
 */
@SpringBootTest
@Transactional
class TruckOrderServiceImplIT {

    private static final int COMPANY = 6;

    @Autowired private TruckOrderService service;
    @Autowired private TruckMasterRepository truckMasterRepository;

    // Qualified: actuator contributes a second RequestMappingHandlerMapping
    // (controllerEndpointHandlerMapping), so the bare type is ambiguous.
    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    // ------------------------------------------------------------- wiring

    @Test
    void everyEndpointOfTheScreenIsMapped() {
        Set<String> patterns = handlerMapping.getHandlerMethods().entrySet().stream()
                .filter(entry -> entry.getValue().getBeanType().getSimpleName()
                        .equals("TruckOrderController"))
                .flatMap(entry -> entry.getKey().getPathPatternsCondition() == null
                        ? java.util.stream.Stream.<String>empty()
                        : entry.getKey().getPathPatternsCondition().getPatternValues().stream())
                .collect(Collectors.toSet());

        assertTrue(patterns.contains("/api/truck-orders"), "list/save route missing: " + patterns);
        assertTrue(patterns.contains("/api/truck-orders/trucks"), "trucks route missing");
        assertTrue(patterns.contains("/api/truck-orders/next-no"), "next-no route missing");
        assertTrue(patterns.contains("/api/truck-orders/clash"), "clash route missing");
        assertTrue(patterns.contains("/api/truck-orders/{id}"), "get/delete route missing");
    }

    // ------------------------------------------------------------- trucks

    @Test
    void orderableTrucksReturnsExactlyTheFlaggedTrucks() {
        Set<Integer> flagged = truckMasterRepository
                .findByCompanyRefIdAndActive(COMPANY, 1).stream()
                .filter(truck -> Integer.valueOf(1).equals(truck.getOrderableTruck()))
                .map(TruckMaster::getId)
                .collect(Collectors.toSet());

        Set<Integer> offered = service.orderableTrucks(COMPANY).stream()
                .map(OrderableTruckDto::getId)
                .collect(Collectors.toSet());

        assertEquals(flagged, offered, "the calendar should offer OrderableTruck = 1 and nothing else");
    }

    /**
     * A truck marked orderable but soft-deleted must not be offered. The legacy
     * screen asked for Active != 2 and matched plates in the browser, so a dead
     * row sharing a plate with a live one appeared twice.
     */
    @Test
    void orderableTrucksExcludesDeletedTrucks() {
        Set<Integer> offered = service.orderableTrucks(COMPANY).stream()
                .map(OrderableTruckDto::getId)
                .collect(Collectors.toSet());

        for (Integer id : offered) {
            TruckMaster truck = truckMasterRepository.findById(id).orElseThrow();
            assertEquals(1, truck.getActive(), truck.getTruckName() + " is not active");
            assertEquals(1, truck.getOrderableTruck(), truck.getTruckName() + " is not orderable");
        }
    }

    /**
     * The live data has JWS 7151 twice - one row active, one soft-deleted, so a
     * plate is not unique in TruckMaster. The Active = 1 filter is what keeps the
     * dropdown clean.
     */
    @Test
    void orderableTrucksHasNoDuplicatePlates() {
        List<String> names = service.orderableTrucks(COMPANY).stream()
                .map(OrderableTruckDto::getTruckName)
                .toList();
        assertEquals(names.size(), Set.copyOf(names).size(), "duplicate plate in " + names);
    }

    /** The stored plate for GOLD 7151 carries a trailing space. */
    @Test
    void orderableTrucksTrimsStoredNames() {
        for (OrderableTruckDto truck : service.orderableTrucks(COMPANY)) {
            assertEquals(truck.getTruckName().trim(), truck.getTruckName());
        }
    }

    // --------------------------------------------------------------- list

    @Test
    void searchFindsTheOrdersInTheRangeAndNamesTheirTrucks() {
        TruckOrderCalendarResponse response = service.search(TruckOrderSearchRequest.builder()
                .companyRefId(COMPANY)
                .fromDate(LocalDate.of(2026, 3, 1))
                .toDate(LocalDate.of(2026, 3, 31))
                .build());

        assertEquals(2, response.getItems().size(), "March 2026 holds two orders");
        assertEquals(2, response.getTotalOrders());
        // Both March orders are on BNU 7151, so the distinct-truck count is 1.
        assertEquals(1, response.getBookedTrucks());
        for (TruckOrderDto order : response.getItems()) {
            assertNotNull(order.getTruckName(), "the join should resolve a truck name");
            assertTrue(order.getCNumberDisplay().startsWith("ORD"));
        }
    }

    @Test
    void searchOrdersByDateAscending() {
        List<TruckOrderDto> items = service.search(TruckOrderSearchRequest.builder()
                .companyRefId(COMPANY)
                .fromDate(LocalDate.of(2026, 1, 1))
                .toDate(LocalDate.of(2026, 12, 31))
                .build()).getItems();

        for (int i = 1; i < items.size(); i++) {
            assertFalse(items.get(i).getOrderDate().isBefore(items.get(i - 1).getOrderDate()),
                    "orders came back out of date order");
        }
    }

    /**
     * The legacy screen sent a multi-select combo's value as one joined string
     * to a {@code Status =} comparison, so ticking two statuses matched nothing.
     */
    @Test
    void searchAcceptsSeveralStatusesAtOnce() {
        TruckOrderSearchRequest.TruckOrderSearchRequestBuilder base = TruckOrderSearchRequest.builder()
                .companyRefId(COMPANY)
                .fromDate(LocalDate.of(2026, 1, 1))
                .toDate(LocalDate.of(2026, 12, 31));

        int both = service.search(base.statuses(List.of("Pending", "Confirmed")).build())
                .getItems().size();
        int pendingOnly = service.search(base.statuses(List.of("Pending")).build())
                .getItems().size();

        assertTrue(both > 0, "Pending + Confirmed should not return nothing");
        assertEquals(pendingOnly, both, "no Confirmed orders exist, so the two agree");
        assertEquals(0, service.search(base.statuses(List.of("Delivered")).build())
                .getItems().size());
    }

    @Test
    void searchFiltersByTruck() {
        TruckOrderCalendarResponse all = service.search(TruckOrderSearchRequest.builder()
                .companyRefId(COMPANY)
                .fromDate(LocalDate.of(2026, 1, 1))
                .toDate(LocalDate.of(2026, 12, 31))
                .build());

        Integer someTruck = all.getItems().get(0).getTruckRefId();
        TruckOrderCalendarResponse filtered = service.search(TruckOrderSearchRequest.builder()
                .companyRefId(COMPANY)
                .fromDate(LocalDate.of(2026, 1, 1))
                .toDate(LocalDate.of(2026, 12, 31))
                .truckRefId(someTruck)
                .build());

        assertFalse(filtered.getItems().isEmpty());
        assertTrue(filtered.getItems().size() < all.getItems().size(), "the truck filter did nothing");
        assertTrue(filtered.getItems().stream()
                .allMatch(order -> order.getTruckRefId().equals(someTruck)));
    }

    @Test
    void searchRejectsABackwardsRange() {
        assertThrows(InvalidRequestException.class, () -> service.search(TruckOrderSearchRequest.builder()
                .companyRefId(COMPANY)
                .fromDate(LocalDate.of(2026, 3, 31))
                .toDate(LocalDate.of(2026, 3, 1))
                .build()));
    }

    @Test
    void searchRequiresACompany() {
        assertThrows(InvalidRequestException.class,
                () -> service.search(TruckOrderSearchRequest.builder().companyRefId(0).build()));
    }

    // ---------------------------------------------------------- numbering

    @Test
    void nextOrderNumberIsOrdPlusNineDigits() {
        assertTrue(service.nextOrderNumber(COMPANY).matches("^ORD\\d{9}$"),
                "got " + service.nextOrderNumber(COMPANY));
    }

    // --------------------------------------------------------------- save

    @Test
    void savingAnOrderAssignsTheNextNumber() {
        TruckOrderDto saved = service.save(newOrderOn(LocalDate.of(2030, 1, 7)), "tester");

        assertNotNull(saved.getId());
        assertTrue(saved.getCNumberDisplay().matches("^ORD\\d{9}$"));
        assertEquals("Pending", saved.getStatus());
        assertNotNull(saved.getTruckName());
    }

    @Test
    void twoOrdersForTheSameTruckOnTheSameDayAreRejected() {
        LocalDate day = LocalDate.of(2030, 1, 8);
        service.save(newOrderOn(day), "tester");

        InvalidRequestException error =
                assertThrows(InvalidRequestException.class, () -> service.save(newOrderOn(day), "tester"));
        assertEquals("This truck is already booked on the selected date.", error.getMessage());
    }

    @Test
    void anEditDoesNotClashWithItself() {
        TruckOrderDto saved = service.save(newOrderOn(LocalDate.of(2030, 1, 9)), "tester");

        TruckOrderSaveRequest edit = newOrderOn(LocalDate.of(2030, 1, 9));
        edit.setId(saved.getId());
        edit.setRemarks("moved to Penang");

        TruckOrderDto updated = service.save(edit, "tester");
        assertEquals("moved to Penang", updated.getRemarks());
    }

    @Test
    void anEditKeepsTheOrderNumberItWasGiven() {
        TruckOrderDto saved = service.save(newOrderOn(LocalDate.of(2030, 1, 10)), "tester");

        TruckOrderSaveRequest edit = newOrderOn(LocalDate.of(2030, 1, 11));
        edit.setId(saved.getId());
        edit.setStatus("Delivered");

        TruckOrderDto updated = service.save(edit, "tester");
        assertEquals(saved.getCNumberDisplay(), updated.getCNumberDisplay());
        assertEquals(saved.getCNumber(), updated.getCNumber());
        assertEquals("Delivered", updated.getStatus());
        assertEquals(LocalDate.of(2030, 1, 11), updated.getOrderDate());
    }

    /**
     * The legacy path pasted the payload into the EXEC as a quoted literal and
     * stripped apostrophes first to survive it.
     */
    @Test
    void remarksKeepTheirApostrophes() {
        TruckOrderSaveRequest request = newOrderOn(LocalDate.of(2030, 1, 12));
        request.setRemarks("driver's run, KL' -- to Penang");

        assertEquals("driver's run, KL' -- to Penang", service.save(request, "tester").getRemarks());
    }

    @Test
    void savingRejectsATruckThatIsNotOurs() {
        TruckOrderSaveRequest request = newOrderOn(LocalDate.of(2030, 1, 13));
        request.setTruckRefId(999_999);

        assertEquals("Truck Not Found.",
                assertThrows(InvalidRequestException.class,
                        () -> service.save(request, "tester")).getMessage());
    }

    @Test
    void savingRejectsAStatusTheDialogDoesNotOffer() {
        TruckOrderSaveRequest request = newOrderOn(LocalDate.of(2030, 1, 14));
        request.setStatus("Whatever");

        assertThrows(InvalidRequestException.class, () -> service.save(request, "tester"));
    }

    /** Stored in the enum's own casing, so the calendar colour lookup matches. */
    @Test
    void savingNormalisesTheCasingOfAStatus() {
        TruckOrderSaveRequest request = newOrderOn(LocalDate.of(2030, 1, 20));
        request.setStatus("in transit");

        assertEquals("In Transit", service.save(request, "tester").getStatus());
    }

    @Test
    void savingDefaultsAMissingStatusToPending() {
        TruckOrderSaveRequest request = newOrderOn(LocalDate.of(2030, 1, 21));
        request.setStatus(null);

        assertEquals("Pending", service.save(request, "tester").getStatus());
    }

    @Test
    void savingRejectsAMissingTruck() {
        TruckOrderSaveRequest request = newOrderOn(LocalDate.of(2030, 1, 15));
        request.setTruckRefId(0);

        assertEquals("Truck is required.",
                assertThrows(InvalidRequestException.class,
                        () -> service.save(request, "tester")).getMessage());
    }

    // -------------------------------------------------------------- clash

    @Test
    void findClashSeesAnExistingBookingAndIgnoresTheOrderBeingEdited() {
        LocalDate day = LocalDate.of(2030, 1, 16);
        TruckOrderDto saved = service.save(newOrderOn(day), "tester");
        Integer truck = saved.getTruckRefId();

        TruckOrderDto clash = service.findClash(COMPANY, truck, day, 0);
        assertNotNull(clash);
        assertEquals(saved.getId(), clash.getId());

        assertNull(service.findClash(COMPANY, truck, day, saved.getId()),
                "an order should not clash with itself");
        assertNull(service.findClash(COMPANY, truck, day.plusDays(1), 0));
    }

    // ------------------------------------------------------------- delete

    @Test
    void deleteHidesTheOrderFromTheCalendar() {
        LocalDate day = LocalDate.of(2030, 1, 17);
        TruckOrderDto saved = service.save(newOrderOn(day), "tester");

        service.delete(saved.getId(), COMPANY, "tester");

        assertThrows(EntityNotFoundException.class, () -> service.getForEdit(saved.getId(), COMPANY));
        assertEquals(0, service.search(TruckOrderSearchRequest.builder()
                .companyRefId(COMPANY).fromDate(day).toDate(day).build()).getItems().size());
    }

    /** A deleted day is free again, so the truck can be rebooked onto it. */
    @Test
    void deleteFreesTheDayForANewBooking() {
        LocalDate day = LocalDate.of(2030, 1, 18);
        TruckOrderDto saved = service.save(newOrderOn(day), "tester");
        service.delete(saved.getId(), COMPANY, "tester");

        assertNotNull(service.save(newOrderOn(day), "tester").getId());
    }

    @Test
    void deletingSomethingElsesOrderIsNotFound() {
        TruckOrderDto saved = service.save(newOrderOn(LocalDate.of(2030, 1, 19)), "tester");
        assertThrows(EntityNotFoundException.class,
                () -> service.delete(saved.getId(), COMPANY + 1, "tester"));
    }

    // ------------------------------------------------------------ helpers

    /** A blank order on the first truck the calendar offers. */
    private TruckOrderSaveRequest newOrderOn(LocalDate orderDate) {
        Integer truckRefId = service.orderableTrucks(COMPANY).get(0).getId();
        return TruckOrderSaveRequest.builder()
                .companyRefId(COMPANY)
                .truckRefId(truckRefId)
                .employeeRefId(14)
                .orderDate(orderDate)
                .status("Pending")
                .remarks("integration test")
                .build();
    }
}
