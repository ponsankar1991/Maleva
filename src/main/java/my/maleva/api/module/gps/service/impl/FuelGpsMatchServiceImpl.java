package my.maleva.api.module.gps.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.entity.FuelEntry;
import my.maleva.api.module.fleet.entity.FuelFillings;
import my.maleva.api.module.gps.config.FuelGpsMatchProperties;
import my.maleva.api.module.gps.dto.GpsFuelMatchDto;
import my.maleva.api.module.gps.repository.GpsFuelEntryRepository;
import my.maleva.api.module.gps.repository.GpsFuelFillingRepository;
import my.maleva.api.module.gps.service.FuelGpsMatchService;
import my.maleva.api.module.gps.service.FuelMatchType;
import my.maleva.api.module.gps.service.FuelVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * Greedy one-to-one assignment of GPS fillings to fuel entries.
 *
 * Links already stored on the entry are honoured first and their filling is
 * taken out of the pool. Whatever is left becomes candidate pairs, sorted by
 * how close the litres are, and the list is walked once taking a pair only when
 * neither side is claimed. Ties break on entry id then filling id, so the result
 * is stable across runs.
 *
 * Greedy on a single dimension, not a globally optimal assignment: a case where
 * sacrificing the closest pair would let two other pairs match better is
 * theoretically possible. It cannot double-count, which is the defect that
 * actually shows up in the data, and it stays explainable to whoever reads the
 * variance report.
 */
@Service
public class FuelGpsMatchServiceImpl implements FuelGpsMatchService {

    private static final Logger logger = LoggerFactory.getLogger(FuelGpsMatchServiceImpl.class);

    private static final String REASON_NO_GPS_DATA = "NO_GPS_DATA";
    private static final String REASON_OUT_OF_TOLERANCE = "OUT_OF_TOLERANCE";
    private static final String REASON_ALREADY_CLAIMED = "ALREADY_CLAIMED";

    private final GpsFuelEntryRepository fuelEntryRepository;
    private final GpsFuelFillingRepository fuelFillingRepository;
    private final FuelGpsMatchProperties properties;

    public FuelGpsMatchServiceImpl(GpsFuelEntryRepository fuelEntryRepository,
                                   GpsFuelFillingRepository fuelFillingRepository,
                                   FuelGpsMatchProperties properties) {
        this.fuelEntryRepository = fuelEntryRepository;
        this.fuelFillingRepository = fuelFillingRepository;
        this.properties = properties;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GpsFuelMatchDto> matchForTruckOnDay(Integer companyRefId,
                                                    Integer truckRefId,
                                                    LocalDate saleDate) {
        DayContext context = loadDay(companyRefId, truckRefId, saleDate);
        if (context == null) {
            return List.of();
        }
        return assign(context).results;
    }

    @Override
    @Transactional
    public int persistAutoMatches(Integer companyRefId, Integer truckRefId, LocalDate saleDate) {
        DayContext context = loadDay(companyRefId, truckRefId, saleDate);
        if (context == null) {
            return 0;
        }

        Assignment assignment = assign(context);
        Map<Integer, FuelEntry> entriesById = new HashMap<>();
        context.entries.forEach(entry -> entriesById.put(entry.getId(), entry));

        int changed = 0;
        for (GpsFuelMatchDto result : assignment.results) {
            FuelEntry entry = entriesById.get(result.getFuelEntryId());
            if (entry == null || FuelMatchType.isManual(entry.getFuelFillingMatchType())) {
                continue;
            }
            if (Objects.equals(entry.getFuelFillingRefId(), result.getFuelFillingId())) {
                continue;
            }
            entry.setFuelFillingRefId(result.getFuelFillingId());
            entry.setFuelFillingMatchType(result.getFuelFillingId() == null ? null : FuelMatchType.AUTO);
            fuelEntryRepository.save(entry);
            changed++;
        }

        if (changed > 0) {
            logger.info("Stored {} GPS links for truck {} on {}", changed, truckRefId, saleDate);
        }
        return changed;
    }

    @Override
    @Transactional
    public GpsFuelMatchDto setManualMatch(Integer companyRefId, Integer fuelEntryId, Integer fuelFillingId) {
        FuelEntry entry = fuelEntryRepository.findById(fuelEntryId)
                .filter(candidate -> Objects.equals(candidate.getCompanyRefId(), companyRefId))
                .orElseThrow(() -> new EntityNotFoundException("Fuel entry " + fuelEntryId + " was not found"));

        if (fuelFillingId == null) {
            entry.setFuelFillingRefId(null);
            entry.setFuelFillingMatchType(null);
            fuelEntryRepository.save(entry);
            return GpsFuelMatchDto.builder()
                    .fuelEntryId(fuelEntryId)
                    .enteredLitres(toDouble(entry.getAliter()))
                    .unmatchedReason(REASON_NO_GPS_DATA)
                    .build();
        }

        FuelFillings filling = fuelFillingRepository.findById(fuelFillingId)
                .orElseThrow(() -> new EntityNotFoundException("GPS filling " + fuelFillingId + " was not found"));

        // A filling from a different truck or a different day is not the same
        // physical event, so it can never be the right answer.
        if (!Objects.equals(filling.getTruckRefId(), entry.getTruckRefid())) {
            throw new InvalidRequestException("That GPS filling belongs to a different truck");
        }
        if (entry.getSaleDate() == null || filling.getTime() == null
                || !filling.getTime().toLocalDate().equals(entry.getSaleDate().toLocalDate())) {
            throw new InvalidRequestException("That GPS filling is from a different date");
        }

        List<FuelEntry> holders = fuelEntryRepository.findByFuelFillingRefId(fuelFillingId);
        boolean heldByAnother = holders.stream().anyMatch(other -> !other.getId().equals(fuelEntryId));
        if (heldByAnother) {
            throw new InvalidRequestException(
                    "That GPS filling is already matched to another fuel entry");
        }

        entry.setFuelFillingRefId(fuelFillingId);
        entry.setFuelFillingMatchType(FuelMatchType.MANUAL);
        fuelEntryRepository.save(entry);

        double entered = toDouble(entry.getAliter());
        OptionalDouble litres = FuelVolume.parseLitres(filling.getFilled());
        return toMatch(entry.getId(), filling, litres.isPresent() ? litres.getAsDouble() : null, entered);
    }

    // --------------------------------------------------------------- matching

    private DayContext loadDay(Integer companyRefId, Integer truckRefId, LocalDate saleDate) {
        if (companyRefId == null || truckRefId == null || saleDate == null) {
            return null;
        }
        LocalDateTime dayStart = saleDate.atStartOfDay();
        LocalDateTime nextDayStart = dayStart.plusDays(1);

        List<FuelEntry> entries = fuelEntryRepository.findActiveForTruckOnDay(
                companyRefId, truckRefId, dayStart, nextDayStart);
        if (entries.isEmpty()) {
            return null;
        }
        List<FuelFillings> fillings = fuelFillingRepository.findForTruckOnDay(
                companyRefId, truckRefId, dayStart, nextDayStart);
        return new DayContext(entries, fillings);
    }

    private Assignment assign(DayContext context) {
        Map<Integer, FuelFillings> fillingsById = new HashMap<>();
        context.fillings.forEach(filling -> fillingsById.put(filling.getId(), filling));

        Set<Integer> claimedEntries = new HashSet<>();
        Set<Integer> claimedFillings = new HashSet<>();
        List<GpsFuelMatchDto> results = new ArrayList<>();

        // 1. Honour what is already stored. A decision that has been made stays
        //    made, whatever else changes around it that day.
        for (FuelEntry entry : context.entries) {
            Integer storedId = entry.getFuelFillingRefId();
            if (storedId == null) {
                continue;
            }
            FuelFillings filling = fillingsById.get(storedId);
            if (filling == null) {
                // Points at a filling that is no longer on this truck-day; treat
                // it as unlinked rather than showing a foreign reading.
                logger.warn("Fuel entry {} references filling {} which is not on this truck-day",
                        entry.getId(), storedId);
                continue;
            }
            OptionalDouble litres = FuelVolume.parseLitres(filling.getFilled());
            claimedEntries.add(entry.getId());
            claimedFillings.add(storedId);
            results.add(toMatch(entry.getId(), filling,
                    litres.isPresent() ? litres.getAsDouble() : null, toDouble(entry.getAliter())));
        }

        // 2. Assign what is left, closest pair first.
        List<Candidate> candidates = buildCandidates(context, claimedEntries, claimedFillings);
        candidates.sort(Comparator
                .comparingDouble((Candidate c) -> c.difference)
                .thenComparing(c -> c.entry.getId())
                .thenComparing(c -> c.filling.getId()));

        for (Candidate candidate : candidates) {
            Integer entryId = candidate.entry.getId();
            Integer fillingId = candidate.filling.getId();

            if (claimedEntries.contains(entryId)) {
                continue;
            }
            if (properties.isOneToOne() && claimedFillings.contains(fillingId)) {
                continue;
            }
            claimedEntries.add(entryId);
            claimedFillings.add(fillingId);
            results.add(toMatch(entryId, candidate.filling, candidate.litres, candidate.entered));
        }

        // 3. Say why the rest got nothing, instead of returning a bare null.
        for (FuelEntry entry : context.entries) {
            if (claimedEntries.contains(entry.getId())) {
                continue;
            }
            results.add(unmatched(entry, context.fillings));
        }

        results.sort(Comparator.comparing(GpsFuelMatchDto::getFuelEntryId));
        return new Assignment(results);
    }

    private List<Candidate> buildCandidates(DayContext context,
                                            Set<Integer> claimedEntries,
                                            Set<Integer> claimedFillings) {
        List<Candidate> candidates = new ArrayList<>();
        for (FuelEntry entry : context.entries) {
            if (claimedEntries.contains(entry.getId())) {
                continue;
            }
            double entered = toDouble(entry.getAliter());
            if (entered <= 0d) {
                // Nothing to compare against; the tolerance window would be zero.
                continue;
            }
            double allowed = entered * properties.getTolerance();

            for (FuelFillings filling : context.fillings) {
                if (claimedFillings.contains(filling.getId())) {
                    continue;
                }
                OptionalDouble litres = FuelVolume.parseLitres(filling.getFilled());
                if (litres.isEmpty()) {
                    continue;
                }
                double difference = Math.abs(litres.getAsDouble() - entered);
                if (difference <= allowed) {
                    candidates.add(new Candidate(entry, filling, litres.getAsDouble(), entered, difference));
                }
            }
        }
        return candidates;
    }

    private GpsFuelMatchDto toMatch(Integer entryId, FuelFillings filling,
                                    Double litres, Double entered) {
        Double difference = (litres == null || entered == null) ? null : Math.abs(litres - entered);
        return GpsFuelMatchDto.builder()
                .fuelEntryId(entryId)
                .fuelFillingId(filling.getId())
                .vehicle(filling.getVehicle())
                .gpsTime(filling.getTime())
                .gpsLocation(filling.getLocation())
                .gpsDriver(filling.getDriver())
                .gpsCreatedDate(filling.getCreatedDate())
                .filled(filling.getFilled())
                .filledLitres(litres)
                .enteredLitres(entered)
                .differenceLitres(difference)
                .build();
    }

    /**
     * Distinguishes "there was no GPS data at all" from "a filling existed but
     * another entry got it first" - the difference matters when someone asks
     * why a row shows no GPS litres.
     *
     * The tolerance is re-tested against every filling of the day rather than
     * against the leftover candidate list. A filling that another entry has
     * already taken is removed from the candidates, so reading the reason off
     * that list reported OUT_OF_TOLERANCE for litres that in fact matched
     * perfectly well and were simply spoken for.
     */
    private GpsFuelMatchDto unmatched(FuelEntry entry, List<FuelFillings> fillings) {
        String reason;
        if (fillings.isEmpty()) {
            reason = REASON_NO_GPS_DATA;
        } else {
            double entered = toDouble(entry.getAliter());
            double allowed = entered * properties.getTolerance();
            boolean anyWithinTolerance = entered > 0d && fillings.stream().anyMatch(filling -> {
                OptionalDouble litres = FuelVolume.parseLitres(filling.getFilled());
                return litres.isPresent() && Math.abs(litres.getAsDouble() - entered) <= allowed;
            });
            reason = anyWithinTolerance ? REASON_ALREADY_CLAIMED : REASON_OUT_OF_TOLERANCE;
        }
        return GpsFuelMatchDto.builder()
                .fuelEntryId(entry.getId())
                .enteredLitres(toDouble(entry.getAliter()))
                .unmatchedReason(reason)
                .build();
    }

    private Double toDouble(Float value) {
        return value == null ? 0d : value.doubleValue();
    }

    /** The entries and fillings of one truck-day. */
    private static final class DayContext {
        private final List<FuelEntry> entries;
        private final List<FuelFillings> fillings;

        private DayContext(List<FuelEntry> entries, List<FuelFillings> fillings) {
            this.entries = entries;
            this.fillings = fillings;
        }
    }

    /** Result of one assignment pass. */
    private static final class Assignment {
        private final List<GpsFuelMatchDto> results;

        private Assignment(List<GpsFuelMatchDto> results) {
            this.results = results;
        }
    }

    /** One possible pairing, already scored. */
    private static final class Candidate {
        private final FuelEntry entry;
        private final FuelFillings filling;
        private final double litres;
        private final double entered;
        private final double difference;

        private Candidate(FuelEntry entry, FuelFillings filling,
                          double litres, double entered, double difference) {
            this.entry = entry;
            this.filling = filling;
            this.litres = litres;
            this.entered = entered;
            this.difference = difference;
        }
    }
}
