package my.maleva.api.module.saleorder.controller;

import jakarta.validation.ConstraintViolationException;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.saleorder.util.SaleOrderApiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Keeps sale-order API error responses aligned with the existing ApiResponse
 * contract without changing the behavior of unrelated modules.
 */
@RestControllerAdvice(assignableTypes = {
        SaleOrderMasterController.class,

})
public class SaleOrderExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException exception) {
        logger.warn("Sale order resource not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(exception.getMessage(), SaleOrderApiConstants.NOT_FOUND_STATUS_CODE));
    }

    @ExceptionHandler({InvalidRequestException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(Exception exception) {
        logger.warn("Sale order request validation failed: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(exception.getMessage(), SaleOrderApiConstants.BAD_REQUEST_STATUS_CODE));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String errorDetails = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        logger.warn("Sale order body validation failed: {}", errorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        SaleOrderApiConstants.MESSAGE_SAVE_FAILED,
                        SaleOrderApiConstants.BAD_REQUEST_STATUS_CODE,
                        errorDetails
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception exception) {
        logger.error("Unhandled sale order exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        exception.getMessage() != null ? exception.getMessage() : SaleOrderApiConstants.MESSAGE_SAVE_FAILED,
                        SaleOrderApiConstants.INTERNAL_SERVER_ERROR_STATUS_CODE,
                        exception.getClass().getSimpleName()
                ));
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
