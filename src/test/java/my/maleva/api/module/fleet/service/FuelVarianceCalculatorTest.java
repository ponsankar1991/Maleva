package my.maleva.api.module.fleet.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks in the arithmetic the legacy FuelEntry.js Calculation() performed in the
 * browser, using the shape of real rows (truck 13 on 2026-08-18).
 */
class FuelVarianceCalculatorTest {

    private final FuelVarianceCalculator calculator = new FuelVarianceCalculator();

    @Test
    void computesEveryDerivedFigure() {
        // entry 6174: 213.002 actual, 210.44 GPS
        FuelVarianceCalculator.FuelVariance v =
                calculator.calculate(213.002, 639.01, 213.002, 210.44, 3.0);

        assertEquals(639.01, v.getAAmount());
        assertEquals(639.01, v.getPAmount());
        assertEquals(631.32, v.getGAmount());

        // pump vs GPS: 2.56 litres paid for that never reached the tank
        assertEquals(2.56, v.getDgLiter());
        assertEquals(7.69, v.getDgAmount());

        // pump vs actual: the paperwork agrees here
        assertEquals(0.0, v.getDpLiter());
        assertEquals(0.0, v.getDpAmount());
    }

    @Test
    void flagsPumpAboveGpsAsALoss() {
        FuelVarianceCalculator.FuelVariance v = calculator.calculate(100.0, 300.0, 100.0, 90.0, 3.0);

        // 10 litres were paid for but never entered the tank.
        assertTrue(v.isPumpOverGps());
        assertEquals(10.0, v.getDgLiter());
        assertTrue(calculator.isAdverse(v.getDgLiter()));
    }

    @Test
    void doesNotFlagPumpBelowGps() {
        FuelVarianceCalculator.FuelVariance v = calculator.calculate(100.0, 300.0, 90.0, 100.0, 3.0);

        assertFalse(v.isPumpOverGps());
        assertEquals(-10.0, v.getDgLiter());
        assertFalse(calculator.isAdverse(v.getDgLiter()));
    }

    @Test
    void zeroesTheDerivedFieldsWhenTheRateIsBlank() {
        // The legacy screen blanked these rather than showing amounts computed
        // against a rate of zero.
        FuelVarianceCalculator.FuelVariance v = calculator.calculate(100.0, 300.0, 95.0, 90.0, 0.0);

        assertEquals(300.0, v.getAAmount(), "the receipt total stands on its own");
        assertEquals(0.0, v.getPAmount());
        assertEquals(0.0, v.getGAmount());
        assertEquals(0.0, v.getDgLiter());
        assertEquals(0.0, v.getDpLiter());
    }

    @Test
    void treatsMissingValuesAsZero() {
        FuelVarianceCalculator.FuelVariance v = calculator.calculate(null, null, null, null, 3.0);

        assertEquals(0.0, v.getAAmount());
        assertEquals(0.0, v.getPAmount());
        assertEquals(0.0, v.getGAmount());
        assertFalse(v.isPumpOverGps());
    }

    @Test
    void keepsTheReceiptAmountItWasGiven() {
        // Entry 6201: 25 litres at a rate of 22, but the receipt says 55.
        // Deriving this would silently replace 55 with 550.
        FuelVarianceCalculator.FuelVariance v = calculator.calculate(25.0, 55.0, 25.0, 25.0, 22.0);
        assertEquals(55.0, v.getAAmount());
    }

    @Test
    void roundsMoneyToTwoDecimals() {
        FuelVarianceCalculator.FuelVariance v = calculator.calculate(1.0, 3.34, 1.0, 0.0, 3.335);
        assertEquals(3.34, v.getPAmount());
    }
}
