package my.maleva.api.module.ai.planning.service.impl;

import my.maleva.api.common.config.PlanningSuggestProperties;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestResponse;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ranks trucks and drivers for one trip (one or more chained jobs) from the
 * planning history and the day's situation. Pure arithmetic over records, so
 * it is unit-tested without a database: the service loads the inputs, this
 * class decides.
 */
final class AssignmentScorer {

    /** A job on the trip, with pre-normalised comparison keys. */
    record Job(Integer saleOrderId, Integer customerRefId, String customerName, String originKey,
               String destinationKey, String sPortKey, String oPortKey, LocalDate date) {
    }

    /** One past assignment: which truck and driver did what, when. */
    record HistoryEvent(LocalDate date, Integer truckId, Integer driverId, Integer customerRefId,
                        String originKey, String destinationKey, String sPortKey, String oPortKey) {
    }

    /** A truck that may be suggested; ineligible ones carry the reason and are never picked. */
    record TruckInfo(Integer id, String name, boolean eligible, String ineligibleReason) {
    }

    record DriverInfo(Integer id, String name, boolean eligible, String ineligibleReason, Integer defaultTruckId) {
    }

    /**
     * What the rest of the day looks like: load per truck, where each unit is
     * right now (its last planned stop), who is already busy, and which driver
     * each truck already has. All maps are mutable: booking a trip updates them
     * so the next trip sees it.
     */
    record DaySituation(Map<Integer, Integer> jobsTodayByTruck,
                        Map<Integer, String> lastDestinationByTruck,
                        Map<Integer, String> lastDestinationByDriver,
                        Map<Integer, Set<Integer>> trucksTodayByDriver,
                        Map<Integer, Integer> driverTodayByTruck) {
    }

    private record Tally(double score, int customerJobs, int laneJobs, int portJobs, int pairJobs) {
        Tally add(double delta, int customer, int lane, int port, int pair) {
            return new Tally(score + delta, customerJobs + customer, laneJobs + lane, portJobs + port, pairJobs + pair);
        }
    }

    private final PlanningSuggestProperties props;

    AssignmentScorer(PlanningSuggestProperties props) {
        this.props = props;
    }

    double recencyWeight(LocalDate eventDate, LocalDate planningDate) {
        long days = Math.abs(ChronoUnit.DAYS.between(eventDate, planningDate));
        if (days <= props.getRecentDays()) {
            return 3.0;
        }
        if (days <= props.getMidDays()) {
            return 2.0;
        }
        return 1.0;
    }

    List<PlanningSuggestResponse.Pick> rankTrucks(List<Job> trip, Collection<TruckInfo> trucks,
                                                  List<HistoryEvent> history, DaySituation day) {
        Map<Integer, Tally> tallies = new LinkedHashMap<>();
        Map<Integer, TruckInfo> byId = new LinkedHashMap<>();
        for (TruckInfo truck : trucks) {
            if (truck.eligible()) {
                byId.put(truck.id(), truck);
                tallies.put(truck.id(), new Tally(0, 0, 0, 0, 0));
            }
        }
        for (Job job : trip) {
            for (HistoryEvent event : history) {
                Tally tally = tallies.get(event.truckId());
                if (tally != null) {
                    tallies.put(event.truckId(), affinity(tally, job, event, null));
                }
            }
        }
        Job first = trip.get(0);
        List<PlanningSuggestResponse.Pick> picks = new ArrayList<>();
        for (Map.Entry<Integer, Tally> entry : tallies.entrySet()) {
            TruckInfo truck = byId.get(entry.getKey());
            Tally tally = entry.getValue();
            double score = tally.score();
            List<String> reasons = reasonsFor(trip, tally, null);
            String lastStop = day.lastDestinationByTruck().get(truck.id());
            if (lastStop != null && !lastStop.isEmpty() && lastStop.equals(first.originKey())) {
                score += props.getContinuityBonus();
                reasons.add("Is already where this trip starts - no empty run");
            }
            int load = day.jobsTodayByTruck().getOrDefault(truck.id(), 0);
            if (load > props.getJobsBeforeLoadPenalty()) {
                score -= props.getLoadPenalty() * (load - props.getJobsBeforeLoadPenalty());
                reasons.add("Already has " + load + " job(s) that day");
            } else if (load == 0 && score > 0) {
                reasons.add("No other jobs that day");
            }
            if (score > 0) {
                picks.add(new PlanningSuggestResponse.Pick(truck.id(), truck.name(), (int) Math.round(score * 100), reasons));
            }
        }
        return normalise(picks);
    }

    List<PlanningSuggestResponse.Pick> rankDrivers(List<Job> trip, Integer chosenTruckId, Collection<DriverInfo> drivers,
                                                   List<HistoryEvent> history, DaySituation day) {
        Map<Integer, Tally> tallies = new LinkedHashMap<>();
        Map<Integer, DriverInfo> byId = new LinkedHashMap<>();
        for (DriverInfo driver : drivers) {
            if (!driver.eligible()) {
                continue;
            }
            Set<Integer> trucksToday = day.trucksTodayByDriver().getOrDefault(driver.id(), Set.of());
            boolean onAnotherTruck = !trucksToday.isEmpty()
                    && (chosenTruckId == null || !trucksToday.contains(chosenTruckId));
            if (onAnotherTruck) {
                continue; // driving a different truck that day - never suggested for this one
            }
            byId.put(driver.id(), driver);
            tallies.put(driver.id(), new Tally(0, 0, 0, 0, 0));
        }
        for (Job job : trip) {
            for (HistoryEvent event : history) {
                Tally tally = tallies.get(event.driverId());
                if (tally != null) {
                    tallies.put(event.driverId(), affinity(tally, job, event, chosenTruckId));
                }
            }
        }
        Job first = trip.get(0);
        List<PlanningSuggestResponse.Pick> picks = new ArrayList<>();
        for (Map.Entry<Integer, Tally> entry : tallies.entrySet()) {
            DriverInfo driver = byId.get(entry.getKey());
            Tally tally = entry.getValue();
            double score = tally.score();
            List<String> reasons = reasonsFor(trip, tally, chosenTruckId);
            if (chosenTruckId != null && chosenTruckId.equals(driver.defaultTruckId())) {
                score += props.getPairingWeight() * 3;
                reasons.add("Regular driver of this truck");
            }
            Set<Integer> trucksToday = day.trucksTodayByDriver().getOrDefault(driver.id(), Set.of());
            if (chosenTruckId != null && trucksToday.contains(chosenTruckId)) {
                score += props.getPairingWeight() * 2;
                reasons.add("Already on this truck that day");
            }
            String lastStop = day.lastDestinationByDriver().get(driver.id());
            if (lastStop != null && !lastStop.isEmpty() && lastStop.equals(first.originKey())) {
                score += props.getContinuityBonus();
                reasons.add("Ended the previous trip where this one starts");
            }
            if (score > 0) {
                picks.add(new PlanningSuggestResponse.Pick(driver.id(), driver.name(), (int) Math.round(score * 100), reasons));
            }
        }
        return normalise(picks);
    }

    private Tally affinity(Tally tally, Job job, HistoryEvent event, Integer chosenTruckId) {
        double weight = recencyWeight(event.date(), job.date());
        double delta = 0;
        int customer = 0;
        int lane = 0;
        int port = 0;
        int pair = 0;
        if (job.customerRefId() != null && job.customerRefId().equals(event.customerRefId())) {
            delta += props.getCustomerWeight() * weight;
            customer = 1;
        }
        if (!job.originKey().isEmpty() && !job.destinationKey().isEmpty()
                && job.originKey().equals(event.originKey()) && job.destinationKey().equals(event.destinationKey())) {
            delta += props.getLaneWeight() * weight;
            lane = 1;
        } else if (samePort(job, event)) {
            delta += props.getPortWeight() * weight;
            port = 1;
        }
        if (chosenTruckId != null && chosenTruckId.equals(event.truckId())) {
            delta += props.getPairingWeight() * weight;
            pair = 1;
        }
        return tally.add(delta, customer, lane, port, pair);
    }

    private static boolean samePort(Job job, HistoryEvent event) {
        boolean s = !job.sPortKey().isEmpty() && job.sPortKey().equals(event.sPortKey());
        boolean o = !job.oPortKey().isEmpty() && job.oPortKey().equals(event.oPortKey());
        return s || o;
    }

    private List<String> reasonsFor(List<Job> trip, Tally tally, Integer chosenTruckId) {
        List<String> reasons = new ArrayList<>();
        if (tally.customerJobs() > 0) {
            Set<String> names = new LinkedHashSet<>();
            for (Job job : trip) {
                if (job.customerName() != null) {
                    names.add(job.customerName());
                }
            }
            String who = names.size() == 1 ? names.iterator().next()
                    : (names.isEmpty() ? "this customer" : "the customers on this trip");
            reasons.add(tally.customerJobs() + " job(s) for " + who + " in the last " + props.getHistoryDays() + " days");
        }
        if (tally.laneJobs() > 0) {
            reasons.add(tally.laneJobs() + " job(s) on " + (trip.size() > 1 ? "these lanes" : "this lane"));
        }
        if (tally.portJobs() > 0) {
            reasons.add(tally.portJobs() + " job(s) through " + (trip.size() > 1 ? "these ports" : "this port"));
        }
        if (chosenTruckId != null && tally.pairJobs() > 0) {
            reasons.add(tally.pairJobs() + " job(s) driving the chosen truck");
        }
        return reasons;
    }

    /** Sort best first and rescale to 0-100 against the best candidate. */
    private static List<PlanningSuggestResponse.Pick> normalise(List<PlanningSuggestResponse.Pick> picks) {
        picks.sort(Comparator.comparingInt(PlanningSuggestResponse.Pick::score).reversed()
                .thenComparing(p -> String.valueOf(p.name())));
        if (picks.isEmpty()) {
            return picks;
        }
        int top = Math.max(1, picks.get(0).score());
        List<PlanningSuggestResponse.Pick> scaled = new ArrayList<>();
        for (PlanningSuggestResponse.Pick pick : picks) {
            scaled.add(new PlanningSuggestResponse.Pick(pick.id(), pick.name(),
                    (int) Math.round(100.0 * pick.score() / top), pick.reasons()));
        }
        return scaled;
    }
}
