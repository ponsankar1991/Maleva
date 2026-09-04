package my.maleva.api.module.ai.purchaseorder.service.impl;

import my.maleva.api.module.ai.common.ExtractionSupport;
import my.maleva.api.module.ai.common.NameKeys;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.TruckMaster;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Resolves a printed vehicle plate to TruckMaster and a printed driver name to DriverMaster. */
final class FleetMatcher {

    private FleetMatcher() {
    }

    /** Plates compare on upper-case alphanumerics: "JKA 1234" == "jka-1234". A unique match is required. */
    static Optional<TruckMaster> truck(String plate, List<TruckMaster> trucks) {
        String wanted = ExtractionSupport.compact(plate);
        if (wanted.length() < 4) {
            return Optional.empty();
        }
        TruckMaster exact = null;
        TruckMaster partial = null;
        int partials = 0;
        for (TruckMaster truck : trucks) {
            String name = ExtractionSupport.compact(truck.getTruckName());
            if (name.isEmpty()) {
                continue;
            }
            if (name.equals(wanted)) {
                if (exact != null) {
                    return Optional.empty();
                }
                exact = truck;
            } else if (name.length() >= 4 && (name.contains(wanted) || wanted.contains(name))) {
                partial = truck;
                partials++;
            }
        }
        if (exact != null) {
            return Optional.of(exact);
        }
        return partials == 1 ? Optional.of(partial) : Optional.empty();
    }

    /** Driver names match exactly (normalised), by containment, or by sharing most name tokens; ties are rejected. */
    static Optional<DriverMaster> driver(String name, List<DriverMaster> drivers) {
        String wanted = normalizeName(name);
        if (wanted.length() < 3) {
            return Optional.empty();
        }
        DriverMaster best = null;
        double bestScore = 0;
        boolean tie = false;
        for (DriverMaster driver : drivers) {
            String candidate = normalizeName(driver.getDriverName());
            if (candidate.isEmpty()) {
                continue;
            }
            double score = nameScore(wanted, candidate);
            if (score > bestScore) {
                best = driver;
                bestScore = score;
                tie = false;
            } else if (score == bestScore && score > 0) {
                tie = true;
            }
        }
        if (best == null || tie || bestScore < 0.75) {
            return Optional.empty();
        }
        return Optional.of(best);
    }

    static String normalizeName(String value) {
        return NameKeys.driver(value);
    }

    static double nameScore(String a, String b) {
        if (a.equals(b)) {
            return 1.0;
        }
        if (a.length() >= 5 && b.length() >= 5 && (a.contains(b) || b.contains(a))) {
            return 0.85;
        }
        Set<String> ta = new HashSet<>(List.of(a.split(" ")));
        Set<String> tb = new HashSet<>(List.of(b.split(" ")));
        Set<String> union = new HashSet<>(ta);
        union.addAll(tb);
        ta.retainAll(tb);
        return union.isEmpty() ? 0 : 0.9 * ta.size() / union.size();
    }
}
