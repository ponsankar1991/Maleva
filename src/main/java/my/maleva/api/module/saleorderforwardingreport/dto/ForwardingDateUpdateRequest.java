package my.maleva.api.module.saleorderforwardingreport.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Re-date one forwarding leg of one sale order.
 *
 * <p>Legacy `UpdateForwardingDate` took the same four values and picked the
 * column to write from {@code fwNo}. It validated none of them: an out-of-range
 * {@code fwNo} built an UPDATE with no SET clause beyond `Modified_Date`, which
 * silently touched the row without changing a date. The bounds here make that
 * a 400 instead.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForwardingDateUpdateRequest {

    @NotNull
    private Integer comId;

    /** SaleOrderMaster.Id. */
    @NotNull
    private Integer jobId;

    /** Which leg to re-date: 1, 2 or 3. */
    @NotNull
    @Min(1)
    @Max(3)
    private Integer fwNo;

    /**
     * The new date as `yyyy-MM-dd HH:mm:ss` (or `yyyy-MM-dd`).
     *
     * <p>Null clears the leg's date — legacy could not express that, because it
     * always concatenated whatever the picker's text was, writing the literal
     * string `''` when empty.
     */
    private String forwardingDate;
}
