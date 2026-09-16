package my.maleva.api.module.fleet.entity;

import my.maleva.api.common.exception.InvalidRequestException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * What a truck order's quantity is counted in.
 *
 * <p>Taken from the units the sale orders actually use: {@code 1 PKG},
 * {@code 8 PLT}, {@code 9 IBC}, {@code 3 BIN}, {@code 2 DRUM} are the common
 * ones, with tonnage and CBM behind them. Stored as {@link #getCode()} in
 * {@code TruckOrderMaster.QuantityUnit}.
 *
 * <p>A closed list on purpose: the sale order's free-text Quantity column holds
 * things like {@code "20 FT/: 7 pallets // 2 IBC"}, which no report can ever add
 * up. Here the number and the unit are kept apart so they can be.
 *
 * <p>Adding a unit means adding it here and to TRUCK_LOAD_UNITS in the front
 * end's types/truckOrder.ts. Keep the two in step.
 */
public enum TruckLoadUnit {

    PALLET("PLT", "Pallet", "1.0"),
    PACKAGE("PKG", "Package", "0.1"),
    IBC("IBC", "IBC", "1.0"),
    BIN("BIN", "Bin", "1.0"),
    DRUM("DRUM", "Drum", "0.25"),
    CARTON("CTN", "Carton", "0.05"),
    /** Roughly a pallet space per tonne on these trucks; a rule of thumb, not physics. */
    TON("TON", "Tonne", "1.0"),
    CBM("CBM", "CBM", "0.55"),
    /**
     * A whole truck, however it is filled - the "1x40FT" the customer asks for.
     * Its space is the truck's whole capacity, so it is handled by the caller
     * rather than by a per-unit figure.
     */
    TRUCK("TRUCK", "Truck", "0.0");

    private final String code;
    private final String label;
    private final BigDecimal palletSpaces;

    TruckLoadUnit(String code, String label, String palletSpaces) {
        this.code = code;
        this.label = label;
        this.palletSpaces = new BigDecimal(palletSpaces);
    }

    /**
     * How much floor one of these takes, counted in pallet spaces.
     *
     * <p>A rough conversion, and deliberately so: a pallet is one space, an IBC
     * and a bin stand on the same footprint, four drums fit on a pallet, ten
     * small packages likewise. It exists to add a day's load up to something
     * comparable, not to model the deck. The dispatcher always overrules it -
     * nothing in the application refuses a booking because of this number.
     *
     * <p>{@link #TRUCK} returns zero here; it means the whole truck and is
     * expanded by the caller, which knows the truck's capacity.
     */
    public BigDecimal getPalletSpaces() {
        return palletSpaces;
    }

    /** True for the unit that means "the whole truck", not a quantity of things. */
    public boolean isWholeTruck() {
        return this == TRUCK;
    }

    /** The stored form, e.g. {@code PLT}. */
    public String getCode() {
        return code;
    }

    /** The display form, e.g. {@code Pallet}. */
    public String getLabel() {
        return label;
    }

    public static List<String> codes() {
        return Arrays.stream(values()).map(TruckLoadUnit::getCode).toList();
    }

    /**
     * Resolves a posted or stored unit, ignoring case and space. The enum name
     * ({@code PALLET}) resolves as well as the code ({@code PLT}), so a caller
     * that sends either is understood.
     *
     * @return null for a blank value, which means "no quantity recorded"
     * @throws InvalidRequestException if it is not one of the list
     */
    public static TruckLoadUnit fromCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String wanted = value.trim();
        return Arrays.stream(values())
                .filter(unit -> unit.code.equalsIgnoreCase(wanted) || unit.name().equalsIgnoreCase(wanted))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(
                        "Quantity unit must be one of " + String.join(", ", codes())));
    }
}
