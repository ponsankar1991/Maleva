package my.maleva.api.module.rti.batch.service.impl;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.ai.common.NameKeys;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchDtos.DriverSource;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchDtos.SkipReason;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchPreview;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchRequest;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchResult;
import my.maleva.api.module.rti.batch.dto.PlanningRtiGroup;
import my.maleva.api.module.rti.batch.dto.PlanningRtiJob;
import my.maleva.api.module.rti.batch.dto.PlanningRtiSkip;
import my.maleva.api.module.rti.batch.repository.PlanningRtiBatchReader;
import my.maleva.api.module.rti.batch.repository.PlanningRtiBatchReader.DriverRow;
import my.maleva.api.module.rti.batch.repository.PlanningRtiBatchReader.LastDriver;
import my.maleva.api.module.rti.batch.repository.PlanningRtiBatchReader.PlanHeader;
import my.maleva.api.module.rti.batch.repository.PlanningRtiBatchReader.PlanRow;
import my.maleva.api.module.rti.batch.service.PlanningRtiBatchService;
import my.maleva.api.module.rti.dto.RTIDetailsDto;
import my.maleva.api.module.rti.dto.RTIMasterDto;
import my.maleva.api.module.rti.service.RTIMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Turns one saved plan into all of its RTIs.
 *
 * <p><b>The grouping rule.</b> One RTI per <em>truck + driver + trip</em>. Every
 * job the same truck and driver carry on one run goes onto one RTI; a second run
 * is a second RTI. The run comes from the planner's REMARKS — "1ST TRIP",
 * "2ND TRIP" are the two most common remarks in the whole system — and "COMBINE"
 * puts a load onto the run going the same way.
 *
 * <p>Truck and driver are forced by the record: {@code RTIMaster} holds one of
 * each, so two drivers on one truck split into two RTIs whatever the remarks
 * say. (The history agrees this is rare: a truck keeps one driver through the
 * day in 2299 of 2372 truck-days in the 2026 data.) Rows with no trip written
 * stay together, which is how every plan without trip markers behaves.
 *
 * <p><b>Drivers.</b> 99% of planning rows carry no {@code DriverRefId} and 57%
 * carry no driver name either — the planner picks the driver when opening the
 * RTI. So a driver has to be found before anything can be created. Three
 * sources, in order: the name typed on the plan; the driver typed on a sibling
 * row of the same truck; and failing both, the driver who last ran that truck.
 * That last one matches what actually happened 2698 times out of 3038 (89%) —
 * good enough to put in front of a person, not good enough to save without
 * asking, which is exactly what the preview is for.
 *
 * <p><b>Jobs that were on an earlier RTI.</b> Included by default. A job carried
 * again on a later plan is real work and needs its own RTI, and the RTI the
 * lookup finds is usually weeks old and belongs to another day's run — so the
 * earlier RTI is named and dated on the job rather than used to block it. A
 * planner who wants them left out asks for that ({@code includeExisting} false
 * on the preview), and the server honours the request either way.
 *
 * <p><b>What is not checked.</b> Driver leave, licence and GDL expiry, and port
 * passes are all ignored. A truck and driver chosen on the plan are the ones the
 * planner means, and that data is not kept current enough to argue with them
 * over — the RTI is created for the truck as planned.
 *
 * <p><b>What is never guessed.</b> Salary is entered by a person, always. The
 * allowance switches (sleeping, empty pickup, punctuality and the rest) are
 * statements about what happened on the day and are left at zero, exactly as
 * they are when a human creates an RTI by hand.
 */
@Service
public class PlanningRtiBatchServiceImpl implements PlanningRtiBatchService {

    private static final Logger log = LoggerFactory.getLogger(PlanningRtiBatchServiceImpl.class);

    /** How far back to look for "who last drove this truck". */
    private static final int DRIVER_HISTORY_DAYS = 180;

    /** Normalised name of the outside-driver placeholder rows in DriverMaster. */
    private static final String OUTSIDE_DRIVER_KEY = "OUTSIDE DRIVER";

    private static final DateTimeFormatter WIRE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PlanningRtiBatchReader reader;
    private final RTIMasterService rtiMasterService;
    private final NamedParameterJdbcTemplate jdbc;

    public PlanningRtiBatchServiceImpl(PlanningRtiBatchReader reader,
                                       RTIMasterService rtiMasterService,
                                       NamedParameterJdbcTemplate jdbc) {
        this.reader = reader;
        this.rtiMasterService = rtiMasterService;
        this.jdbc = jdbc;
    }

    // ───────────────────────────── preview ─────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PlanningRtiBatchPreview preview(Integer planningId, Integer companyRefId,
                                          List<Integer> onlyJobIds, boolean includeExisting) {
        PlanHeader header = requirePlan(planningId, companyRefId);
        List<PlanRow> rows = reader.planRows(planningId, companyRefId);
        if (rows.isEmpty()) {
            throw new InvalidRequestException("This plan has no rows to create RTI from.");
        }

        Context context = loadContext(companyRefId);
        List<PlanningRtiSkip> skipped = new ArrayList<>();
        List<PlanRow> creatable = partition(rows, skipped, includeExisting);

        // Ticking rows narrows what is created, never how it is grouped: the
        // selection is still cut into one RTI per truck and driver. Jobs left
        // out are listed rather than forgotten.
        if (onlyJobIds != null && !onlyJobIds.isEmpty()) {
            Set<Integer> wanted = new LinkedHashSet<>(onlyJobIds);
            List<PlanRow> selected = new ArrayList<>();
            for (PlanRow row : creatable) {
                if (wanted.contains(row.saleOrderMasterRefId())) {
                    selected.add(row);
                } else {
                    skipped.add(skip(row, SkipReason.NOT_CONFIRMED, "Not selected on the planning grid.", 0, ""));
                }
            }
            creatable = selected;
        }

        List<PlanningRtiGroup> groups = group(creatable, context);

        int jobsToCreate = groups.stream().mapToInt(group -> group.jobs().size()).sum();
        List<String> warnings = new ArrayList<>();
        long needingDriver = groups.stream().filter(group -> !group.isReady()).count();
        if (needingDriver > 0) {
            warnings.add(needingDriver + " truck(s) still need a driver before their RTI can be created.");
        }

        PlanningRtiBatchPreview preview = new PlanningRtiBatchPreview(
                header.id(), header.planningNo(), header.planningDate(),
                rows.size(), jobsToCreate, skipped.size(),
                groups, skipped, warnings);

        // The promise of the feature, checked rather than assumed: no planned job
        // may go missing between "will be created" and "skipped, and here is why".
        if (!preview.isComplete()) {
            throw new IllegalStateException("RTI preview lost a job: planned=" + rows.size()
                    + " toCreate=" + jobsToCreate + " skipped=" + skipped.size());
        }
        return preview;
    }

    // ───────────────────────────── create ─────────────────────────────

    @Override
    @Transactional
    public PlanningRtiBatchResult create(Integer planningId, PlanningRtiBatchRequest request) {
        Integer companyRefId = request.getCompanyRefId();
        PlanHeader header = requirePlan(planningId, companyRefId);
        if (request.getGroups() == null || request.getGroups().isEmpty()) {
            throw new InvalidRequestException("Nothing to create — no trucks were confirmed.");
        }

        // One planner's click at a time for this plan. Held by the transaction, so
        // a second press cannot slip past the duplicate check below while the
        // first is still writing.
        lockPlan(planningId, companyRefId);

        List<PlanRow> rows = reader.planRows(planningId, companyRefId);
        Map<Integer, PlanRow> rowByJob = new LinkedHashMap<>();
        for (PlanRow row : rows) {
            if (row.saleOrderMasterRefId() != null && row.saleOrderMasterRefId() > 0) {
                rowByJob.putIfAbsent(row.saleOrderMasterRefId(), row);
            }
        }

        // Re-asked with the lock held: the preview may be minutes old, and someone
        // else may have created an RTI for one of these jobs in the meantime.
        Set<Integer> requestedJobs = new LinkedHashSet<>();
        for (PlanningRtiBatchRequest.Group group : request.getGroups()) {
            if (group.getSaleOrderMasterRefIds() != null) {
                requestedJobs.addAll(group.getSaleOrderMasterRefIds());
            }
        }
        // Asked again with the lock held. When the planner deliberately allowed a
        // second RTI, nothing is set aside for already having one.
        Map<Integer, String> alreadyInRti = request.isAllowDuplicates()
                ? Map.of()
                : reader.jobsAlreadyInRti(requestedJobs);

        List<PlanningRtiBatchResult.Created> created = new ArrayList<>();
        List<PlanningRtiSkip> skipped = new ArrayList<>();
        Set<Integer> handled = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();

        for (PlanningRtiBatchRequest.Group group : request.getGroups()) {
            List<PlanRow> jobRows = new ArrayList<>();
            for (Integer jobId : group.getSaleOrderMasterRefIds() == null ? List.<Integer>of() : group.getSaleOrderMasterRefIds()) {
                PlanRow row = rowByJob.get(jobId);
                if (row == null) {
                    // Refuse rather than create an RTI for a job the plan does not hold.
                    throw new InvalidRequestException("Job " + jobId + " is not on plan " + header.planningNo() + ".");
                }
                if (!handled.add(jobId)) {
                    continue;
                }
                // Two sources say whether this job already has an RTI: the fresh
                // lookup taken under the lock, and the plan row's own reference.
                // Either one is enough to set it aside — a guard that depends on
                // a single source is one query away from doubling a job.
                String existingNo = alreadyInRti.get(jobId);
                if (existingNo == null && row.existingRtiId() != null && row.existingRtiId() > 0) {
                    existingNo = row.existingRtiNo();
                }
                if (!request.isAllowDuplicates() && existingNo != null) {
                    skipped.add(skip(row, SkipReason.ALREADY_IN_RTI,
                            "Already in RTI " + existingNo, row.existingRtiId(), existingNo));
                    continue;
                }
                jobRows.add(row);
            }
            if (jobRows.isEmpty()) {
                continue;
            }
            if (group.getTruckRefId() == null || group.getTruckRefId() <= 0
                    || group.getDriverRefId() == null || group.getDriverRefId() <= 0) {
                throw new InvalidRequestException(
                        "Every RTI needs a truck and a driver — one of the confirmed trucks has neither.");
            }

            RTIMasterDto saved = rtiMasterService.create(
                    buildRtiPayload(group, jobRows, header, request, now));

            created.add(new PlanningRtiBatchResult.Created(
                    saved.getId(),
                    saved.getCNumberDisplay(),
                    group.getTruckRefId(),
                    jobRows.get(0).truckName(),
                    group.getDriverRefId(),
                    group.getOutsideDriver() != null && !group.getOutsideDriver().isBlank()
                            ? group.getOutsideDriver()
                            : jobRows.get(0).driverName(),
                    jobRows.size()));
        }

        // Everything on the plan that the planner did not confirm is reported, not
        // dropped: a job left out of the batch is still a job with no RTI.
        //
        // A plan lists the same job more than once fairly often (plan 1756: 28
        // rows, 24 jobs). Only the first row of a job becomes an RTI line; the
        // repeats are said out loud rather than passed over, or the count below
        // would come up short and fail a batch that was in fact correct.
        Set<Integer> countedJobs = new HashSet<>();
        for (PlanRow row : rows) {
            Integer jobId = row.saleOrderMasterRefId();
            if (jobId == null || jobId <= 0) {
                skipped.add(skip(row, SkipReason.NO_JOB_REFERENCE,
                        "This row has no job reference — save the plan first.", 0, ""));
                continue;
            }
            if (!countedJobs.add(jobId)) {
                skipped.add(skip(row, SkipReason.DUPLICATE_IN_PLAN,
                        "This job is on the plan more than once; it goes on one RTI line.", 0, ""));
                continue;
            }
            if (handled.contains(jobId)) {
                // Already created, or already set aside, by the group loop above.
                continue;
            }
            handled.add(jobId);
            String existingNo = alreadyInRti.get(jobId);
            if (!request.isAllowDuplicates()
                    && (existingNo != null || (row.existingRtiId() != null && row.existingRtiId() > 0))) {
                String number = existingNo != null ? existingNo : row.existingRtiNo();
                skipped.add(skip(row, SkipReason.ALREADY_IN_RTI, "Already in RTI " + number,
                        row.existingRtiId(), number));
            } else if (row.truckRefId() == null || row.truckRefId() <= 0) {
                skipped.add(skip(row, SkipReason.NO_TRUCK, "No truck on this row.", 0, ""));
            } else {
                skipped.add(skip(row, SkipReason.NOT_CONFIRMED,
                        "This truck was not ticked — no RTI was created for it.", 0, ""));
            }
        }

        int jobsCreated = created.stream().mapToInt(PlanningRtiBatchResult.Created::jobCount).sum();
        PlanningRtiBatchResult result = new PlanningRtiBatchResult(
                header.id(), header.planningNo(), rows.size(), jobsCreated, skipped.size(), created, skipped);

        if (!result.isComplete()) {
            // Rolls the whole batch back: better no RTIs than an unexplained gap.
            // Naming the rows that were never accounted for makes the gap
            // findable, instead of leaving three numbers that do not add up.
            Set<Integer> accounted = new LinkedHashSet<>();
            skipped.forEach(entry -> accounted.add(entry.planningDetailId()));
            for (PlanningRtiBatchRequest.Group group : request.getGroups()) {
                accounted.addAll(group.getSaleOrderMasterRefIds() == null
                        ? List.of()
                        : group.getSaleOrderMasterRefIds());
            }
            List<Integer> unexplained = rows.stream()
                    .filter(row -> !accounted.contains(row.planningDetailId())
                            && !accounted.contains(row.saleOrderMasterRefId()))
                    .map(PlanRow::planningDetailId)
                    .toList();
            throw new IllegalStateException("RTI batch lost a job on plan " + header.planningNo()
                    + ": planned=" + rows.size() + " created=" + jobsCreated
                    + " skipped=" + skipped.size() + "; unaccounted planning rows " + unexplained);
        }
        log.info("PLAN_RTI_BATCH: plan {} created {} RTI covering {} job(s), {} skipped{}",
                header.planningNo(), created.size(), jobsCreated, skipped.size(),
                request.isAllowDuplicates() ? " (second RTI allowed by the planner)" : "");
        return result;
    }

    // ───────────────────────── grouping and drivers ─────────────────────────

    /**
     * Splits the plan into rows that can become RTI lines and rows that cannot.
     *
     * <p>A job already on an active RTI is normally set aside, which is what
     * stops a second press doubling the day's paperwork. When the planner asks
     * for those jobs, they come through instead, still carrying the number of the
     * RTI they are already on so the dialog can say so out loud.
     */
    private List<PlanRow> partition(List<PlanRow> rows, List<PlanningRtiSkip> skipped, boolean includeExisting) {
        List<PlanRow> creatable = new ArrayList<>();
        Set<Integer> seenJobs = new HashSet<>();
        for (PlanRow row : rows) {
            Integer jobId = row.saleOrderMasterRefId();
            if (jobId == null || jobId <= 0) {
                skipped.add(skip(row, SkipReason.NO_JOB_REFERENCE,
                        "This row has no job reference — save the plan first.", 0, ""));
            } else if (!seenJobs.add(jobId)) {
                skipped.add(skip(row, SkipReason.DUPLICATE_IN_PLAN,
                        "This job is on the plan more than once; it goes on one RTI line.", 0, ""));
            } else if (!includeExisting && row.existingRtiId() != null && row.existingRtiId() > 0) {
                skipped.add(skip(row, SkipReason.ALREADY_IN_RTI,
                        "Already in RTI " + row.existingRtiNo(), row.existingRtiId(), row.existingRtiNo()));
            } else if (row.truckRefId() == null || row.truckRefId() <= 0) {
                skipped.add(skip(row, SkipReason.NO_TRUCK,
                        "No truck on this row — assign one and try again.", 0, ""));
            } else {
                creatable.add(row);
            }
        }
        return creatable;
    }

    /**
     * Groups rows into RTIs-to-be: one RTI per truck and driver.
     *
     * <p>Every job that the same truck and the same driver carry goes onto one
     * RTI, whatever days they fall on. That is the planner's own rule, and it is
     * also what {@code RTIMaster} can hold — one truck, one driver.
     *
     * <p>Within a truck, a driver typed on any row applies to the rest: planners
     * name the driver once and leave the other rows blank. Two different drivers
     * typed on the same truck split into two RTIs, because the record cannot
     * hold both, and rows with no driver at all then form their own group rather
     * than being handed to one of them on a guess.
     */
    private List<PlanningRtiGroup> group(List<PlanRow> rows, Context context) {
        Map<Integer, List<PlanRow>> byTruck = new LinkedHashMap<>();
        for (PlanRow row : rows) {
            byTruck.computeIfAbsent(row.truckRefId(), key -> new ArrayList<>()).add(row);
        }

        List<PlanningRtiGroup> groups = new ArrayList<>();
        for (Map.Entry<Integer, List<PlanRow>> truck : byTruck.entrySet()) {
            String truckKey = String.valueOf(truck.getKey());
            List<PlanRow> truckRows = truck.getValue();

            Map<String, ResolvedDriver> explicit = new LinkedHashMap<>();
            for (PlanRow row : truckRows) {
                ResolvedDriver resolved = resolveTypedDriver(row, context);
                if (resolved != null) {
                    explicit.putIfAbsent(resolved.key(), resolved);
                }
            }

            if (explicit.size() == 1) {
                // One driver named anywhere on this truck covers all of its jobs.
                groups.addAll(splitIntoTrips(truckKey, truckRows,
                        explicit.values().iterator().next(), context, List.of()));
                continue;
            }
            if (explicit.isEmpty()) {
                groups.addAll(splitIntoTrips(truckKey, truckRows,
                        suggestDriver(truckRows.get(0), context), context, List.of()));
                continue;
            }

            Map<String, List<PlanRow>> byDriver = new LinkedHashMap<>();
            List<PlanRow> unnamed = new ArrayList<>();
            for (PlanRow row : truckRows) {
                ResolvedDriver resolved = resolveTypedDriver(row, context);
                if (resolved == null) {
                    unnamed.add(row);
                } else {
                    byDriver.computeIfAbsent(resolved.key(), key -> new ArrayList<>()).add(row);
                }
            }
            for (Map.Entry<String, List<PlanRow>> driverRows : byDriver.entrySet()) {
                groups.addAll(splitIntoTrips(truckKey + "|" + driverRows.getKey(), driverRows.getValue(),
                        explicit.get(driverRows.getKey()), context, List.of()));
            }
            if (!unnamed.isEmpty()) {
                groups.addAll(splitIntoTrips(truckKey + "|?", unnamed,
                        suggestDriver(unnamed.get(0), context), context,
                        List.of("This truck has more than one driver on the plan — check which one these jobs belong to.")));
            }
        }

        groups.sort(Comparator
                .comparing((PlanningRtiGroup group) -> safe(group.truckName()))
                .thenComparing(group -> safe(group.tripLabel())));
        return groups;
    }

    /**
     * Splits one truck-and-driver's jobs into the trips the planner wrote.
     *
     * <p>"1ST TRIP" and "2ND TRIP" in the REMARKS column are the two most common
     * remarks in the system. They mean the truck runs a load, comes back, and
     * runs another — two runs, two RTIs — so they are honoured rather than merged
     * into one sheet.
     *
     * <p>"COMBINE" means a load rides with another one, and the one it rides with
     * is the load going the same way: same origin, same destination. So a COMBINE
     * row joins the trip whose lane it shares, even when that trip is written
     * lower down the plan. Failing a lane match it joins the nearest trip above
     * it, and failing that it stands on its own.
     *
     * <p>Rows with no trip written stay together, which is how every plan without
     * trip markers behaves.
     */
    private List<PlanningRtiGroup> splitIntoTrips(String keyPrefix, List<PlanRow> rows,
                                                  ResolvedDriver driver, Context context,
                                                  List<String> extraWarnings) {
        List<PlanRow> ordered = rows.stream()
                .sorted(Comparator.comparing((PlanRow row) -> row.sortBy() == null ? 0 : row.sortBy())
                        .thenComparing(row -> row.planningDetailId() == null ? 0 : row.planningDetailId()))
                .toList();

        Map<Integer, List<PlanRow>> byTrip = new LinkedHashMap<>();
        List<PlanRow> combines = new ArrayList<>();
        List<PlanRow> unmarked = new ArrayList<>();
        for (PlanRow row : ordered) {
            int trip = TripMarkers.tripNumber(row.remarks());
            if (trip > 0) {
                byTrip.computeIfAbsent(trip, key -> new ArrayList<>()).add(row);
            } else if (TripMarkers.isCombine(row.remarks())) {
                combines.add(row);
            } else {
                unmarked.add(row);
            }
        }

        for (PlanRow row : combines) {
            Integer trip = tripSharingLane(row, byTrip);
            if (trip == null) {
                trip = nearestTripAbove(row, ordered);
            }
            if (trip == null) {
                unmarked.add(row);
            } else {
                byTrip.get(trip).add(row);
            }
        }

        List<PlanningRtiGroup> groups = new ArrayList<>();
        for (Map.Entry<Integer, List<PlanRow>> trip : new java.util.TreeMap<>(byTrip).entrySet()) {
            groups.add(withTrip(
                    buildGroup(keyPrefix + "|t" + trip.getKey(), trip.getValue(), driver, context),
                    TripMarkers.label(trip.getKey()), extraWarnings));
        }
        if (!unmarked.isEmpty()) {
            groups.add(withTrip(
                    buildGroup(byTrip.isEmpty() ? keyPrefix : keyPrefix + "|t0", unmarked, driver, context),
                    "", extraWarnings));
        }
        return groups;
    }

    /** The trip already holding a job that runs the same lane as this one. */
    private static Integer tripSharingLane(PlanRow row, Map<Integer, List<PlanRow>> byTrip) {
        String lane = laneKey(row);
        if (lane.isBlank()) {
            return null;
        }
        for (Map.Entry<Integer, List<PlanRow>> trip : byTrip.entrySet()) {
            for (PlanRow candidate : trip.getValue()) {
                if (lane.equals(laneKey(candidate))) {
                    return trip.getKey();
                }
            }
        }
        return null;
    }

    /** The trip written on the closest row above this one in the plan's order. */
    private static Integer nearestTripAbove(PlanRow row, List<PlanRow> ordered) {
        Integer found = null;
        for (PlanRow candidate : ordered) {
            if (candidate == row) {
                return found;
            }
            int trip = TripMarkers.tripNumber(candidate.remarks());
            if (trip > 0) {
                found = trip;
            }
        }
        return found;
    }

    /** Origin and destination as one comparable key, short forms folded together. */
    private static String laneKey(PlanRow row) {
        String from = NameKeys.place(safe(row.origin()));
        String to = NameKeys.place(safe(row.destination()));
        return from.isBlank() && to.isBlank() ? "" : from + ">" + to;
    }

    /** Re-stamps a built group with its trip name and any extra warnings. */
    private static PlanningRtiGroup withTrip(PlanningRtiGroup group, String tripLabel, List<String> extra) {
        List<String> warnings = new ArrayList<>(group.warnings());
        warnings.addAll(extra);
        return new PlanningRtiGroup(group.groupKey(), group.truckRefId(), group.truckName(),
                group.driverRefId(), group.driverName(), group.driverSource(), group.outsideDriver(),
                group.pickupDate(), tripLabel, group.jobs(), warnings);
    }

    private PlanningRtiGroup buildGroup(String groupKey, List<PlanRow> rows, ResolvedDriver driver, Context context) {
        return buildGroup(groupKey, rows, driver, context, unmatchedNames(rows, context));
    }

    /**
     * Driver names the plan carries that no single active driver answers to.
     *
     * <p>Shown rather than resolved: "SATHISH" and "ANBU" are real people to the
     * planner and nobody at all to the master, and the difference matters when
     * the document being created is the one a driver is paid from.
     */
    private List<String> unmatchedNames(List<PlanRow> rows, Context context) {
        List<String> names = new ArrayList<>();
        for (PlanRow row : rows) {
            String typed = safe(row.driverName()).trim();
            if (!typed.isBlank() && resolveTypedDriver(row, context) == null && !names.contains(typed)) {
                names.add(typed);
            }
        }
        return names;
    }

    private PlanningRtiGroup buildGroup(String groupKey, List<PlanRow> rows, ResolvedDriver driver,
                                        Context context, List<String> unmatched) {
        PlanRow first = rows.get(0);
        List<PlanningRtiJob> jobs = rows.stream()
                .sorted(Comparator.comparing((PlanRow row) -> row.sortBy() == null ? 0 : row.sortBy())
                        .thenComparing(row -> safe(row.pickupDate())))
                .map(row -> new PlanningRtiJob(
                        row.planningDetailId(), row.saleOrderMasterRefId(), row.jobNo(), row.customerName(),
                        row.origin(), row.destination(), row.pickupDate(), row.deliveryDate(), row.sortBy(),
                        safe(row.remarks()), row.existingRtiId(), safe(row.existingRtiNo()),
                        safe(row.existingRtiDate())))
                .toList();

        List<String> warnings = new ArrayList<>();
        DriverSource source = driver == null ? DriverSource.NONE : driver.source();
        Integer driverId = driver == null ? 0 : driver.driverRefId();
        String driverName = driver == null ? "" : driver.driverName();

        for (String typed : unmatched) {
            warnings.add("The plan says \"" + typed + "\" — no single driver in the master answers to that name.");
        }
        if (driver != null && driver.matchedFrom() != null && !driver.matchedFrom().isBlank()) {
            warnings.add("Read \"" + driver.matchedFrom() + "\" on the plan as " + driverName + ".");
        }
        // Naming the date matters: the RTI a job was on is usually weeks old and
        // belongs to another day's run, so "already has an RTI" on its own reads
        // as though today's work were already done.
        List<String> alreadyOn = rows.stream()
                .filter(row -> row.existingRtiId() != null && row.existingRtiId() > 0)
                .map(row -> safe(row.existingRtiNo())
                        + (safe(row.existingRtiDate()).isBlank() ? "" : " of " + row.existingRtiDate()))
                .filter(text -> !text.isBlank())
                .distinct()
                .toList();
        if (!alreadyOn.isEmpty()) {
            warnings.add("Some of these jobs were already on " + String.join(", ", alreadyOn)
                    + ". That RTI stays; this creates a new one.");
        }
        if (source == DriverSource.NONE) {
            warnings.add(unmatched.isEmpty()
                    ? "No driver on the plan and no recent trip for this truck — pick a driver."
                    : "Pick the driver for this truck.");
        } else if (source == DriverSource.LAST_TRIP) {
            warnings.add("Driver suggested from this truck's last trip"
                    + (driver.lastRun() == null || driver.lastRun().isBlank() ? "" : " on " + driver.lastRun())
                    + " — check it before saving.");
        }
        // The RTI's date line shows the first day this truck and driver work.
        String earliestDay = rows.stream()
                .map(row -> safe(row.pickupDay()))
                .filter(day -> !day.isBlank())
                .min(Comparator.naturalOrder())
                .orElse(safe(first.pickupDay()));

        return new PlanningRtiGroup(groupKey, first.truckRefId(), first.truckName(),
                driverId, driverName, source,
                source == DriverSource.OUTSIDE ? driverName : "",
                earliestDay, "", jobs, warnings);
    }

    /**
     * The driver named on the row itself, or null when the row does not name one
     * the master can confirm.
     *
     * <p>Planners type first names: "KESAVAN", "UGUNTHAN", "SATHISH". Two rules
     * resolve those, and both demand a single unambiguous answer:
     * <ol>
     *   <li>the full name matches a driver outright, or</li>
     *   <li>the typed text is the whole <em>first name</em> of exactly one active
     *       driver — "KESAVAN" is KESAVAN A/L TAMILSALVAN.</li>
     * </ol>
     *
     * <p>Anything looser is refused on purpose. "NAVIN" is a character prefix of
     * both NAVINDREN and NAVINA, and "RAVI" of RAVINDRAN; guessing there would
     * put a real person's name on another person's pay document. Those rows come
     * back unresolved, with the typed text shown, for the planner to settle.
     */
    private ResolvedDriver resolveTypedDriver(PlanRow row, Context context) {
        Integer id = row.driverRefId();
        if (id != null && id > 0) {
            if (context.outsidePlaceholderIds().contains(id)) {
                // The planning screen's driver modal stores the placeholder id and
                // the typed person's name; that is an outside driver, stated.
                return outsideDriver(safe(row.driverName()), context);
            }
            DriverRow master = context.driverById().get(id);
            if (master != null) {
                return new ResolvedDriver("D:" + id, id, master.name(), DriverSource.PLAN, null, null);
            }
        }

        String typed = safe(row.driverName()).trim();
        if (typed.isBlank()) {
            return null;
        }
        String key = NameKeys.driver(typed);
        if (key.isBlank()) {
            return null;
        }
        if (key.equals(NameKeys.driver(OUTSIDE_DRIVER_KEY))) {
            return outsideDriver(typed, context);
        }

        DriverRow exact = single(context.driversByKey().get(key));
        if (exact != null) {
            return new ResolvedDriver("D:" + exact.id(), exact.id(), exact.name(), DriverSource.PLAN, null, null);
        }

        DriverRow byFirstName = single(context.driversByFirstName().get(key));
        if (byFirstName != null) {
            // Resolved, but say out loud what was read into the shorthand.
            return new ResolvedDriver("D:" + byFirstName.id(), byFirstName.id(), byFirstName.name(),
                    DriverSource.PLAN, null, typed);
        }

        return null;
    }

    /**
     * The one driver these rows mean, or null when they mean more than one.
     *
     * <p>The master holds duplicate rows for the same person (KEVIN RAJ A/L
     * KRISHNAN is there twice). Same name is the same person, so that is not an
     * ambiguity — the oldest id wins. Two different names is.
     */
    private static DriverRow single(List<DriverRow> matches) {
        if (matches == null || matches.isEmpty()) {
            return null;
        }
        DriverRow best = matches.get(0);
        for (DriverRow candidate : matches) {
            if (!NameKeys.driver(candidate.name()).equals(NameKeys.driver(best.name()))) {
                return null;
            }
            if (candidate.id() < best.id()) {
                best = candidate;
            }
        }
        return best;
    }

    private ResolvedDriver outsideDriver(String typedName, Context context) {
        Integer placeholder = context.outsidePlaceholderIds().stream().min(Integer::compareTo).orElse(0);
        String name = safe(typedName).trim();
        return new ResolvedDriver("O:" + NameKeys.driver(name), placeholder, name,
                DriverSource.OUTSIDE, null, null);
    }

    /** Nobody named this truck's driver, so offer the one who last ran it. */
    private ResolvedDriver suggestDriver(PlanRow row, Context context) {
        LastDriver last = context.lastDriverByTruck().get(row.truckRefId());
        if (last == null) {
            return null;
        }
        return new ResolvedDriver("D:" + last.driverRefId(), last.driverRefId(), last.driverName(),
                DriverSource.LAST_TRIP, last.lastRun(), null);
    }

    // ───────────────────────────── payload ─────────────────────────────

    /**
     * Builds one RTI exactly as the RTI screen would, minus the parts a person
     * owns: salary stays null and every allowance switch stays at its default.
     */
    private RTIMasterDto buildRtiPayload(PlanningRtiBatchRequest.Group group,
                                         List<PlanRow> jobRows,
                                         PlanHeader header,
                                         PlanningRtiBatchRequest request,
                                         LocalDateTime now) {
        RTIMasterDto master = new RTIMasterDto();
        master.setCompanyRefId(request.getCompanyRefId());
        master.setSaleDate(now);
        master.setEmployeeRefId(request.getEmployeeRefId() != null && request.getEmployeeRefId() > 0
                ? request.getEmployeeRefId()
                : header.employeeRefId());
        master.setLastEmployeeRefId(master.getEmployeeRefId());
        master.setUserRefId(request.getUserRefId());
        master.setTruckRefId(group.getTruckRefId());
        master.setDriverRefId(group.getDriverRefId());
        master.setOutsideDriver(trimToEmpty(group.getOutsideDriver()));
        master.setOutsideTruck(trimToEmpty(group.getOutsideTruck()));
        master.setActive(1);

        // Nothing is claimed on an RTI the moment it is created - a person fills
        // the allowances in afterwards. These columns are NOT NULL in the table,
        // so "nothing" has to be written as zero, never left null.
        master.setAmount(0.0);
        master.setSleeping(0);
        master.setSleepingAmount(0.0);
        master.setPickup(0);
        master.setPickupCount(0);
        master.setPickupAmount(0.0);
        master.setDropCount(0);
        master.setDropAmount(0.0);
        master.setAddDrop(0);
        master.setExitYN(0);
        master.setExitAmount(0);

        master.setCreatedBy("PLAN " + safe(header.planningNo()));
        master.setModifiedBy(master.getCreatedBy());
        master.setRemarks("From plan " + safe(header.planningNo()));

        List<RTIDetailsDto> details = new ArrayList<>();
        for (PlanRow row : jobRows) {
            RTIDetailsDto detail = new RTIDetailsDto();
            detail.setSaleOrderMasterRefId(row.saleOrderMasterRefId());
            detail.setPwdType(0);
            detail.setPickupDateD(parseDateTime(row.pickupDate()));
            detail.setDeliveryDateD(parseDateTime(row.deliveryDate()));
            detail.setOriginD(trimToEmpty(row.origin()));
            detail.setDestinationD(trimToEmpty(row.destination()));
            detail.setPickupAddressD(trimToEmpty(row.pickupAddress()));
            detail.setDeliveryAddressD(trimToEmpty(row.deliveryAddress()));
            detail.setPickupAddressTimelistD(trimToEmpty(row.pickupTimeList()));
            detail.setPickupAddressQuantityD(trimToEmpty(row.pickupQuantityList()));
            detail.setDeliveryAddressQuantityD(trimToEmpty(row.deliveryQuantityList()));
            detail.setDeliveryAddressdatelistD(trimToEmpty(row.deliveryTimeList()));
            // Salary, PPIC and DPIC are left for a person to enter on the RTI screen.
            details.add(detail);
        }
        master.setRtiDetails(details);
        return master;
    }

    // ───────────────────────────── helpers ─────────────────────────────

    private Context loadContext(Integer companyRefId) {
        List<DriverRow> drivers = reader.activeDrivers(companyRefId);
        Map<Integer, DriverRow> byId = new HashMap<>();
        Map<String, List<DriverRow>> byKey = new HashMap<>();
        Map<String, List<DriverRow>> byFirstName = new HashMap<>();
        Set<Integer> outsideIds = new LinkedHashSet<>();
        String outsideKey = NameKeys.driver(OUTSIDE_DRIVER_KEY);
        for (DriverRow driver : drivers) {
            byId.put(driver.id(), driver);
            String key = NameKeys.driver(driver.name());
            if (key.equals(outsideKey)) {
                outsideIds.add(driver.id());
            } else {
                byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(driver);
                // First-name index: "KESAVAN" reaches KESAVAN A/L TAMILSALVAN, and
                // only when no other active driver shares that first name.
                int space = key.indexOf(' ');
                if (space > 0) {
                    byFirstName.computeIfAbsent(key.substring(0, space), k -> new ArrayList<>()).add(driver);
                }
            }
        }

        return new Context(
                byId,
                byKey,
                byFirstName,
                outsideIds,
                reader.lastDriverByTruck(companyRefId, LocalDate.now().minusDays(DRIVER_HISTORY_DAYS)));
    }

    private PlanHeader requirePlan(Integer planningId, Integer companyRefId) {
        if (planningId == null || planningId <= 0) {
            throw new InvalidRequestException("Save the plan before creating RTI from it.");
        }
        if (companyRefId == null || companyRefId <= 0) {
            throw new InvalidRequestException("Company is required.");
        }
        PlanHeader header = reader.planHeader(planningId, companyRefId);
        if (header == null) {
            throw new InvalidRequestException("Plan not found for this company.");
        }
        return header;
    }

    /**
     * Blocks a second simultaneous batch for the same plan.
     *
     * <p>Held by the transaction and released with it, and living in the database
     * rather than the JVM so it still works with more than one app instance.
     * A failure to take it never blocks the save — the duplicate check still
     * runs, it just loses its guard against an identical twin.
     */
    private void lockPlan(Integer planningId, Integer companyRefId) {
        try {
            jdbc.queryForObject(
                    "DECLARE @status int; "
                            + "EXEC @status = sp_getapplock @Resource = :key, "
                            + "@LockMode = 'Exclusive', @LockOwner = 'Transaction', "
                            + "@LockTimeout = 15000; SELECT @status",
                    new MapSqlParameterSource("key", "PlanningRtiBatch:" + companyRefId + ":" + planningId),
                    Integer.class);
        } catch (Exception ex) {
            log.warn("Could not take the plan RTI batch lock ({}); continuing unguarded", ex.getMessage());
        }
    }

    private PlanningRtiSkip skip(PlanRow row, SkipReason reason, String message,
                                 Integer existingRtiId, String existingRtiNo) {
        return new PlanningRtiSkip(row.planningDetailId(), row.saleOrderMasterRefId(), row.jobNo(),
                row.customerName(), row.truckName(), reason, message,
                existingRtiId == null ? 0 : existingRtiId, safe(existingRtiNo));
    }

    private static LocalDateTime parseDateTime(String value) {
        String text = safe(value);
        if (text.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, WIRE_DATE_TIME);
        } catch (Exception ignored) {
            try {
                return LocalDate.parse(text.substring(0, Math.min(10, text.length()))).atStartOfDay();
            } catch (Exception alsoIgnored) {
                return null;
            }
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /** Master data the grouping needs, read once per request. */
    private record Context(
            Map<Integer, DriverRow> driverById,
            Map<String, List<DriverRow>> driversByKey,
            Map<String, List<DriverRow>> driversByFirstName,
            Set<Integer> outsidePlaceholderIds,
            Map<Integer, LastDriver> lastDriverByTruck) {
    }

    /**
     * @param matchedFrom the shorthand the plan actually carried, when the driver
     *                    was reached by first name rather than in full
     */
    private record ResolvedDriver(String key, Integer driverRefId, String driverName,
                                  DriverSource source, String lastRun, String matchedFrom) {
    }
}
