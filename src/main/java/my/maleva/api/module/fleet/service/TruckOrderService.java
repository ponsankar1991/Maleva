package my.maleva.api.module.fleet.service;

import my.maleva.api.module.fleet.dto.OrderableTruckDto;
import my.maleva.api.module.fleet.dto.TruckOrderCalendarResponse;
import my.maleva.api.module.fleet.dto.TruckOrderDto;
import my.maleva.api.module.fleet.dto.request.TruckOrderSaveRequest;
import my.maleva.api.module.fleet.dto.request.TruckOrderSearchRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * Truck orders: one truck reserved for one whole day, shown as a calendar.
 *
 * <p>Replaces the legacy TruckServices truck-order methods. The five actions the
 * jqx screen called map onto the methods here:
 *
 * <pre>
 *   /TruckMaster/GetTruckOrders       -> search
 *   /TruckMaster/GetNextOrderNumber   -> nextOrderNumber
 *   /TruckMaster/SaveTruckOrder       -> save
 *   /TruckMaster/GetTruckOrderById    -> getForEdit
 *   /TruckMaster/SelectTruckAll       -> orderableTrucks   (narrowed, see below)
 * </pre>
 *
 * <p>A sixth, /TruckOrder/DeleteTruckOrder, was called by the screen but had no
 * controller anywhere in the .NET solution. {@link #delete} is that action,
 * implemented for the first time.
 *
 * <p>SP_TruckOrderMaster is reimplemented in Java rather than invoked, which is
 * the house rule for ported screens. It touches only TruckOrderMaster and the
 * SequenceNoMaster counter, so nothing outside this service depends on it
 * running.
 */
public interface TruckOrderService {

    /** The orders in a date range, with the two counters the summary row shows. */
    TruckOrderCalendarResponse search(TruckOrderSearchRequest request);

    /**
     * The trucks this calendar books, active ones only.
     *
     * <p>Narrower than the legacy SelectTruckAll, which returned every truck of
     * the company - including soft-deleted ones - and left the browser to filter
     * them against a hardcoded plate list. Doing it here also removes the
     * duplicate row the live data produces: one plate exists twice, once
     * deleted, and both copies reached the old dropdown.
     */
    List<OrderableTruckDto> orderableTrucks(Integer companyRefId);

    /**
     * The next order number, formatted as the dialog shows it: {@code ORD}
     * followed by nine digits.
     *
     * <p>A preview only. The number is fixed when the order is actually saved,
     * so two people opening a blank dialog at once will both see it and only one
     * will keep it.
     */
    String nextOrderNumber(Integer companyRefId);

    /** One order, for the edit dialog. */
    TruckOrderDto getForEdit(Integer id, Integer companyRefId);

    /**
     * Creates or updates an order.
     *
     * <p>A truck can hold one live order per day; a second one is rejected. On
     * update the order number is preserved - re-pointing it would rewrite a
     * document number that has already been quoted.
     */
    TruckOrderDto save(TruckOrderSaveRequest request, String username);

    /**
     * Would this truck clash on this date? Powers the warning the dialog shows
     * before the user commits, so the answer is advisory - {@link #save} checks
     * again and is the one that decides.
     *
     * @param excludeId the order being edited, or null when creating
     * @return the clashing order, or null when the day is free
     */
    TruckOrderDto findClash(Integer companyRefId, Integer truckRefId, LocalDate orderDate, Integer excludeId);

    /** Soft delete: {@code Active = 2}, matching the other fleet documents. */
    void delete(Integer id, Integer companyRefId, String username);
}
