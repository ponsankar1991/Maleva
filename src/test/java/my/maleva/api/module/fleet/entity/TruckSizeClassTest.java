package my.maleva.api.module.fleet.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Reading a size out of TruckMaster.TruckType, which is free text nobody ever
 * validated. The values here are the ones actually stored on the live fleet.
 */
class TruckSizeClassTest {

    @Test
    void readsTheFortyFootFleetIncludingTheLetterOTypos() {
        assertEquals(TruckSizeClass.FORTY_FT, TruckSizeClass.fromTruckType("40 FT"));
        assertEquals(TruckSizeClass.FORTY_FT, TruckSizeClass.fromTruckType("40FT SIDE CURTAIN"));
        assertEquals(TruckSizeClass.FORTY_FT, TruckSizeClass.fromTruckType("40FT OPEN CARGO TRUCK"));
        // Typed with the letter O instead of a zero on three trucks.
        assertEquals(TruckSizeClass.FORTY_FT, TruckSizeClass.fromTruckType("4O FT  BOX TRUCK"));
        assertEquals(TruckSizeClass.FORTY_FT, TruckSizeClass.fromTruckType("4O FT OPEN TRUCK"));
    }

    @Test
    void readsTheTwentyFootFleet() {
        assertEquals(TruckSizeClass.TWENTY_FT, TruckSizeClass.fromTruckType("20 FT SIDE CURTAIN"));
        assertEquals(TruckSizeClass.TWENTY_FT, TruckSizeClass.fromTruckType("2O FT BOX TRUCK"));
    }

    @Test
    void readsTonnage() {
        assertEquals(TruckSizeClass.TEN_TON, TruckSizeClass.fromTruckType("10 TONNER"));
        assertEquals(TruckSizeClass.FIVE_TON, TruckSizeClass.fromTruckType("5 TONNER BOX TRCUK"));
        assertEquals(TruckSizeClass.THREE_TON, TruckSizeClass.fromTruckType("3 TON OPEN"));
        assertEquals(TruckSizeClass.ONE_TON, TruckSizeClass.fromTruckType("1 TON OPEN"));
        assertEquals(TruckSizeClass.ONE_TON, TruckSizeClass.fromTruckType("1.90 TONS"));
    }

    /** A 4 TON truck is dispatched as a 3 TON; they are not kept apart. */
    @Test
    void fourTonIsReadAsThreeTon() {
        assertEquals(TruckSizeClass.THREE_TON, TruckSizeClass.fromTruckType("4 TON"));
    }

    /** "10 TONNER" must not be read as a 1 TON truck. */
    @Test
    void tenTonIsNotMistakenForOneTon() {
        assertEquals(TruckSizeClass.TEN_TON, TruckSizeClass.fromTruckType("10 TONNER"));
    }

    @Test
    void lowBedAndRigidAreSpecial() {
        assertEquals(TruckSizeClass.SPECIAL, TruckSizeClass.fromTruckType("LOW BED TRUCK"));
        assertEquals(TruckSizeClass.SPECIAL, TruckSizeClass.fromTruckType("LORRY RIGID- KARGO AM"));
        // The owner's own wording for UMS 7151.
        assertEquals(TruckSizeClass.SPECIAL, TruckSizeClass.fromTruckType("LONG LOADER"));
    }

    /** The wording the fleet list sets, exactly as TRUCK_FLEET_TYPES.sql writes it. */
    @Test
    void everyTypeInTheOwnersFleetListIsUnderstood() {
        assertEquals(TruckSizeClass.FORTY_FT, TruckSizeClass.fromTruckType("40 FT SIDE CURTAIN"));
        assertEquals(TruckSizeClass.FORTY_FT, TruckSizeClass.fromTruckType("40 FT BOX"));
        assertEquals(TruckSizeClass.FORTY_FT, TruckSizeClass.fromTruckType("40 FT OPEN TRUCK"));
        assertEquals(TruckSizeClass.TEN_TON, TruckSizeClass.fromTruckType("10 TON SIDE CURTAIN"));
        assertEquals(TruckSizeClass.TEN_TON, TruckSizeClass.fromTruckType("10 TON BOX"));
        assertEquals(TruckSizeClass.FIVE_TON, TruckSizeClass.fromTruckType("5 TON BOX"));
        assertEquals(TruckSizeClass.SPECIAL, TruckSizeClass.fromTruckType("LONG LOADER"));
    }

    /** These trucks still appear under "all sizes" - they are simply not size-filtered. */
    @Test
    void saysNothingForABlankOrUnreadableType() {
        assertNull(TruckSizeClass.fromTruckType(null));
        assertNull(TruckSizeClass.fromTruckType("   "));
        assertNull(TruckSizeClass.fromTruckType("SIDE CURTAIN"));
        assertNull(TruckSizeClass.fromTruckType("FORKLIFT JOHOR"));
    }

    @Test
    void codesAndLabelsStayInStepWithTheFrontEnd() {
        assertEquals("40FT", TruckSizeClass.FORTY_FT.getCode());
        assertEquals("40 FT", TruckSizeClass.FORTY_FT.getLabel());
        // The dialog posts the code; the label is what the user picked from.
        assertEquals(TruckSizeClass.FORTY_FT, TruckSizeClass.fromCode("40 FT"));
        assertEquals(TruckSizeClass.FORTY_FT, TruckSizeClass.fromCode("40FT"));
        assertNull(TruckSizeClass.fromCode(""));
    }
}
