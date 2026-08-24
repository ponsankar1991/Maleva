package my.maleva.api.module.fleet.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.dto.PassEntryListResponse;
import my.maleva.api.module.fleet.dto.request.PassEntrySaveRequest;
import my.maleva.api.module.fleet.dto.request.PassEntrySearchRequest;
import my.maleva.api.module.fleet.entity.AutoPassEntry;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.AutoPassEntryRepository;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Auto pass entries share {@link AbstractPassEntryService} with levi entries,
 * which {@link LeviEntryServiceImplTest} covers in depth. These tests pin the
 * parts that make an auto pass an auto pass: the AP prefix, the SequenceNoMaster
 * name, and the procedure it calls.
 *
 * Values are real rows in MalevanewDemo: AutoPassEntry belongs to company 6 and
 * its numbers run up to AP000000167.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AutoPassEntryServiceImplTest {

    private static final int COMPANY = 6;

    @Mock private AutoPassEntryRepository autoPassEntryRepository;
    @Mock private TruckMasterRepository truckMasterRepository;
    @Mock private DriverMasterRepository driverMasterRepository;
    @Mock private RTIMasterRepository rtiMasterRepository;
    @Mock private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AutoPassEntryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AutoPassEntryServiceImpl(autoPassEntryRepository, truckMasterRepository,
                driverMasterRepository, rtiMasterRepository, jdbcTemplate, objectMapper);

        when(truckMasterRepository.findById(72)).thenReturn(Optional.of(
                TruckMaster.builder().id(72).companyRefId(COMPANY).truckName("FORKLIFT WORKSHOP").build()));
        when(driverMasterRepository.findById(5)).thenReturn(Optional.of(
                DriverMaster.builder().id(5).companyRefId(COMPANY).driverName("RAJU").build()));
        when(rtiMasterRepository.findById(10)).thenReturn(Optional.of(rti(10)));
    }

    private RTIMaster rti(int id) {
        RTIMaster master = new RTIMaster();
        master.setId(id);
        master.setCompanyRefId(COMPANY);
        master.setCNumberDisplay("RTI000000118");
        master.setActive(1);
        return master;
    }

    private AutoPassEntry entry(int id, double amount) {
        return AutoPassEntry.builder()
                .id(id)
                .companyRefId(COMPANY)
                .cNumber(167)
                .cNumberDisplay("AP000000167")
                .saleDate(LocalDateTime.of(2026, 8, 23, 9, 15))
                .truckRefid(72)
                .driverRefId(5)
                .rtiRefId(10)
                .employeeRefId(3)
                .amount((float) amount)
                .remarks("gate pass")
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
                .amount(35.0);
    }

    private void stubSuccessfulSave(int savedId) {
        // Typed matchers: JdbcTemplate also declares queryForMap(String, Object[], int[]),
        // which untyped any() matchers bind to instead of the varargs overload.
        when(jdbcTemplate.queryForMap(anyString(), anyString(), eq(COMPANY)))
                .thenReturn(Map.of("Result", 1, "msg", "", "BillNo", "AP000000167", "id", savedId));
        when(autoPassEntryRepository.findByIdAndCompanyRefIdAndActive(savedId, COMPANY, 1))
                .thenReturn(Optional.of(entry(savedId, 35.0)));
    }

    @Test
    void numbersAutoPassEntriesWithTheApPrefix() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any()))
                .thenReturn(168);

        assertEquals("AP000000168", service.nextNumber(COMPANY),
                "the levi prefix LE must not leak into this document");
    }

    @Test
    void readsItsSequenceFromTheAutoPassEntryRow() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any()))
                .thenReturn(168);

        service.nextNumber(COMPANY);

        verify(jdbcTemplate).queryForObject(
                anyString(), eq(Integer.class), eq(COMPANY), eq("AutoPassEntry"));
    }

    @Test
    void callsItsOwnStoredProcedure() {
        stubSuccessfulSave(1234);

        service.save(saveRequest().remarks("gate pass").build(), "tester");

        verify(jdbcTemplate).queryForMap(
                eq("EXEC [SP_AutoPassEntry] ?, ?"), anyString(), eq(COMPANY));
    }

    @Test
    void sendsTheFormValuesToTheProcedureAsBoundJson() throws Exception {
        stubSuccessfulSave(1234);

        service.save(saveRequest().remarks("O'Brien's gate pass").build(), "tester");

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForMap(anyString(), payload.capture(), eq(COMPANY));

        JsonNode row = objectMapper.readTree(payload.getValue()).get(0);
        assertEquals(0, row.get("Id").asInt());
        assertEquals(COMPANY, row.get("CompanyRefId").asInt());
        assertEquals(72, row.get("TruckRefid").asInt());
        assertEquals(35.0, row.get("Amount").asDouble());
        // The legacy service stripped apostrophes out of the JSON before pasting
        // it into the statement, so this reached the database as "OBriens".
        assertEquals("O'Brien's gate pass", row.get("Remarks").asText());
    }

    @Test
    void rejectsAnUnknownTruckBeforeReachingTheProcedure() {
        when(truckMasterRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class,
                () -> service.save(saveRequest().truckRefId(999).build(), "tester"));
    }

    @Test
    void listsAutoPassEntriesWithTheirTotal() {
        when(autoPassEntryRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(entry(1, 35.0), entry(2, 15.5)));
        when(truckMasterRepository.findByCompanyRefId(COMPANY)).thenReturn(List.of(
                TruckMaster.builder().id(72).companyRefId(COMPANY).truckName("FORKLIFT WORKSHOP").build()));
        when(driverMasterRepository.findByCompanyRefId(COMPANY)).thenReturn(List.of(
                DriverMaster.builder().id(5).companyRefId(COMPANY).driverName("RAJU").build()));
        when(rtiMasterRepository.findByCompanyRefId(COMPANY)).thenReturn(List.of(rti(10)));

        PassEntryListResponse response = service.search(PassEntrySearchRequest.builder()
                .companyRefId(COMPANY)
                .fromDate(LocalDate.of(2026, 8, 1))
                .toDate(LocalDate.of(2026, 8, 31))
                .build());

        assertEquals(50.5, response.getEntriesTotal());
        assertEquals("AP000000167", response.getItems().get(0).getCNumberDisplay());
        assertEquals("FORKLIFT WORKSHOP", response.getItems().get(0).getTruckName());
    }

    @Test
    void requiresADateRangeUnlessAnAutoPassNumberIsGiven() {
        assertThrows(InvalidRequestException.class, () -> service.search(
                PassEntrySearchRequest.builder().companyRefId(COMPANY).build()));
    }
}
