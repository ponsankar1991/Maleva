package my.maleva.api.module.gps.service.impl;

import my.maleva.api.module.fleet.entity.FuelEntry;
import my.maleva.api.module.fleet.entity.FuelFillings;
import my.maleva.api.module.gps.config.FuelGpsMatchProperties;
import my.maleva.api.module.gps.dto.GpsFuelMatchDto;
import my.maleva.api.module.gps.repository.GpsFuelEntryRepository;
import my.maleva.api.module.gps.repository.GpsFuelFillingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * The scenarios here are taken from real rows in MalevanewDemo, so a regression
 * shows up as a failing assertion rather than as quietly wrong fuel variance.
 */
@ExtendWith(MockitoExtension.class)
class FuelGpsMatchServiceImplTest {

    private static final Integer COMPANY = 6;
    private static final LocalDate DAY = LocalDate.of(2026, 5, 19);

    @Mock
    private GpsFuelEntryRepository fuelEntryRepository;

    @Mock
    private GpsFuelFillingRepository fuelFillingRepository;

    private FuelGpsMatchProperties properties;
    private FuelGpsMatchServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new FuelGpsMatchProperties();
        service = new FuelGpsMatchServiceImpl(fuelEntryRepository, fuelFillingRepository, properties);
    }

    private FuelEntry entry(int id, double aliter) {
        return FuelEntry.builder().id(id).companyRefId(COMPANY).truckRefid(21)
                .aliter((float) aliter).saleDate(DAY.atStartOfDay()).active(1).build();
    }

    /** An entry that already carries a stored link. */
    private FuelEntry linkedEntry(int id, double aliter, int fillingId, String matchType) {
        FuelEntry entry = entry(id, aliter);
        entry.setFuelFillingRefId(fillingId);
        entry.setFuelFillingMatchType(matchType);
        return entry;
    }

    private FuelFillings filling(int id, String filled, int hour) {
        return FuelFillings.builder().id(id).companyRefId(COMPANY).truckRefId(21)
                .filled(filled).time(DAY.atTime(hour, 0)).vehicle("GOLD 7151").build();
    }

    private void given(List<FuelEntry> entries, List<FuelFillings> fillings) {
        when(fuelEntryRepository.findActiveForTruckOnDay(eq(COMPANY), eq(21), any(), any()))
                .thenReturn(entries);
        if (!entries.isEmpty()) {
            when(fuelFillingRepository.findForTruckOnDay(eq(COMPANY), eq(21), any(), any()))
                    .thenReturn(fillings);
        }
    }

    @Test
    void assignsEachFillingToOnlyOneEntry() {
        // Truck 33 on 2025-12-15: two entries with identical litres, one filling.
        // The legacy query gave the same 57 l filling to both, counting it twice.
        given(List.of(entry(3469, 65.359), entry(3471, 65.359)),
                List.of(filling(11583, "57 l", 9)));

        List<GpsFuelMatchDto> result = service.matchForTruckOnDay(COMPANY, 21, DAY);

        assertEquals(2, result.size());
        long withFilling = result.stream().filter(r -> r.getFuelFillingId() != null).count();
        assertEquals(1, withFilling, "the same filling must not be claimed twice");
        assertEquals("ALREADY_CLAIMED", result.stream()
                .filter(r -> r.getFuelFillingId() == null)
                .findFirst().orElseThrow().getUnmatchedReason());
    }

    @Test
    void givesEachEntryItsOwnFillingWhenVolumesOverlap() {
        // Truck 13 on 2025-06-04: entry 2158 took the 109 l filling that belongs
        // to entry 2159, because it was evaluated independently.
        given(List.of(entry(2158, 142.914), entry(2159, 109.794)),
                List.of(filling(131, "109 l", 8), filling(132, "140 l", 15)));

        List<GpsFuelMatchDto> result = service.matchForTruckOnDay(COMPANY, 21, DAY);

        GpsFuelMatchDto first = result.get(0);
        GpsFuelMatchDto second = result.get(1);
        assertEquals(132, first.getFuelFillingId(), "142.9 l entry should take the 140 l filling");
        assertEquals(131, second.getFuelFillingId(), "109.8 l entry should keep the 109 l filling");
        assertNotEquals(first.getFuelFillingId(), second.getFuelFillingId());
    }

    @Test
    void matchesAllThreeFillsWhenATruckRefuelsThreeTimes() {
        // Truck 21 on 2026-05-19, the real three-fill day.
        given(List.of(entry(5079, 41.77), entry(5080, 26.9), entry(5091, 60.78)),
                List.of(filling(24677, "41 l", 11), filling(24690, "27 l", 14),
                        filling(24711, "59 l", 20)));

        List<GpsFuelMatchDto> result = service.matchForTruckOnDay(COMPANY, 21, DAY);

        assertEquals(3, result.size());
        assertEquals(24677, result.get(0).getFuelFillingId());
        assertEquals(24690, result.get(1).getFuelFillingId());
        assertEquals(24711, result.get(2).getFuelFillingId());
        assertEquals(3, result.stream().map(GpsFuelMatchDto::getFuelFillingId).distinct().count());
    }

    @Test
    void survivesThePlaceholderRowThatBreaksTheLegacySql() {
        // FuelFillings id 6165 holds "-----"; CAST(... AS FLOAT) throws on it and
        // takes the whole EditFuelEntry query down.
        given(List.of(entry(1, 100.0)),
                List.of(filling(6165, "-----", 4), filling(6166, "98 l", 10)));

        List<GpsFuelMatchDto> result = service.matchForTruckOnDay(COMPANY, 21, DAY);

        assertEquals(1, result.size());
        assertEquals(6166, result.get(0).getFuelFillingId());
    }

    @Test
    void reportsOutOfToleranceSeparatelyFromNoData() {
        given(List.of(entry(1, 100.0)), List.of(filling(9, "10 l", 10)));

        GpsFuelMatchDto result = service.matchForTruckOnDay(COMPANY, 21, DAY).get(0);

        assertNull(result.getFuelFillingId());
        assertEquals("OUT_OF_TOLERANCE", result.getUnmatchedReason());
    }

    @Test
    void reportsNoGpsDataWhenTheTruckHasNoFillings() {
        given(List.of(entry(1, 100.0)), List.of());

        GpsFuelMatchDto result = service.matchForTruckOnDay(COMPANY, 21, DAY).get(0);

        assertEquals("NO_GPS_DATA", result.getUnmatchedReason());
    }

    @Test
    void honoursTheThirtyFivePercentTolerance() {
        // 135 l is exactly at the edge for a 100 l entry, 136 l is past it.
        given(List.of(entry(1, 100.0)), List.of(filling(9, "135 l", 10)));
        assertEquals(9, service.matchForTruckOnDay(COMPANY, 21, DAY).get(0).getFuelFillingId());
    }

    @Test
    void skipsEntriesWithNoEnteredLitres() {
        given(List.of(entry(1, 0.0)), List.of(filling(9, "50 l", 10)));

        GpsFuelMatchDto result = service.matchForTruckOnDay(COMPANY, 21, DAY).get(0);

        assertNull(result.getFuelFillingId());
    }

    @Test
    void canReproduceTheLegacyBehaviourWhenAskedTo() {
        properties.setOneToOne(false);
        given(List.of(entry(3469, 65.359), entry(3471, 65.359)),
                List.of(filling(11583, "57 l", 9)));

        List<GpsFuelMatchDto> result = service.matchForTruckOnDay(COMPANY, 21, DAY);

        assertTrue(result.stream().allMatch(r -> r.getFuelFillingId() != null),
                "with one-to-one off, both entries take the same filling as the legacy query did");
    }

    @Test
    void honoursAStoredLinkEvenWhenAnotherFillingIsCloser() {
        // Entry 1 is pinned to the 90 l filling. Left to the matcher it would
        // take the 100 l one, and the answer on screen would change the moment a
        // second entry was keyed in.
        given(List.of(linkedEntry(1, 100.0, 90, "MANUAL"), entry(2, 95.0)),
                List.of(filling(90, "90 l", 8), filling(100, "100 l", 15)));

        List<GpsFuelMatchDto> result = service.matchForTruckOnDay(COMPANY, 21, DAY);

        assertEquals(90, result.get(0).getFuelFillingId(), "the stored link must win");
        assertEquals(100, result.get(1).getFuelFillingId(), "the sibling gets what is left");
    }

    @Test
    void ignoresAStoredLinkPointingOutsideTheTruckDay() {
        // The truck or date was edited, so the linked filling is no longer on
        // this day. Showing a foreign reading would be worse than showing none.
        given(List.of(linkedEntry(1, 100.0, 4242, "AUTO")),
                List.of(filling(90, "98 l", 8)));

        List<GpsFuelMatchDto> result = service.matchForTruckOnDay(COMPANY, 21, DAY);

        assertEquals(90, result.get(0).getFuelFillingId());
    }

    @Test
    void aStoredLinkRemovesThatFillingFromEveryoneElse() {
        given(List.of(linkedEntry(1, 57.0, 11583, "MANUAL"), entry(2, 57.0)),
                List.of(filling(11583, "57 l", 9)));

        List<GpsFuelMatchDto> result = service.matchForTruckOnDay(COMPANY, 21, DAY);

        assertEquals(11583, result.get(0).getFuelFillingId());
        assertNull(result.get(1).getFuelFillingId());
        assertEquals("ALREADY_CLAIMED", result.get(1).getUnmatchedReason());
    }

    @Test
    void returnsNothingWhenTheTruckHasNoEntriesThatDay() {
        given(List.of(), List.of());
        assertTrue(service.matchForTruckOnDay(COMPANY, 21, DAY).isEmpty());
    }
}
