package my.maleva.api.module.inventory.controller;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.inventory.exception.InsufficientStockException;
import my.maleva.api.module.inventory.exception.InvalidAssetStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Error handling for the inventory endpoints.
 *
 * Scoped to this module's controllers so the mapping of business failures to
 * status codes stays local: a stock shortage or a unit in the wrong state is a
 * 409 the caller can act on, not the 500 the application-wide handler would
 * otherwise return for any RuntimeException.
 */
@RestControllerAdvice(assignableTypes = {
        InventoryController.class,
        InventoryItemController.class,
        RepairableAssetController.class
})
public class InventoryExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(InventoryExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        logger.warn("Inventory resource not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(InvalidRequestException ex) {
        logger.warn("Invalid inventory request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    /**
     * Issuing more than is on hand, or acting on a unit whose status does not
     * allow it. Both are legitimate states the caller should retry differently,
     * so they map to 409 rather than 400 or 500.
     */
    @ExceptionHandler({InsufficientStockException.class, InvalidAssetStateException.class})
    public ResponseEntity<Map<String, Object>> handleConflict(RuntimeException ex) {
        logger.warn("Inventory operation rejected: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        logger.warn("Inventory request validation failed: {}", details);
        return build(HttpStatus.BAD_REQUEST, "Validation Failed", details);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
