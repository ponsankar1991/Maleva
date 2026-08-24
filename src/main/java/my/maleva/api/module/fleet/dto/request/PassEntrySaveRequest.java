package my.maleva.api.module.fleet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * One levi entry as the form submits it.
 *
 * Creates when {@link #id} is null or zero, updates otherwise - the same
 * discriminator {@code SP_LeviEntry} uses internally.
 *
 * The truck, driver and RTI were mandatory in the legacy screen but only
 * enforced in JavaScript, so anything posting directly could write an entry
 * with none of them. They are required here.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassEntrySaveRequest {

    /** Null or 0 creates a new entry. */
    private Integer id;

    @NotNull(message = "companyRefId is required")
    private Integer companyRefId;

    @NotNull(message = "Select a truck")
    private Integer truckRefId;

    @NotNull(message = "Select a driver")
    private Integer driverRefId;

    @NotNull(message = "Select an RTI number")
    private Integer rtiRefId;

    /** The logged-in employee. Recorded as both EmployeeRefId and LastEmployeeRefId. */
    private Integer employeeRefId;

    @NotNull(message = "saleDate is required")
    private LocalDate saleDate;

    @NotNull(message = "Enter an amount")
    @PositiveOrZero(message = "Amount must not be negative")
    private Double amount;

    @Size(max = 2000, message = "Remarks must be 2000 characters or fewer")
    private String remarks;

    /** One of {@code IN} / {@code OUT}. */
    @Size(max = 50)
    private String enterLink;

    /** One of {@code 1ST LINK} / {@code 2ND LINK}. */
    @Size(max = 50)
    private String exitLink;
}
