package my.maleva.api.module.gps.service;

import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Maps a Wialon unit name onto a TruckMaster row.
 *
 * The legacy job ran one query per row:
 * {@code select Id from TruckMaster where TruckName like '%<vehicle>%' and Active = 1}
 * - a round trip for every filling, and a SQL injection hole, since the vehicle
 * string comes straight back from Wialon. Here the active trucks are loaded once
 * per sync run and matched in memory.
 *
 * Two differences from the legacy behaviour, both deliberate:
 * <ul>
 *   <li>the lookup is scoped to the company being synced, which the legacy
 *       query was not;</li>
 *   <li>when several trucks match, the lowest id wins and a warning is logged,
 *       instead of silently taking whatever the database returned first.</li>
 * </ul>
 *
 * Internal helper for the GPS sync - not part of the module's public service API.
 */
@Component
public class TruckResolver {

    private static final Logger logger = LoggerFactory.getLogger(TruckResolver.class);

    private static final Integer ACTIVE = 1;

    private final TruckMasterRepository truckMasterRepository;

    public TruckResolver(TruckMasterRepository truckMasterRepository) {
        this.truckMasterRepository = truckMasterRepository;
    }

    /** Snapshot of the active trucks of one company, reused for a whole sync run. */
    public Snapshot snapshot(Integer companyRefId) {
        List<TruckMaster> trucks = truckMasterRepository.findByCompanyRefIdAndActive(companyRefId, ACTIVE);
        logger.info("Loaded {} active trucks for company {}", trucks.size(), companyRefId);
        return new Snapshot(trucks);
    }

    /** Immutable view over the active trucks, with the name matching baked in. */
    public static class Snapshot {

        private final List<TruckMaster> trucks;
        private final Map<String, Optional<TruckMaster>> cache = new LinkedHashMap<>();

        private Snapshot(List<TruckMaster> trucks) {
            this.trucks = trucks;
        }

        public int size() {
            return trucks.size();
        }

        /**
         * Finds the truck for a Wialon unit name: exact name first, then the
         * legacy {@code TruckName like '%vehicle%'} fallback, both case- and
         * whitespace-insensitive.
         */
        public Optional<TruckMaster> resolve(String vehicle) {
            if (vehicle == null || vehicle.isBlank()) {
                return Optional.empty();
            }
            return cache.computeIfAbsent(normalise(vehicle), this::match);
        }

        private Optional<TruckMaster> match(String upperVehicle) {
            // An exact name always wins over a substring hit. TruckMaster holds
            // both "WORKSHOP" (id 75) and "FORKLIFT WORKSHOP" (id 72), and both
            // satisfy the legacy LIKE '%WORKSHOP%', so a unit genuinely named
            // WORKSHOP could be filed against the forklift. Same for "PTP"
            // (id 90) inside "FORKLIFT PTP" (id 77).
            List<TruckMaster> exact = trucks.stream()
                    .filter(truck -> normalise(truck.getTruckName()).equals(upperVehicle))
                    .sorted(Comparator.comparing(TruckMaster::getId))
                    .toList();
            if (exact.size() == 1) {
                return Optional.of(exact.get(0));
            }
            if (exact.size() > 1) {
                logger.warn("Wialon unit '{}' matches {} trucks by exact name {}, using id {}",
                        upperVehicle, exact.size(), names(exact), exact.get(0).getId());
                return Optional.of(exact.get(0));
            }

            List<TruckMaster> partial = trucks.stream()
                    .filter(truck -> normalise(truck.getTruckName()).contains(upperVehicle))
                    .sorted(Comparator.comparing(TruckMaster::getId))
                    .toList();

            if (partial.isEmpty()) {
                return Optional.empty();
            }
            if (partial.size() > 1) {
                // Ambiguous by name alone. Rather than guessing, prefer the
                // shortest name - the closest thing to the unit as reported -
                // and say so loudly.
                TruckMaster best = partial.stream()
                        .min(Comparator
                                .comparingInt((TruckMaster t) -> normalise(t.getTruckName()).length())
                                .thenComparing(TruckMaster::getId))
                        .orElseThrow();
                logger.warn("Wialon unit '{}' matches {} trucks {}, using id {} ({})",
                        upperVehicle, partial.size(), names(partial), best.getId(), best.getTruckName());
                return Optional.of(best);
            }
            return Optional.of(partial.get(0));
        }

        private List<String> names(List<TruckMaster> trucks) {
            return trucks.stream().map(TruckMaster::getTruckName).toList();
        }

        private static String normalise(String value) {
            return value == null ? "" : value.trim().toUpperCase();
        }
    }
}
