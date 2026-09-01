package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.dto.OrderableTruckDto;
import my.maleva.api.module.fleet.dto.TruckOrderCalendarResponse;
import my.maleva.api.module.fleet.dto.TruckOrderDto;
import my.maleva.api.module.fleet.dto.request.TruckOrderSaveRequest;
import my.maleva.api.module.fleet.dto.request.TruckOrderSearchRequest;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.entity.TruckOrderStatus;
import my.maleva.api.module.fleet.entity.TruckOrder;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.fleet.repository.TruckOrderRepository;
import my.maleva.api.module.fleet.service.TruckOrderService;
import my.maleva.api.module.fleet.specification.TruckOrderSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Truck order business logic - SP_TruckOrderMaster, reimplemented.
 *
 * <p>The procedure itself is left in the database so the legacy .NET page keeps
 * working; Java is the source of truth from here. Its rules are reproduced
 * exactly: truck required, truck must be an active truck of this company, one
 * live order per truck per day, order number assigned at insert and preserved on
 * edit, {@code EmployeeRefId = 0} stored as NULL.
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

    /** Message kept verbatim from SP_TruckOrderMaster so users see what they always saw. */
    private static final String CLASH_MESSAGE = "This truck is already booked on the selected date.";
    private static final String TRUCK_NOT_FOUND_MESSAGE = "Truck Not Found.";
    private static final String ORDER_NOT_FOUND_MESSAGE = "Order not found.";

    private final TruckOrderRepository truckOrderRepository;
    private final TruckMasterRepository truckMasterRepository;
    private final JdbcTemplate jdbcTemplate;

    public TruckOrderServiceImpl(TruckOrderRepository truckOrderRepository,
                                 TruckMasterRepository truckMasterRepository,
                                 JdbcTemplate jdbcTemplate) {
        this.truckOrderRepository = truckOrderRepository;
        this.truckMasterRepository = truckMasterRepository;
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

        List<TruckOrderDto> items = orders.stream()
                .map(order -> toDto(order, truckNames.get(order.getTruckRefId())))
                .toList();

        Set<Integer> bookedTrucks = new HashSet<>();
        for (TruckOrder order : orders) {
            bookedTrucks.add(order.getTruckRefId());
        }

        return TruckOrderCalendarResponse.builder()
                .items(items)
                .totalOrders(items.size())
                .bookedTrucks(bookedTrucks.size())
                .build();
    }

    // ---------------------------------------------------------------- trucks

    /**
     * The rule is one column: {@code TruckMaster.OrderableTruck = 1}. Marking a
     * truck orderable is a data change now, not a JavaScript edit and redeploy.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderableTruckDto> orderableTrucks(Integer companyRefId) {
        requireCompany(companyRefId);

        return truckMasterRepository.findOrderableTrucks(companyRefId).stream()
                .map(truck -> OrderableTruckDto.builder()
                        .id(truck.getId())
                        // Trimmed: at least one plate is stored with a trailing space.
                        .truckName(truck.getTruckName() == null ? null : truck.getTruckName().trim())
                        .truckType(truck.getTruckType())
                        .build())
                .toList();
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
        return toDto(order, truckName(order.getTruckRefId()));
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
                .findFirst()
                .map(order -> toDto(order, truckName(order.getTruckRefId())))
                .orElse(null);
    }

    // ------------------------------------------------------------------ save

    /**
     * No catch anywhere in this method, on purpose: it mixes a JdbcTemplate
     * counter bump with a JPA write inside one transaction, and swallowing a
     * failure here would surface later as an opaque "Transaction silently rolled
     * back" instead of the real message.
     */
    @Override
    @Transactional
    public TruckOrderDto save(TruckOrderSaveRequest request, String username) {
        Integer companyRefId = request.getCompanyRefId();
        requireCompany(companyRefId);

        if (request.getTruckRefId() == null || request.getTruckRefId() == 0) {
            throw new InvalidRequestException("Truck is required.");
        }
        if (request.getOrderDate() == null) {
            throw new InvalidRequestException("Please select Order Date.");
        }
        if (!truckMasterRepository.existsByIdAndCompanyRefIdAndActive(
                request.getTruckRefId(), companyRefId, ACTIVE)) {
            throw new InvalidRequestException(TRUCK_NOT_FOUND_MESSAGE);
        }

        String status = resolveStatus(request.getStatus());
        boolean creating = request.getId() == null || request.getId() == 0;

        if (truckOrderRepository.countClashes(companyRefId, request.getTruckRefId(),
                request.getOrderDate(), creating ? 0 : request.getId()) > 0) {
            throw new InvalidRequestException(CLASH_MESSAGE);
        }

        LocalDateTime now = LocalDateTime.now();
        String actor = stamp(username);

        TruckOrder order;
        if (creating) {
            int sequenceNo = claimNextSequenceNo(companyRefId);
            order = TruckOrder.builder()
                    .companyRefId(companyRefId)
                    .truckRefId(request.getTruckRefId())
                    .employeeRefId(nullIfZero(request.getEmployeeRefId()))
                    .cNumber(sequenceNo)
                    .cNumberDisplay(formatOrderNumber(sequenceNo))
                    .orderDate(request.getOrderDate())
                    .status(status)
                    .remarks(trimToNull(request.getRemarks()))
                    .active(ACTIVE)
                    .createdDate(now)
                    .createdBy(actor)
                    .modifiedDate(now)
                    .modifiedBy(actor)
                    .build();
        } else {
            order = truckOrderRepository
                    .findByIdAndCompanyRefIdAndActive(request.getId(), companyRefId, ACTIVE)
                    .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND_MESSAGE));

            // CNumber and CNumberDisplay are not reassigned - the procedure left
            // them alone too, and the number may already have been quoted.
            order.setTruckRefId(request.getTruckRefId());
            order.setEmployeeRefId(nullIfZero(request.getEmployeeRefId()));
            order.setOrderDate(request.getOrderDate());
            order.setStatus(status);
            order.setRemarks(trimToNull(request.getRemarks()));
            order.setModifiedDate(now);
            order.setModifiedBy(actor);
        }

        TruckOrder saved = truckOrderRepository.save(order);
        logger.info("Saved truck order {} ({}) for truck {} on {}",
                saved.getId(), saved.getCNumberDisplay(), saved.getTruckRefId(), saved.getOrderDate());
        return toDto(saved, truckName(saved.getTruckRefId()));
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

    private TruckOrderDto toDto(TruckOrder order, String truckName) {
        return TruckOrderDto.builder()
                .id(order.getId())
                .companyRefId(order.getCompanyRefId())
                .cNumber(order.getCNumber())
                .cNumberDisplay(order.getCNumberDisplay())
                .orderDate(order.getOrderDate())
                .truckRefId(order.getTruckRefId())
                .truckName(truckName)
                .employeeRefId(order.getEmployeeRefId())
                .status(order.getStatus())
                .remarks(order.getRemarks())
                .build();
    }

    private Map<Integer, String> truckNames(Integer companyRefId) {
        Map<Integer, String> names = new HashMap<>();
        for (TruckMaster truck : truckMasterRepository.findByCompanyRefId(companyRefId)) {
            names.put(truck.getId(), truck.getTruckName() == null ? null : truck.getTruckName().trim());
        }
        return names;
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
