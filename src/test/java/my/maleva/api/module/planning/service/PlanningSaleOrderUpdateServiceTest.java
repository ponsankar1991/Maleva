package my.maleva.api.module.planning.service;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.planning.dto.PlanningSaleOrderUpdateResponse;
import my.maleva.api.module.planning.dto.request.PlanningSaleOrderUpdateRequest;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.saleorder.entity.SaleOrderPickup;
import my.maleva.api.module.saleorder.repository.SaleOrderDeliveryRepository;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import my.maleva.api.module.saleorder.repository.SaleOrderPickupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
 * The Planning Update window must change only what that window edits.
 *
 * It used to PUT the whole sale order from a form that never loaded the totals, so every
 * update set GrossAmount, TaxAmount, Amount and CurrencyValue to 0 and re-inserted every
 * line item and stop.
 */
@ExtendWith(MockitoExtension.class)
class PlanningSaleOrderUpdateServiceTest {

    private static final Integer JOB_ID = 21507;
    private static final Integer COMPANY_ID = 6;

    @Mock
    private SaleOrderMasterRepository saleOrderRepository;

    @Mock
    private SaleOrderPickupRepository pickupRepository;

    @Mock
    private SaleOrderDeliveryRepository deliveryRepository;

    @InjectMocks
    private PlanningSaleOrderUpdateService service;

    @Captor
    private ArgumentCaptor<Iterable<SaleOrderPickup>> savedPickups;

    @Test
    @DisplayName("writes only the Update window's columns - the sale order entity is never saved")
    void writesOnlyTheWindowsColumns() {
        givenActiveJob();
        when(pickupRepository.findBySaleOrderMasterRefId(JOB_ID))
                .thenReturn(List.of(pickup(11, "WESTPORT", "2"), pickup(12, "NORTHPORT", "2")));

        PlanningSaleOrderUpdateRequest request = request();
        request.setPickupDate("2026-09-15T08:30");
        request.setDeliveryDate("2026-09-16T17:00:00");
        request.setOrigin(" KLANG ");
        request.setDestination("SINGAPORE");
        request.setOriginRefId(5);
        request.setDestinationRefId(0);
        request.setQuantity("4");
        request.setTotalWeight("114.96");
        request.setWareHouseEnterDate("2026-09-15 10:00");
        request.setWareHouseAddress("WESTPORT WAREHOUSE 3");
        request.setEmployeeId(15);

        PlanningSaleOrderUpdateResponse response = service.update(request);

        verify(saleOrderRepository).updatePlanningFields(eq(JOB_ID), eq(COMPANY_ID),
                eq(LocalDateTime.of(2026, 9, 15, 8, 30)),
                eq(LocalDateTime.of(2026, 9, 16, 17, 0)),
                eq(LocalDateTime.of(2026, 9, 15, 10, 0)),
                isNull(),
                eq("WESTPORT WAREHOUSE 3"),
                eq("KLANG"), eq("SINGAPORE"), eq(5), isNull(),
                eq("4"), eq("114.96"),
                eq("WESTPORT{@}NORTHPORT"), isNull(),
                eq("2{@}2"), eq(""), eq("2{@}2"),
                eq(15), any(LocalDateTime.class));
        verify(saleOrderRepository, never()).save(any());
        verify(saleOrderRepository, never()).saveAndFlush(any());
        // Stops were not on the request, so their rows are left alone.
        verify(pickupRepository, never()).saveAll(any());
        verify(pickupRepository, never()).deleteAll(any());

        assertThat(response.isOk()).isTrue();
        assertThat(response.getPickupDate()).isEqualTo("2026-09-15 08:30");
        assertThat(response.getDeliveryDate()).isEqualTo("2026-09-16 17:00");
        assertThat(response.getPackageType()).isEqualTo("4/114.96");
        assertThat(response.getPickupCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("unticked dates, typed origin and no employee are cleared")
    void blankValuesClearColumns() {
        givenActiveJob();
        PlanningSaleOrderUpdateRequest request = request();
        request.setPickupDate("");
        request.setOrigin("  ");
        request.setOriginRefId(null);
        request.setEmployeeId(0);
        request.setPickups(List.of());
        request.setDeliveries(List.of());

        PlanningSaleOrderUpdateResponse response = service.update(request);

        verify(saleOrderRepository).updatePlanningFields(eq(JOB_ID), eq(COMPANY_ID),
                isNull(), isNull(), isNull(), isNull(), eq(""),
                isNull(), isNull(), isNull(), isNull(),
                eq(""), eq(""),
                isNull(), isNull(), eq(""), eq(""), eq(""),
                isNull(), any(LocalDateTime.class));
        assertThat(response.getPickupDate()).isEmpty();
        assertThat(response.getPackageType()).isEqualTo("/");
    }

    @Test
    @DisplayName("stops are updated, inserted and removed as the form says, in the form's order")
    void stopsAreReconciled() {
        givenActiveJob();
        SaleOrderPickup westport = pickup(11, "WESTPORT", "2");
        SaleOrderPickup northport = pickup(12, "NORTHPORT", "2");
        when(pickupRepository.findBySaleOrderMasterRefId(JOB_ID)).thenReturn(List.of(westport, northport));

        PlanningSaleOrderUpdateRequest request = request();
        request.setPickups(List.of(
                stop(12, "NORTHPORT GATE B", "2026-09-15T11:45", "50", "3"),
                stop(null, "PORT KLANG", "", "", "1")));
        request.setRemovedPickupIds(List.of(11));

        service.update(request);

        verify(pickupRepository).deleteAll(List.of(westport));
        verify(pickupRepository).saveAll(savedPickups.capture());
        List<SaleOrderPickup> saved = new ArrayList<>();
        savedPickups.getValue().forEach(saved::add);

        assertThat(saved).hasSize(2);
        assertThat(saved.get(0)).isSameAs(northport);
        assertThat(northport.getPickupAddress()).isEqualTo("NORTHPORT GATE B");
        assertThat(northport.getPickupTime()).isEqualTo(LocalDateTime.of(2026, 9, 15, 11, 45));
        assertThat(northport.getPickupWeight()).isEqualTo("50");
        assertThat(saved.get(1).getId()).isNull();
        assertThat(saved.get(1).getSaleOrderMasterRefId()).isEqualTo(JOB_ID);
        assertThat(saved.get(1).getPickupAddress()).isEqualTo("PORT KLANG");
        assertThat(saved.get(1).getPickupTime()).isNull();
        assertThat(saved.get(1).getCreatedDate()).isNotNull();

        verify(saleOrderRepository).updatePlanningFields(eq(JOB_ID), eq(COMPANY_ID),
                isNull(), isNull(), isNull(), isNull(), eq(""),
                isNull(), isNull(), isNull(), isNull(),
                eq(""), eq(""),
                eq("NORTHPORT GATE B{@}PORT KLANG"), isNull(), eq("3{@}1"), eq(""), eq("3{@}1"),
                isNull(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("a stop someone else added while the form was open is kept")
    void unlistedStopIsKept() {
        givenActiveJob();
        when(pickupRepository.findBySaleOrderMasterRefId(JOB_ID)).thenReturn(List.of(
                pickup(11, "WESTPORT", "2"), pickup(13, "ADDED ELSEWHERE", "5")));

        PlanningSaleOrderUpdateRequest request = request();
        request.setPickups(List.of(stop(11, "WESTPORT", "", "", "2")));

        service.update(request);

        verify(pickupRepository, never()).deleteAll(any());
        verify(saleOrderRepository).updatePlanningFields(eq(JOB_ID), eq(COMPANY_ID),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                eq("WESTPORT{@}ADDED ELSEWHERE"), any(), eq("2{@}5"), any(), any(), any(), any());
    }

    @Test
    @DisplayName("a stop that is no longer on the job is refused before anything is written")
    void staleStopIsRefusedBeforeAnyWrite() {
        givenActiveJob();
        when(pickupRepository.findBySaleOrderMasterRefId(JOB_ID)).thenReturn(List.of(pickup(11, "WESTPORT", "2")));

        PlanningSaleOrderUpdateRequest request = request();
        request.setPickups(List.of(stop(99, "WESTPORT", "", "", "2")));

        assertThatThrownBy(() -> service.update(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("99");

        verifyNothingWritten();
    }

    @Test
    @DisplayName("a stop without an address is refused before anything is written")
    void stopWithoutAddressIsRefused() {
        givenActiveJob();
        PlanningSaleOrderUpdateRequest request = request();
        request.setDeliveries(List.of(stop(null, "  ", "2026-09-16T17:00", "", "4")));

        assertThatThrownBy(() -> service.update(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Delivery stop 1");

        verifyNothingWritten();
    }

    @Test
    @DisplayName("an unreadable date is refused before anything is written")
    void invalidDateIsRefusedBeforeAnyWrite() {
        givenActiveJob();
        PlanningSaleOrderUpdateRequest request = request();
        request.setDeliveryDate("16/09/2026 5pm");

        assertThatThrownBy(() -> service.update(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Delivery Date");

        verifyNothingWritten();
    }

    @Test
    @DisplayName("a deleted job, or one from another company, is not found")
    void deletedOrForeignJobIsNotFound() {
        SaleOrderMaster deleted = new SaleOrderMaster();
        deleted.setId(JOB_ID);
        deleted.setActive(2);
        when(saleOrderRepository.findByIdAndCompanyRefId(JOB_ID, COMPANY_ID)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> service.update(request())).isInstanceOf(EntityNotFoundException.class);

        when(saleOrderRepository.findByIdAndCompanyRefId(JOB_ID, COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(request())).isInstanceOf(EntityNotFoundException.class);

        verifyNothingWritten();
    }

    private void verifyNothingWritten() {
        verify(saleOrderRepository, never()).updatePlanningFields(anyInt(), anyInt(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(pickupRepository, never()).saveAll(any());
        verify(pickupRepository, never()).deleteAll(any());
        verify(deliveryRepository, never()).saveAll(any());
        verify(deliveryRepository, never()).deleteAll(any());
    }

    private void givenActiveJob() {
        SaleOrderMaster job = new SaleOrderMaster();
        job.setId(JOB_ID);
        job.setCompanyRefId(COMPANY_ID);
        job.setActive(1);
        when(saleOrderRepository.findByIdAndCompanyRefId(JOB_ID, COMPANY_ID)).thenReturn(Optional.of(job));
    }

    private static PlanningSaleOrderUpdateRequest request() {
        PlanningSaleOrderUpdateRequest request = new PlanningSaleOrderUpdateRequest();
        request.setSaleOrderId(JOB_ID);
        request.setCompanyId(COMPANY_ID);
        return request;
    }

    private static SaleOrderPickup pickup(int id, String address, String quantity) {
        SaleOrderPickup pickup = new SaleOrderPickup();
        pickup.setId(id);
        pickup.setSaleOrderMasterRefId(JOB_ID);
        pickup.setPickupAddress(address);
        pickup.setPickupQuantity(quantity);
        return pickup;
    }

    private static PlanningSaleOrderUpdateRequest.Stop stop(Integer id, String address, String time,
                                                            String weight, String quantity) {
        PlanningSaleOrderUpdateRequest.Stop stop = new PlanningSaleOrderUpdateRequest.Stop();
        stop.setId(id);
        stop.setAddress(address);
        stop.setTime(time);
        stop.setWeight(weight);
        stop.setQuantity(quantity);
        return stop;
    }
}
