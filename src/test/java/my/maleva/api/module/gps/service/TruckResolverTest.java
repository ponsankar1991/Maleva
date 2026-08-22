package my.maleva.api.module.gps.service;

import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * The ambiguous names here are real rows in MalevanewDemo: TruckMaster holds
 * both "WORKSHOP" (75) and "FORKLIFT WORKSHOP" (72), and both "PTP" (90) and
 * "FORKLIFT PTP" (77).
 */
@ExtendWith(MockitoExtension.class)
class TruckResolverTest {

    @Mock
    private TruckMasterRepository truckMasterRepository;

    private TruckMaster truck(int id, String name) {
        return TruckMaster.builder().id(id).truckName(name).companyRefId(6).active(1).build();
    }

    private TruckResolver.Snapshot snapshotOf(TruckMaster... trucks) {
        when(truckMasterRepository.findByCompanyRefIdAndActive(eq(6), eq(1)))
                .thenReturn(List.of(trucks));
        return new TruckResolver(truckMasterRepository).snapshot(6);
    }

    @Test
    void prefersAnExactNameOverALongerNameContainingIt() {
        TruckResolver.Snapshot snapshot = snapshotOf(
                truck(72, "FORKLIFT WORKSHOP"),
                truck(75, "WORKSHOP"));

        // The legacy LIKE '%WORKSHOP%' matched both and took whichever the
        // database returned first.
        assertEquals(75, snapshot.resolve("WORKSHOP").orElseThrow().getId());
        assertEquals(72, snapshot.resolve("FORKLIFT WORKSHOP").orElseThrow().getId());
    }

    @Test
    void prefersAnExactNameForThePtpPair() {
        TruckResolver.Snapshot snapshot = snapshotOf(
                truck(77, "FORKLIFT PTP"),
                truck(90, "PTP"));

        assertEquals(90, snapshot.resolve("PTP").orElseThrow().getId());
        assertEquals(77, snapshot.resolve("FORKLIFT PTP").orElseThrow().getId());
    }

    @Test
    void stillFallsBackToASubstringMatch() {
        TruckResolver.Snapshot snapshot = snapshotOf(truck(13, "QD 7151 TRAILER"));
        assertEquals(13, snapshot.resolve("QD 7151").orElseThrow().getId());
    }

    @Test
    void ignoresCaseAndSurroundingWhitespace() {
        TruckResolver.Snapshot snapshot = snapshotOf(truck(5, "VR 7151"));
        assertEquals(5, snapshot.resolve("  vr 7151  ").orElseThrow().getId());
    }

    @Test
    void picksTheShortestNameWhenOnlySubstringHitsRemain() {
        TruckResolver.Snapshot snapshot = snapshotOf(
                truck(1, "AAA 1111 LONG TRAILER NAME"),
                truck(2, "AAA 1111 X"));

        assertEquals(2, snapshot.resolve("AAA 1111").orElseThrow().getId());
    }

    @Test
    void returnsEmptyForAnUnknownUnit() {
        TruckResolver.Snapshot snapshot = snapshotOf(truck(5, "VR 7151"));
        assertTrue(snapshot.resolve("ZZZ 9999").isEmpty());
        assertTrue(snapshot.resolve(null).isEmpty());
        assertTrue(snapshot.resolve("   ").isEmpty());
    }
}
