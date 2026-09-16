package my.maleva.api.module.fleet.controller;

import my.maleva.api.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Turns the database's one-own-order-per-truck-per-day index into the message
 * the dialog already shows for a clash.
 *
 * <p>The service checks for a clash before it saves, but two dispatchers booking
 * the same free truck at the same moment both pass that check. The filtered
 * UNIQUE index {@value #OWN_BOOKING_INDEX} (db/sql/TRUCK_ORDER_INDEXES.sql) stops
 * the second insert; without this handler the loser would see a raw constraint
 * error. HTTP 409 tells the screen to refresh availability.
 *
 * <p>Caught here, outside the service, on purpose: catching inside the
 * {@code @Transactional} save would leave the transaction rollback-only and
 * surface as "Transaction silently rolled back" instead.
 *
 * <p>Ordered first so it is consulted before the global handler, and it handles
 * nothing but {@link DataIntegrityViolationException}, so every other exception
 * still reaches the global handler unchanged.
 */
@RestControllerAdvice(assignableTypes = TruckOrderController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TruckOrderExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(TruckOrderExceptionHandler.class);

    static final String OWN_BOOKING_INDEX = "UX_TruckOrderMaster_Truck_Day_Active";

    static final String ALREADY_BOOKED_MESSAGE =
            "No truck available: this truck was just booked on the selected date. Choose another truck.";

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException exception) {
        String detail = rootMessage(exception);

        if (detail.contains(OWN_BOOKING_INDEX)) {
            logger.warn("Concurrent truck booking rejected by {}", OWN_BOOKING_INDEX);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(ALREADY_BOOKED_MESSAGE, HttpStatus.CONFLICT.value()));
        }

        logger.error("Truck order write violated a database constraint: {}", detail);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Could not save the truck order: " + detail,
                        HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    /**
     * A parameter that is not the type the method wants - {@code excludeId=abc},
     * or a date that is not a date.
     *
     * <p>Without this the binder's failure reaches the generic handler as a 500
     * with "For input string" in it, which reads like a server fault when it is a
     * bad request. The screen never sends one; a hand-typed URL does.
     *
     * <p>The sibling defect is fixed in the controller: {@code /{id}} used to
     * accept any text, so {@code GET /api/truck-orders/availability} was matched
     * by the by-id route on a build without the availability endpoint and failed
     * as a number conversion. The path variable is now {@code {id:\d+}}, so a
     * non-numeric path is simply not this route.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String wanted = exception.getRequiredType() == null
                ? "the expected type" : exception.getRequiredType().getSimpleName();
        String message = "'" + exception.getName() + "' must be " + wanted
                + ", but was '" + exception.getValue() + "'.";

        logger.warn("Bad truck order parameter: {}", message);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message, HttpStatus.BAD_REQUEST.value()));
    }

    private static String rootMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return String.valueOf(cause.getMessage());
    }
}
