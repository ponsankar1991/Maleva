package my.maleva.api.module.fleet.controller;

import my.maleva.api.common.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The concurrent-booking race is reported as the clash the user understands, not a raw SQL error. */
class TruckOrderExceptionHandlerTest {

    private final TruckOrderExceptionHandler handler = new TruckOrderExceptionHandler();

    @Test
    void theOwnBookingIndexBecomesA409WithTheAlreadyBookedMessage() {
        DataIntegrityViolationException race = new DataIntegrityViolationException("could not execute statement",
                new SQLException("Cannot insert duplicate key row in object 'dbo.TruckOrderMaster' "
                        + "with unique index 'UX_TruckOrderMaster_Truck_Day_Active'."));

        ResponseEntity<ApiResponse<Void>> response = handler.handleDataIntegrity(race);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(String.valueOf(response.getBody()).contains("No truck available"),
                "body was " + response.getBody());
    }

    /**
     * What a hand-typed URL produces. It used to be a 500 reading
     * "For input string: \"availability\"" - the by-id route matched a path that
     * was never an id.
     */
    @Test
    void aParameterOfTheWrongTypeIsA400ThatNamesIt() {
        MethodArgumentTypeMismatchException mismatch = new MethodArgumentTypeMismatchException(
                "abc", Integer.class, "excludeId", null, new NumberFormatException("For input string: \"abc\""));

        ResponseEntity<ApiResponse<Void>> response = handler.handleTypeMismatch(mismatch);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(String.valueOf(response.getBody()).contains("excludeId"), "body was " + response.getBody());
    }

    @Test
    void anyOtherConstraintIsAServerError() {
        DataIntegrityViolationException other = new DataIntegrityViolationException("could not execute statement",
                new SQLException("Cannot insert the value NULL into column 'Created_By'"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.handleDataIntegrity(other).getStatusCode());
    }
}
