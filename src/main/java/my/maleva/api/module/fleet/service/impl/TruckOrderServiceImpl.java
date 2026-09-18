package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.customer.dto.response.CustomerOptionDto;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.fleet.dto.OrderableTruckDto;
import my.maleva.api.module.fleet.dto.TruckAvailabilityDto;
import my.maleva.api.module.fleet.dto.TruckDayCapacityDto;
import my.maleva.api.module.fleet.dto.TruckOrderCalendarResponse;
import my.maleva.api.module.fleet.dto.TruckOrderDto;
import my.maleva.api.module.fleet.dto.request.TruckOrderSaveRequest;
import my.maleva.api.module.fleet.dto.request.TruckOrderSearchRequest;
import my.maleva.api.module.fleet.entity.TruckBookingType;
import my.maleva.api.module.fleet.entity.TruckLoadUnit;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.entity.TruckOrderStatus;
import my.maleva.api.module.fleet.entity.TruckOrder;
import my.maleva.api.module.fleet.entity.TruckSizeClass;
import my.maleva.api.module.fleet.entity.TruckStatus;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.fleet.repository.TruckOrderRepository;
import my.maleva.api.module.fleet.service.TruckOrderService;
import my.maleva.api.module.fleet.specification.TruckOrderSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Truck order business logic - SP_TruckOrderMaster, reimplemented, plus the
 * free-truck check the order dialog runs before anything is saved.
 *
 * <p>The procedure itself is left in the database so the legacy .NET page keeps
 * working; Java is the source of truth from here. Its rules are reproduced:
 * truck must be an active truck of this company, order number assigned at insert
 * and preserved on edit, {@code EmployeeRefId = 0} stored as NULL.
 *
 * <p>Booking types (added 2026-09-16, change truck-availability-on-order):
 * <ul>
 *   <li>OWN - one of our trucks for the day; refused when the truck already
 *       carries a live OWN or SHARED order that day (the procedure's rule);</li>
 *   <li>SHARED - a second job the dispatcher deliberately puts on a truck that is
 *       already booked; refused when the truck is in fact free, so a free truck
 *       is never recorded as shared;</li>
 *   <li>OUTSIDE - a hired truck; no truck of ours, no clash rule, never counted.</li>
 * </ul>
 *
 * <p>Deliberate differences from the legacy path, each of them fixing something
 * the old screen got wrong:
 *
 * <ul>
 *   <li>every statement is parameterised. The legacy service pasted the whole
 *       payload into the EXEC as a quoted literal and coped by stripping
 *       apostrophes out of Remarks first, so a remark with an apostrophe lost it,
 *       and one containing the text {@code null} was corrupted by the blanket
 *       string replace that ran alongside;</li>
 *   <li>the counter row is created when it is missing. The procedure only ever
 *       UPDATEs SequenceNoMaster, so a company with no seed row would have been
 *       handed ORD000000001 for every order it ever placed;</li>
 *   <li>the counter is bumped in one atomic statement instead of read-then-write,
 *       so two saves at once cannot claim the same number;</li>
 *   <li>Created_By / Modified_By carry the user, not {@code suser_name()} - which
 *       stamped every legacy row 'sa'. Same divergence as Bills and Payment.</li>
 * </ul>
 */
@Service
public class TruckOrderServiceImpl implements TruckOrderService {

    private static final Logger logger = LoggerFactory.getLogger(TruckOrderServiceImpl.class);

    private static final Integer ACTIVE = 1;
    private static final Integer DELETED = 2;
    private static final String ORDER_NUMBER_PREFIX = "ORD";
    private static final int ORDER_NUMBER_DIGITS = 9;
    private static final String SEQUENCE_NAME = "TruckOrderMaster";
    private static final TruckOrderStatus DEFAULT_STATUS = TruckOrderStatus.PENDING;

    /** Day counters are built for at most this many dates - a month view plus slack. */
    static final int MAX_CAPACITY_DAYS = 62;

    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private static final String TRUCK_NOT_FOUND_MESSAGE = "Truck Not Found.";
    private static final String ORDER_NOT_FOUND_MESSAGE = "Order not found.";

    private final TruckOrderRepository truckOrderRepository;
    private final TruckMasterRepository truckMasterRepository;
    private final CustomerRepository customerRepository;
    private final JdbcTemplate jdbcTemplate;

    public TruckOrderServiceImpl(TruckOrderRepository truckOrderRepository,
                                 TruckMasterRepository truckMasterRepository,
                                 CustomerRepository customerRepository,
                                 JdbcTemplate jdbcTemplate) {
        this.truckOrderRepository = truckOrderRepository;
        this.truckMasterRepository = truckMasterRepository;
        this.customerRepository = customerRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ------------------------------------------------------------------ list

    @Override
    @Transactional(readOnly = true)
    public TruckOrderCalendarResponse search(TruckOrderSearchRequest request) {
        requireCompany(request.getCompanyRefId());
        if (request.getFromDate() != null && request.getToDate() != null
                && request.getFromDate().isAfter(request.getToDate())) {
            throw new InvalidRequestException("fromDate must not be after toDate");
        }

        List<TruckOrder> orders = truckOrderRepository.findAll(
                TruckOrderSpecification.from(request), TruckOrderRepository.DEFAULT_SORT);

        Map<Integer, String> truckNames = truckNames(request.getCompanyRefId());
        Map<Integer, String> customerNames = customerNames(request.getCompanyRefId());

        List<TruckOrderDto> items = orders.stream()
                .map(order -> toDto(order, truckNames.get(order.getTruckRefId()),
                        customerNames.get(order.getCustomerRefId())))
                .toList();

        Set<Integer> bookedTrucks = new HashSet<>();
        for (TruckOrder order : orders) {
            if (order.getTruckRefId() != null) {
                bookedTrucks.add(order.getTruckRefId());
            }
        }

        return TruckOrderCalendarResponse.builder()
                .items(items)
                .totalOrders(items.size())
                .bookedTrucks(bookedTrucks.size())
                .days(dayCapacities(request.getCompanyRefId(), request.getFromDate(), request.getToDate()))
                .build();
    }

    /**
     * Trucks / taken / free for each date, counted over the whole fleet and
     * every live order - not the filtered list, because a truck filter changes
     * what the grid shows, not whether a truck is free.
     */
    private List<TruckDayCapacityDto> dayCapacities(Integer companyRefId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null
                || ChronoUnit.DAYS.between(fromDate, toDate) + 1 > MAX_CAPACITY_DAYS) {
            return List.of();
        }

        List<TruckMaster> fleet = truckMasterRepository.findOrderableTrucks(companyRefId);
        Set<Integer> fleetIds = new HashSet<>();
        for (TruckMaster truck : fleet) {
            fleetIds.add(truck.getId());
        }

        Map<LocalDate, Set<Integer>> taken = new HashMap<>();
        Map<LocalDate, Integer> outside = new HashMap<>();
        for (TruckOrder order : truckOrderRepository.findLiveInRange(companyRefId, fromDate, toDate)) {
            if (storedType(order).usesOwnTruck()) {
                if (order.getTruckRefId() != null && fleetIds.contains(order.getTruckRefId())) {
                    taken.computeIfAbsent(order.getOrderDate(), day -> new HashSet<>()).add(order.getTruckRefId());
                }
            } else {
                outside.merge(order.getOrderDate(), 1, Integer::sum);
            }
        }

        List<TruckDayCapacityDto> days = new ArrayList<>();
        for (LocalDate day = fromDate; !day.isAfter(toDate); day = day.plusDays(1)) {
            // Counted per day: a truck due back on Friday belongs to Friday's
            // fleet and not to Thursday's.
            int bookable = 0;
            int inWorkshop = 0;
            int takenTrucks = 0;
            Set<Integer> takenThatDay = taken.getOrDefault(day, Set.of());
            for (TruckMaster truck : fleet) {
                if (bookableOn(truck, day)) {
                    bookable++;
                    if (takenThatDay.contains(truck.getId())) {
                        takenTrucks++;
                    }
                } else if (statusOf(truck) == TruckStatus.WORKSHOP) {
                    inWorkshop++;
                }
            }
            days.add(TruckDayCapacityDto.builder()
                    .date(day)
                    .totalTrucks(bookable)
                    .takenTrucks(takenTrucks)
                    .freeTrucks(bookable - takenTrucks)
                    .outsideOrders(outside.getOrDefault(day, 0))
                    .workshopTrucks(inWorkshop)
                    .build());
        }
        return days;
    }

    // ---------------------------------------------------------------- trucks

    /**
     * The rule is {@code TruckMaster.OrderableTruck = 1} on an active own truck.
     * Marking a truck orderable is a data change, not a JavaScript edit and
     * redeploy.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderableTruckDto> orderableTrucks(Integer companyRefId) {
        requireCompany(companyRefId);

        return truckMasterRepository.findOrderableTrucks(companyRefId).stream()
                .map(this::toTruckDto)
                .toList();
    }

    // ---------------------------------------------------------- availability

    @Override
    @Transactional(readOnly = true)
    public TruckAvailabilityDto availability(Integer companyRefId, LocalDate orderDate,
                                             String sizeClass, Integer excludeId) {
        requireCompany(companyRefId);
        if (orderDate == null) {
            throw new InvalidRequestException("orderDate is required");
        }
        TruckSizeClass size = TruckSizeClass.fromCode(sizeClass);

        Map<Integer, TruckMaster> fleet = new LinkedHashMap<>();
        int inWorkshop = 0;
        for (TruckMaster truck : truckMasterRepository.findOrderableTrucks(companyRefId)) {
            if (size != null && size != TruckSizeClass.fromTruckType(truck.getTruckType())) {
                continue;
            }
            if (bookableOn(truck, orderDate)) {
                fleet.put(truck.getId(), truck);
            } else if (statusOf(truck) == TruckStatus.WORKSHOP) {
                // Reported, not offered: the dispatcher should see why the fleet
                // is two trucks smaller today rather than wonder.
                inWorkshop++;
            }
        }

        int skip = excludeId == null ? 0 : excludeId;
        Map<Integer, List<TruckOrderDto>> ordersByTruck = new LinkedHashMap<>();
        // The entities as well as their DTOs: the load sum reads Quantity and
        // QuantityUnit, which the list DTO carries but in display form.
        Map<Integer, List<TruckOrder>> liveByTruck = new LinkedHashMap<>();
        List<TruckOrderDto> outsideOrders = new ArrayList<>();
        // Named here too: the dispatcher decides whether to share a truck by
        // looking at whose cargo is already on it, and how much.
        Map<Integer, String> customerNames = customerNames(companyRefId);

        for (TruckOrder order : truckOrderRepository.findLiveInRange(companyRefId, orderDate, orderDate)) {
            if (order.getId() != null && order.getId() == skip) {
                continue;
            }
            String customerName = customerNames.get(order.getCustomerRefId());
            if (!storedType(order).usesOwnTruck()) {
                outsideOrders.add(toDto(order, null, customerName));
                continue;
            }
            TruckMaster truck = fleet.get(order.getTruckRefId());
            if (truck != null) {
                ordersByTruck.computeIfAbsent(truck.getId(), id -> new ArrayList<>())
                        .add(toDto(order, trimmed(truck.getTruckName()), customerName));
                liveByTruck.computeIfAbsent(truck.getId(), id -> new ArrayList<>()).add(order);
            }
        }

        List<OrderableTruckDto> freeTrucks = fleet.values().stream()
                .filter(truck -> !ordersByTruck.containsKey(truck.getId()))
                .map(this::toTruckDto)
                .toList();

        List<TruckAvailabilityDto.TakenTruck> takenTrucks = fleet.values().stream()
                .filter(truck -> ordersByTruck.containsKey(truck.getId()))
                .map(truck -> {
                    Integer capacity = capacityOf(truck);
                    List<TruckOrderDto> onTruck = ordersByTruck.get(truck.getId());
                    // Summed from the orders that recorded a quantity; the rest
                    // are counted separately so the screen can say the total is
                    // partial instead of pretending it is complete.
                    BigDecimal used = BigDecimal.ZERO;
                    int unmeasured = 0;
                    for (TruckOrder order : liveByTruck.getOrDefault(truck.getId(), List.of())) {
                        BigDecimal spaces = spacesOf(order, capacity);
                        if (spaces == null) {
                            unmeasured++;
                        } else {
                            used = used.add(spaces);
                        }
                    }
                    return TruckAvailabilityDto.TakenTruck.builder()
                            .id(truck.getId())
                            .truckName(trimmed(truck.getTruckName()))
                            .sizeClass(sizeCodeOf(truck))
                            .orders(onTruck)
                            .palletCapacity(capacity)
                            .usedSpaces(used.stripTrailingZeros())
                            .ordersWithoutQuantity(unmeasured)
                            .build();
                })
                .toList();

        return TruckAvailabilityDto.builder()
                .orderDate(orderDate)
                .sizeClass(size == null ? null : size.getCode())
                .totalTrucks(fleet.size())
                .takenCount(takenTrucks.size())
                .freeCount(freeTrucks.size())
                .workshopTrucks(inWorkshop)
                .freeTrucks(freeTrucks)
                .takenTrucks(takenTrucks)
                .outsideOrders(outsideOrders)
                .build();
    }

    // ------------------------------------------------------------- numbering

    @Override
    @Transactional(readOnly = true)
    public String nextOrderNumber(Integer companyRefId) {
        requireCompany(companyRefId);
        Integer next = jdbcTemplate.queryForObject(
                "SELECT ISNULL(MAX(SequenceNo) + 1, 1) FROM SequenceNoMaster WITH (NOLOCK) "
                        + "WHERE CompanyRefId = ? AND SequenceName = ?",
                Integer.class, companyRefId, SEQUENCE_NAME);
        return formatOrderNumber(next == null ? 1 : next);
    }

    // ------------------------------------------------------------------ read

    @Override
    @Transactional(readOnly = true)
    public TruckOrderDto getForEdit(Integer id, Integer companyRefId) {
        requireCompany(companyRefId);
        TruckOrder order = truckOrderRepository
                .findByIdAndCompanyRefIdAndActive(id, companyRefId, ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND_MESSAGE));
        return toDto(order, truckName(order.getTruckRefId()),
                customerName(companyRefId, order.getCustomerRefId()));
    }

    @Override
    @Transactional(readOnly = true)
    public TruckOrderDto findClash(Integer companyRefId, Integer truckRefId,
                                   LocalDate orderDate, Integer excludeId) {
        requireCompany(companyRefId);
        if (truckRefId == null || truckRefId == 0 || orderDate == null) {
            return null;
        }

        TruckOrderSearchRequest probe = TruckOrderSearchRequest.builder()
                .companyRefId(companyRefId)
                .fromDate(orderDate)
                .toDate(orderDate)
                .truckRefId(truckRefId)
                .build();

        int skip = excludeId == null ? 0 : excludeId;
        return truckOrderRepository
                .findAll(TruckOrderSpecification.from(probe), TruckOrderRepository.DEFAULT_SORT)
                .stream()
                .filter(order -> !order.getId().equals(skip))
                .filter(order -> storedType(order).usesOwnTruck())
                .findFirst()
                .map(order -> toDto(order, truckName(order.getTruckRefId()),
                        customerName(companyRefId, order.getCustomerRefId())))
                .orElse(null);
    }

    // ------------------------------------------------------------------ save

    /**
     * No catch anywhere in this method, on purpose: it mixes a JdbcTemplate
     * counter bump with a JPA write inside one transaction, and swallowing a
     * failure here would surface later as an opaque "Transaction silently rolled
     * back" instead of the real message. A race lost to the unique index surfaces
     * as a DataIntegrityViolationException at the flush and is turned into a 409
     * by TruckOrderExceptionHandler.
     */
    @Override
    @Transactional
    public TruckOrderDto save(TruckOrderSaveRequest request, String username) {
        Integer companyRefId = request.getCompanyRefId();
        requireCompany(companyRefId);

        if (request.getOrderDate() == null) {
            throw new InvalidRequestException("Please select Order Date.");
        }

        TruckBookingType type = TruckBookingType.fromCode(request.getBookingType());
        String status = resolveStatus(request.getStatus());
        TruckSizeClass sizeClass = TruckSizeClass.fromCode(request.getTruckSizeClass());
        boolean creating = request.getId() == null || request.getId() == 0;

        Integer truckRefId = null;
        String outsideTruckName = null;
        String outsideSupplierName = null;

        if (type.usesOwnTruck()) {
            if (request.getTruckRefId() == null || request.getTruckRefId() == 0) {
                throw new InvalidRequestException("Truck is required.");
            }
            TruckMaster truck = truckMasterRepository.findById(request.getTruckRefId())
                    .filter(candidate -> companyRefId.equals(candidate.getCompanyRefId()))
                    .filter(candidate -> ACTIVE.equals(candidate.getActive()))
                    .orElseThrow(() -> new InvalidRequestException(TRUCK_NOT_FOUND_MESSAGE));

            // A truck in the workshop is not deleted, it is simply not on the
            // road that day - so the message says which, and until when.
            if (!bookableOn(truck, request.getOrderDate())) {
                throw new InvalidRequestException(offRoadMessage(truck, request.getOrderDate()));
            }
            truckRefId = request.getTruckRefId();

            long others = truckOrderRepository.countClashes(companyRefId, truckRefId,
                    request.getOrderDate(), creating ? 0 : request.getId());
            if (type == TruckBookingType.OWN && others > 0) {
                throw new InvalidRequestException("No truck available: " + displayName(truckRefId)
                        + " is already booked on " + DAY_LABEL.format(request.getOrderDate()) + ".");
            }
            if (type == TruckBookingType.SHARED && others == 0) {
                throw new InvalidRequestException(displayName(truckRefId) + " is free on "
                        + DAY_LABEL.format(request.getOrderDate()) + " - book it as own.");
            }
        } else {
            outsideTruckName = trimToNull(request.getOutsideTruckName());
            if (outsideTruckName == null) {
                throw new InvalidRequestException("Outside truck name is required.");
            }
            outsideSupplierName = trimToNull(request.getOutsideSupplierName());
        }

        Integer customerRefId = nullIfZero(request.getCustomerRefId());
        if (customerRefId != null
                && customerRepository.findByIdAndCompanyRefId(customerRefId, companyRefId).isEmpty()) {
            throw new InvalidRequestException("Customer not found.");
        }

        // A number and its unit are one fact: half of it recorded is a quantity
        // nothing can add up, and a unit with no number says nothing at all.
        BigDecimal quantity = request.getQuantity();
        TruckLoadUnit unit = TruckLoadUnit.fromCode(request.getQuantityUnit());
        if (quantity != null && quantity.signum() <= 0) {
            throw new InvalidRequestException("Quantity must be more than zero.");
        }
        if (quantity != null && unit == null) {
            throw new InvalidRequestException("Choose what the quantity is counted in.");
        }
        if (quantity == null && unit != null) {
            throw new InvalidRequestException("Enter a quantity for " + unit.getLabel() + ".");
        }

        LocalDateTime now = LocalDateTime.now();
        String actor = stamp(username);

        TruckOrder order;
        if (creating) {
            int sequenceNo = claimNextSequenceNo(companyRefId);
            order = TruckOrder.builder()
                    .companyRefId(companyRefId)
                    .cNumber(sequenceNo)
                    .cNumberDisplay(formatOrderNumber(sequenceNo))
                    .active(ACTIVE)
                    .createdDate(now)
                    .createdBy(actor)
                    .build();
        } else {
            // CNumber and CNumberDisplay are not reassigned - the procedure left
            // them alone too, and the number may already have been quoted.
            order = truckOrderRepository
                    .findByIdAndCompanyRefIdAndActive(request.getId(), companyRefId, ACTIVE)
                    .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND_MESSAGE));
        }

        order.setBookingType(type.name());
        order.setTruckRefId(truckRefId);
        order.setOutsideTruckName(outsideTruckName);
        order.setOutsideSupplierName(outsideSupplierName);
        order.setTruckSizeClass(sizeClass == null ? null : sizeClass.getCode());
        order.setCustomerRefId(customerRefId);
        order.setQuantity(quantity);
        order.setQuantityUnit(unit == null ? null : unit.getCode());
        order.setOrigin(normalisePlace(request.getOrigin()));
        order.setDestination(normalisePlace(request.getDestination()));
        order.setEmployeeRefId(nullIfZero(request.getEmployeeRefId()));
        order.setOrderDate(request.getOrderDate());
        order.setStatus(status);
        order.setRemarks(trimToNull(request.getRemarks()));
        order.setModifiedDate(now);
        order.setModifiedBy(actor);

        TruckOrder saved = truckOrderRepository.saveAndFlush(order);
        logger.info("Saved truck order {} ({}) as {} for truck {} on {}",
                saved.getId(), saved.getCNumberDisplay(), saved.getBookingType(),
                saved.getTruckRefId() != null ? saved.getTruckRefId() : saved.getOutsideTruckName(),
                saved.getOrderDate());
        return toDto(saved, truckName(saved.getTruckRefId()),
                customerName(companyRefId, saved.getCustomerRefId()));
    }

    /**
     * Takes the next running number and records it, in one statement.
     *
     * <p>The procedure read {@code MAX(SequenceNo)} and wrote it back separately,
     * so two concurrent inserts could both read the same value and produce two
     * orders with one number. It also only ever UPDATEd, so a company with no
     * counter row silently stayed at 1 forever; the row is created here when it
     * is missing, the same fix Payment and Payment Voucher carry.
     */
    private int claimNextSequenceNo(Integer companyRefId) {
        List<Integer> claimed = jdbcTemplate.queryForList(
                "UPDATE SequenceNoMaster SET SequenceNo = SequenceNo + 1 "
                        + "OUTPUT INSERTED.SequenceNo "
                        + "WHERE CompanyRefId = ? AND SequenceName = ?",
                Integer.class, companyRefId, SEQUENCE_NAME);

        if (!claimed.isEmpty()) {
            return claimed.stream().mapToInt(Integer::intValue).max().orElse(1);
        }

        jdbcTemplate.update(
                "INSERT INTO SequenceNoMaster (CompanyRefId, SequenceName, SequenceNo, SequenceDate) "
                        + "VALUES (?, ?, 1, GETDATE())",
                companyRefId, SEQUENCE_NAME);
        logger.info("Created the {} counter for company {}", SEQUENCE_NAME, companyRefId);
        return 1;
    }

    // ---------------------------------------------------------------- delete

    /**
     * Soft delete.
     *
     * <p>Deliberately loads the order first rather than firing a bulk UPDATE and
     * testing its affected-row count. The pool sets {@code SET NOCOUNT ON} as its
     * connection-init SQL (application.yaml), so SQL Server never sends a row
     * count and JDBC reports -1 for every UPDATE on this datasource. An
     * {@code updated == 0} test therefore never fires, and deleting an id that
     * does not exist - or belongs to another company - would report success.
     *
     * <p>Deleting an OWN order leaves any SHARED orders on the same truck and day
     * alone; the truck still counts as taken while they are live.
     */
    @Override
    @Transactional
    public void delete(Integer id, Integer companyRefId, String username) {
        requireCompany(companyRefId);

        TruckOrder order = truckOrderRepository
                .findByIdAndCompanyRefIdAndActive(id, companyRefId, ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND_MESSAGE));

        order.setActive(DELETED);
        order.setModifiedDate(LocalDateTime.now());
        order.setModifiedBy(stamp(username));
        truckOrderRepository.save(order);
    }

    // --------------------------------------------------------------- helpers

    private TruckOrderDto toDto(TruckOrder order, String truckName, String customerName) {
        return TruckOrderDto.builder()
                .id(order.getId())
                .companyRefId(order.getCompanyRefId())
                .cNumber(order.getCNumber())
                .cNumberDisplay(order.getCNumberDisplay())
                .orderDate(order.getOrderDate())
                .bookingType(storedType(order).name())
                .truckRefId(order.getTruckRefId())
                .truckName(truckName)
                .outsideTruckName(order.getOutsideTruckName())
                .outsideSupplierName(order.getOutsideSupplierName())
                .truckSizeClass(order.getTruckSizeClass())
                .customerRefId(order.getCustomerRefId())
                .customerName(customerName)
                .quantity(order.getQuantity())
                .quantityUnit(order.getQuantityUnit())
                .origin(order.getOrigin())
                .destination(order.getDestination())
                .employeeRefId(order.getEmployeeRefId())
                .status(order.getStatus())
                .remarks(order.getRemarks())
                .build();
    }

    private OrderableTruckDto toTruckDto(TruckMaster truck) {
        return OrderableTruckDto.builder()
                .id(truck.getId())
                // Trimmed: at least one plate is stored with a trailing space.
                .truckName(trimmed(truck.getTruckName()))
                .truckType(truck.getTruckType())
                .sizeClass(sizeCodeOf(truck))
                .palletCapacity(capacityOf(truck))
                .build();
    }

    /**
     * Pallet spaces this truck holds: its own figure when one is recorded,
     * otherwise the usual figure for its size. Null means "do not count spaces
     * for this truck" - a long loader carries one machine.
     */
    private Integer capacityOf(TruckMaster truck) {
        if (truck.getPalletCapacity() != null && truck.getPalletCapacity() > 0) {
            return truck.getPalletCapacity();
        }
        TruckSizeClass size = TruckSizeClass.fromTruckType(truck.getTruckType());
        return size == null ? null : size.getDefaultPalletCapacity();
    }

    /**
     * What one order takes up, in pallet spaces, or null when nothing was
     * recorded - which is not zero and must not be counted as zero.
     *
     * <p>A quantity booked as TRUCK means the whole vehicle, so it fills whatever
     * the truck holds.
     */
    private BigDecimal spacesOf(TruckOrder order, Integer capacity) {
        if (order.getQuantity() == null || order.getQuantityUnit() == null) {
            return null;
        }
        TruckLoadUnit unit;
        try {
            unit = TruckLoadUnit.fromCode(order.getQuantityUnit());
        } catch (InvalidRequestException unknownUnit) {
            // A unit this build does not know is "unmeasured", never zero.
            return null;
        }
        if (unit.isWholeTruck()) {
            BigDecimal trucks = order.getQuantity();
            return capacity == null ? null : trucks.multiply(BigDecimal.valueOf(capacity));
        }
        return order.getQuantity().multiply(unit.getPalletSpaces());
    }

    /** The size read out of the truck's free-text type, or null when it says nothing. */
    private String sizeCodeOf(TruckMaster truck) {
        TruckSizeClass size = TruckSizeClass.fromTruckType(truck.getTruckType());
        return size == null ? null : size.getCode();
    }

    /**
     * The booking type as stored, read leniently: a row is only ever written by
     * {@link #save}, but a value this code does not know must not break the
     * calendar for everyone, so it falls back on what the row itself says - no
     * truck means outside.
     */
    private TruckBookingType storedType(TruckOrder order) {
        String stored = trimToEmpty(order.getBookingType());
        for (TruckBookingType type : TruckBookingType.values()) {
            if (type.name().equalsIgnoreCase(stored)) {
                return type;
            }
        }
        return order.getTruckRefId() == null ? TruckBookingType.OUTSIDE : TruckBookingType.OWN;
    }

    private Map<Integer, String> truckNames(Integer companyRefId) {
        Map<Integer, String> names = new HashMap<>();
        for (TruckMaster truck : truckMasterRepository.findByCompanyRefId(companyRefId)) {
            names.put(truck.getId(), trimmed(truck.getTruckName()));
        }
        return names;
    }

    /**
     * Customer names for a whole page of orders, in one query.
     *
     * <p>{@code findOptions} is the dropdown query - active customers of the
     * company. An order pointing at a customer since deactivated simply shows no
     * name; {@link #customerName} is the by-id read used where one row matters.
     */
    private Map<Integer, String> customerNames(Integer companyRefId) {
        Map<Integer, String> names = new HashMap<>();
        for (CustomerOptionDto option : customerRepository.findOptions(companyRefId)) {
            names.put(option.id(), option.customerName());
        }
        return names;
    }

    private String customerName(Integer companyRefId, Integer customerRefId) {
        if (customerRefId == null) {
            return null;
        }
        return customerRepository.findByIdAndCompanyRefId(customerRefId, companyRefId)
                .map(Customer::getCustomerName)
                .orElse(null);
    }

    private String truckName(Integer truckRefId) {
        if (truckRefId == null) {
            return null;
        }
        return truckMasterRepository.findById(truckRefId)
                .map(TruckMaster::getTruckName)
                .map(String::trim)
                .orElse(null);
    }

    /**
     * The truck's status, read leniently: an unreadable value must not take the
     * whole calendar down, and a truck nobody has marked is on the road.
     */
    private TruckStatus statusOf(TruckMaster truck) {
        String stored = trimToEmpty(truck.getTruckStatus());
        for (TruckStatus status : TruckStatus.values()) {
            if (status.name().equalsIgnoreCase(stored)) {
                return status;
            }
        }
        return TruckStatus.ACTIVE;
    }

    /** Can this truck be booked on this day? SOLD never; WORKSHOP until it is back. */
    private boolean bookableOn(TruckMaster truck, LocalDate day) {
        return statusOf(truck).bookableOn(truck.getWorkshopUntil(), day);
    }

    /** Says which state the truck is in, and until when, rather than just refusing. */
    private String offRoadMessage(TruckMaster truck, LocalDate day) {
        String name = trimmed(truck.getTruckName());
        String plate = name == null || name.isEmpty() ? "This truck" : name;
        if (statusOf(truck) == TruckStatus.SOLD) {
            return plate + " is marked sold and cannot be booked.";
        }
        return truck.getWorkshopUntil() == null
                ? plate + " is in the workshop."
                : plate + " is in the workshop until " + DAY_LABEL.format(truck.getWorkshopUntil()) + ".";
    }

    /** Most place names a suggestion list needs; more is noise. */
    private static final int MAX_PLACES = 300;

    @Override
    @Transactional(readOnly = true)
    public List<String> places(Integer companyRefId) {
        requireCompany(companyRefId);
        // Both sources, counted together so the places people actually use come
        // first. Sale orders are limited to a year: older spellings are the ones
        // this list exists to stop.
        return jdbcTemplate.queryForList(
                "SELECT TOP " + MAX_PLACES + " Place FROM ("
                        + " SELECT UPPER(LTRIM(RTRIM(Origin))) AS Place FROM TruckOrderMaster WITH (NOLOCK)"
                        + "  WHERE CompanyRefId = ? AND Active = 1 AND NULLIF(LTRIM(RTRIM(Origin)), '') IS NOT NULL"
                        + " UNION ALL"
                        + " SELECT UPPER(LTRIM(RTRIM(Destination))) FROM TruckOrderMaster WITH (NOLOCK)"
                        + "  WHERE CompanyRefId = ? AND Active = 1 AND NULLIF(LTRIM(RTRIM(Destination)), '') IS NOT NULL"
                        + " UNION ALL"
                        + " SELECT UPPER(LTRIM(RTRIM(Origin))) FROM SaleOrderMaster WITH (NOLOCK)"
                        + "  WHERE CompanyRefId = ? AND PickupDate >= DATEADD(month, -12, GETDATE())"
                        + "    AND NULLIF(LTRIM(RTRIM(Origin)), '') IS NOT NULL"
                        + " UNION ALL"
                        + " SELECT UPPER(LTRIM(RTRIM(Destination))) FROM SaleOrderMaster WITH (NOLOCK)"
                        + "  WHERE CompanyRefId = ? AND PickupDate >= DATEADD(month, -12, GETDATE())"
                        + "    AND NULLIF(LTRIM(RTRIM(Destination)), '') IS NOT NULL"
                        + ") used GROUP BY Place ORDER BY COUNT(*) DESC, Place",
                String.class, companyRefId, companyRefId, companyRefId, companyRefId);
    }

    /**
     * A place as it is stored: trimmed, inner spaces collapsed to one, capitals.
     * "  singapore " and "SINGAPORE" are one place, and the search can then find
     * both. Blank means not recorded.
     */
    private static String normalisePlace(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ENGLISH);
        return cleaned.isEmpty() ? null : cleaned;
    }

    /** The truck's plate for a message, or a neutral phrase when it has none. */
    private String displayName(Integer truckRefId) {
        String name = truckName(truckRefId);
        return name == null || name.isEmpty() ? "This truck" : name;
    }

    /**
     * Accepts a status only if the dialog offers it, and returns it in the
     * enum's own casing so the calendar colour lookup always matches.
     *
     * <p>A divergence from the procedure, which stored whatever arrived into a
     * free varchar(30). That was only safe while the sole writer was a closed
     * dropdown; it is a closed dropdown here too, and now the server checks.
     */
    private String resolveStatus(String status) {
        if (status == null || status.isBlank()) {
            return DEFAULT_STATUS.getLabel();
        }
        return TruckOrderStatus.fromLabel(status).getLabel();
    }

    private String formatOrderNumber(int sequenceNo) {
        return ORDER_NUMBER_PREFIX + String.format("%0" + ORDER_NUMBER_DIGITS + "d", sequenceNo);
    }

    private Integer nullIfZero(Integer value) {
        return value == null || value == 0 ? null : value;
    }

    private static String trimmed(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Created_By / Modified_By are varchar(50) and NOT NULL. */
    private String stamp(String username) {
        if (username == null || username.isBlank()) {
            return "system";
        }
        String trimmed = username.trim();
        return trimmed.length() > 50 ? trimmed.substring(0, 50) : trimmed;
    }

    private void requireCompany(Integer companyRefId) {
        if (companyRefId == null || companyRefId == 0) {
            throw new InvalidRequestException("companyRefId is required");
        }
    }
}
