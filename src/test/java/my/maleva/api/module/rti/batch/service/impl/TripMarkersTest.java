package my.maleva.api.module.rti.batch.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Every spelling of "trip" that appears in the live planning data. */
class TripMarkersTest {

    @ParameterizedTest
    @CsvSource({
            // The two that dominate the data: 918 and 893 rows.
            "1ST TRIP,1",
            "2ND TRIP,2",
            "3RD TRIP,3",
            "4RTH TRIP,4",
            // Typed without the space, or with a stray letter.
            "1STRIP,1",
            "2NDNTRIP,2",
            "2N TRIP,2",
            "2D TRIP(THINA),2",
            "1SR TRIP,1",
            "1ST  TRIP,1",
            // The number written after the word.
            "TRIP 2,2",
            "TRIP 7,7",
            // Notes in brackets or appended, which planners add constantly.
            "1ST TRIP(RAVI),1",
            "2ND TRIP(MONDAY DELIVERY),2",
            "SG TO PTP(1ST TRIP),1",
            "1ST TRIP/KEVIN,1",
            "2ND TRIP SG TO SG,2",
            "STAND BY 2ND TRIP,2",
            "WP TO SP(3RD TRIP),3",
    })
    void readsTheTripNumberPlannersWrite(String remark, int expected) {
        assertThat(TripMarkers.tripNumber(remark)).isEqualTo(expected);
    }

    @Test
    void takesTheNumberBesideTheWordNotTheFirstNumberInTheLine() {
        // "27TH" is a date. The trip is 2.
        assertThat(TripMarkers.tripNumber("27TH ETD(2ND TRIP)")).isEqualTo(2);
        assertThat(TripMarkers.tripNumber("3 DAYS PORT STAY(5TH PM)(3RD TRIP)")).isEqualTo(3);
        assertThat(TripMarkers.tripNumber("9 AM BERTH 20 HURS ETD(1ST TRIP)")).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SG TO PTP",          // a lane
            "MONDAY DELIVERY",    // a note
            "UGUNTHAN",           // a driver's name
            "22 IBC",             // a quantity
            "CANCEL",
            "LOCAL TRIP",         // says trip, names no number
            "",
    })
    void leavesRowsWithoutATripNumberUnmarked(String remark) {
        assertThat(TripMarkers.tripNumber(remark)).isZero();
    }

    @Test
    void survivesNullAndRefusesAnAbsurdNumber() {
        assertThat(TripMarkers.tripNumber(null)).isZero();
        // 30 trips in a day is a typo or a quantity, not a trip.
        assertThat(TripMarkers.tripNumber("30 TRIPS")).isZero();
    }

    @Test
    void recognisesCombine() {
        assertThat(TripMarkers.isCombine("COMBINE")).isTrue();
        assertThat(TripMarkers.isCombine("combine with 2nd trip")).isTrue();
        assertThat(TripMarkers.isCombine("1ST TRIP")).isFalse();
        assertThat(TripMarkers.isCombine(null)).isFalse();
    }

    @Test
    void namesTheTripForTheScreen() {
        assertThat(TripMarkers.label(2)).isEqualTo("Trip 2");
        assertThat(TripMarkers.label(0)).isEmpty();
    }
}
