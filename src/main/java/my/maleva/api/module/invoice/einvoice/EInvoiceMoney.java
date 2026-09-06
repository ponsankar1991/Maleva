package my.maleva.api.module.invoice.einvoice;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The one place money is converted for the e-invoice.
 *
 * <p>Every amount column behind the invoice is SQL {@code real} — a 32-bit
 * float. It cannot hold 1234.56 exactly; it holds 1234.56005859375. The legacy
 * push sent that noise to the government as-is. Here each stored value is
 * rounded ONCE, to two decimals, HALF_UP, at the moment it is read — and never
 * again. Arithmetic on already-rounded figures uses {@link BigDecimal} so a sen
 * cannot be lost in a double.
 *
 * <p>Why {@code BigDecimal.valueOf(double)} and not {@code new BigDecimal(double)}:
 * {@code valueOf} goes through {@code Double.toString}, which gives the
 * shortest decimal that round-trips the double (e.g. {@code 1234.56005859375});
 * rounding that to 2 dp yields {@code 1234.56}. {@code new BigDecimal(double)}
 * expands the full binary fraction (dozens of digits) — also fine after
 * rounding, but slower and harder to read in a debugger.
 *
 * <p>Limit: a float32 has ~7 significant digits, so from 131,072 upward it
 * cannot hold sen at all (its step is 0.0156). For such values the stored
 * figure may already be a sen away from what was typed and nothing at push
 * time can recover it; the validator refuses those invoices and says why.
 *
 * <p>{@link #of(Double)} is for RM columns only. Quantities and percentages
 * are not money; use {@link #quantity(Double)} for them.
 */
public final class EInvoiceMoney {

    /** Sen precision. */
    public static final int SCALE = 2;

    /**
     * One sen. The screen stored each line's tax UNROUNDED, so rounding the
     * stored value and rounding a recomputed one can land on different sides
     * of a half-sen; a half-sen tolerance would refuse legitimate invoices.
     * Checked against every invoice already on LHDN's record: none differs
     * from its recomputed figures by more than one sen.
     */
    public static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    /**
     * From this value up a 32-bit float cannot represent sen (its step is
     * 0.015625). A stored amount this large may already be wrong by a sen and
     * nothing at push time can recover it.
     */
    public static final BigDecimal FLOAT32_SEN_LIMIT = new BigDecimal("131072");

    /** Quantities and percentages keep up to four decimals, never fewer than two. */
    private static final int QUANTITY_MAX_SCALE = 4;

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private EInvoiceMoney() {
    }

    /** A stored RM amount as sen-exact money; null stays null so callers can validate it. */
    public static BigDecimal of(Double stored) {
        return stored == null ? null : BigDecimal.valueOf(stored).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * A stored quantity or percentage. Not money: the value keeps whatever
     * precision it was saved with (to four decimals) so a quantity of 0.125 is
     * not silently turned into 0.13 before it is compared or sent.
     */
    public static BigDecimal quantity(Double stored) {
        if (stored == null) {
            return null;
        }
        BigDecimal value = BigDecimal.valueOf(stored).setScale(QUANTITY_MAX_SCALE, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return value.scale() < SCALE ? value.setScale(SCALE, RoundingMode.UNNECESSARY) : value;
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /** Rounds a computed figure to sen. */
    public static BigDecimal round(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * {@code qty × rate × percent / 100}, rounded to sen — the way the Sale
     * Invoice screen computed the line tax (on the unrounded product).
     */
    public static BigDecimal lineTax(BigDecimal qty, BigDecimal rate, BigDecimal percent) {
        return round(unroundedTax(qty, rate, percent));
    }

    /**
     * {@code qty × rate + qty × rate × percent / 100}, rounded once at the end —
     * the tax-inclusive line amount exactly as the screen stored it.
     */
    public static BigDecimal lineInclusive(BigDecimal qty, BigDecimal rate, BigDecimal percent) {
        return round(qty.multiply(rate).add(unroundedTax(qty, rate, percent)));
    }

    private static BigDecimal unroundedTax(BigDecimal qty, BigDecimal rate, BigDecimal percent) {
        return qty.multiply(rate).multiply(percent).divide(HUNDRED, 10, RoundingMode.HALF_UP);
    }

    /** True when the two figures agree to within one sen. */
    public static boolean agrees(BigDecimal expected, BigDecimal actual) {
        return agrees(expected, actual, TOLERANCE);
    }

    /** True when the two figures agree to within the given tolerance. */
    public static boolean agrees(BigDecimal expected, BigDecimal actual, BigDecimal tolerance) {
        return expected.subtract(actual).abs().compareTo(tolerance) <= 0;
    }

    /**
     * Tolerance for a sum of {@code count} stored figures that were each
     * rounded independently: one sen per figure, never less than one sen.
     */
    public static BigDecimal sumTolerance(int count) {
        return TOLERANCE.multiply(BigDecimal.valueOf(Math.max(1, count)));
    }

    /** True when the figure is too large for the float32 column to hold sen. */
    public static boolean exceedsFloat32SenPrecision(BigDecimal value) {
        return value != null && value.abs().compareTo(FLOAT32_SEN_LIMIT) >= 0;
    }

    public static boolean isNegative(BigDecimal value) {
        return value != null && value.signum() < 0;
    }
}
