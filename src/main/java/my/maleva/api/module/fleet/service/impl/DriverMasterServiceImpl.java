package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.module.fleet.dto.DriverMasterDto;
import my.maleva.api.module.fleet.dto.DriverSearchResultDto;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.mapper.DriverMasterMapper;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.service.DriverMasterService;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.accountsgroupmaster.repository.AccountsGroupMasterRepository;
import my.maleva.api.module.leave.repository.LeaveRequestRepository;
import my.maleva.api.module.leave.mapper.LeaveRequestMapper;
import my.maleva.api.module.leave.dto.response.LeaveRequestResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DriverMasterServiceImpl - Implementation of DriverMasterService
 * Handles CRUD operations and search functionality with pagination
 */
@Service
@Transactional(readOnly = true)
public class DriverMasterServiceImpl implements DriverMasterService {

    private static final Logger logger = LoggerFactory.getLogger(DriverMasterServiceImpl.class);

    private final DriverMasterRepository repository;
    private final DriverMasterMapper mapper;
    private final EntityManager entityManager;
    private final AccountsGroupMasterRepository accountsGroupMasterRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestMapper leaveRequestMapper;

    public DriverMasterServiceImpl(DriverMasterRepository repository, DriverMasterMapper mapper, EntityManager entityManager, AccountsGroupMasterRepository accountsGroupMasterRepository, LeaveRequestRepository leaveRequestRepository, LeaveRequestMapper leaveRequestMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
        this.accountsGroupMasterRepository = accountsGroupMasterRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveRequestMapper = leaveRequestMapper;
    }

    @Override
    public List<DriverMasterDto> listAll() {
        logger.info("Fetching all active drivers (active=1)");
        return repository.findByActive(1).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public DriverMasterDto getById(Integer id) {
        logger.info("Fetching driver by ID: {}", id);
        DriverMaster ent = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DriverMaster not found: " + id));
        return mapper.toDto(ent);
    }

    /** Legacy delete marker on DriverMaster.Active - see {@link #delete(Integer)}. */
    private static final int DELETED = 2;

    @Override
    @Transactional
    public DriverMasterDto create(DriverMasterDto dto) {
        logger.info("Creating new driver");
        LocalDateTime now = LocalDateTime.now();
        DriverMaster ent = mapper.toEntity(dto);

        // The screen sends Id 0 for "this driver is new" - the convention the
        // legacy SP_Driver payload used. An Integer 0 is not null, so save() read
        // the row as an existing one and issued an UPDATE against Id 0, which
        // matches nothing: "Row was already updated or deleted by another
        // transaction ... with id '0'". Clearing it is what makes this an INSERT
        // and lets IDENTITY hand out the real id.
        ent.setId(null);

        ent.setCreatedDate(now);
        ent.setModifiedDate(now);

        // Defaults the row cannot go in without. processDriver sets these too,
        // but a driver created straight through POST /api/driver-masters comes
        // here without passing that way.
        assignCNumber(ent, dto.getCompanyRefId());
        if (ent.getActive() == null) {
            ent.setActive(1);
        }
        if (ent.getAccountRefid() == null) {
            ent.setAccountRefid(1);
        }
        if (ent.getModifiedBy() == null || ent.getModifiedBy().isBlank()) {
            ent.setModifiedBy("SYSTEM");
        }

        DriverMaster saved = repository.save(ent);
        logger.info("Driver created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    /**
     * Insert or update, generating the driver number for a new record.
     *
     * The legacy screen relied on SP_Driver to allocate CNumber; this does the
     * same by taking the company's highest number and adding one, so the
     * displayed number keeps the D000000123 shape the reports expect.
     */
    @Override
    @Transactional
    public DriverMasterDto processDriver(DriverMasterDto dto, Integer companyId) {
        logger.info("Processing driver for company: {}", companyId);
        dto.setCompanyRefId(companyId);

        if (dto.getActive() == null) {
            dto.setActive(1);
        }
        if (dto.getAccountRefid() == null) {
            dto.setAccountRefid(1);
        }
        if (dto.getModifiedBy() == null || dto.getModifiedBy().isBlank()) {
            dto.setModifiedBy("SYSTEM");
        }

        // The form posts Id 0 for a new driver, so anything that is not a real
        // row id means "new" - and it is cleared here so nothing downstream can
        // mistake 0 for an existing row.
        Integer id = dto.getId() == null || dto.getId() <= 0 ? null : dto.getId();
        dto.setId(id);

        if (id == null) {
            // create() allocates the driver number, so a driver saved straight
            // through POST /api/driver-masters is numbered the same way.
            return create(dto);
        }

        // An edit must never renumber the driver. The form posts CNumber 0 and
        // CNumberDisplay "AUTO" whenever it did not load the stored numbering;
        // leaving them null here keeps what is on the row, because the mapper
        // ignores nulls.
        if (dto.getCNumber() == null || dto.getCNumber() <= 0) {
            dto.setCNumber(null);
            dto.setCNumberDisplay(null);
        } else if (isGeneratedCNumberDisplay(dto.getCNumberDisplay())) {
            dto.setCNumberDisplay(null);
        }

        return update(id, dto);
    }

    /**
     * Gives a new driver its CNumber and the CNumberDisplay that shows it.
     *
     * <p>The number is the company's highest so far plus one - asked of the
     * database rather than counted in memory - and the display keeps the
     * {@code D000000123} shape the reports expect. A caller that already carries
     * a real number keeps it; only the blank and the {@code AUTO} placeholder the
     * form posts are filled in.
     *
     * <p>Two people adding a driver in the same second can still be handed the
     * same number: CNumber is not unique in the table and never was, and the
     * legacy screen read the maximum the same way. It is a display number, not a
     * key - the row is identified by Id.
     */
    private void assignCNumber(DriverMaster entity, Integer companyRefId) {
        Integer cNumber = entity.getCNumber();
        if (cNumber == null || cNumber <= 0) {
            Integer highest = repository.findMaxCNumber(companyRefId);
            cNumber = (highest == null ? 0 : highest) + 1;
            entity.setCNumber(cNumber);
        }
        if (isGeneratedCNumberDisplay(entity.getCNumberDisplay())) {
            entity.setCNumberDisplay(String.format("D%09d", cNumber));
        }
    }

    /** Blank, or the "AUTO" the form posts when it wants a number generated. */
    private boolean isGeneratedCNumberDisplay(String display) {
        return display == null || display.isBlank() || "AUTO".equalsIgnoreCase(display.trim());
    }

    @Override
    @Transactional
    public DriverMasterDto update(Integer id, DriverMasterDto dto) {
        logger.info("Updating driver with ID: {}", id);
        DriverMaster ent = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DriverMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        DriverMaster saved = repository.save(ent);
        logger.info("Driver updated with ID: {}", id);
        return mapper.toDto(saved);
    }

    /**
     * Soft delete, the way the legacy screen did it.
     *
     * Active = 2 is the delete marker the whole system already reads: the
     * driver search filters on `active != 2`, and RTI / fuel / planning rows
     * keep pointing at the driver, so removing the row outright would orphan
     * years of history. Active = 0 is a different thing - a driver still on
     * the books but not currently driving - so it must not be reused here.
     */
    @Override
    @Transactional
    public void delete(Integer id) {
        logger.info("Soft deleting driver with ID: {}", id);
        DriverMaster ent = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DriverMaster not found: " + id));
        ent.setActive(DELETED);
        ent.setModifiedDate(LocalDateTime.now());
        repository.save(ent);
        logger.info("Driver marked as deleted with ID: {}", id);
    }

    /**
     * Brings a soft-deleted driver back, as active.
     */
    @Override
    @Transactional
    public DriverMasterDto restore(Integer id) {
        logger.info("Restoring driver with ID: {}", id);
        DriverMaster ent = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DriverMaster not found: " + id));
        ent.setActive(1);
        ent.setModifiedDate(LocalDateTime.now());
        DriverMaster saved = repository.save(ent);
        logger.info("Driver restored with ID: {}", id);
        return mapper.toDto(saved);
    }

    /**
     * Search drivers with pagination and filtering
     * Equivalent to C# SelectDriver method
     *
     * Supports filtering by:
     * - DriverName: LIKE search
     * - MobileNo: LIKE search
     * - Id: Exact match
     * - All: No additional filter (only company and active)
     *
     * Pagination:
     * - startIndex = -1 means get last page
     * - pageCount = 0 or negative means get all records
     */
    @Override
    public DriverSearchResultDto searchDrivers(Integer companyId, Integer startIndex, Integer pageCount, String keyword, String column) {
        logger.info("Searching drivers for company:{} startIndex:{} pageCount:{} keyword:{} column:{}", companyId, startIndex, pageCount, keyword, column);

        if (companyId == null) {
            throw new IllegalArgumentException("Company ID is required");
        }

        // Build base criteria query
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DriverMaster> cq = cb.createQuery(DriverMaster.class);
        Root<DriverMaster> root = cq.from(DriverMaster.class);

        // Build predicates list
        List<Predicate> predicates = new ArrayList<>();

        // Base filters: company and active status
        predicates.add(cb.equal(root.get("companyRefId"), companyId));
        predicates.add(cb.notEqual(root.get("active"), 2));

        // Apply search filters based on column
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword + "%";

            if ("DriverName".equalsIgnoreCase(column)) {
                logger.debug("Filtering by driver name");
                predicates.add(cb.like(root.get("driverName"), kw));
            } else if ("MobileNo".equalsIgnoreCase(column)) {
                logger.debug("Filtering by mobile number");
                predicates.add(cb.like(root.get("mobileNo"), kw));
            } else if ("Id".equalsIgnoreCase(column)) {
                logger.debug("Filtering by ID");
                try {
                    Integer idVal = Integer.valueOf(keyword);
                    predicates.add(cb.equal(root.get("id"), idVal));
                } catch (NumberFormatException ex) {
                    logger.warn("Invalid ID for search: {}", keyword);
                    return DriverSearchResultDto.builder()
                            .items(Collections.emptyList())
                            .totalCount(0L)
                            .build();
                }
            } else if ("All".equalsIgnoreCase(column)) {
                logger.debug("No additional column filter applied");
                // No additional predicate for "All"
            } else {
                throw new IllegalArgumentException("Unsupported column filter: " + column);
            }
        }

        // Apply predicates to main query
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("id")));

        // Get total count for pagination
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<DriverMaster> countRoot = countCq.from(DriverMaster.class);

        List<Predicate> countPredicates = new ArrayList<>();
        countPredicates.add(cb.equal(countRoot.get("companyRefId"), companyId));
        countPredicates.add(cb.notEqual(countRoot.get("active"), 2));

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword + "%";
            if ("DriverName".equalsIgnoreCase(column)) {
                countPredicates.add(cb.like(countRoot.get("driverName"), kw));
            } else if ("MobileNo".equalsIgnoreCase(column)) {
                countPredicates.add(cb.like(countRoot.get("mobileNo"), kw));
            } else if ("Id".equalsIgnoreCase(column)) {
                try {
                    Integer idVal = Integer.valueOf(keyword);
                    countPredicates.add(cb.equal(countRoot.get("id"), idVal));
                } catch (NumberFormatException ex) {
                    return DriverSearchResultDto.builder()
                            .items(Collections.emptyList())
                            .totalCount(0L)
                            .build();
                }
            }
        }

        countCq.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countCq).getSingleResult();

        // Handle pagination parameters
        if (startIndex == null) startIndex = 0;
        if (pageCount == null) pageCount = 0;

        // Calculate start index for last page if startIndex = -1
        if (startIndex == -1 && pageCount > 0) {
            if (total > pageCount) {
                long pages = (total - 1) / pageCount;
                startIndex = (int) (pages * pageCount);
            } else {
                startIndex = 0;
            }
        }

        // Create typed query and apply pagination
        TypedQuery<DriverMaster> typedQuery = entityManager.createQuery(cq);

        if (pageCount != null && pageCount > 0) {
            typedQuery.setFirstResult(Math.max(0, startIndex));
            typedQuery.setMaxResults(pageCount);
        }

        // Get results
        List<DriverMaster> results = typedQuery.getResultList();
        List<DriverMasterDto> dtos = results.stream().map(mapper::toDto).collect(Collectors.toList());

        // Enrich with AccountCode from AccountsGroupMaster
        for (int i = 0; i < results.size(); i++) {
            DriverMaster driver = results.get(i);
            DriverMasterDto dto = dtos.get(i);

            if (driver.getAccountRefid() != null) {
                accountsGroupMasterRepository.findByIdAndCompanyRefId(driver.getAccountRefid(), companyId)
                        .ifPresent(ag -> dto.setAccountCode(ag.getAccountCode()));
            }
        }

        logger.info("Found {} drivers out of {} total", results.size(), total);
        return DriverSearchResultDto.builder()
                .items(dtos)
                .totalCount(total)
                .build();
    }
    @Override
    public List<DriverMasterDto> getAllDriverDetails(Integer companyId) {
        logger.info("Fetching all driver details for company: {} with active = 1", companyId);
        
        if (companyId == null) {
            throw new IllegalArgumentException("Company ID is required");
        }

        List<DriverMaster> results = repository.findByCompanyRefIdAndActive(companyId, 1);
        List<DriverMasterDto> dtos = results.stream().map(mapper::toDto).collect(Collectors.toList());

        // Optimize N+1 Query Problem: Collect all AccountRefid and fetch at once
        List<Integer> accountRefIds = results.stream()
                .map(DriverMaster::getAccountRefid)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());

        if (!accountRefIds.isEmpty()) {
            java.util.Map<Integer, String> accountMap = accountsGroupMasterRepository.findByIdInAndCompanyRefId(accountRefIds, companyId)
                    .stream()
                    .collect(Collectors.toMap(my.maleva.api.module.accountsgroupmaster.entity.AccountsGroupMaster::getId, my.maleva.api.module.accountsgroupmaster.entity.AccountsGroupMaster::getAccountCode));

            for (DriverMasterDto dto : dtos) {
                if (dto.getAccountRefid() != null && accountMap.containsKey(dto.getAccountRefid())) {
                    dto.setAccountCode(accountMap.get(dto.getAccountRefid()));
                }
            }
        }

        // Optimize N+1 Query Problem for Leaves
        List<Integer> driverIds = dtos.stream().map(DriverMasterDto::getId).collect(Collectors.toList());
        if (!driverIds.isEmpty()) {
            java.time.LocalDateTime startDate = java.time.LocalDateTime.now().with(java.time.LocalTime.MIN);
            java.time.LocalDateTime endDate = startDate.plusDays(3).with(java.time.LocalTime.MAX);
            
            List<my.maleva.api.module.leave.entity.LeaveRequestMaster> leaves = leaveRequestRepository.findOverlappingLeavesForApplicants(2, driverIds, 1, startDate, endDate);
            if (!leaves.isEmpty()) {
                List<LeaveRequestResponseDto> leaveDtos = leaveRequestMapper.toResponseDtoList(leaves);
                java.util.Map<Integer, List<LeaveRequestResponseDto>> leavesByDriver = leaveDtos.stream()
                        .collect(Collectors.groupingBy(LeaveRequestResponseDto::getApplicantRefId));
                
                for (DriverMasterDto dto : dtos) {
                    dto.setLeaves(leavesByDriver.getOrDefault(dto.getId(), new ArrayList<>()));
                }
            } else {
                for (DriverMasterDto dto : dtos) {
                    dto.setLeaves(new ArrayList<>());
                }
            }
        }

        logger.info("Found {} active drivers for company {}", results.size(), companyId);
        return dtos;
    }
}
