package my.maleva.api.module.fleet.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The fuel variance arithmetic behind the FuelEntry screen.
 *
 * Three litre figures are compared:
 * <ul>
 *   <li><b>A</b>liter - actual, what the receipt says was bought;</li>
 *   <li><b>P</b>liter - patron, what the fuel patron (station account) billed;</li>
 *   <li><b>G</b>liter - GPS, what the on-board fuel sensor saw enter the tank.</li>
 * </ul>
 *
 * Everything else is derived from those three and the rate. The legacy version
 * lived in FuelEntry.js, so the browser owned the numbers and the server never
 * checked them; keeping it here means the grid, the form and the report all
 * agree.
 *
 * <p><b>Colour rule.</b> The legacy code contradicted itself: the entry form
 * painted the difference red when {@code Pliter > Gliter}, while the F5 grid
 * painted the same condition green. Red is correct - paying for more litres
 * than the tank actually received is the loss this screen exists to surface -
 * so {@link #isAdverse} follows the form.
 */
@Component
public class FuelVarianceCalculator {

    private static final int SCALE = 2;

    /**
     * Fills in every derived figure from the three litre readings and the rate.
     *
     * @param aliter actual litres from the receipt
     * @param aAmount the amount on the receipt, as keyed in
     * @param pliter patron litres
     * @param gliter GPS litres
     * @param rate   price per litre
     */
    public FuelVariance calculate(Double aliter, Double aAmount,
                                  Double pliter, Double gliter, Double rate) {
        double a = nullSafe(aliter);
        double p = nullSafe(pliter);
        double g = nullSafe(gliter);
        double r = nullSafe(rate);
        // The receipt amount is what the fuel slip says was paid. It is keyed in,
        // not computed: 512 of 4641 rows have an AAmount that is nothing like
        // Aliter x PRate, so deriving it would overwrite the real figure.
        double receiptAmount = nullSafe(aAmount);

        // The legacy screen zeroed every derived field when the rate was blank,
        // rather than showing amounts computed against a rate of zero.
        if (r == 0d) {
            return FuelVariance.builder()
                    .aAmount(round(receiptAmount))
                    .pAmount(0d).gAmount(0d)
                    .dpLiter(0d).dpAmount(0d)
                    .dgLiter(0d).dgAmount(0d)
                    .pumpOverGps(false).pumpOverActual(false)
                    .build();
        }

        double pAmount = p * r;
        double gAmount = g * r;

        return FuelVariance.builder()
                .aAmount(round(receiptAmount))
                .pAmount(round(pAmount))
                .gAmount(round(gAmount))
                // patron against actual: paperwork disagreement
                .dpLiter(round(p - a))
                .dpAmount(round(pAmount - (a * r)))
                // patron against GPS: fuel paid for that never reached the tank
                .dgLiter(round(p - g))
                .dgAmount(round(pAmount - gAmount))
                .pumpOverGps(p > g)
                .pumpOverActual(p > a)
                .build();
    }

    /**
     * Whether a difference should be shown as a loss.
     *
     * @param difference patron litres minus the figure being compared
     */
    public boolean isAdverse(double difference) {
        return difference > 0d;
    }

    private double nullSafe(Double value) {
        return value == null ? 0d : value;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(SCALE, RoundingMode.HALF_UP).doubleValue();
    }

    /** The derived half of a fuel entry. */
    @lombok.Builder
    @lombok.Data
    public static class FuelVariance {
        private Double aAmount;
        private Double pAmount;
        private Double gAmount;

        /** Patron litres minus actual litres, and the same in money. */
        private Double dpLiter;
        private Double dpAmount;

        /** Patron litres minus GPS litres, and the same in money. */
        private Double dgLiter;
        private Double dgAmount;

        /** True when the patron billed more litres than the GPS sensor saw - shown in red. */
        private boolean pumpOverGps;

        /** True when the patron billed more litres than the receipt claims - shown in red. */
        private boolean pumpOverActual;
    }
}
