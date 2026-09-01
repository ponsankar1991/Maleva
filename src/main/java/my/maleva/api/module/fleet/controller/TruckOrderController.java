package my.maleva.api.module.fleet.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.fleet.dto.OrderableTruckDto;
import my.maleva.api.module.fleet.dto.TruckOrderCalendarResponse;
import my.maleva.api.module.fleet.dto.TruckOrderDto;
import my.maleva.api.module.fleet.dto.request.TruckOrderSaveRequest;
import my.maleva.api.module.fleet.dto.request.TruckOrderSearchRequest;
import my.maleva.api.module.fleet.service.TruckOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * The Truck Order Calendar.
 *
 * <p>Replaces the truck-order actions of the legacy {@code /TruckMaster/*}
 * controller. Reads are GETs with query parameters rather than POSTs with a JSON
 * body, and the company id travels as a parameter instead of the {@code Comid}
 * header the legacy save used.
 *
 * <p>Parameter names are camelCase and bind by exact case - a PascalCase
 * {@code CompanyRefId} would bind to nothing and quietly return every row.
 */
@RestController
@RequestMapping("/api/truck-orders")
@Validated
@PermitAll
public class TruckOrderController {

    private static final Logger logger = LoggerFactory.getLogger(TruckOrderController.class);

    private final TruckOrderService service;

    public TruckOrderController(TruckOrderService service) {
        this.service = service;
    }

    /**
     * The orders in a date range, with the summary counters.
     * Legacy equivalent: POST /TruckMaster/GetTruckOrders
     *
     * <p>{@code statuses} repeats or comma-separates. The legacy screen sent one
     * joined string to an {@code =} comparison, so picking two statuses matched
     * nothing.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<TruckOrderCalendarResponse>> search(
            @RequestParam Integer companyRefId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer truckRefId,
            @RequestParam(required = false) List<String> statuses) {

        TruckOrderCalendarResponse data = service.search(TruckOrderSearchRequest.builder()
                .companyRefId(companyRefId)
                .fromDate(fromDate)
                .toDate(toDate)
                .truckRefId(truckRefId)
                .statuses(statuses)
                .build());

        return ResponseEntity.ok(ApiResponse.success(data, "Truck orders retrieved"));
    }

    /**
     * The trucks this calendar books.
     * Legacy equivalent: POST /TruckMaster/SelectTruckAll, then a plate list
     * hardcoded in the browser.
     */
    @GetMapping("/trucks")
    public ResponseEntity<ApiResponse<List<OrderableTruckDto>>> orderableTrucks(
            @RequestParam Integer companyRefId) {
        return ResponseEntity.ok(
                ApiResponse.success(service.orderableTrucks(companyRefId), "Orderable trucks"));
    }

    /**
     * The order number to show on a blank dialog, e.g. {@code ORD002600005}.
     * Legacy equivalent: POST /TruckMaster/GetNextOrderNumber
     */
    @GetMapping("/next-no")
    public ResponseEntity<ApiResponse<String>> nextOrderNumber(@RequestParam Integer companyRefId) {
        return ResponseEntity.ok(
                ApiResponse.success(service.nextOrderNumber(companyRefId), "Next order number"));
    }

    /**
     * Is this truck already taken on this date?
     *
     * <p>Backs the warning the dialog shows while the user is still typing. The
     * legacy page answered this from whatever orders the calendar happened to
     * have loaded, so a clash outside the visible month went unseen until the
     * save was rejected.
     */
    @GetMapping("/clash")
    public ResponseEntity<ApiResponse<TruckOrderDto>> findClash(
            @RequestParam Integer companyRefId,
            @RequestParam Integer truckRefId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate orderDate,
            @RequestParam(required = false) Integer excludeId) {

        TruckOrderDto clash = service.findClash(companyRefId, truckRefId, orderDate, excludeId);
        return ResponseEntity.ok(ApiResponse.success(clash,
                clash == null ? "No clash" : "Truck already booked"));
    }

    /**
     * One order, for the edit dialog.
     * Legacy equivalent: POST /TruckMaster/GetTruckOrderById
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TruckOrderDto>> getForEdit(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId) {
        return ResponseEntity.ok(
                ApiResponse.success(service.getForEdit(id, companyRefId), "Truck order retrieved"));
    }

    /**
     * Creates or updates an order.
     * Legacy equivalent: POST /TruckMaster/SaveTruckOrder
     *
     * <p>The body is one object, not the single-element array the legacy screen
     * posted to satisfy the procedure's OPENJSON loop.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TruckOrderDto>> save(
            @Valid @RequestBody TruckOrderSaveRequest request,
            Authentication authentication) {

        TruckOrderDto saved = service.save(request, usernameOf(authentication));
        logger.info("Saved truck order {}", saved.getCNumberDisplay());
        return ResponseEntity.ok(ApiResponse.success(saved, "Truck order saved"));
    }

    /**
     * Soft delete.
     *
     * <p>No legacy equivalent: the old screen posted to
     * /TruckOrder/DeleteTruckOrder, which no controller in the .NET solution
     * serves, and its delete button was disabled by a hardcoded permission flag
     * before it could find out.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId,
            Authentication authentication) {

        service.delete(id, companyRefId, usernameOf(authentication));
        return ResponseEntity.ok(ApiResponse.success(null, "Truck order deleted"));
    }

    private String usernameOf(Authentication authentication) {
        return authentication == null ? "system" : authentication.getName();
    }
}
