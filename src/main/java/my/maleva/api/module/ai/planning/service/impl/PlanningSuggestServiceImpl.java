package my.maleva.api.module.ai.planning.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.PlanningSuggestProperties;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.ai.common.NameKeys;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestFeedbackRequest;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestRequest;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestResponse;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestResponse.Pick;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestResponse.PlanWarning;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestResponse.RowSuggestion;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestResponse.RowWarning;
import my.maleva.api.module.ai.planning.repository.PlanningHistoryReader;
import my.maleva.api.module.ai.planning.repository.PlanningHistoryReader.AssignmentRow;
import my.maleva.api.module.ai.planning.repository.PlanningHistoryReader.HistoryRow;
import my.maleva.api.module.ai.planning.repository.PlanningHistoryReader.SuggestionLogRow;
import my.maleva.api.module.ai.planning.service.PlanningSuggestService;
import my.maleva.api.module.ai.planning.service.impl.AssignmentScorer.DaySituation;
import my.maleva.api.module.ai.planning.service.impl.AssignmentScorer.DriverInfo;
import my.maleva.api.module.ai.planning.service.impl.AssignmentScorer.HistoryEvent;
import my.maleva.api.module.ai.planning.service.impl.AssignmentScorer.Job;
import my.maleva.api.module.ai.planning.service.impl.AssignmentScorer.TruckInfo;
import my.maleva.api.module.ai.planning.service.impl.TripChainer.JobStop;
import my.maleva.api.module.ai.planning.service.impl.TripChainer.Trip;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Plans a day the way the planners do: chain the jobs into trips so a truck
 * keeps moving loaded, then give each trip one truck and one driver, scored
 * from the planning history and what is already booked that day. Availability
 * comes from saved plans only; leave and workshop status plug in here later,
 * and port passes are deliberately ignored because that data is not maintained.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanningSuggestServiceImpl implements PlanningSuggestService {

    static final String LEVEL_INFO = "info";
    static final String LEVEL_WARNING = "warning";
    static final String LEVEL_ERROR = "error";

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PlanningSuggestProperties props;
    private final PlanningHistoryReader reader;
    private final SaleOrderMasterRepository saleOrderRepository;
    private final CustomerRepository customerRepository;
    private final TruckMasterRepository truckRepository;
    private final DriverMasterRepository driverRepository;

    private Clock clock = Clock.system(ZoneId.of("Asia/Kuala_Lumpur"));

    /** Visible for tests. */
    void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public PlanningSuggestResponse suggest(PlanningSuggestRequest request) {
        Integer companyId = request.getCompanyRefId();
        if (companyId == null) {
            throw new InvalidRequestException("companyRefId is required");
        }
        List<PlanningSuggestRequest.Row> rows = request.getRows() == null ? List.of() : request.getRows();
        if (rows.isEmpty()) {
            throw new InvalidRequestException("Load jobs into the planning grid first");
        }
        boolean replace = request.isReplaceExisting();

        // --- the jobs -------------------------------------------------------
        Set<Integer> jobIds = new HashSet<>();
        for (PlanningSuggestRequest.Row row : rows) {
            Integer id = positive(row.getSaleOrderMasterRefId());
            if (id != null) {
                jobIds.add(id);
            }
        }
        Map<Integer, SaleOrderMaster> orders = new HashMap<>();
        if (!jobIds.isEmpty()) {
            for (SaleOrderMaster order : saleOrderRepository.findAllById(jobIds)) {
                orders.put(order.getId(), order);
            }
        }
        LocalDate planningDate = resolvePlanningDate(request, rows, orders);

        Set<Integer> customerIds = new HashSet<>();
        for (SaleOrderMaster order : orders.values()) {
            if (order.getCustomerRefId() != null) {
                customerIds.add(order.getCustomerRefId());
            }
        }
        Map<Integer, String> customerNames = new HashMap<>();
        if (!customerIds.isEmpty()) {
            for (Customer customer : customerRepository.findAllById(customerIds)) {
                customerNames.put(customer.getId(), customer.getCustomerName());
            }
        }

        // --- the fleet ------------------------------------------------------
        List<TruckMaster> trucks = truckRepository.findByCompanyRefIdAndActive(companyId, 1);
        List<DriverMaster> allDrivers = driverRepository.findByCompanyRefId(companyId);
        Map<String, Integer> driverIdByNameKey = driverIdsByNameKey(allDrivers);
        Map<Integer, TruckInfo> truckInfos = new LinkedHashMap<>();
        for (TruckMaster truck : trucks) {
            truckInfos.put(truck.getId(), truckInfo(truck, planningDate));
        }
        Map<Integer, DriverInfo> driverInfos = new LinkedHashMap<>();
        Map<Integer, String> driverNames = new HashMap<>();
        for (DriverMaster driver : allDrivers) {
            driverNames.put(driver.getId(), driver.getDriverName());
            if (driver.getActive() != null && driver.getActive() == 1) {
                driverInfos.put(driver.getId(), driverInfo(driver, planningDate));
            }
        }

        // --- history and the day's situation --------------------------------
        LocalDate historyFrom = planningDate.minusDays(props.getHistoryDays());
        List<HistoryEvent> events = new ArrayList<>();
        for (HistoryRow row : reader.history(companyId, historyFrom, planningDate)) {
            if (jobIds.contains(row.saleOrderMasterRefId())) {
                continue; // the jobs being planned are not evidence for themselves
            }
            Integer driverId = positive(row.driverId());
            if (driverId == null) {
                driverId = driverIdByNameKey.get(NameKeys.driver(row.driverName()));
            }
            events.add(new HistoryEvent(row.planDate(), positive(row.truckId()), driverId, positive(row.customerRefId()),
                    NameKeys.place(firstNonBlank(row.originD(), row.origin())),
                    NameKeys.place(firstNonBlank(row.destinationD(), row.destination())),
                    NameKeys.place(row.sPort()), NameKeys.place(row.oPort())));
        }

        Map<Integer, Integer> jobsTodayByTruck = new HashMap<>();
        Map<Integer, Set<Integer>> trucksTodayByDriver = new HashMap<>();
        Map<Integer, Integer> driverTodayByTruck = new HashMap<>();
        Map<Integer, String> lastDestinationByTruck = new HashMap<>();
        Map<Integer, String> lastDestinationByDriver = new HashMap<>();
        if (props.getContinuityDays() > 0) {
            for (AssignmentRow previous : reader.assignments(companyId, planningDate.minusDays(props.getContinuityDays()),
                    planningDate.minusDays(1))) {
                String destination = NameKeys.place(previous.destinationD());
                if (destination.isEmpty()) {
                    continue;
                }
                Integer truckId = positive(previous.truckId());
                if (truckId != null) {
                    lastDestinationByTruck.put(truckId, destination); // rows arrive date-ascending, last write wins
                }
                Integer driverId = driverIdOf(previous.driverId(), previous.driverName(), driverIdByNameKey);
                if (driverId != null) {
                    lastDestinationByDriver.put(driverId, destination);
                }
            }
        }
        for (AssignmentRow assignment : reader.assignments(companyId, planningDate, planningDate)) {
            boolean thisPlan = request.getPlanningMasterId() != null
                    && request.getPlanningMasterId().equals(assignment.planningMasterId());
            if (thisPlan || jobIds.contains(assignment.saleOrderMasterRefId())) {
                continue; // the grid's own rows are counted from the request below
            }
            Integer truckId = positive(assignment.truckId());
            countAssignment(jobsTodayByTruck, trucksTodayByDriver, driverTodayByTruck, truckId,
                    driverIdOf(assignment.driverId(), assignment.driverName(), driverIdByNameKey));
            String destination = NameKeys.place(assignment.destinationD());
            if (truckId != null && !destination.isEmpty()) {
                lastDestinationByTruck.put(truckId, destination); // today's saved stops come after yesterday's
            }
        }
        for (PlanningSuggestRequest.Row row : rows) {
            Integer typedTruck = replace ? null : positive(row.getTruckRefId());
            Integer typedDriver = replace ? null : driverIdOf(row.getDriverRefId(), row.getDriverName(), driverIdByNameKey);
            countAssignment(jobsTodayByTruck, trucksTodayByDriver, driverTodayByTruck, typedTruck, typedDriver);
        }
        DaySituation day = new DaySituation(jobsTodayByTruck, lastDestinationByTruck, lastDestinationByDriver,
                trucksTodayByDriver, driverTodayByTruck);
        AssignmentScorer scorer = new AssignmentScorer(props);

        // --- chain the jobs into trips --------------------------------------
        Map<String, PlanningSuggestRequest.Row> rowsByKey = new LinkedHashMap<>();
        Map<String, RowSuggestion> results = new LinkedHashMap<>();
        List<JobStop> stops = new ArrayList<>();
        for (PlanningSuggestRequest.Row row : rows) {
            String key = row.getRowKey() == null ? String.valueOf(rowsByKey.size()) : row.getRowKey();
            rowsByKey.put(key, row);
            Integer saleOrderId = positive(row.getSaleOrderMasterRefId());
            SaleOrderMaster order = saleOrderId == null ? null : orders.get(saleOrderId);
            if (order == null) {
                List<RowWarning> warnings = new ArrayList<>();
                warnings.add(new RowWarning("JOB_NOT_FOUND", LEVEL_ERROR,
                        "Sale order " + row.getSaleOrderMasterRefId() + " was not found"));
                results.put(key, RowSuggestion.builder().rowKey(key).saleOrderMasterRefId(saleOrderId)
                        .skipped(true).skipReason("Sale order not found")
                        .alternativeTrucks(List.of()).alternativeDrivers(List.of()).warnings(warnings).build());
                continue;
            }
            LocalDateTime pickup = parseDateTime(row.getPickupDate());
            if (pickup == null) {
                pickup = order.getPickupDate();
            }
            stops.add(new JobStop(key, saleOrderId, NameKeys.place(order.getOrigin()), NameKeys.place(order.getDestination()),
                    order.getOrigin(), order.getDestination(), pickup, order.getDeliveryDate(),
                    replace ? null : positive(row.getTruckRefId())));
        }
        List<Trip> trips = TripChainer.chain(stops, props.getMaxJobsPerTrip());

        // --- one truck and one driver per trip -------------------------------
        Map<Integer, Map<Integer, List<String>>> rowKeysByDriverAndTruck = new HashMap<>();
        List<SuggestionLogRow> logRows = new ArrayList<>();
        int sortBy = 1;
        for (Trip trip : trips) {
            List<Job> jobs = new ArrayList<>();
            Integer typedDriverInTrip = null;
            for (JobStop stop : trip.jobs()) {
                SaleOrderMaster order = orders.get(stop.saleOrderId());
                jobs.add(new Job(stop.saleOrderId(), order.getCustomerRefId(), customerNames.get(order.getCustomerRefId()),
                        stop.originKey(), stop.destinationKey(), NameKeys.place(order.getSPort()),
                        NameKeys.place(order.getOPort()), planningDate));
                if (typedDriverInTrip == null && !replace) {
                    typedDriverInTrip = driverIdOf(rowsByKey.get(stop.rowKey()).getDriverRefId(),
                            rowsByKey.get(stop.rowKey()).getDriverName(), driverIdByNameKey);
                }
            }

            // Truck: the planner's choice anchors the trip, otherwise the best-scoring truck.
            Integer truckId = trip.anchorTruckId();
            Pick truckPick = null;
            List<Pick> truckPicks = List.of();
            if (truckId == null) {
                truckPicks = scorer.rankTrucks(jobs, truckInfos.values(), events, day);
                truckPick = truckPicks.isEmpty() ? null : truckPicks.get(0);
                truckId = truckPick == null ? null : truckPick.id();
            } else {
                // Jobs chained onto a truck the planner already chose ride on that truck.
                TruckInfo info = truckInfos.get(truckId);
                truckPick = new Pick(truckId, info == null ? "Truck " + truckId : info.name(), 100,
                        List.of("Continues the trip already planned on this truck"));
            }

            // Driver: a truck keeps one driver for the day - a driver typed on any
            // row of the trip, or already in the truck from a saved plan or an
            // earlier trip, takes the whole trip. Only otherwise is one ranked.
            Integer driverId = typedDriverInTrip;
            Pick driverPick = null;
            List<Pick> driverPicks = List.of();
            if (driverId == null && truckId != null) {
                Integer inTruck = day.driverTodayByTruck().get(truckId);
                DriverInfo info = inTruck == null ? null : driverInfos.get(inTruck);
                if (info != null && info.eligible()) {
                    driverPick = new Pick(info.id(), info.name(), 100, List.of("Already driving this truck that day"));
                    driverId = info.id();
                }
            }
            if (driverId == null) {
                driverPicks = scorer.rankDrivers(jobs, truckId, driverInfos.values(), events, day);
                driverPick = driverPicks.isEmpty() ? null : driverPicks.get(0);
                driverId = driverPick == null ? null : driverPick.id();
            } else if (driverPick == null) {
                DriverInfo info = driverInfos.get(driverId);
                if (info != null) {
                    driverPick = new Pick(info.id(), info.name(), 100, List.of("Already driving this truck that day"));
                }
            }

            // Book the trip so later trips see the truck, its driver and where it ends.
            if (truckId != null) {
                day.jobsTodayByTruck().merge(truckId, trip.jobs().size(), Integer::sum);
                if (!trip.last().destinationKey().isEmpty()) {
                    day.lastDestinationByTruck().put(truckId, trip.last().destinationKey());
                }
                if (driverId != null) {
                    if (claimsTruck(day.trucksTodayByDriver(), driverId, truckId)) {
                        day.driverTodayByTruck().putIfAbsent(truckId, driverId);
                    }
                    day.lastDestinationByDriver().put(driverId, trip.last().destinationKey());
                }
            }

            String label = trip.label();
            int position = 1;
            for (JobStop stop : trip.jobs()) {
                PlanningSuggestRequest.Row row = rowsByKey.get(stop.rowKey());
                List<RowWarning> warnings = new ArrayList<>();
                Integer existingTruck = replace ? null : positive(row.getTruckRefId());
                Integer existingDriver = replace ? null : driverIdOf(row.getDriverRefId(), row.getDriverName(), driverIdByNameKey);
                rowWarnings(existingTruck, existingDriver, truckInfos, driverInfos, day, warnings);

                boolean skipped = existingTruck != null && existingDriver != null;
                Pick rowTruck = existingTruck != null ? null : truckPick;
                Pick rowDriver = existingDriver != null ? null : driverPick;
                if (!skipped) {
                    if (existingTruck == null && rowTruck == null) {
                        warnings.add(new RowWarning("NO_TRUCK_HISTORY", LEVEL_INFO,
                                "No past plan for this customer, lane or port - choose the truck manually"));
                    }
                    if (existingDriver == null && rowDriver == null) {
                        warnings.add(new RowWarning("NO_DRIVER_HISTORY", LEVEL_INFO,
                                "No free driver with history for this trip - choose the driver manually"));
                    }
                }
                results.put(stop.rowKey(), RowSuggestion.builder()
                        .rowKey(stop.rowKey())
                        .saleOrderMasterRefId(stop.saleOrderId())
                        .skipped(skipped)
                        .skipReason(skipped ? "Already has a truck and driver" : null)
                        .truck(rowTruck)
                        .driver(rowDriver)
                        .alternativeTrucks(rowTruck == null ? List.of() : alternatives(truckPicks))
                        .alternativeDrivers(rowDriver == null ? List.of() : alternatives(driverPicks))
                        .warnings(warnings)
                        .tripNo(trip.tripNo())
                        .tripPosition(position++)
                        .tripLabel(label)
                        .sortBy(sortBy++)
                        .build());

                Integer finalTruck = existingTruck != null ? existingTruck : truckId;
                Integer finalDriver = existingDriver != null ? existingDriver : driverId;
                if (finalDriver != null && finalTruck != null) {
                    rowKeysByDriverAndTruck.computeIfAbsent(finalDriver, k -> new TreeMap<>())
                            .computeIfAbsent(finalTruck, k -> new ArrayList<>()).add(stop.rowKey());
                }
                if (!skipped && (rowTruck != null || rowDriver != null)) {
                    logRows.add(new SuggestionLogRow(stop.saleOrderId(),
                            rowTruck == null ? null : rowTruck.id(), rowDriver == null ? null : rowDriver.id(), null, null));
                }
            }
        }

        List<PlanWarning> planWarnings = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, List<String>>> entry : rowKeysByDriverAndTruck.entrySet()) {
            if (entry.getValue().size() > 1) {
                List<String> keys = new ArrayList<>();
                List<String> truckNames = new ArrayList<>();
                for (Map.Entry<Integer, List<String>> byTruck : entry.getValue().entrySet()) {
                    keys.addAll(byTruck.getValue());
                    TruckInfo truck = truckInfos.get(byTruck.getKey());
                    truckNames.add(truck == null ? "truck " + byTruck.getKey() : truck.name());
                }
                planWarnings.add(new PlanWarning("DRIVER_ON_TWO_TRUCKS", LEVEL_WARNING,
                        "Driver " + driverNames.getOrDefault(entry.getKey(), String.valueOf(entry.getKey()))
                                + " is on " + String.join(" and ", truckNames) + " on the same day", keys));
            }
        }

        // Rows come back in grid order.
        List<RowSuggestion> suggestions = new ArrayList<>();
        for (String key : rowsByKey.keySet()) {
            suggestions.add(results.get(key));
        }

        reader.logSuggestions(companyId, planningDate, request.getPlanningMasterId(), currentUser(), logRows);
        log.info("Planning suggest company={} date={} rows={} trips={} suggested={} history={} warnings={}",
                companyId, planningDate, rows.size(), trips.size(), logRows.size(), events.size(), planWarnings.size());

        return PlanningSuggestResponse.builder()
                .planningDate(planningDate.toString())
                .historyPlans(events.size())
                .historyFrom(historyFrom.toString())
                .historyTo(planningDate.toString())
                .rows(suggestions)
                .warnings(planWarnings)
                .build();
    }

    private static void rowWarnings(Integer existingTruck, Integer existingDriver, Map<Integer, TruckInfo> truckInfos,
                                    Map<Integer, DriverInfo> driverInfos, DaySituation day, List<RowWarning> warnings) {
        if (existingTruck != null) {
            TruckInfo truck = truckInfos.get(existingTruck);
            if (truck != null && !truck.eligible()) {
                warnings.add(new RowWarning("TRUCK_INELIGIBLE", LEVEL_WARNING, truck.name() + ": " + truck.ineligibleReason()));
            }
        }
        if (existingDriver != null) {
            DriverInfo driver = driverInfos.get(existingDriver);
            if (driver != null && !driver.eligible()) {
                warnings.add(new RowWarning("DRIVER_INELIGIBLE", LEVEL_WARNING, driver.name() + ": " + driver.ineligibleReason()));
            }
            if (existingTruck != null) {
                for (Integer other : day.trucksTodayByDriver().getOrDefault(existingDriver, Set.of())) {
                    if (!other.equals(existingTruck)) {
                        TruckInfo truck = truckInfos.get(other);
                        warnings.add(new RowWarning("DRIVER_DOUBLE_BOOKED", LEVEL_WARNING,
                                (driver == null ? "This driver" : driver.name()) + " is also on "
                                        + (truck == null ? "another truck" : truck.name()) + " that day"));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public int feedback(PlanningSuggestFeedbackRequest request) {
        if (request.getCompanyRefId() == null) {
            throw new InvalidRequestException("companyRefId is required");
        }
        LocalDate date = parseDate(request.getPlanningDate());
        if (date == null) {
            throw new InvalidRequestException("planningDate (yyyy-MM-dd) is required");
        }
        List<SuggestionLogRow> rows = new ArrayList<>();
        for (PlanningSuggestFeedbackRequest.Row row : request.getRows() == null ? List.<PlanningSuggestFeedbackRequest.Row>of() : request.getRows()) {
            Integer saleOrderId = positive(row.getSaleOrderMasterRefId());
            if (saleOrderId == null) {
                continue;
            }
            rows.add(new SuggestionLogRow(saleOrderId, positive(row.getSuggestedTruckId()), positive(row.getSuggestedDriverId()),
                    positive(row.getChosenTruckId()), positive(row.getChosenDriverId())));
        }
        return reader.recordFeedback(request.getCompanyRefId(), date, request.getPlanningMasterId(), rows);
    }

    // ---------------------------------------------------------------------

    private List<Pick> alternatives(List<Pick> picks) {
        if (picks.size() <= 1) {
            return List.of();
        }
        return new ArrayList<>(picks.subList(1, Math.min(picks.size(), 1 + props.getMaxAlternatives())));
    }

    private TruckInfo truckInfo(TruckMaster truck, LocalDate date) {
        String name = truck.getTruckName() == null ? "Truck " + truck.getId() : truck.getTruckName().trim();
        if (props.isExcludeOutsideTrucks() && (truck.getMalevaTruck() == null || truck.getMalevaTruck() != 1)) {
            return new TruckInfo(truck.getId(), name, false, "outside truck - assign by hand");
        }
        if (props.isExcludeExpiredTrucks()) {
            if (expired(truck.getInsuranceExp(), date)) {
                return new TruckInfo(truck.getId(), name, false, "insurance expired on " + truck.getInsuranceExp());
            }
            if (expired(truck.getRotexMyExp(), date)) {
                return new TruckInfo(truck.getId(), name, false, "road tax expired on " + truck.getRotexMyExp());
            }
            if (expired(truck.getPuspacomExp(), date)) {
                return new TruckInfo(truck.getId(), name, false, "PUSPAKOM expired on " + truck.getPuspacomExp());
            }
        }
        return new TruckInfo(truck.getId(), name, true, null);
    }

    private DriverInfo driverInfo(DriverMaster driver, LocalDate date) {
        String name = driver.getDriverName() == null ? "Driver " + driver.getId() : driver.getDriverName().trim();
        Integer defaultTruck = positive(driver.getTruckRefId());
        if (driver.getLeavingDate() != null && driver.getLeavingDate().isBefore(date)) {
            return new DriverInfo(driver.getId(), name, false, "left on " + driver.getLeavingDate(), defaultTruck);
        }
        if (expired(driver.getLicenseExp(), date)) {
            return new DriverInfo(driver.getId(), name, false, "licence expired on " + driver.getLicenseExp(), defaultTruck);
        }
        if (expired(driver.getGdlExp(), date)) {
            return new DriverInfo(driver.getId(), name, false, "GDL expired on " + driver.getGdlExp(), defaultTruck);
        }
        return new DriverInfo(driver.getId(), name, true, null, defaultTruck);
    }

    private static boolean expired(LocalDate expiry, LocalDate date) {
        return expiry != null && expiry.isBefore(date);
    }

    /** Name keys that identify exactly one driver; ambiguous names resolve to nobody. */
    static Map<String, Integer> driverIdsByNameKey(List<DriverMaster> drivers) {
        Map<String, Integer> out = new HashMap<>();
        Set<String> ambiguous = new HashSet<>();
        for (DriverMaster driver : drivers) {
            String key = NameKeys.driver(driver.getDriverName());
            if (key.isEmpty()) {
                continue;
            }
            if (out.containsKey(key) && !out.get(key).equals(driver.getId())) {
                ambiguous.add(key);
            }
            out.putIfAbsent(key, driver.getId());
        }
        for (String key : ambiguous) {
            out.remove(key);
        }
        return out;
    }

    private static Integer driverIdOf(Integer driverId, String driverName, Map<String, Integer> driverIdByNameKey) {
        Integer id = positive(driverId);
        if (id != null) {
            return id;
        }
        return driverIdByNameKey.get(NameKeys.driver(driverName));
    }

    private static void countAssignment(Map<Integer, Integer> jobsTodayByTruck, Map<Integer, Set<Integer>> trucksTodayByDriver,
                                        Map<Integer, Integer> driverTodayByTruck, Integer truckId, Integer driverId) {
        if (truckId != null) {
            jobsTodayByTruck.merge(truckId, 1, Integer::sum);
        }
        if (driverId != null && truckId != null && claimsTruck(trucksTodayByDriver, driverId, truckId)) {
            driverTodayByTruck.putIfAbsent(truckId, driverId);
        }
    }

    /**
     * Registers the driver on the truck and says whether the driver may become
     * the truck's driver for the day: only while on no other truck. A driver
     * already in another truck is double-booked - the row is warned about, and
     * the claim must not spread to a second truck.
     */
    private static boolean claimsTruck(Map<Integer, Set<Integer>> trucksTodayByDriver, Integer driverId, Integer truckId) {
        Set<Integer> trucks = trucksTodayByDriver.computeIfAbsent(driverId, k -> new HashSet<>());
        trucks.add(truckId);
        for (Integer other : trucks) {
            if (!other.equals(truckId)) {
                return false;
            }
        }
        return true;
    }

    private LocalDate resolvePlanningDate(PlanningSuggestRequest request, List<PlanningSuggestRequest.Row> rows,
                                          Map<Integer, SaleOrderMaster> orders) {
        LocalDate explicit = parseDate(request.getPlanningDate());
        if (explicit != null) {
            return explicit;
        }
        LocalDate earliest = null;
        for (PlanningSuggestRequest.Row row : rows) {
            LocalDate candidate = parseDate(row.getPickupDate());
            if (candidate == null) {
                SaleOrderMaster order = orders.get(positive(row.getSaleOrderMasterRefId()));
                if (order != null && order.getPickupDate() != null) {
                    candidate = order.getPickupDate().toLocalDate();
                }
            }
            if (candidate != null && (earliest == null || candidate.isBefore(earliest))) {
                earliest = candidate;
            }
        }
        return earliest != null ? earliest : LocalDate.now(clock);
    }

    static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String text = raw.trim();
        if (text.length() >= 10) {
            try {
                return LocalDate.parse(text.substring(0, 10).replace('/', '-'));
            } catch (DateTimeParseException ignored) {
                // fall through
            }
        }
        return null;
    }

    /** 'yyyy-MM-dd HH:mm' (or with '/' or 'T'); a bare date is midnight. */
    static LocalDateTime parseDateTime(String raw) {
        LocalDate date = parseDate(raw);
        if (date == null) {
            return null;
        }
        String text = raw.trim().replace('/', '-').replace('T', ' ');
        if (text.length() >= 16) {
            try {
                return LocalDateTime.parse(text.substring(0, 16), DATE_TIME);
            } catch (DateTimeParseException ignored) {
                // fall through to the date
            }
        }
        return date.atStartOfDay();
    }

    private static Integer positive(Integer value) {
        return value == null || value <= 0 ? null : value;
    }

    private static String firstNonBlank(String a, String b) {
        return a != null && !a.isBlank() ? a : (b == null ? "" : b);
    }

    private static String currentUser() {
        Authentication auth = SecurityContextHolder.getContext() == null ? null
                : SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return "SYSTEM";
        }
        return auth.getName().length() > 50 ? auth.getName().substring(0, 50) : auth.getName();
    }
}
