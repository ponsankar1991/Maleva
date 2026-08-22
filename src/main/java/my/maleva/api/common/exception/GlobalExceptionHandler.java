package my.maleva.api.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import my.maleva.api.common.exception.InvalidDateRangeException;
import my.maleva.api.common.exception.DateRangeTooLargeException;
import my.maleva.api.common.exception.RtiJobWiseQueryException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex, WebRequest request) {
        ApiError err = new ApiError(Instant.now(), HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI(), null);
        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(InvalidRequestException ex, WebRequest request) {
        ApiError err = new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI(), null);
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    /**
     * A rollback that itself fails replaces the exception the application threw,
     * so the caller is told "Unable to rollback against JDBC Connection" and the
     * real problem - the validation message or constraint violation that failed
     * the request - is discarded before it reaches this handler.
     *
     * Spring logs the discarded exception once, at DEBUG, under
     * TransactionInterceptor ("Application exception overridden by rollback
     * exception"); logback-spring.xml raises that logger so it is always
     * printed. This handler names the underlying cause in the response as well,
     * so the failure is diagnosable from the HTTP call alone.
     */
    @ExceptionHandler({JpaSystemException.class, TransactionSystemException.class,
            UnexpectedRollbackException.class})
    public ResponseEntity<ApiError> handleRollbackFailure(RuntimeException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        String message = String.valueOf(ex.getMessage());

        if (message.contains("Unable to rollback") || message.contains("auto-commit")) {
            logger.error("Rollback failed on {} - the original application exception was discarded."
                    + " Look for \"Application exception overridden by rollback exception\" just above this"
                    + " line for the real cause. This happens when the connection is in autocommit mode:"
                    + " check spring.datasource.hikari.auto-commit is false.", path, ex);

            ApiError err = new ApiError(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    "The request failed and the transaction could not be rolled back, so the original error"
                            + " was lost. The server log records it as \"Application exception overridden by"
                            + " rollback exception\".",
                    path, List.of("rollback failed: " + message));
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        logger.error("Transaction failure on {}", path, ex);
        ApiError err = new ApiError(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error", rootMessage(ex), path, null);
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Logs the stack trace. Without this a 500 left no trace of its own in the
     * log: Spring's own record of an unhandled exception is a single WARN line
     * carrying the message and nothing else.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        logger.error("Unhandled exception on {}", path, ex);
        ApiError err = new ApiError(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                rootMessage(ex), path, null);
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * The deepest message in the cause chain. A wrapped SQL error carries the
     * detail that identifies the failing column or constraint, while the
     * outermost message is usually a generic framework sentence.
     */
    private static String rootMessage(Throwable ex) {
        Throwable cursor = ex;
        while (cursor.getCause() != null && cursor.getCause() != cursor) {
            cursor = cursor.getCause();
        }
        String message = cursor.getMessage();
        if (message == null || message.isBlank()) {
            return cursor.getClass().getSimpleName();
        }
        return cursor == ex ? message : message + " (" + cursor.getClass().getSimpleName() + ")";
    }

    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        ApiError err = new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Validation Failed",
                "Request validation failed", ((ServletWebRequest) request).getRequest().getRequestURI(), details);
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({InvalidDateRangeException.class, DateRangeTooLargeException.class})
    public ResponseEntity<ApiError> handleValidationExceptions(RuntimeException ex, WebRequest request) {
        ApiError err = new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI(), null);
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RtiJobWiseQueryException.class)
    public ResponseEntity<ApiError> handleQueryException(RtiJobWiseQueryException ex, WebRequest request) {
        ApiError err = new ApiError(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "An internal error occurred while fetching data.", ((ServletWebRequest) request).getRequest().getRequestURI(), null);
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        return handleMethodArgumentNotValid(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
}
