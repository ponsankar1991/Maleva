package my.maleva.api.module.vessalplanning.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.boardingsettlement.service.BoardingEventSyncService;
import my.maleva.api.module.jobs.entity.JobStatusMaster;
import my.maleva.api.module.jobs.repository.JobStatusMasterRepository;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import my.maleva.api.module.vessalplanning.dto.VesselPlanningSaleOrderUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The Vessel Planning Update window must change only what that window edits.
 *
 * It used to PUT the whole sale order from a form that never loaded the totals, so every
 * update set GrossAmount, TaxAmount and Amount to 0.
 */
@ExtendWith(MockitoExtension.class)
class VesselPlanningSaleOrderUpdateServiceTest {

    private static final Integer JOB_ID = 21507;
    private static final Integer COMPANY_ID = 6;

    @Mock
    private SaleOrderMasterRepository saleOrderRepository;

    @Mock
    private JobStatusMasterRepository jobStatusRepository;

    @Mock
    private BoardingEventSyncService boardingEventSyncService;

    @InjectMocks
    private VesselPlanningSaleOrderUpdateService service;

    @Test
    @DisplayName("writes through the targeted update only - the sale order entity is never saved")
    void writesOnlyTheWindowsColumns() {
        SaleOrderMaster job = storedJob();
        givenJob(job);

        VesselPlanningSaleOrderUpdateRequest request = request();
        request.setEta("2026-09-15 08:30:00");
        request.setEtb("");
        request.setPtw(" PTW-9 ");

        service.update(request);

        verify(saleOrderRepository).updateVesselPlanningFields(eq(JOB_ID), eq(COMPANY_ID),
                eq(4), eq("PORT KLANG"), eq("PTW-9"),
                eq(LocalDateTime.of(2026, 9, 15, 8, 30)), isNull(), eq(job.getEtd()),
                eq(job.getOeta()), eq(job.getOetb()), eq(job.getOetd()),
                eq(101), eq(102), eq(103), eq(20.0), eq(20.0), eq("20.00"),
                eq(201), isNull(), isNull(), eq(50.0), eq(0.0), eq("0.00"),
                any(LocalDateTime.class));
        verify(saleOrderRepository, never()).save(any());
        verify(saleOrderRepository, never()).saveAndFlush(any());
        verify(boardingEventSyncService).syncEventsForJob(job);
    }

    @Test
    @DisplayName("a third officer that was saved comes back in the response")
    void thirdOfficerIsReturned() {
        givenJob(storedJob());

        var response = service.update(request());

        assertThat(response.getLoadingOfficer3()).isEqualTo(103);
        assertThat(response.getLoadingAmount3()).isEqualTo(20.0);
        assertThat(response.getOffOfficer1()).isEqualTo(201);
    }

    @Test
    @DisplayName("untouched officers keep the amounts already on the job")
    void unchangedOfficersKeepStoredAmounts() {
        var stored = new VesselPlanningSaleOrderUpdateService.Officers(101, 102, 103, 25.0, 25.0, "10.00");

        var result = VesselPlanningSaleOrderUpdateService.resolveOfficers(101, 102, 103, stored);

        assertThat(result).isSameAs(stored);
    }

    @Test
    @DisplayName("changed officers are re-priced 50 / 30 / 20 like the Sale Order screen")
    void changedOfficersAreRepriced() {
        var stored = new VesselPlanningSaleOrderUpdateService.Officers(101, 102, null, 30.0, 30.0, "0.00");

        var three = VesselPlanningSaleOrderUpdateService.resolveOfficers(101, 102, 103, stored);
        assertThat(three).isEqualTo(new VesselPlanningSaleOrderUpdateService.Officers(101, 102, 103, 20.0, 20.0, "20.00"));

        var one = VesselPlanningSaleOrderUpdateService.resolveOfficers(0, null, 103, stored);
        assertThat(one).isEqualTo(new VesselPlanningSaleOrderUpdateService.Officers(null, null, 103, 0.0, 0.0, "50.00"));

        var none = VesselPlanningSaleOrderUpdateService.resolveOfficers(null, null, null, stored);
        assertThat(none).isEqualTo(new VesselPlanningSaleOrderUpdateService.Officers(null, null, null, 0.0, 0.0, "0.00"));
    }

    @Test
    @DisplayName("null keeps a date, blank clears it, garbage is rejected")
    void dateRules() {
        LocalDateTime stored = LocalDateTime.of(2026, 1, 2, 3, 4);

        assertThat(VesselPlanningSaleOrderUpdateService.resolveDate(null, stored, "L ETA")).isEqualTo(stored);
        assertThat(VesselPlanningSaleOrderUpdateService.resolveDate(" ", stored, "L ETA")).isNull();
        assertThat(VesselPlanningSaleOrderUpdateService.resolveDate("2026-09-15T08:30", stored, "L ETA"))
                .isEqualTo(LocalDateTime.of(2026, 9, 15, 8, 30));
        assertThatThrownBy(() -> VesselPlanningSaleOrderUpdateService.resolveDate("15/09/2026", stored, "L ETA"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("L ETA");
    }

    @Test
    @DisplayName("a bad date fails before anything is written")
    void badDateWritesNothing() {
        givenJobOnly(storedJob());
        VesselPlanningSaleOrderUpdateRequest request = request();
        request.setOetd("not a date");

        assertThatThrownBy(() -> service.update(request)).isInstanceOf(InvalidRequestException.class);

        verifyNoUpdate();
    }

    @Test
    @DisplayName("blank job status and cargo keep what the job has")
    void blankStatusAndCargoAreKept() {
        givenJob(storedJob());
        VesselPlanningSaleOrderUpdateRequest request = request();
        request.setJobStatusId(0);
        request.setCargo("");

        service.update(request);

        verify(saleOrderRepository).updateVesselPlanningFields(eq(JOB_ID), eq(COMPANY_ID),
                eq(4), eq("PORT KLANG"), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("moving to WAITING FOR BILLING with open purchase orders is refused and writes nothing")
    void closingStatusWithOpenPurchaseOrdersIsRefused() {
        givenJobOnly(storedJob());
        JobStatusMaster billing = new JobStatusMaster();
        billing.setId(15);
        billing.setCompanyRefId(COMPANY_ID);
        billing.setActive(1);
        billing.setName("WAITING FOR BILLING");
        when(jobStatusRepository.findById(15)).thenReturn(Optional.of(billing));
        when(saleOrderRepository.countPendingPortCharges(COMPANY_ID, JOB_ID)).thenReturn(2);

        VesselPlanningSaleOrderUpdateRequest request = request();
        request.setJobStatusId(15);

        assertThatThrownBy(() -> service.update(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("purchase orders");
        verifyNoUpdate();
    }

    @Test
    @DisplayName("a status from another company is refused")
    void otherCompanyStatusIsRefused() {
        givenJobOnly(storedJob());
        JobStatusMaster foreign = new JobStatusMaster();
        foreign.setId(9);
        foreign.setCompanyRefId(99);
        foreign.setActive(1);
        when(jobStatusRepository.findById(9)).thenReturn(Optional.of(foreign));

        VesselPlanningSaleOrderUpdateRequest request = request();
        request.setJobStatusId(9);

        assertThatThrownBy(() -> service.update(request)).isInstanceOf(InvalidRequestException.class);
        verifyNoUpdate();
    }

    @Test
    @DisplayName("a deleted or missing job is not found")
    void deletedJobIsNotFound() {
        SaleOrderMaster deleted = storedJob();
        deleted.setActive(2);
        when(saleOrderRepository.findByIdAndCompanyRefId(JOB_ID, COMPANY_ID)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> service.update(request())).isInstanceOf(EntityNotFoundException.class);
        verifyNoUpdate();
    }

    private void verifyNoUpdate() {
        verify(saleOrderRepository, never()).updateVesselPlanningFields(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any());
    }

    private void givenJobOnly(SaleOrderMaster job) {
        when(saleOrderRepository.findByIdAndCompanyRefId(JOB_ID, COMPANY_ID)).thenReturn(Optional.of(job));
    }

    private void givenJob(SaleOrderMaster job) {
        givenJobOnly(job);
        JobStatusMaster current = new JobStatusMaster();
        current.setId(4);
        current.setName("VESSEL ARRIVED");
        when(jobStatusRepository.findById(4)).thenReturn(Optional.of(current));
    }

    /** The window as it opens on the stored job: every officer loaded, statuses and cargo blank. */
    private static VesselPlanningSaleOrderUpdateRequest request() {
        VesselPlanningSaleOrderUpdateRequest request = new VesselPlanningSaleOrderUpdateRequest();
        request.setSaleOrderId(JOB_ID);
        request.setCompanyId(COMPANY_ID);
        request.setLoadingOfficer1(101);
        request.setLoadingOfficer2(102);
        request.setLoadingOfficer3(103);
        request.setOffOfficer1(201);
        return request;
    }

    private static SaleOrderMaster storedJob() {
        SaleOrderMaster job = new SaleOrderMaster();
        job.setId(JOB_ID);
        job.setCompanyRefId(COMPANY_ID);
        job.setActive(1);
        job.setJStatus(4);
        job.setCargo("PORT KLANG");
        job.setPtw("PTW-1");
        job.setEta(LocalDateTime.of(2026, 9, 10, 9, 0));
        job.setEtb(LocalDateTime.of(2026, 9, 10, 12, 0));
        job.setEtd(LocalDateTime.of(2026, 9, 11, 18, 0));
        job.setOeta(LocalDateTime.of(2026, 9, 12, 7, 0));
        job.setGrossAmount(1500.0);
        job.setAmount(1605.0);
        job.setLBoardingOfficerRefid(101);
        job.setLBoardingOfficer1Refid(102);
        job.setLBoardingOfficer2Refid(103);
        job.setLBoardingAmount(20.0);
        job.setLBoardingAmount1(20.0);
        job.setLBoardingAmount2("20.00");
        job.setOBoardingOfficerRefid(201);
        job.setOBoardingAmount(50.0);
        job.setOBoardingAmount1(0.0);
        job.setOBoardingAmount2("0.00");
        return job;
    }
}
