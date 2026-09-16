package my.maleva.api.module.fleet.entity;

import my.maleva.api.common.exception.InvalidRequestException;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The fixed truck size list the order calendar filters on.
 *
 * <p>Stored as {@link #getCode()} in {@code TruckMaster.SizeClass} and
 * {@code TruckOrderMaster.TruckSizeClass}. It is deliberately separate from the
 * free-text {@code TruckMaster.TruckType}: that column carries typos
 * ({@code 4O FT BOX TRUCK}, with the letter O) and blanks, and other screens
 * filter on its exact text, so it is not rewritten.
 *
 * <p>A 4 TON truck is recorded as {@link #THREE_TON}, the class dispatchers treat
 * it as. Adding a class means adding it here and to TRUCK_SIZE_CLASSES in the
 * front end's types/truckOrder.ts. Keep the two in step.
 */
public enum TruckSizeClass {

    ONE_TON("1TON", "1 TON", 2),
    THREE_TON("3TON", "3 TON", 4),
    FIVE_TON("5TON", "5 TON", 6),
    TEN_TON("10TON", "10 TON", 12),
    TWENTY_FT("20FT", "20 FT", 12),
    FORTY_FT("40FT", "40 FT", 24),
    /**
     * Low bed, rigid lorry and anything else that is not interchangeable. No
     * pallet count: a long loader carries one machine, and counting pallet
     * spaces on it would be meaningless.
     */
    SPECIAL("SPECIAL", "SPECIAL", null);

    private final String code;
    private final String label;
    private final Integer defaultPalletCapacity;

    TruckSizeClass(String code, String label, Integer defaultPalletCapacity) {
        this.code = code;
        this.label = label;
        this.defaultPalletCapacity = defaultPalletCapacity;
    }

    /**
     * Pallet spaces a truck of this size normally holds, used when the truck
     * itself has no {@code PalletCapacity} recorded.
     *
     * <p>Read off two years of RTI pickups: a 40 ft trip is normally 20-26
     * pallets, a 10 tonner up to about 12, and 116 of 123 five-tonner trips
     * carried five or fewer. The forty-pallet trips in that history are small
     * pallets stacked two high, which is why the figure here is the everyday
     * load and not the record.
     *
     * @return null for {@link #SPECIAL}, which is counted in jobs, not spaces
     */
    public Integer getDefaultPalletCapacity() {
        return defaultPalletCapacity;
    }

    /** The stored form, e.g. {@code 40FT}. */
    public String getCode() {
        return code;
    }

    /** The display form, e.g. {@code 40 FT}. */
    public String getLabel() {
        return label;
    }

    public static List<String> codes() {
        return Arrays.stream(values()).map(TruckSizeClass::getCode).toList();
    }

    /**
     * Reads the size out of {@code TruckMaster.TruckType}, so no column has to be
     * added to TruckMaster and no backfill has to be kept in step with it.
     *
     * <p>The stored text is free-form and inconsistent - {@code 40 FT},
     * {@code 40FT SIDE CURTAIN}, {@code 4O FT BOX TRUCK} (letter O),
     * {@code 10 TONNER}, {@code 1.90 TONS} - so it is upper-cased, the letter-O
     * typos are repaired and spaces are dropped before matching. Order matters:
     * {@code 10TONNER} must be read as 10 TON before the 1 TON test sees it.
     *
     * @return null when the type is blank or says nothing about size; such a
     *         truck is offered under "all sizes" only
     */
    public static TruckSizeClass fromTruckType(String truckType) {
        if (truckType == null) {
            return null;
        }
        String text = truckType.toUpperCase(Locale.ENGLISH)
                .replace("4O", "40")
                .replace("2O", "20")
                .replace(" ", "");
        if (text.isEmpty()) {
            return null;
        }
        // LONG LOADER is the owner's word for the low bed (UMS 7151): a long,
        // low trailer for machinery and over-length steel, never pallet groupage.
        if (text.contains("LONGLOADER") || text.contains("LOWBED") || text.contains("RIGID")) {
            return SPECIAL;
        }
        if (text.contains("40FT") || text.contains("40FEET")) {
            return FORTY_FT;
        }
        if (text.contains("20FT") || text.contains("20FEET")) {
            return TWENTY_FT;
        }
        if (text.contains("10TON")) {
            return TEN_TON;
        }
        if (text.contains("5TON")) {
            return FIVE_TON;
        }
        if (text.contains("3TON") || text.contains("4TON")) {
            return THREE_TON;
        }
        if (text.contains("1TON") || text.contains("1.9")) {
            return ONE_TON;
        }
        return null;
    }

    /**
     * Resolves a posted or stored value. Case and spaces are ignored, so the code
     * ({@code 40FT}) and the label ({@code 40 FT}) both resolve.
     *
     * @return null for a blank value, which means "any size"
     * @throws InvalidRequestException if it is not one of the list
     */
    public static TruckSizeClass fromCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String wanted = value.replace(" ", "").trim();
        return Arrays.stream(values())
                .filter(size -> size.code.equalsIgnoreCase(wanted))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(
                        "Truck size must be one of " + String.join(", ", codes())));
    }
}
