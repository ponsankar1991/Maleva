package my.maleva.api.module.rti.batch.service.impl;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchDtos.DriverSource;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchDtos.SkipReason;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchPreview;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchRequest;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchResult;
import my.maleva.api.module.rti.batch.dto.PlanningRtiGroup;
import my.maleva.api.module.rti.batch.repository.PlanningRtiBatchReader;
import my.maleva.api.module.rti.batch.repository.PlanningRtiBatchReader.DriverRow;
import my.maleva.api.module.rti.batch.repository.PlanningRtiBatchReader.LastDriver;
import my.maleva.api.module.rti.batch.repository.PlanningRtiBatchReader.PlanHeader;
import my.maleva.api.module.rti.batch.repository.PlanningRtiBatchReader.PlanRow;
import my.maleva.api.module.rti.dto.RTIMasterDto;
import my.maleva.api.module.rti.service.RTIMasterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlanningRtiBatchServiceImplTest {

    private static final int COMPANY = 6;
    private static final int PLAN = 752;

    @Mock
    private PlanningRtiBatchReader reader;
    @Mock
    private RTIMasterService rtiMasterService;
    @Mock
    private NamedParameterJdbcTemplate jdbc;

    private PlanningRtiBatchServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PlanningRtiBatchServiceImpl(reader, rtiMasterService, jdbc);

        when(reader.planHeader(PLAN, COMPANY))
                .thenReturn(new PlanHeader(PLAN, "PL000000752", "2026-08-10", 4));
        when(reader.activeDrivers(COMPANY)).thenReturn(List.of(
                new DriverRow(85, "UGUNTHAN TANGAVELU"),
                new DriverRow(100, "KESAVAN A/L TAMILSALVAN"),
                new DriverRow(27, "NADARAJAH GOVINDARAJOO"),
                new DriverRow(22, "OUTSIDE DRIVER")));
        when(reader.activeTrucks(COMPANY)).thenReturn(List.of());
        when(reader.lastDriverByTruck(anyInt(), any())).thenReturn(Map.of());
        when(reader.jobsAlreadyInRti(anyCollection())).thenReturn(Map.of());

        AtomicInteger rtiId = new AtomicInteger(9000);
        when(rtiMasterService.create(any())).thenAnswer(invocation -> {
            RTIMasterDto posted = invocation.getArgument(0);
            RTIMasterDto saved = new RTIMasterDto();
            int id = rtiId.incrementAndGet();
            saved.setId(id);
            saved.setCNumberDisplay("RTI" + String.format("%09d", id));
            saved.setRtiDetails(posted.getRtiDetails());
            return saved;
        });
    }

    private static PlanRow row(int detailId, int jobId, int truckId, String truckName,
                               String driverName, String pickupDay, int existingRtiId, String existingRtiNo) {
        return new PlanRow(detailId, jobId, truckId, truckName, 0, driverName, detailId,
                "NORTHPORT", "WESTPORT",
                pickupDay.isEmpty() ? "" : pickupDay + " 08:00",
                pickupDay.isEmpty() ? "" : pickupDay + " 17:00",
                pickupDay,
                "SO" + jobId, "KLIA LOGISTICS", "Pickup addr", "Delivery addr",
                "", "", "", "",
                existingRtiId, existingRtiNo, existingRtiId > 0 ? "2026-08-07" : "");
    }

    @Test
    void groupsByTruckAndDayAndSpreadsTheDriverTypedOnOneRow() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, ""),
                row(2, 102, 64, "JPM 7151", "", "2026-08-10", 0, ""),
                row(3, 103, 13, "JPM 8020", "", "2026-08-10", 0, "")));
        when(reader.lastDriverByTruck(anyInt(), any()))
                .thenReturn(Map.of(13, new LastDriver(27, "NADARAJAH GOVINDARAJOO", "2026-08-07")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, false);

        assertThat(preview.isComplete()).isTrue();
        assertThat(preview.plannedJobs()).isEqualTo(3);
        assertThat(preview.jobsToCreate()).isEqualTo(3);
        assertThat(preview.groups()).hasSize(2);

        PlanningRtiGroup jpm7151 = preview.groups().stream()
                .filter(group -> "JPM 7151".equals(group.truckName())).findFirst().orElseThrow();
        // The driver named on row 1 covers row 2 as well - planners type it once.
        assertThat(jpm7151.jobs()).hasSize(2);
        assertThat(jpm7151.driverRefId()).isEqualTo(85);
        assertThat(jpm7151.driverSource()).isEqualTo(DriverSource.PLAN);
        assertThat(jpm7151.isReady()).isTrue();

        PlanningRtiGroup jpm8020 = preview.groups().stream()
                .filter(group -> "JPM 8020".equals(group.truckName())).findFirst().orElseThrow();
        assertThat(jpm8020.driverRefId()).isEqualTo(27);
        assertThat(jpm8020.driverSource()).isEqualTo(DriverSource.LAST_TRIP);
        assertThat(jpm8020.warnings())
                .anyMatch(warning -> warning.contains("last trip") && warning.contains("2026-08-07"));
    }

    @Test
    void doesNotHandUnnamedRowsToEitherDriverWhenATruckCarriesTwo() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, ""),
                row(2, 102, 64, "JPM 7151", "KESAVAN A/L TAMILSALVAN", "2026-08-10", 0, ""),
                row(3, 103, 64, "JPM 7151", "", "2026-08-10", 0, "")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, false);

        // RTIMaster holds one driver, so the truck has to split; the unnamed row
        // is not handed to either driver on a guess.
        assertThat(preview.groups()).hasSize(3);
        assertThat(preview.isComplete()).isTrue();
        assertThat(preview.groups()).anyMatch(group -> group.warnings().stream()
                .anyMatch(warning -> warning.contains("more than one driver")));
    }

    @Test
    void keepsOneTruckAndDriverOnOneRtiEvenAcrossDays() {
        // The rule is truck + driver, not truck + day: every job this truck and
        // this driver carry belongs on the same RTI.
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-11", 0, ""),
                row(2, 102, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, "")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, false);

        assertThat(preview.groups()).hasSize(1);
        assertThat(preview.groups().get(0).jobs()).hasSize(2);
        // The date line shows the first day the pair works.
        assertThat(preview.groups().get(0).pickupDate()).isEqualTo("2026-08-10");
    }

    @Test
    void splitsOneTruckBetweenTwoNamedDriversBecauseAnRtiHoldsOne() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, ""),
                row(2, 102, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-11", 0, ""),
                row(3, 103, 64, "JPM 7151", "KESAVAN A/L TAMILSALVAN", "2026-08-10", 0, "")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, false);

        assertThat(preview.groups()).hasSize(2);
        assertThat(preview.groups()).extracting(PlanningRtiGroup::driverRefId)
                .containsExactlyInAnyOrder(85, 100);
        assertThat(preview.groups()).filteredOn(group -> group.driverRefId() == 85)
                .singleElement().satisfies(group -> assertThat(group.jobs()).hasSize(2));
        assertThat(preview.isComplete()).isTrue();
    }

    @Test
    void everyPlannedJobIsEitherCreatedOrExplained() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, ""),
                row(2, 102, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 7788, "RTI000007788"),
                row(3, 103, 0, "", "", "2026-08-10", 0, ""),
                row(4, 0, 13, "JPM 8020", "", "2026-08-10", 0, ""),
                row(5, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, "")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, false);

        assertThat(preview.isComplete()).isTrue();
        assertThat(preview.plannedJobs()).isEqualTo(5);
        assertThat(preview.jobsToCreate()).isEqualTo(1);
        assertThat(preview.jobsSkipped()).isEqualTo(4);
        assertThat(preview.skipped()).extracting(skip -> skip.reason())
                .containsExactlyInAnyOrder(SkipReason.ALREADY_IN_RTI, SkipReason.NO_TRUCK,
                        SkipReason.NO_JOB_REFERENCE, SkipReason.DUPLICATE_IN_PLAN);
    }

    @Test
    void tickedRowsNarrowTheSelectionButAreGroupedTheSameWay() {
        // Three trucks on the plan; the planner ticks jobs from two of them. That
        // is two RTIs, not one holding both trucks - an RTI record has room for
        // exactly one truck and one driver.
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, ""),
                row(2, 102, 13, "JPM 8020", "KESAVAN A/L TAMILSALVAN", "2026-08-10", 0, ""),
                row(3, 103, 9, "JPM 9000", "NADARAJAH GOVINDARAJOO", "2026-08-10", 0, "")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, List.of(101, 102), false);

        assertThat(preview.groups()).hasSize(2);
        assertThat(preview.jobsToCreate()).isEqualTo(2);
        // The untouched job is still counted and explained, never dropped.
        assertThat(preview.plannedJobs()).isEqualTo(3);
        assertThat(preview.jobsSkipped()).isEqualTo(1);
        assertThat(preview.isComplete()).isTrue();
        assertThat(preview.skipped()).singleElement().satisfies(skip -> {
            assertThat(skip.saleOrderMasterRefId()).isEqualTo(103);
            assertThat(skip.reason()).isEqualTo(SkipReason.NOT_CONFIRMED);
        });
    }

    @Test
    void tickingRowsOfOneTruckStillMakesOneRti() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, ""),
                row(2, 102, 64, "JPM 7151", "", "2026-08-10", 0, ""),
                row(3, 103, 13, "JPM 8020", "KESAVAN A/L TAMILSALVAN", "2026-08-10", 0, "")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, List.of(101, 102), false);

        assertThat(preview.groups()).hasSize(1);
        assertThat(preview.groups().get(0).jobs()).hasSize(2);
        assertThat(preview.groups().get(0).driverRefId()).isEqualTo(85);
    }

    @Test
    void createsOneRtiPerConfirmedGroupAndNeverFillsSalary() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, ""),
                row(2, 102, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, ""),
                row(3, 103, 13, "JPM 8020", "KESAVAN A/L TAMILSALVAN", "2026-08-10", 0, "")));

        PlanningRtiBatchResult result = service.create(PLAN, request(
                group(64, 85, List.of(101, 102)),
                group(13, 100, List.of(103))));

        assertThat(result.isComplete()).isTrue();
        assertThat(result.created()).hasSize(2);
        assertThat(result.jobsCreated()).isEqualTo(3);
        assertThat(result.jobsSkipped()).isZero();

        ArgumentCaptor<RTIMasterDto> captor = ArgumentCaptor.forClass(RTIMasterDto.class);
        verify(rtiMasterService, org.mockito.Mockito.times(2)).create(captor.capture());
        RTIMasterDto first = captor.getAllValues().get(0);
        assertThat(first.getTruckRefId()).isEqualTo(64);
        assertThat(first.getDriverRefId()).isEqualTo(85);
        assertThat(first.getRtiDetails()).hasSize(2);
        assertThat(first.getRemarks()).contains("PL000000752");
        // The driver's pay is a person's decision, every time.
        assertThat(first.getRtiDetails()).allSatisfy(detail -> {
            assertThat(detail.getSalary()).isNull();
            assertThat(detail.getPpic()).isNull();
            assertThat(detail.getDpic()).isNull();
        });
        // Nothing is claimed on the RTI header either - but the NOT NULL money
        // columns must carry a zero, not a null, or the insert is rejected.
        assertThat(first.getAmount()).isEqualTo(0.0);
        assertThat(first.getSleepingAmount()).isEqualTo(0.0);
        assertThat(first.getPickupAmount()).isEqualTo(0.0);
        assertThat(first.getDropAmount()).isEqualTo(0.0);
        assertThat(first.getExitAmount()).isEqualTo(0);
    }

    @Test
    void skipsAJobThatGainedAnRtiBetweenPreviewAndConfirm() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, ""),
                row(2, 102, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, "")));
        // Someone else created an RTI for job 102 while the preview was on screen.
        when(reader.jobsAlreadyInRti(anyCollection())).thenReturn(Map.of(102, "RTI000009001"));

        PlanningRtiBatchResult result = service.create(PLAN, request(group(64, 85, List.of(101, 102))));

        assertThat(result.isComplete()).isTrue();
        assertThat(result.jobsCreated()).isEqualTo(1);
        assertThat(result.skipped()).singleElement()
                .satisfies(skip -> assertThat(skip.existingRtiNo()).isEqualTo("RTI000009001"));
    }

    @Test
    void refusesAJobThatIsNotOnThePlan() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, "")));

        assertThatThrownBy(() -> service.create(PLAN, request(group(64, 85, List.of(101, 999)))))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("999");
        verify(rtiMasterService, never()).create(any());
    }

    @Test
    void refusesAGroupWithNoDriver() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "", "2026-08-10", 0, "")));

        assertThatThrownBy(() -> service.create(PLAN, request(group(64, 0, List.of(101)))))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("truck and a driver");
    }

    @Test
    void reportsJobsThePlannerLeftOutOfTheBatch() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, ""),
                row(2, 102, 13, "JPM 8020", "KESAVAN A/L TAMILSALVAN", "2026-08-10", 0, "")));

        // Only the first truck is confirmed; the second must still be accounted for.
        PlanningRtiBatchResult result = service.create(PLAN, request(group(64, 85, List.of(101))));

        assertThat(result.isComplete()).isTrue();
        assertThat(result.jobsCreated()).isEqualTo(1);
        assertThat(result.skipped()).singleElement()
                .satisfies(skip -> assertThat(skip.saleOrderMasterRefId()).isEqualTo(102));
    }

    @Test
    void leavesAnUnknownTypedNameForThePlannerRatherThanGuessing() {
        // "SATHISH" is a real person to the planner and nobody at all to the
        // driver master. Naming the wrong driver on a pay document is worse than
        // asking, so the group comes back unresolved with the text shown.
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "SATHISH", "2026-08-10", 0, "")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, false);

        PlanningRtiGroup group = preview.groups().get(0);
        assertThat(group.driverSource()).isEqualTo(DriverSource.NONE);
        assertThat(group.driverRefId()).isZero();
        assertThat(group.isReady()).isFalse();
        assertThat(group.warnings()).anyMatch(warning -> warning.contains("SATHISH"));
    }

    @Test
    void resolvesTheFirstNamePlannersActuallyType() {
        // The plan carries "KESAVAN"; exactly one active driver is a KESAVAN.
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "KESAVAN", "2026-08-10", 0, "")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, false);

        PlanningRtiGroup group = preview.groups().get(0);
        assertThat(group.driverRefId()).isEqualTo(100);
        assertThat(group.driverSource()).isEqualTo(DriverSource.PLAN);
        assertThat(group.warnings())
                .anyMatch(warning -> warning.contains("KESAVAN") && warning.contains("TAMILSALVAN"));
    }

    @Test
    void refusesAShorthandThatCouldMeanTwoDifferentDrivers() {
        // NAVIN is a character prefix of both NAVINDREN and NAVINA - the kind of
        // near-match that would put one driver's name on another's pay document.
        when(reader.activeDrivers(COMPANY)).thenReturn(List.of(
                new DriverRow(64, "NAVINDREN A/L ARASIE"),
                new DriverRow(110, "NAVINA BO"),
                new DriverRow(22, "OUTSIDE DRIVER")));
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "NAVIN", "2026-08-10", 0, "")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, false);

        assertThat(preview.groups().get(0).driverSource()).isEqualTo(DriverSource.NONE);
    }

    @Test
    void treatsDuplicateMasterRowsForOnePersonAsOneDriver() {
        // KEVIN RAJ A/L KRISHNAN exists twice in DriverMaster; same name is the
        // same person, so that is not an ambiguity.
        when(reader.activeDrivers(COMPANY)).thenReturn(List.of(
                new DriverRow(65, "KEVIN RAJ A/L KRISHNAN"),
                new DriverRow(7, "KEVIN RAJ A/L KRISHNAN"),
                new DriverRow(22, "OUTSIDE DRIVER")));
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "KEVIN", "2026-08-10", 0, "")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, false);

        PlanningRtiGroup group = preview.groups().get(0);
        assertThat(group.driverSource()).isEqualTo(DriverSource.PLAN);
        assertThat(group.driverRefId()).isEqualTo(7);
    }

    @Test
    void keepsAnExplicitOutsideDriverFromThePlanningModal() {
        // The planning driver modal stores the placeholder id plus the typed name.
        PlanRow outside = new PlanRow(1, 101, 64, "JPM 7151", 22, "RAJU SUBRAMANIAM", 1,
                "NORTHPORT", "WESTPORT", "2026-08-10 08:00", "2026-08-10 17:00", "2026-08-10",
                "SO101", "KLIA LOGISTICS", "", "", "", "", "", "", 0, "", "");
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(outside));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, false);

        PlanningRtiGroup group = preview.groups().get(0);
        assertThat(group.driverSource()).isEqualTo(DriverSource.OUTSIDE);
        assertThat(group.driverRefId()).isEqualTo(22);
        assertThat(group.outsideDriver()).isEqualTo("RAJU SUBRAMANIAM");
    }

    @Test
    void matchesAPlannedNameToTheDriverMasterDespiteSpellingAndPhoneSuffix() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "Kesavan a/l Tamilsalvan-+65 9164 1447", "2026-08-10", 0, "")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, false);

        PlanningRtiGroup group = preview.groups().get(0);
        assertThat(group.driverSource()).isEqualTo(DriverSource.PLAN);
        assertThat(group.driverRefId()).isEqualTo(100);
    }

    @Test
    void createsForThePlannedTruckWithoutCheckingLeaveLicenceOrPortPasses() {
        // A truck and driver chosen on the plan are the ones the planner means.
        // Leave, licence and GDL expiry and port passes are not consulted at all -
        // that data is not kept current, and it is not this feature's argument.
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "NADARAJAH GOVINDARAJOO", "2026-08-10", 0, "")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, false);

        PlanningRtiGroup group = preview.groups().get(0);
        assertThat(group.driverRefId()).isEqualTo(27);
        assertThat(group.isReady()).isTrue();
        assertThat(group.warnings()).isEmpty();
    }

    @Test
    void refusesAPlanFromAnotherCompany() {
        when(reader.planHeader(PLAN, COMPANY)).thenReturn(null);

        assertThatThrownBy(() -> service.preview(PLAN, COMPANY, null, false))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Plan not found");
    }

    @Test
    void leavesJobsThatAlreadyHaveAnRtiOutUnlessAskedFor() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 7788, "RTI000007788"),
                row(2, 102, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 7788, "RTI000007788")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, false);

        assertThat(preview.groups()).isEmpty();
        assertThat(preview.jobsSkipped()).isEqualTo(2);
    }

    @Test
    void bringsThoseJobsBackWhenThePlannerAsksForASecondRti() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 7788, "RTI000007788"),
                row(2, 102, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 0, "")));

        PlanningRtiBatchPreview preview = service.preview(PLAN, COMPANY, null, true);

        assertThat(preview.groups()).hasSize(1);
        assertThat(preview.jobsToCreate()).isEqualTo(2);
        assertThat(preview.jobsSkipped()).isZero();
        // Every job carries the RTI it is already on, and the card says so.
        assertThat(preview.groups().get(0).jobs())
                .extracting(job -> job.existingRtiNo())
                .containsExactlyInAnyOrder("RTI000007788", "");
        // The warning names the earlier RTI and its date - "already has an RTI"
        // on its own reads as though today's work were already done.
        assertThat(preview.groups().get(0).warnings())
                .anyMatch(warning -> warning.contains("RTI000007788")
                        && warning.contains("2026-08-07")
                        && warning.contains("stays"));
    }

    @Test
    void refusesToDoubleAJobWhenTheRequestDidNotAskFor() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 7788, "RTI000007788")));

        // The client posted the job anyway; the server still sets it aside.
        PlanningRtiBatchResult result = service.create(PLAN, request(group(64, 85, List.of(101))));

        assertThat(result.created()).isEmpty();
        assertThat(result.skipped()).singleElement()
                .satisfies(skip -> assertThat(skip.existingRtiNo()).isEqualTo("RTI000007788"));
        verify(rtiMasterService, never()).create(any());
    }

    @Test
    void createsTheSecondRtiWhenThePlannerAllowedIt() {
        when(reader.planRows(PLAN, COMPANY)).thenReturn(List.of(
                row(1, 101, 64, "JPM 7151", "UGUNTHAN TANGAVELU", "2026-08-10", 7788, "RTI000007788")));
        when(reader.jobsAlreadyInRti(anyCollection())).thenReturn(Map.of(101, "RTI000007788"));

        PlanningRtiBatchRequest request = request(group(64, 85, List.of(101)));
        request.setAllowDuplicates(true);

        PlanningRtiBatchResult result = service.create(PLAN, request);

        assertThat(result.created()).hasSize(1);
        assertThat(result.jobsCreated()).isEqualTo(1);
        assertThat(result.skipped()).isEmpty();
        assertThat(result.isComplete()).isTrue();
    }

    private static PlanningRtiBatchRequest request(PlanningRtiBatchRequest.Group... groups) {
        PlanningRtiBatchRequest request = new PlanningRtiBatchRequest();
        request.setCompanyRefId(COMPANY);
        request.setEmployeeRefId(4);
        request.setGroups(new ArrayList<>(List.of(groups)));
        return request;
    }

    private static PlanningRtiBatchRequest.Group group(int truckId, int driverId, List<Integer> jobs) {
        PlanningRtiBatchRequest.Group group = new PlanningRtiBatchRequest.Group();
        group.setTruckRefId(truckId);
        group.setDriverRefId(driverId);
        group.setSaleOrderMasterRefIds(jobs);
        return group;
    }
}
