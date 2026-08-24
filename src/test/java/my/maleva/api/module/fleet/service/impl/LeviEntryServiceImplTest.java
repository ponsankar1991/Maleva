package my.maleva.api.module.fleet.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.dto.PassEntryDetailDto;
import my.maleva.api.module.fleet.dto.PassEntryListResponse;
import my.maleva.api.module.fleet.dto.RtiOptionDto;
import my.maleva.api.module.fleet.dto.request.PassEntrySaveRequest;
import my.maleva.api.module.fleet.dto.request.PassEntrySearchRequest;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.LeviEntry;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.LeviEntryRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.rti.entity.RTIMaster;
import my.maleva.api.module.rti.repository.RTIMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Values here are real rows in MalevanewDemo: LeviEntry belongs to company 6,
 * and EnterLink is IN/OUT while ExitLink is 1ST LINK/2ND LINK.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LeviEntryServiceImplTest {

    private static final int COMPANY = 6;

    @Mock private LeviEntryRepository leviEntryRepository;
    @Mock private TruckMasterRepository truckMasterRepository;
    @Mock private DriverMasterRepository driverMasterRepository;
    @Mock private RTIMasterRepository rtiMasterRepository;
    @Mock private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private LeviEntryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LeviEntryServiceImpl(leviEntryRepository, truckMasterRepository,
                driverMasterRepository, rtiMasterRepository, jdbcTemplate, objectMapper);

        when(truckMasterRepository.findById(72)).thenReturn(Optional.of(
                TruckMaster.builder().id(72).companyRefId(COMPANY).truckName("FORKLIFT WORKSHOP").build()));
        when(driverMasterRepository.findById(5)).thenReturn(Optional.of(
                DriverMaster.builder().id(5).companyRefId(COMPANY).driverName("RAJU").build()));
        when(rtiMasterRepository.findById(10)).thenReturn(Optional.of(
                rti(10, "RTI000000118")));
    }

    private RTIMaster rti(int id, String number) {
        RTIMaster master = new RTIMaster();
        master.setId(id);
        master.setCompanyRefId(COMPANY);
        master.setCNumberDisplay(number);
        master.setActive(1);
        return master;
    }

    private LeviEntry entry(int id, double amount) {
        return LeviEntry.builder()
                .id(id)
                .companyRefId(COMPANY)
                .cNumber(241)
                .cNumberDisplay("LE000000241")
                .saleDate(LocalDateTime.of(2026, 8, 23, 14, 30))
                .truckRefid(72)
                .driverRefId(5)
                .rtiRefId(10)
                .employeeRefId(3)
                .enterLink("IN")
                .exitLink("1ST LINK")
                .amount((float) amount)
                .remarks("O'Brien said \"go\"")
                .active(1)
                .createdDate(LocalDateTime.now())
                .modifiedDate(LocalDateTime.now())
                .build();
    }

    private PassEntrySaveRequest.PassEntrySaveRequestBuilder saveRequest() {
        return PassEntrySaveRequest.builder()
                .companyRefId(COMPANY)
                .truckRefId(72)
                .driverRefId(5)
                .rtiRefId(10)
                .employeeRefId(3)
                .saleDate(LocalDate.of(2026, 8, 23))
                .amount(12.5)
                .enterLink("IN")
                .exitLink("1ST LINK");
    }

    /** Makes the SP return success and the saved row readable. */
    private void stubSuccessfulSave(int savedId) {
        // Matchers are typed String + Integer on purpose. JdbcTemplate also
        // declares queryForMap(String, Object[], int[]), and untyped any()
        // matchers bind to that overload instead of the varargs one, so the stub
        // silently never applies.
        when(jdbcTemplate.queryForMap(anyString(), anyString(), eq(COMPANY)))
                .thenReturn(Map.of("Result", 1, "msg", "", "BillNo", "LE000000241", "id", savedId));
        when(leviEntryRepository.findByIdAndCompanyRefIdAndActive(savedId, COMPANY, 1))
                .thenReturn(Optional.of(entry(savedId, 12.5)));
    }

    // -------------------------------------------------------------- numbering

    @Test
    void formatsTheNextLeviNumberAsLePlusNineDigits() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any()))
                .thenReturn(241);

        assertEquals("LE000000241", service.nextNumber(COMPANY));
    }

    @Test
    void startsNumberingAtOneWhenTheCompanyHasNoSequenceRow() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any()))
                .thenReturn(null);

        assertEquals("LE000000001", service.nextNumber(COMPANY));
    }

    // ------------------------------------------------------------------ save

    @Test
    void sendsTheFormValuesToTheProcedureAsBoundJson() throws Exception {
        stubSuccessfulSave(1234);

        service.save(saveRequest().remarks("weighbridge levi").build(), "tester");

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForMap(eq("EXEC [SP_LeviEntry] ?, ?"), payload.capture(), eq(COMPANY));

        JsonNode row = objectMapper.readTree(payload.getValue()).get(0);
        assertEquals(0, row.get("Id").asInt(), "a create sends Id 0, which is what the procedure branches on");
        assertEquals(COMPANY, row.get("CompanyRefId").asInt());
        assertEquals(72, row.get("TruckRefid").asInt());
        assertEquals(5, row.get("DriverRefId").asInt());
        assertEquals(10, row.get("RTIRefId").asInt());
        assertEquals("2026-08-23", row.get("SaleDate").asText());
        assertEquals(12.5, row.get("Amount").asDouble());
        assertEquals("IN", row.get("EnterLink").asText());
        assertEquals("1ST LINK", row.get("ExitLink").asText());
        assertEquals(1, row.get("boundindex").asInt(), "the procedure orders its work queue by SNo");
    }

    @Test
    void keepsApostrophesAndQuotesInRemarks() throws Exception {
        stubSuccessfulSave(1234);
        String remarks = "O'Brien said \"go\"";

        service.save(saveRequest().remarks(remarks).build(), "tester");

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForMap(anyString(), payload.capture(), eq(COMPANY));

        // The legacy service ran Replace("'", "") over the JSON before pasting it
        // into the statement, so this text reached the database as "OBrien".
        JsonNode row = objectMapper.readTree(payload.getValue()).get(0);
        assertEquals(remarks, row.get("Remarks").asText());
    }

    @Test
    void sendsTheExistingIdWhenUpdating() throws Exception {
        stubSuccessfulSave(1234);

        service.save(saveRequest().id(1234).remarks("edit").build(), "tester");

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForMap(anyString(), payload.capture(), eq(COMPANY));

        JsonNode row = objectMapper.readTree(payload.getValue()).get(0);
        assertEquals(1234, row.get("Id").asInt());
    }

    @Test
    void reportsTheProceduresOwnMessageWhenItRejectsTheEntry() {
        // Matchers are typed String + Integer on purpose. JdbcTemplate also
        // declares queryForMap(String, Object[], int[]), and untyped any()
        // matchers bind to that overload instead of the varargs one, so the stub
        // silently never applies.
        when(jdbcTemplate.queryForMap(anyString(), anyString(), eq(COMPANY)))
                .thenReturn(Map.of("Result", 0, "msg", "Truck Not Found", "id", 0));

        InvalidRequestException thrown = assertThrows(InvalidRequestException.class,
                () -> service.save(saveRequest().build(), "tester"));
        assertEquals("Truck Not Found", thrown.getMessage());
    }

    @Test
    void rejectsAnUnknownTruckBeforeReachingTheProcedure() {
        when(truckMasterRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class,
                () -> service.save(saveRequest().truckRefId(999).build(), "tester"));
        verify(jdbcTemplate, never()).queryForMap(anyString(), any(), any());
    }

    @Test
    void rejectsALookupBelongingToAnotherCompany() {
        when(rtiMasterRepository.findById(10)).thenReturn(Optional.of(
                RTIMaster.builder().id(10).companyRefId(99).CNumberDisplay("RTI000000118").active(1).build()));

        assertThrows(InvalidRequestException.class,
                () -> service.save(saveRequest().build(), "tester"));
        verify(jdbcTemplate, never()).queryForMap(anyString(), any(), any());
    }

    // ------------------------------------------------------------------ list

    @Test
    void requiresADateRangeUnlessALeviNumberIsGiven() {
        assertThrows(InvalidRequestException.class, () -> service.search(
                PassEntrySearchRequest.builder().companyRefId(COMPANY).build()));
    }

    @Test
    void rejectsAnInvertedDateRange() {
        assertThrows(InvalidRequestException.class, () -> service.search(
                PassEntrySearchRequest.builder()
                        .companyRefId(COMPANY)
                        .fromDate(LocalDate.of(2026, 8, 23))
                        .toDate(LocalDate.of(2026, 8, 1))
                        .build()));
    }

    @Test
    void searchesByLeviNumberWithoutADateRange() {
        when(leviEntryRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(entry(1234, 12.5)));

        PassEntryListResponse response = service.search(PassEntrySearchRequest.builder()
                .companyRefId(COMPANY)
                .search("LE000000241")
                .build());

        assertEquals(1, response.getItems().size());
        assertEquals("LE000000241", response.getItems().get(0).getCNumberDisplay());
    }

    @Test
    void totalsTheAmountsAndResolvesLookupNames() {
        when(leviEntryRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(entry(1, 12.5), entry(2, 7.25)));
        when(truckMasterRepository.findByCompanyRefId(COMPANY)).thenReturn(List.of(
                TruckMaster.builder().id(72).companyRefId(COMPANY).truckName("FORKLIFT WORKSHOP").build()));
        when(driverMasterRepository.findByCompanyRefId(COMPANY)).thenReturn(List.of(
                DriverMaster.builder().id(5).companyRefId(COMPANY).driverName("RAJU").build()));
        when(rtiMasterRepository.findByCompanyRefId(COMPANY)).thenReturn(List.of(rti(10, "RTI000000118")));

        PassEntryListResponse response = service.search(PassEntrySearchRequest.builder()
                .companyRefId(COMPANY)
                .fromDate(LocalDate.of(2026, 8, 1))
                .toDate(LocalDate.of(2026, 8, 31))
                .build());

        assertEquals(19.75, response.getEntriesTotal());
        assertEquals("FORKLIFT WORKSHOP", response.getItems().get(0).getTruckName());
        assertEquals("RAJU", response.getItems().get(0).getDriverName());
        assertEquals("RTI000000118", response.getItems().get(0).getRtiNumber());
    }

    @Test
    void roundsAwayTheNoiseOfTheRealAmountColumn() {
        when(leviEntryRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(entry(1, 4.67)));

        PassEntryListResponse response = service.search(PassEntrySearchRequest.builder()
                .companyRefId(COMPANY)
                .fromDate(LocalDate.of(2026, 8, 1))
                .toDate(LocalDate.of(2026, 8, 31))
                .build());

        assertEquals(4.67, response.getItems().get(0).getAmount(),
                "a 4-byte float reaches Java as 4.670000076293945");
    }

    // ------------------------------------------------------------------ read

    @Test
    void opensAnEntryByItsPrintedLeviNumber() {
        when(leviEntryRepository.findIdsByCNumber(COMPANY, 241)).thenReturn(List.of(1234));
        when(leviEntryRepository.findByIdAndCompanyRefIdAndActive(1234, COMPANY, 1))
                .thenReturn(Optional.of(entry(1234, 12.5)));

        PassEntryDetailDto detail = service.getForEdit(null, 241, COMPANY);

        assertEquals(1234, detail.getId());
        assertEquals("LE000000241", detail.getCNumberDisplay());
    }

    @Test
    void reportsAMissingLeviNumberAsNotFound() {
        when(leviEntryRepository.findIdsByCNumber(COMPANY, 9999)).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class, () -> service.getForEdit(null, 9999, COMPANY));
    }

    @Test
    void doesNotLeakAnotherCompanysEntry() {
        when(leviEntryRepository.findByIdAndCompanyRefIdAndActive(1234, 99, 1))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getForEdit(1234, null, 99));
    }

    // ---------------------------------------------------------------- delete

    @Test
    void reportsADeleteThatMatchedNothing() {
        when(leviEntryRepository.softDelete(anyInt(), anyInt(), anyString())).thenReturn(0);

        assertThrows(EntityNotFoundException.class, () -> service.delete(1234, COMPANY, "tester"));
    }

    @Test
    void softDeletesAnExistingEntry() {
        when(leviEntryRepository.softDelete(1234, COMPANY, "tester")).thenReturn(1);

        service.delete(1234, COMPANY, "tester");

        verify(leviEntryRepository).softDelete(1234, COMPANY, "tester");
    }

    // --------------------------------------------------------------- lookups

    @Test
    void listsActiveRtiNumbersForTheDropdown() {
        when(rtiMasterRepository.findByCompanyRefIdAndActive(COMPANY, 1))
                .thenReturn(List.of(rti(10, "RTI000000118"), rti(11, "RTI000000119")));

        List<RtiOptionDto> options = service.rtiOptions(COMPANY);

        assertEquals(2, options.size());
        assertEquals("RTI000000118", options.get(0).getRtiNumber());
        assertTrue(options.stream().allMatch(option -> option.getId() != null));
    }

    @Test
    void refusesAnyCallWithoutACompany() {
        assertThrows(InvalidRequestException.class, () -> service.rtiOptions(null));
        assertThrows(InvalidRequestException.class, () -> service.rtiOptions(0));
    }
}
