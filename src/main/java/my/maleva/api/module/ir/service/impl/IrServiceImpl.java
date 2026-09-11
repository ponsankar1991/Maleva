package my.maleva.api.module.ir.service.impl;

import my.maleva.api.common.constant.UserRoles;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.employee.entity.EmployeeMaster;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.ir.dto.IrDepartmentOptionDto;
import my.maleva.api.module.ir.dto.IrDetailDto;
import my.maleva.api.module.ir.dto.IrListResponse;
import my.maleva.api.module.ir.dto.IrSaveRequest;
import my.maleva.api.module.ir.dto.IrSearchRequest;
import my.maleva.api.module.ir.dto.IrStatusOptionDto;
import my.maleva.api.module.ir.entity.IrMaster;
import my.maleva.api.module.ir.entity.IrStatusMaster;
import my.maleva.api.module.ir.repository.IrMasterRepository;
import my.maleva.api.module.ir.repository.IrStatusMasterRepository;
import my.maleva.api.module.ir.specification.IrMasterSpecification;
import my.maleva.api.module.ir.service.IrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class IrServiceImpl implements IrService {

    private static final Logger logger = LoggerFactory.getLogger(IrServiceImpl.class);

    /**
     * The status codes that end an IR's life.
     *
     * <p>IRStatusMaster carries no "finished" column, so this is the one place
     * that knows which codes mean finished. A status added later that also ends
     * the workflow must be added here, or "open IRs" will keep counting it.
     */
    private static final Set<String> FINISHED_STATUS_CODES = Set.of("CLOSED", "REJECTED");

    /** EmployeeMaster uses the same 1-is-live flag as everything else here. */
    private static final Integer ACTIVE_EMPLOYEE = 1;

    /** Newest incident first, and by id within a day so the order is stable. */
    private static final Sort DEFAULT_SORT =
            Sort.by(Sort.Direction.DESC, "irDate").and(Sort.by(Sort.Direction.DESC, "id"));

    private final IrMasterRepository irMasterRepository;
    private final IrStatusMasterRepository irStatusMasterRepository;
    private final TruckMasterRepository truckMasterRepository;
    private final DriverMasterRepository driverMasterRepository;
    private final EmployeeMasterRepository employeeMasterRepository;

    public IrServiceImpl(IrMasterRepository irMasterRepository,
                         IrStatusMasterRepository irStatusMasterRepository,
                         TruckMasterRepository truckMasterRepository,
                         DriverMasterRepository driverMasterRepository,
                         EmployeeMasterRepository employeeMasterRepository) {
        this.irMasterRepository = irMasterRepository;
        this.irStatusMasterRepository = irStatusMasterRepository;
        this.truckMasterRepository = truckMasterRepository;
        this.driverMasterRepository = driverMasterRepository;
        this.employeeMasterRepository = employeeMasterRepository;
    }

    // ------------------------------------------------------------------ read

    @Override
    @Transactional(readOnly = true)
    public IrListResponse search(IrSearchRequest request) {
        requireCompany(request.getCompanyRefId());

        if (request.getFromDate() != null && request.getToDate() != null
                && request.getFromDate().isAfter(request.getToDate())) {
            throw new InvalidRequestException("fromDate must not be after toDate");
        }

        if (Boolean.TRUE.equals(request.getOpenOnly())) {
            request.setExcludeStatusRefIds(finishedStatusIds(request.getCompanyRefId()));
        }

        List<IrMaster> rows = irMasterRepository.findAll(
                IrMasterSpecification.from(request), DEFAULT_SORT);

        Map<Integer, IrStatusMaster> statuses = statusesById(request.getCompanyRefId());
        // One query for the authors on this page, not one per row.
        Map<Integer, String> authorNames = employeeNames(rows.stream()
                .map(IrMaster::getCreateEmployeeRefId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        List<IrDetailDto> items = rows.stream()
                .map(row -> toDto(row,
                        lookup(statuses, row.getIrStatusRefId()),
                        lookup(authorNames, row.getCreateEmployeeRefId())))
                .toList();

        // Summed here, not in the browser: a grid that adds its own rows drifts
        // from the database the moment paging or a hidden row appears.
        long total = rows.stream()
                .mapToLong(row -> row.getActualAmount() == null ? 0L : row.getActualAmount())
                .sum();

        return IrListResponse.builder()
                .items(items)
                .count(items.size())
                .totalAmount(total)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public IrDetailDto getById(Integer id, Integer companyRefId) {
        requireCompany(companyRefId);
        IrMaster entity = requireExisting(id, companyRefId);
        return toDto(entity,
                findStatus(entity.getIrStatusRefId(), companyRefId),
                employeeName(entity.getCreateEmployeeRefId()));
    }

    // ------------------------------------------------------------------ save

    /**
     * Insert when the request carries no id, update that row otherwise.
     *
     * <p>Not wrapped in a try/catch: a swallowed exception inside a transaction
     * leaves the connection rollback-only and the caller gets a confusing 500
     * after an apparently successful save. Let it propagate to the global
     * handler, which turns the typed exceptions into 400/404.
     */
    @Override
    @Transactional
    public IrDetailDto save(IrSaveRequest request, String username) {
        requireCompany(request.getCompanyRefId());

        IrStatusMaster status = irStatusMasterRepository
                .findByIdAndCompanyRefIdAndActive(
                        request.getIrStatusRefId(), request.getCompanyRefId(), IrMaster.ACTIVE)
                .orElseThrow(() -> new InvalidRequestException(
                        "Status " + request.getIrStatusRefId() + " was not found for this company"));

        // The name is taken from the enum, never from the request, so the stored
        // snapshot can never disagree with the id it was taken from.
        UserRoles department = UserRoles.fromId(request.getDepartmentRefId())
                .orElseThrow(() -> new InvalidRequestException(
                        "Department " + request.getDepartmentRefId() + " is not a known role id"));

        LocalDateTime now = LocalDateTime.now();
        String stamp = stamp(username);
        boolean isNew = request.getId() == null || request.getId() == 0;

        IrMaster entity;
        if (isNew) {
            entity = new IrMaster();
            entity.setCompanyRefId(request.getCompanyRefId());
            entity.setActive(IrMaster.ACTIVE);
            entity.setCreatedDate(now);
            entity.setCreatedBy(stamp);
            // Taken from the logged-in user, never from the request body, so a
            // caller cannot file a report under someone else's name. Set only
            // here: an edit by anyone else leaves the author alone.
            entity.setCreateEmployeeRefId(currentEmployeeId(username, request.getCompanyRefId()));
        } else {
            // Company-scoped, so an id from another company cannot be updated,
            // and a soft-deleted row cannot be resurrected by editing it.
            entity = requireExisting(request.getId(), request.getCompanyRefId());
        }

        entity.setIrDate(request.getIrDate());
        entity.setIrStatusRefId(status.getId());
        entity.setDescription(request.getDescription().trim());
        entity.setReason(trimToNull(request.getReason()));
        entity.setDepartmentRefId(department.getRoleId());
        entity.setDepartmentName(department.name());
        entity.setVesselName(trimToNull(request.getVesselName()));
        entity.setActualAmount(request.getActualAmount());

        applyTruck(entity, request);
        applyDriver(entity, request);
        applyEmployee(entity, request);

        entity.setModifiedDate(now);
        entity.setModifiedBy(stamp);

        // FilePath is untouched on purpose - /api/attachments owns that column.

        IrMaster saved = irMasterRepository.save(entity);
        logger.info("{} IR {} for company {}", isNew ? "Created" : "Updated",
                saved.getId(), saved.getCompanyRefId());

        return toDto(saved, status, employeeName(saved.getCreateEmployeeRefId()));
    }

    /**
     * EmployeeMaster.Id of the logged-in user.
     *
     * <p>The JWT subject is the user name, which is what
     * {@code Authentication.getName()} returns, so the employee row is looked
     * up by that. Null when the caller is not a real employee of this company
     * (a system job, or an account with no EmployeeMaster row) - the column is
     * nullable and {@code Created_By} still records the user name.
     */
    private Integer currentEmployeeId(String username, Integer companyRefId) {
        String name = trimToNull(username);
        if (name == null || "system".equalsIgnoreCase(name)) {
            return null;
        }

        Integer employeeId = employeeMasterRepository
                .findByUserNameAndActive(name, ACTIVE_EMPLOYEE)
                .filter(employee -> companyRefId.equals(employee.getCompanyRefId()))
                .map(EmployeeMaster::getId)
                .orElse(null);

        if (employeeId == null) {
            logger.debug("No employee row for user {} in company {}; IR author left null",
                    name, companyRefId);
        }
        return employeeId;
    }

    /**
     * Truck plate snapshot. Picking a truck wins over typed text; free text is
     * kept only when no truck was picked, which is how a hired lorry with no
     * TruckMaster row gets recorded.
     */
    private void applyTruck(IrMaster entity, IrSaveRequest request) {
        Integer truckRefId = zeroToNull(request.getTruckRefId());
        if (truckRefId == null) {
            entity.setTruckRefId(null);
            entity.setTruckNo(trimToNull(request.getTruckNo()));
            return;
        }

        TruckMaster truck = truckMasterRepository.findById(truckRefId)
                .filter(row -> request.getCompanyRefId().equals(row.getCompanyRefId()))
                .orElseThrow(() -> new InvalidRequestException(
                        "Truck " + request.getTruckRefId() + " was not found for this company"));

        entity.setTruckRefId(truck.getId());
        entity.setTruckNo(truck.getTruckNumber());
    }

    /**
     * Driver snapshot. A driver who is not ours has no DriverMaster row, so the
     * typed name is stored on its own with a null ref - that is the whole
     * outside-driver case, and why DriverName is what the grid reads.
     */
    private void applyDriver(IrMaster entity, IrSaveRequest request) {
        Integer driverRefId = zeroToNull(request.getDriverRefId());
        if (driverRefId == null) {
            entity.setDriverRefId(null);
            entity.setDriverName(trimToNull(request.getDriverName()));
            return;
        }

        DriverMaster driver = driverMasterRepository.findById(driverRefId)
                .filter(row -> request.getCompanyRefId().equals(row.getCompanyRefId()))
                .orElseThrow(() -> new InvalidRequestException(
                        "Driver " + request.getDriverRefId() + " was not found for this company"));

        entity.setDriverRefId(driver.getId());
        entity.setDriverName(driver.getDriverName());
    }

    private void applyEmployee(IrMaster entity, IrSaveRequest request) {
        Integer employeeRefId = zeroToNull(request.getEmployeeRefId());
        if (employeeRefId == null) {
            entity.setEmployeeRefId(null);
            entity.setEmployeeName(trimToNull(request.getEmployeeName()));
            return;
        }

        EmployeeMaster employee = employeeMasterRepository.findById(employeeRefId)
                .filter(row -> request.getCompanyRefId().equals(row.getCompanyRefId()))
                .orElseThrow(() -> new InvalidRequestException(
                        "Employee " + request.getEmployeeRefId() + " was not found for this company"));

        entity.setEmployeeRefId(employee.getId());
        entity.setEmployeeName(employee.getEmployeeName());
    }

    // ---------------------------------------------------------------- delete

    /**
     * Soft delete. The row stays so the incident history and its attachments
     * survive; every read filters Active &lt;&gt; 2.
     */
    @Override
    @Transactional
    public void delete(Integer id, Integer companyRefId, String username) {
        requireCompany(companyRefId);

        IrMaster entity = requireExisting(id, companyRefId);
        entity.setActive(IrMaster.DELETED);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy(stamp(username));
        irMasterRepository.save(entity);

        logger.info("Soft-deleted IR {} for company {}", id, companyRefId);
    }

    // --------------------------------------------------------------- lookups

    @Override
    @Transactional(readOnly = true)
    public List<IrStatusOptionDto> statuses(Integer companyRefId) {
        requireCompany(companyRefId);
        return irStatusMasterRepository
                .findByCompanyRefIdAndActiveOrderByIdAsc(companyRefId, IrMaster.ACTIVE).stream()
                .map(status -> IrStatusOptionDto.builder()
                        .id(status.getId())
                        .statusCode(status.getStatusCode())
                        .statusName(status.getStatusName())
                        .colorCode(status.getColorCode())
                        .finished(isFinished(status))
                        .build())
                .toList();
    }

    @Override
    public List<IrDepartmentOptionDto> departments() {
        List<IrDepartmentOptionDto> options = new ArrayList<>();
        for (UserRoles role : UserRoles.values()) {
            options.add(IrDepartmentOptionDto.builder()
                    .id(role.getRoleId())
                    .name(role.name())
                    .build());
        }
        return options;
    }

    // --------------------------------------------------------------- helpers

    private IrMaster requireExisting(Integer id, Integer companyRefId) {
        if (id == null || id == 0) {
            throw new InvalidRequestException("id is required");
        }
        return irMasterRepository.findByIdAndCompanyRefIdAndActive(id, companyRefId, IrMaster.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("IR " + id + " was not found"));
    }

    private Map<Integer, IrStatusMaster> statusesById(Integer companyRefId) {
        return irStatusMasterRepository
                .findByCompanyRefIdAndActiveOrderByIdAsc(companyRefId, IrMaster.ACTIVE).stream()
                .collect(Collectors.toMap(IrStatusMaster::getId, Function.identity(), (a, b) -> a));
    }

    private IrStatusMaster findStatus(Integer statusRefId, Integer companyRefId) {
        if (statusRefId == null) {
            return null;
        }
        return irStatusMasterRepository
                .findByIdAndCompanyRefIdAndActive(statusRefId, companyRefId, IrMaster.ACTIVE)
                .orElse(null);
    }

    private List<Integer> finishedStatusIds(Integer companyRefId) {
        return irStatusMasterRepository
                .findByCompanyRefIdAndStatusCodeIn(companyRefId, FINISHED_STATUS_CODES).stream()
                .map(IrStatusMaster::getId)
                .toList();
    }

    private boolean isFinished(IrStatusMaster status) {
        return status.getStatusCode() != null
                && FINISHED_STATUS_CODES.contains(status.getStatusCode().trim().toUpperCase());
    }

    /** Names of the given employee ids, in one query. */
    private Map<Integer, String> employeeNames(Set<Integer> employeeIds) {
        if (employeeIds.isEmpty()) {
            return new HashMap<>();
        }
        return employeeMasterRepository.findAllById(employeeIds).stream()
                .filter(employee -> employee.getEmployeeName() != null)
                .collect(Collectors.toMap(EmployeeMaster::getId, EmployeeMaster::getEmployeeName,
                        (a, b) -> a));
    }

    /**
     * Null-key-safe map read.
     *
     * <p>{@code CreateEmployeeRefId} is null on every row written before that
     * column existed, and an immutable {@code Map.of()} throws on
     * {@code get(null)} rather than returning null the way a HashMap does. That
     * threw a 500 on the whole list when no row had an author.
     */
    private static <K, V> V lookup(Map<K, V> map, K key) {
        return key == null ? null : map.get(key);
    }

    private String employeeName(Integer employeeId) {
        if (employeeId == null) {
            return null;
        }
        return employeeMasterRepository.findById(employeeId)
                .map(EmployeeMaster::getEmployeeName)
                .orElse(null);
    }

    private IrDetailDto toDto(IrMaster entity, IrStatusMaster status, String createEmployeeName) {
        return IrDetailDto.builder()
                .id(entity.getId())
                .companyRefId(entity.getCompanyRefId())
                .irDate(entity.getIrDate())
                .irStatusRefId(entity.getIrStatusRefId())
                .statusCode(status == null ? null : status.getStatusCode())
                .statusName(status == null ? null : status.getStatusName())
                .statusColor(status == null ? null : status.getColorCode())
                .description(entity.getDescription())
                .reason(entity.getReason())
                .departmentRefId(entity.getDepartmentRefId())
                .departmentName(entity.getDepartmentName())
                .vesselName(entity.getVesselName())
                .truckRefId(entity.getTruckRefId())
                .truckNo(entity.getTruckNo())
                .employeeRefId(entity.getEmployeeRefId())
                .employeeName(entity.getEmployeeName())
                .driverRefId(entity.getDriverRefId())
                .driverName(entity.getDriverName())
                .actualAmount(entity.getActualAmount())
                .createEmployeeRefId(entity.getCreateEmployeeRefId())
                .createEmployeeName(createEmployeeName)
                .filePath(entity.getFilePath())
                .createdDate(entity.getCreatedDate())
                .createdBy(entity.getCreatedBy())
                .modifiedDate(entity.getModifiedDate())
                .modifiedBy(entity.getModifiedBy())
                .build();
    }

    private static void requireCompany(Integer companyRefId) {
        if (companyRefId == null || companyRefId <= 0) {
            throw new InvalidRequestException("companyRefId is required");
        }
    }

    /** 0 means "not chosen" on every dropdown in this system. */
    private static Integer zeroToNull(Integer value) {
        return value == null || value == 0 ? null : value;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Created_By/Modified_By are varchar(50); a longer principal would fail the insert. */
    private static String stamp(String username) {
        String name = trimToNull(username) == null ? "system" : username.trim();
        return name.length() > 50 ? name.substring(0, 50) : name;
    }
}
