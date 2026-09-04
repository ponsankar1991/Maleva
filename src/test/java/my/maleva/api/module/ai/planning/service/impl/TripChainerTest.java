package my.maleva.api.module.ai.planning.service.impl;

import my.maleva.api.module.ai.common.NameKeys;
import my.maleva.api.module.ai.planning.service.impl.TripChainer.JobStop;
import my.maleva.api.module.ai.planning.service.impl.TripChainer.Trip;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TripChainerTest {

    private static JobStop job(String key, String origin, String destination, String pickup, Integer typedTruck) {
        LocalDateTime time = pickup == null ? null : LocalDateTime.parse("2026-08-10T" + pickup);
        return new JobStop(key, Integer.parseInt(key), NameKeys.place(origin), NameKeys.place(destination),
                origin, destination, time, null, typedTruck);
    }

    @Test
    void chainsJobsTheWayThePlannerDid() {
        // The real 10/08 plan: JPM 7151 ran Northport -> Westport -> Johor, then Westport -> Singapore,
        // a second truck shuttled KLIA -> PTP twice, and a Singapore return load came back to Westport.
        List<JobStop> jobs = List.of(
                job("1", "NORTHPORT", "WESTPORT", "08:00", null),
                job("2", "WEST PORT", "JOHOR", "10:00", null),
                job("3", "WESTPORT", "SINGAPORE", "11:00", null),
                job("4", "KLIA", "PTP", "09:00", null),
                job("5", "KLIA", "PTP", "09:30", null),
                job("6", "SG", "WP", "14:00", null));

        List<Trip> trips = TripChainer.chain(jobs, 5);

        assertThat(trips).hasSize(3);
        assertThat(trips.get(0).jobs()).extracting(JobStop::rowKey).containsExactly("1", "2");
        assertThat(trips.get(0).label()).isEqualTo("TRIP 1: NORTHPORT -> WESTPORT -> JOHOR");
        assertThat(trips.get(1).jobs()).extracting(JobStop::rowKey).containsExactly("4", "5");
        assertThat(trips.get(2).jobs()).extracting(JobStop::rowKey).containsExactly("3", "6");
        assertThat(trips.get(2).label()).isEqualTo("TRIP 3: WESTPORT -> SINGAPORE -> WP");
        assertThat(trips).extracting(Trip::tripNo).containsExactly(1, 2, 3);
    }

    @Test
    void typedTrucksAnchorTheirOwnTripsAndPickUpConnectingJobs() {
        List<JobStop> jobs = List.of(
                job("1", "PKFZ", "SINGAPORE", "08:00", 44),
                job("2", "SINGAPORE", "WESTPORT", "13:00", null),
                job("3", "PKFZ", "SINGAPORE", "09:00", 44),
                job("4", "NORTHPORT", "SINGAPORE", "08:30", 45));

        List<Trip> trips = TripChainer.chain(jobs, 5);

        assertThat(trips).hasSize(2);
        Trip vr = trips.stream().filter(t -> Integer.valueOf(44).equals(t.anchorTruckId())).findFirst().orElseThrow();
        assertThat(vr.jobs()).extracting(JobStop::rowKey).containsExactly("1", "3", "2");
        Trip other = trips.stream().filter(t -> Integer.valueOf(45).equals(t.anchorTruckId())).findFirst().orElseThrow();
        assertThat(other.jobs()).extracting(JobStop::rowKey).containsExactly("4");
    }

    @Test
    void respectsTheTripLengthCapAndNeverChainsBackwardsInTime() {
        List<JobStop> jobs = List.of(
                job("1", "A", "B", "10:00", null),
                job("2", "B", "C", "08:00", null),   // picks up before job 1 - not a continuation
                job("3", "B", "C", "11:00", null),
                job("4", "C", "D", "12:00", null),
                job("5", "D", "E", "13:00", null));

        List<Trip> trips = TripChainer.chain(jobs, 3);

        // The earliest pickup seeds the first trip and runs on to the cap of three;
        // job 2 never follows job 1 because it picks up two hours earlier.
        assertThat(trips).hasSize(2);
        assertThat(trips.get(0).jobs()).extracting(JobStop::rowKey).containsExactly("2", "4", "5");
        assertThat(trips.get(1).jobs()).extracting(JobStop::rowKey).containsExactly("1", "3");
    }

    @Test
    void placeKeysMergeShortFormsAndSpacing() {
        assertThat(NameKeys.place("West Port")).isEqualTo("WESTPORT");
        assertThat(NameKeys.place("WP")).isEqualTo("WESTPORT");
        assertThat(NameKeys.place("SG")).isEqualTo("SINGAPORE");
        assertThat(NameKeys.place("Port Klang")).isEqualTo("KLANG");
        assertThat(NameKeys.place("Pasir Gudang")).isEqualTo("PASIRGUDANG");
    }
}
