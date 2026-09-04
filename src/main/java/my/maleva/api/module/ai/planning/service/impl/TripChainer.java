package my.maleva.api.module.ai.planning.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Groups a day's jobs into trips the way a planner does: a job that starts
 * where the previous one ended, and picks up no earlier, rides on the same
 * truck. That is what keeps trucks from running empty between jobs.
 *
 * <p>Jobs the planner already put on a truck anchor a trip for that truck;
 * unassigned jobs are chained onto those trips first, then among themselves.
 */
final class TripChainer {

    /** One grid row with the comparison keys and times chaining needs. */
    record JobStop(String rowKey, Integer saleOrderId, String originKey, String destinationKey,
                   String originText, String destinationText, LocalDateTime pickup, LocalDateTime delivery,
                   Integer typedTruckId) {
    }

    /** An ordered run of jobs for one truck. */
    record Trip(int tripNo, Integer anchorTruckId, List<JobStop> jobs) {

        JobStop first() {
            return jobs.get(0);
        }

        JobStop last() {
            return jobs.get(jobs.size() - 1);
        }

        /** "TRIP 2: NORTHPORT -> WESTPORT -> JOHOR" from the printed place names. */
        String label() {
            StringBuilder sb = new StringBuilder("TRIP ").append(tripNo).append(": ");
            String previousKey = null;
            for (JobStop job : jobs) {
                // Compare on place keys so "WESTPORT" and "WEST PORT" read as one stop.
                if (previousKey == null || !job.originKey().equals(previousKey)) {
                    if (previousKey != null) {
                        sb.append(" -> ");
                    }
                    sb.append(clean(job.originText()));
                }
                sb.append(" -> ").append(clean(job.destinationText()));
                previousKey = job.destinationKey();
            }
            return sb.toString();
        }

        private static String clean(String value) {
            return value == null || value.isBlank() ? "?" : value.trim().toUpperCase();
        }
    }

    private TripChainer() {
    }

    static List<Trip> chain(List<JobStop> jobs, int maxJobsPerTrip) {
        int max = Math.max(1, maxJobsPerTrip);
        List<JobStop> ordered = new ArrayList<>(jobs);
        ordered.sort(Comparator.comparing((JobStop j) -> j.pickup() == null)
                .thenComparing(j -> j.pickup() == null ? LocalDateTime.MAX : j.pickup())
                .thenComparing(JobStop::rowKey));

        Set<JobStop> unassigned = new LinkedHashSet<>(ordered);
        List<List<JobStop>> trips = new ArrayList<>();
        List<Integer> anchors = new ArrayList<>();

        // 1. Trucks the planner already chose: every job typed onto a truck
        //    belongs to that truck's trip, in pickup order.
        Map<Integer, List<JobStop>> typed = new LinkedHashMap<>();
        for (JobStop job : ordered) {
            if (job.typedTruckId() != null) {
                typed.computeIfAbsent(job.typedTruckId(), k -> new ArrayList<>()).add(job);
            }
        }
        for (Map.Entry<Integer, List<JobStop>> entry : typed.entrySet()) {
            List<JobStop> trip = new ArrayList<>(entry.getValue());
            trip.forEach(unassigned::remove);
            trips.add(trip);
            anchors.add(entry.getKey());
        }

        // 2. Extend those trips with unassigned jobs that start where they end.
        for (List<JobStop> trip : trips) {
            extend(trip, unassigned, max);
        }

        // 3. Chain what is left, seeding a new trip from the earliest pickup each time.
        while (!unassigned.isEmpty()) {
            JobStop seed = unassigned.iterator().next();
            unassigned.remove(seed);
            List<JobStop> trip = new ArrayList<>();
            trip.add(seed);
            extend(trip, unassigned, max);
            trips.add(trip);
            anchors.add(null);
        }

        // Number the trips in the order the day unfolds: earliest first pickup first.
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < trips.size(); i++) {
            order.add(i);
        }
        order.sort(Comparator.comparing((Integer i) -> trips.get(i).get(0).pickup() == null)
                .thenComparing(i -> trips.get(i).get(0).pickup() == null ? LocalDateTime.MAX : trips.get(i).get(0).pickup())
                .thenComparing(i -> i));
        List<Trip> out = new ArrayList<>();
        int tripNo = 1;
        for (Integer i : order) {
            out.add(new Trip(tripNo++, anchors.get(i), List.copyOf(trips.get(i))));
        }
        return out;
    }

    /**
     * Keep appending the earliest unassigned job that starts where the trip
     * currently ends. When nothing connects, a repeat of the same lane (a
     * second load KLIA -> PTP after a first one) rides on the same truck too.
     */
    private static void extend(List<JobStop> trip, Set<JobStop> unassigned, int max) {
        while (trip.size() < max) {
            JobStop current = trip.get(trip.size() - 1);
            JobStop next = pickNext(current, unassigned, true);
            if (next == null) {
                next = pickNext(current, unassigned, false);
            }
            if (next == null) {
                return;
            }
            unassigned.remove(next);
            trip.add(next);
        }
    }

    private static JobStop pickNext(JobStop current, Set<JobStop> unassigned, boolean connected) {
        JobStop next = null;
        for (JobStop candidate : unassigned) {
            if (candidate.typedTruckId() != null) {
                continue; // belongs to its own truck's trip
            }
            boolean fits = connected ? connects(current, candidate) : repeatsLane(current, candidate);
            if (!fits) {
                continue;
            }
            if (next == null || earlier(candidate, next)) {
                next = candidate;
            }
        }
        return next;
    }

    /** Same origin and destination, picked up no earlier: a second load on the same lane. */
    static boolean repeatsLane(JobStop current, JobStop candidate) {
        if (current.originKey().isEmpty() || current.destinationKey().isEmpty()) {
            return false;
        }
        if (!current.originKey().equals(candidate.originKey()) || !current.destinationKey().equals(candidate.destinationKey())) {
            return false;
        }
        return current.pickup() == null || candidate.pickup() == null || !candidate.pickup().isBefore(current.pickup());
    }

    /** The next job starts where this one ends, and does not pick up before this one does. */
    static boolean connects(JobStop current, JobStop candidate) {
        if (current.destinationKey().isEmpty() || candidate.originKey().isEmpty()) {
            return false;
        }
        if (!current.destinationKey().equals(candidate.originKey())) {
            return false;
        }
        if (current.pickup() != null && candidate.pickup() != null && candidate.pickup().isBefore(current.pickup())) {
            return false;
        }
        return true;
    }

    private static boolean earlier(JobStop a, JobStop b) {
        if (a.pickup() == null) {
            return false;
        }
        if (b.pickup() == null) {
            return true;
        }
        return a.pickup().isBefore(b.pickup());
    }
}
