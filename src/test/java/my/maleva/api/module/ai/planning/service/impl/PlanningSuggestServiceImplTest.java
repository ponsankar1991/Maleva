package my.maleva.api.module.ai.planning.service.impl;

import my.maleva.api.common.config.PlanningSuggestProperties;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestFeedbackRequest;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestRequest;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestResponse;
import my.maleva.api.module.ai.planning.repository.PlanningHistoryReader;
import my.maleva.api.module.ai.planning.repository.PlanningHistoryReader.AssignmentRow;
import my.maleva.api.module.ai.planning.repository.PlanningHistoryReader.HistoryRow;
import my.maleva.api.module.ai.planning.repository.PlanningHistoryReader.SuggestionLogRow;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlanningSuggestServiceImplTest {

    private static final int COMPANY = 6;
    private static final LocalDate DAY = LocalDate.of(2026, 9, 3);

    @Mock
    private PlanningHistoryReader reader;
    @Mock
    private SaleOrderMasterRepository saleOrders;
    @Mock
    private CustomerRepository customers;
    @Mock
    private TruckMasterRepository trucks;
    @Mock
    private DriverMasterRepository drivers;

    private PlanningSuggestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PlanningSuggestServiceImpl(new PlanningSuggestProperties(), reader, saleOrders, customers, trucks, drivers);
        service.setClock(Clock.fixed(DAY.atStartOfDay(ZoneId.of("Asia/Kuala_Lumpur")).toInstant(), ZoneId.of("Asia/Kuala_Lumpur")));

        // Jobs on the grid
        SaleOrderMaster nestle = order(501, 100, "Westport", "Shah Alam", "WESTPORT", "");
        SaleOrderMaster newCustomer = order(502, 300, "Penang Port", "Ipoh", "PENANG", "");
        SaleOrderMaster planned = order(503, 100, "Westport", "Klang", "WESTPORT", "");
        SaleOrderMaster clash = order(504, 200, "PTP", "Johor Bahru", "PTP", "");
        SaleOrderMaster nestleAgain = order(505, 100, "Westport", "Shah Alam", "WESTPORT", "");
        SaleOrderMaster onWxy = order(506, 300, "Ipoh", "Penang Port", "", "PENANG");
        when(saleOrders.findAllById(any())).thenReturn(List.of(nestle, newCustomer, planned, clash, nestleAgain, onWxy));

        Customer c100 = new Customer();
        c100.setId(100);
        c100.setCustomerName("NESTLE MALAYSIA");
        Customer c200 = new Customer();
        c200.setId(200);
        c200.setCustomerName("SIME DARBY");
        when(customers.findAllById(any())).thenReturn(List.of(c100, c200));

        // Fleet
        TruckMaster jka = truck(11, "JKA 1234", 1, null);
        TruckMaster wxy = truck(12, "WXY 9876", 1, null);
        TruckMaster outside = truck(13, "OUTSIDE TRUCK", 0, null);
        TruckMaster expired = truck(14, "BMA 111", 1, DAY.minusDays(1));
        when(trucks.findByCompanyRefIdAndActive(COMPANY, 1)).thenReturn(List.of(jka, wxy, outside, expired));

        DriverMaster ahmad = driver(21, "AHMAD BIN ALI", 11, null, null);
        DriverMaster muthu = driver(22, "MUTHU A/L RAMAN", 12, null, null);
        DriverMaster lapsed = driver(23, "ZULKIFLI", null, DAY.minusDays(10), null);
        DriverMaster left = driver(24, "RAJU", null, null, DAY.minusDays(30));
        when(drivers.findByCompanyRefId(COMPANY)).thenReturn(List.of(ahmad, muthu, lapsed, left));

        // History: Ahmad on JKA 1234 did Nestle Westport->Shah Alam three times, once recorded by name only.
        when(reader.history(eq(COMPANY), any(), any())).thenReturn(List.of(
                history(DAY.minusDays(5), 901, 11, 21, "", 100, "Westport", "Shah Alam", "WESTPORT"),
                history(DAY.minusDays(20), 902, 11, 0, "Ahmad Ali-0123456789", 100, "WESTPORT", "SHAH ALAM", "WESTPORT"),
                history(DAY.minusDays(200), 903, 11, 21, "", 100, "Westport", "Shah Alam", "WESTPORT"),
                history(DAY.minusDays(3), 904, 12, 22, "", 200, "PTP", "Johor Bahru", "PTP"),
                history(DAY.minusDays(2), 501, 12, 22, "", 100, "Westport", "Shah Alam", "WESTPORT") // the job itself - ignored
        ));
        // Today: Muthu already drives WXY 9876 on another plan.
        when(reader.assignments(eq(COMPANY), eq(DAY), eq(DAY))).thenReturn(List.of(
                new AssignmentRow(77, DAY, 950, 12, 22, "", "PTP", "Pasir Gudang")));
        // Yesterday: JKA 1234 and Ahmad ended at Westport.
        when(reader.assignments(eq(COMPANY), eq(DAY.minusDays(3)), eq(DAY.minusDays(1)))).thenReturn(List.of(
                new AssignmentRow(70, DAY.minusDays(1), 940, 11, 21, "", "Klang", "Westport")));
        when(reader.logSuggestions(anyInt(), any(), any(), any(), anyList())).thenReturn(1);
    }

    private static PlanningSuggestRequest.Row row(String key, int saleOrderId, Integer truckId, Integer driverId, String driverName) {
        return PlanningSuggestRequest.Row.builder().rowKey(key).saleOrderMasterRefId(saleOrderId)
                .truckRefId(truckId).driverRefId(driverId).driverName(driverName).pickupDate("2026-09-03 08:00").build();
    }

    @Test
    void suggestsFromHistoryContinuityAndAvailability() {
        PlanningSuggestRequest request = PlanningSuggestRequest.builder()
                .companyRefId(COMPANY)
                .rows(List.of(
                        row("0", 501, null, null, null),
                        row("1", 502, null, null, null),
                        row("2", 503, 12, 22, "MUTHU A/L RAMAN"),
                        row("3", 504, 11, 0, "Muthu a/l Raman"),
                        row("4", 505, null, null, null),
                        row("5", 506, 12, null, null)))
                .build();

        PlanningSuggestResponse response = service.suggest(request);

        // One driver per truck per day: the second Nestle job lands on JKA 1234 with the same driver,
        // and the row that already names WXY 9876 gets the driver who is in that truck today.
        // Two loads on the same lane shuttle on one truck: both Nestle rows form one trip
        // with the same truck and driver, in pickup order.
        PlanningSuggestResponse.RowSuggestion secondNestle = response.getRows().get(4);
        assertThat(secondNestle.getTruck().id()).isEqualTo(11);
        assertThat(secondNestle.getDriver().id()).isEqualTo(21);
        assertThat(secondNestle.getTripNo()).isEqualTo(response.getRows().get(0).getTripNo());
        assertThat(secondNestle.getTripPosition()).isEqualTo(2);
        assertThat(secondNestle.getDriver().reasons()).contains("Regular driver of this truck");
        PlanningSuggestResponse.RowSuggestion wxyRow = response.getRows().get(5);
        assertThat(wxyRow.getTruck()).isNull(); // truck kept as typed
        assertThat(wxyRow.getDriver().id()).isEqualTo(22);
        assertThat(wxyRow.getDriver().reasons()).contains("Already driving this truck that day");

        assertThat(response.getPlanningDate()).isEqualTo("2026-09-03");
        assertThat(response.getHistoryPlans()).isEqualTo(4);

        PlanningSuggestResponse.RowSuggestion nestle = response.getRows().get(0);
        assertThat(nestle.isSkipped()).isFalse();
        assertThat(nestle.getTruck().id()).isEqualTo(11);
        assertThat(nestle.getTruck().score()).isEqualTo(100);
        // Reasons are counted over the whole trip (both Nestle loads), hence six matches.
        assertThat(nestle.getTruck().reasons())
                .anyMatch(r -> r.contains("6 job(s) for NESTLE MALAYSIA"))
                .anyMatch(r -> r.contains("on these lanes"));
        assertThat(nestle.getSortBy()).isNotNull();
        assertThat(nestle.getTripLabel()).startsWith("TRIP ");
        assertThat(nestle.getDriver().id()).isEqualTo(21);
        assertThat(nestle.getDriver().reasons()).anyMatch(r -> r.contains("Regular driver of this truck"));
        // Outside and expired trucks are never offered, even as alternatives.
        assertThat(nestle.getAlternativeTrucks()).extracting(PlanningSuggestResponse.Pick::id).doesNotContain(13, 14);
        assertThat(nestle.getWarnings()).isEmpty();

        // Penang Port -> Ipoh is the return leg of WXY 9876's typed Ipoh -> Penang Port job (row 5),
        // so it joins that trip instead of being left without history.
        PlanningSuggestResponse.RowSuggestion returnLeg = response.getRows().get(1);
        assertThat(returnLeg.isSkipped()).isFalse();
        assertThat(returnLeg.getTruck().id()).isEqualTo(12);
        assertThat(returnLeg.getDriver().id()).isEqualTo(22);
        assertThat(returnLeg.getTripNo()).isEqualTo(response.getRows().get(5).getTripNo());
        assertThat(returnLeg.getTripPosition()).isEqualTo(3);
        assertThat(returnLeg.getTripLabel()).contains("PENANG PORT -> IPOH");

        PlanningSuggestResponse.RowSuggestion filled = response.getRows().get(2);
        assertThat(filled.isSkipped()).isTrue();
        assertThat(filled.getSkipReason()).contains("Already has");
        assertThat(filled.getTripPosition()).isEqualTo(1);
        assertThat(filled.getSortBy()).isNotNull();
        // Row 3 below also names Muthu, on JKA 1234, so this row is warned about the clash too.
        assertThat(filled.getWarnings()).extracting(PlanningSuggestResponse.RowWarning::code).contains("DRIVER_DOUBLE_BOOKED");

        PlanningSuggestResponse.RowSuggestion doubleBooked = response.getRows().get(3);
        // The typed name resolves to Muthu, so the row counts as filled and is skipped - but warned.
        assertThat(doubleBooked.isSkipped()).isTrue();
        assertThat(doubleBooked.getWarnings()).extracting(PlanningSuggestResponse.RowWarning::code)
                .contains("DRIVER_DOUBLE_BOOKED");
        assertThat(doubleBooked.getDriver()).isNull();

        assertThat(response.getWarnings()).extracting(PlanningSuggestResponse.PlanWarning::code)
                .contains("DRIVER_ON_TWO_TRUCKS");

        ArgumentCaptor<List<SuggestionLogRow>> logged = ArgumentCaptor.forClass(List.class);
        verify(reader).logSuggestions(eq(COMPANY), eq(DAY), any(), any(), logged.capture());
        assertThat(logged.getValue()).extracting(SuggestionLogRow::saleOrderMasterRefId).contains(501);
    }

    @Test
    void replaceExistingRescoresFilledRows() {
        PlanningSuggestRequest request = PlanningSuggestRequest.builder()
                .companyRefId(COMPANY)
                .replaceExisting(true)
                .rows(List.of(row("0", 503, 12, 22, null)))
                .build();

        PlanningSuggestResponse response = service.suggest(request);

        PlanningSuggestResponse.RowSuggestion suggestion = response.getRows().get(0);
        assertThat(suggestion.isSkipped()).isFalse();
        assertThat(suggestion.getTruck().id()).isEqualTo(11); // Nestle history beats the currently typed truck
        // JKA 1234 ended yesterday at Westport, where this job starts: no empty run.
        assertThat(suggestion.getTruck().reasons()).anyMatch(r -> r.contains("no empty run"));
        assertThat(suggestion.getDriver().id()).isEqualTo(21);
        assertThat(suggestion.getTripLabel()).isEqualTo("TRIP 1: WESTPORT -> KLANG");
    }

    @Test
    void rejectsMissingCompanyOrRows() {
        assertThatThrownBy(() -> service.suggest(PlanningSuggestRequest.builder().rows(List.of(row("0", 501, null, null, null))).build()))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.suggest(PlanningSuggestRequest.builder().companyRefId(COMPANY).build()))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void unknownSaleOrderIsReportedNotThrown() {
        PlanningSuggestRequest request = PlanningSuggestRequest.builder()
                .companyRefId(COMPANY)
                .rows(List.of(row("0", 999, null, null, null)))
                .build();
        PlanningSuggestResponse response = service.suggest(request);
        assertThat(response.getRows().get(0).isSkipped()).isTrue();
        assertThat(response.getRows().get(0).getWarnings()).extracting(PlanningSuggestResponse.RowWarning::level).contains("error");
    }

    @Test
    void feedbackIsForwardedToTheLog() {
        when(reader.recordFeedback(eq(COMPANY), eq(DAY), eq(5), anyList())).thenReturn(2);
        PlanningSuggestFeedbackRequest request = PlanningSuggestFeedbackRequest.builder()
                .companyRefId(COMPANY).planningDate("2026-09-03").planningMasterId(5)
                .rows(List.of(
                        PlanningSuggestFeedbackRequest.Row.builder().saleOrderMasterRefId(501).suggestedTruckId(11).suggestedDriverId(21).chosenTruckId(11).chosenDriverId(21).build(),
                        PlanningSuggestFeedbackRequest.Row.builder().saleOrderMasterRefId(502).suggestedTruckId(null).chosenTruckId(12).chosenDriverId(0).build()))
                .build();

        assertThat(service.feedback(request)).isEqualTo(2);

        ArgumentCaptor<List<SuggestionLogRow>> captor = ArgumentCaptor.forClass(List.class);
        verify(reader).recordFeedback(eq(COMPANY), eq(DAY), eq(5), captor.capture());
        assertThat(captor.getValue().get(1).chosenDriverId()).isNull();
    }

    @Test
    void driverNameKeysIgnoreConnectorsAndMobileSuffixesAndDropAmbiguity() {
        DriverMaster a = driver(1, "AHMAD BIN ALI", null, null, null);
        DriverMaster b = driver(2, "AHMAD B ALI", null, null, null);
        DriverMaster c = driver(3, "MUTHU A/L RAMAN", null, null, null);
        Map<String, Integer> keys = PlanningSuggestServiceImpl.driverIdsByNameKey(List.of(a, b, c));
        assertThat(keys).doesNotContainKey("AHMAD ALI"); // two drivers share it
        assertThat(keys).containsEntry("MUTHU RAMAN", 3);
        assertThat(PlanningSuggestServiceImpl.parseDate("2026/09/03 08:00")).isEqualTo(DAY);
        assertThat(PlanningSuggestServiceImpl.parseDate("bad")).isNull();
    }

    // --- fixtures ----------------------------------------------------------

    private static SaleOrderMaster order(int id, int customerId, String origin, String destination, String sPort, String oPort) {
        SaleOrderMaster order = new SaleOrderMaster();
        order.setId(id);
        order.setCompanyRefId(COMPANY);
        order.setCustomerRefId(customerId);
        order.setOrigin(origin);
        order.setDestination(destination);
        order.setSPort(sPort);
        order.setOPort(oPort);
        order.setPickupDate(LocalDateTime.of(2026, 9, 3, 8, 0));
        return order;
    }

    private static TruckMaster truck(int id, String name, int malevaTruck, LocalDate insuranceExp) {
        TruckMaster truck = new TruckMaster();
        truck.setId(id);
        truck.setCompanyRefId(COMPANY);
        truck.setTruckName(name);
        truck.setActive(1);
        truck.setMalevaTruck(malevaTruck);
        truck.setInsuranceExp(insuranceExp);
        return truck;
    }

    private static DriverMaster driver(int id, String name, Integer truckId, LocalDate licenseExp, LocalDate leavingDate) {
        DriverMaster driver = new DriverMaster();
        driver.setId(id);
        driver.setCompanyRefId(COMPANY);
        driver.setDriverName(name);
        driver.setActive(1);
        driver.setTruckRefId(truckId);
        driver.setLicenseExp(licenseExp);
        driver.setLeavingDate(leavingDate);
        return driver;
    }

    private static HistoryRow history(LocalDate date, int saleOrderId, int truckId, int driverId, String driverName,
                                      int customerId, String origin, String destination, String sPort) {
        return new HistoryRow(date, saleOrderId, truckId, driverId, driverName, origin, destination, customerId,
                origin, destination, sPort, "");
    }
}
