package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.module.fleet.dto.TruckDriverContactDto;
import my.maleva.api.module.fleet.dto.TruckMasterDto;
import my.maleva.api.common.dto.ComboListModel;
import my.maleva.api.module.fleet.mapper.TruckMasterMapper;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.TruckSizeClass;
import my.maleva.api.module.fleet.entity.TruckStatus;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.fleet.service.TruckMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TruckMasterServiceImpl - Implementation for TruckMaster service
 * Incorporates SP_Truck stored procedure logic
 * Handles truck management with maintenance tracking
 */
@Service
public class TruckMasterServiceImpl implements TruckMasterService {

    private static final Logger logger = LoggerFactory.getLogger(TruckMasterServiceImpl.class);

    @Autowired
    private TruckMasterRepository repository;

    @Autowired
    private TruckMasterMapper mapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private my.maleva.api.module.accountsgroupmaster.repository.AccountsGroupMasterRepository accountsGroupMasterRepository;

    @Autowired
    private DriverMasterRepository driverMasterRepository;

    @Override
    public List<TruckMasterDto> getByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching TruckMaster for company: {}", companyRefId);
        return withDrivers(companyRefId, repository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList()));
    }

    @Override
    public List<TruckMasterDto> getActiveByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching active TruckMaster for company: {}", companyRefId);
        return withDrivers(companyRefId, repository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList()));
    }

    @Override
    public Optional<TruckMasterDto> getByTruckName(String truckName, Integer companyRefId) {
        logger.info("Fetching TruckMaster by name: {} for company: {}", truckName, companyRefId);
        return repository.findByTruckNameAndCompanyRefId(truckName, companyRefId).map(mapper::toDto);
    }

    @Override
    public Optional<TruckMasterDto> getByTruckNumber(String truckNumber, Integer companyRefId) {
        logger.info("Fetching TruckMaster by number: {} for company: {}", truckNumber, companyRefId);
        return repository.findByTruckNumberAndCompanyRefId(truckNumber, companyRefId).map(mapper::toDto);
    }

    @Override
    public Optional<TruckMasterDto> getByCNumber(Integer cNumber, Integer companyRefId) {
        logger.info("Fetching TruckMaster by C Number: {} for company: {}", cNumber, companyRefId);
        return repository.findByCNumberAndCompanyRefId(cNumber, companyRefId).map(mapper::toDto);
    }

    @Override
    public List<TruckMasterDto> getByTruckType(String truckType) {
        logger.info("Fetching TruckMaster for type: {}", truckType);
        return repository.findByTruckType(truckType)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TruckMasterDto> getByCompanyAndTruckType(Integer companyRefId, String truckType) {
        logger.info("Fetching TruckMaster for company: {} and type: {}", companyRefId, truckType);
        return repository.findByCompanyRefIdAndTruckType(companyRefId, truckType)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TruckMasterDto> getByVehicleType(String vehicleType) {
        logger.info("Fetching TruckMaster for vehicle type: {}", vehicleType);
        return repository.findByVehicleType(vehicleType)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TruckMasterDto> getById(Integer id) {
        logger.info("Fetching TruckMaster by ID: {}", id);
        // The edit form reads this, so it carries the driver too.
        return repository.findById(id).map(mapper::toDto).map(this::withDriver);
    }

    @Override
    @Transactional
    public TruckMasterDto create(TruckMasterDto dto) {
        logger.info("Creating new TruckMaster");
        validateTruckMasterData(dto);
        TruckMaster entity = mapper.toEntity(dto);

        // The screens send Id 0 for "this truck is new" - the convention the
        // legacy SP_Truck payload used, and what the Add Truck form still posts.
        // An Integer 0 is not null, so save() read the row as an existing one and
        // issued an UPDATE against Id 0, which matches no row: "Row was already
        // updated or deleted by another transaction ... with id '0'". Clearing it
        // is what makes this an INSERT and lets IDENTITY hand out the real id.
        entity.setId(null);

        // Set default values as per SP_Truck logic
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(now);
        }
        if (entity.getModifiedDate() == null) {
            entity.setModifiedDate(now);
        }
        if (entity.getModifiedBy() == null || entity.getModifiedBy().isBlank()) {
            entity.setModifiedBy("SYSTEM");
        }
        if (entity.getActive() == null) {
            entity.setActive(1);
        }
        if (entity.getAccountRefid() == null) {
            entity.setAccountRefid(1);
        }
        assignCNumber(entity, dto.getCompanyRefId());

        // Columns the insert has to state itself. Their DB defaults were added
        // with the columns and only apply when the column is left out of the
        // INSERT; Hibernate always lists every column, so without these a new
        // truck arrives with NULLs and quietly drops out of the fleet screens -
        // the maintenance dashboard reads MalevaTruck = 1 and the Truck Order
        // Calendar reads OrderableTruck = 1.
        if (entity.getMalevaTruck() == null) {
            entity.setMalevaTruck(1);
        }
        if (entity.getOrderableTruck() == null) {
            entity.setOrderableTruck(1);
        }
        if (entity.getTruckStatus() == null || entity.getTruckStatus().isBlank()) {
            entity.setTruckStatus(TruckStatus.ACTIVE.name());
        }

        // Convert to uppercase as per SP_Truck
        entity.setTruckName(entity.getTruckName().toUpperCase());
        entity.setTruckNumber(entity.getTruckNumber().toUpperCase());
        entity.setTruckType(entity.getTruckType().toUpperCase());
        if (entity.getVehicleType() != null) {
            entity.setVehicleType(entity.getVehicleType().toUpperCase());
        }
        if (entity.getSidExp() != null) {
            entity.setSidExp(entity.getSidExp().toUpperCase());
        }

        TruckMaster saved = repository.save(entity);
        logger.info("TruckMaster created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public TruckMasterDto update(Integer id, TruckMasterDto dto) {
        logger.info("Updating TruckMaster with ID: {}", id);
        validateTruckMasterData(dto);

        TruckMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TruckMaster not found: " + id));

        // Update modified date as per SP_Truck logic
        LocalDateTime now = LocalDateTime.now();
        entity.setModifiedDate(now);

        mapper.updateEntityFromDto(dto, entity);

        // Who edited it. Stamped after the mapper so a DTO that leaves this
        // blank cannot wipe the previous value, and SYSTEM only stands in when
        // nobody was named - it used to be written unconditionally, which meant
        // Modified_By said SYSTEM for every truck ever edited and the column
        // answered nothing.
        String editedBy = dto.getModifiedBy();
        entity.setModifiedBy(editedBy == null || editedBy.isBlank() ? "SYSTEM" : editedBy);

        // Convert to uppercase as per SP_Truck
        entity.setTruckName(entity.getTruckName().toUpperCase());
        entity.setTruckNumber(entity.getTruckNumber().toUpperCase());
        entity.setTruckType(entity.getTruckType().toUpperCase());
        if (entity.getVehicleType() != null) {
            entity.setVehicleType(entity.getVehicleType().toUpperCase());
        }
        if (entity.getSidExp() != null) {
            entity.setSidExp(entity.getSidExp().toUpperCase());
        }

        TruckMaster updated = repository.save(entity);
        logger.info("TruckMaster updated with ID: {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting TruckMaster with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("TruckMaster deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long countByCompanyRefId(Integer companyRefId) {
        logger.info("Counting TruckMaster for company: {}", companyRefId);
        return repository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompanyRefId(Integer companyRefId) {
        logger.info("Counting active TruckMaster for company: {}", companyRefId);
        return repository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    public void validateTruckMasterData(TruckMasterDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new RuntimeException("Company Reference ID is required");
        }
        if (dto.getTruckName() == null || dto.getTruckName().trim().isEmpty()) {
            throw new RuntimeException("Truck Name is required");
        }
        if (dto.getTruckNumber() == null || dto.getTruckNumber().trim().isEmpty()) {
            throw new RuntimeException("Truck Number is required");
        }
        if (dto.getTruckType() == null || dto.getTruckType().trim().isEmpty()) {
            throw new RuntimeException("Truck Type is required");
        }
    }

    @Override
    @Transactional
    public TruckMasterDto activateTruck(Integer id) {
        logger.info("Activating TruckMaster with ID: {}", id);
        TruckMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TruckMaster not found: " + id));

        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        TruckMaster updated = repository.save(entity);

        logger.info("TruckMaster activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public TruckMasterDto deactivateTruck(Integer id) {
        logger.info("Deactivating TruckMaster with ID: {}", id);
        TruckMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TruckMaster not found: " + id));

        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        TruckMaster updated = repository.save(entity);

        logger.info("TruckMaster deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    public boolean existsByTruckNumber(String truckNumber, Integer companyRefId) {
        logger.info("Checking if TruckMaster exists with number: {} for company: {}", truckNumber, companyRefId);
        return repository.existsByTruckNumberAndCompanyRefId(truckNumber, companyRefId);
    }

    @Override
    @Transactional
    public TruckMasterDto processTruck(TruckMasterDto dto, Integer companyId) {
        logger.info("Processing TruckMaster with SP_Truck logic for company: {}", companyId);

        // SP_Truck Logic:
        // 1. Set company ID
        dto.setCompanyRefId(companyId);

        // 2. Decide INSERT or UPDATE. The form posts Id 0 for a new truck, so
        //    anything that is not a real row id means "new" - and it is cleared
        //    here so nothing downstream can mistake 0 for an existing row.
        Integer id = dto.getId() == null || dto.getId() <= 0 ? null : dto.getId();
        dto.setId(id);

        if (id == null) {
            logger.info("Processing INSERT operation");
            // create() gives the truck its CNumber, so a truck that is saved
            // straight through POST /api/truck-masters is numbered the same way.
            return create(dto);
        }

        // 3. An edit must never renumber the truck. The form sends CNumber 0 and
        //    CNumberDisplay "AUTO" whenever it did not load the stored numbering;
        //    leaving them null here keeps what is on the row, because the mapper
        //    ignores nulls. Before this, a save from such a form moved the truck
        //    to a brand new number on every edit.
        if (dto.getCNumber() == null || dto.getCNumber() <= 0) {
            dto.setCNumber(null);
            dto.setCNumberDisplay(null);
        } else if (isGeneratedCNumberDisplay(dto.getCNumberDisplay())) {
            dto.setCNumberDisplay(null);
        }

        logger.info("Processing UPDATE operation for ID: {}", id);
        return update(id, dto);
    }

    /**
     * Gives a new truck its CNumber and the CNumberDisplay that shows it.
     *
     * <p>The number is the company's highest so far plus one - asked of the
     * database rather than counted in memory - and the display is that number as
     * {@code T000000001}. A caller that already carries a real number keeps it;
     * only the blank and the {@code AUTO} placeholder the form posts are filled
     * in.
     *
     * <p>Two people adding a truck in the same second can still be handed the
     * same number: CNumber is not unique in the table and never was, and the
     * legacy screen read the maximum the same way. It is a display number, not a
     * key - the row is identified by Id.
     */
    private void assignCNumber(TruckMaster entity, Integer companyRefId) {
        Integer cNumber = entity.getCNumber();
        if (cNumber == null || cNumber <= 0) {
            Integer highest = repository.findMaxCNumber(companyRefId);
            cNumber = (highest == null ? 0 : highest) + 1;
            entity.setCNumber(cNumber);
        }
        if (isGeneratedCNumberDisplay(entity.getCNumberDisplay())) {
            entity.setCNumberDisplay(String.format("T%09d", cNumber));
        }
    }

    /** Blank, or the "AUTO" the form posts when it wants a number generated. */
    private boolean isGeneratedCNumberDisplay(String display) {
        return display == null || display.isBlank() || "AUTO".equalsIgnoreCase(display.trim());
    }

    @Override
    public my.maleva.api.module.fleet.dto.SearchResultDto searchTrucks(Integer companyId, Integer startIndex, Integer pageCount, String keyword, String column, String type) {
        logger.info("Searching trucks for company:{} start:{} pageCount:{} keyword:{} column:{} type:{}", companyId, startIndex, pageCount, keyword, column, type);

        if (companyId == null) {
            throw new IllegalArgumentException("Company ID is required");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TruckMaster> cq = cb.createQuery(TruckMaster.class);
        Root<TruckMaster> root = cq.from(TruckMaster.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("companyRefId"), companyId));
        predicates.add(cb.notEqual(root.get("active"), 2));

        if (type != null && !type.trim().isEmpty()) {
            predicates.add(cb.equal(root.get("truckType"), type));
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword + "%";
            if ("TruckName".equalsIgnoreCase(column)) {
                predicates.add(cb.like(root.get("truckName"), kw));
            } else if ("Id".equalsIgnoreCase(column)) {
                try {
                    Integer idVal = Integer.valueOf(keyword);
                    predicates.add(cb.equal(root.get("id"), idVal));
                } catch (NumberFormatException ex) {
                    return my.maleva.api.module.fleet.dto.SearchResultDto.builder().items(java.util.Collections.emptyList()).totalCount(0).build();
                }
            } else if ("All".equalsIgnoreCase(column)) {
                // no extra predicate
            } else {
                throw new IllegalArgumentException("Unsupported column filter: " + column);
            }
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("id")));

        TypedQuery<TruckMaster> typedQuery = entityManager.createQuery(cq);

        // count query
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<TruckMaster> countRoot = countCq.from(TruckMaster.class);
        List<Predicate> countPreds = new ArrayList<>();
        countPreds.add(cb.equal(countRoot.get("companyRefId"), companyId));
        countPreds.add(cb.notEqual(countRoot.get("active"), 2));
        if (type != null && !type.trim().isEmpty()) {
            countPreds.add(cb.equal(countRoot.get("truckType"), type));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword + "%";
            if ("TruckName".equalsIgnoreCase(column)) {
                countPreds.add(cb.like(countRoot.get("truckName"), kw));
            } else if ("Id".equalsIgnoreCase(column)) {
                try {
                    Integer idVal = Integer.valueOf(keyword);
                    countPreds.add(cb.equal(countRoot.get("id"), idVal));
                } catch (NumberFormatException ex) {
                    return my.maleva.api.module.fleet.dto.SearchResultDto.builder().items(java.util.Collections.emptyList()).totalCount(0).build();
                }
            }
        }
        countCq.select(cb.count(countRoot)).where(countPreds.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countCq).getSingleResult();

        if (startIndex == null) startIndex = 0;
        if (pageCount == null) pageCount = 0;
        if (startIndex == -1 && pageCount > 0) {
            if (total > pageCount) {
                long pages = (total - 1) / pageCount;
                startIndex = (int) (pages * pageCount);
            } else {
                startIndex = 0;
            }
        }

        if (pageCount != null && pageCount > 0) {
            typedQuery.setFirstResult(Math.max(0, startIndex));
            typedQuery.setMaxResults(pageCount);
        }

        List<TruckMaster> results = typedQuery.getResultList();
        List<my.maleva.api.module.fleet.dto.TruckMasterDto> dtos = results.stream().map(mapper::toDto).collect(java.util.stream.Collectors.toList());

        for (int i = 0; i < results.size(); i++) {
            TruckMaster t = results.get(i);
            my.maleva.api.module.fleet.dto.TruckMasterDto dto = dtos.get(i);
            if (t.getAccountRefid() != null) {
                accountsGroupMasterRepository.findByIdAndCompanyRefId(t.getAccountRefid(), companyId)
                        .ifPresent(ag -> dto.setAccountCode(ag.getAccountCode()));
            }
        }

        withDrivers(companyId, dtos);

        return my.maleva.api.module.fleet.dto.SearchResultDto.builder().items(dtos).totalCount(total).build();
    }

    /**
     * Get Truck combo list for dropdown/UI
     * Equivalent to .NET GetTruck method
     *
     * SQL: SELECT Id, TruckName as AccountName FROM TruckMaster
     *      WHERE CompanyRefId = ? AND Active = 1 [AND TruckType = ?]
     *
     * @param companyId Company ID (required)
     * @param truckType Truck type filter (optional, null for all types)
     * @return List of ComboListModel with Id and TruckName
     */
    @Override
    public List<ComboListModel> getTruckCombo(Integer companyId, String truckType) {
        logger.info("Fetching truck combo for company: {}, truckType: {}", companyId, truckType);

        // Input validation
        if (companyId == null || companyId <= 0) {
            logger.error("Invalid company ID: {}", companyId);
            throw new IllegalArgumentException("Company ID must be positive");
        }

        List<ComboListModel> result;

        // Query with optional truck type filter
        if (truckType == null || truckType.trim().isEmpty()) {
            // No truck type filter - get all active trucks
            logger.debug("Fetching all active trucks for company: {}", companyId);
            result = repository.getTruckCombo(companyId);
        } else {
            // With truck type filter
            String cleanType = truckType.trim();
            logger.debug("Fetching trucks for company: {} with type: {}", companyId, cleanType);
            result = repository.getTruckComboByType(companyId, cleanType);
        }

        logger.info("Found {} trucks for company: {}", result.size(), companyId);
        return result;
    }    @Override
    public java.util.List<my.maleva.api.module.fleet.dto.TruckMasterDto> getAllTruckDetailCombo(Integer companyId, String keyword, String column, String type) {
        logger.info("Getting all truck detail combo for company:{} keyword:{} column:{} type:{}", companyId, keyword, column, type);

        if (companyId == null) {
            throw new IllegalArgumentException("Company ID is required");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TruckMaster> cq = cb.createQuery(TruckMaster.class);
        Root<TruckMaster> root = cq.from(TruckMaster.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("companyRefId"), companyId));
        predicates.add(cb.notEqual(root.get("active"), 2));

        if (type != null && !type.trim().isEmpty()) {
            predicates.add(cb.equal(root.get("truckType"), type));
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword + "%";
            if ("TruckName".equalsIgnoreCase(column)) {
                predicates.add(cb.like(root.get("truckName"), kw));
            } else if ("Id".equalsIgnoreCase(column)) {
                try {
                    Integer idVal = Integer.valueOf(keyword);
                    predicates.add(cb.equal(root.get("id"), idVal));
                } catch (NumberFormatException ex) {
                    return java.util.Collections.emptyList();
                }
            } else if ("All".equalsIgnoreCase(column)) {
                // no extra predicate
            } else {
                throw new IllegalArgumentException("Unsupported column filter: " + column);
            }
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("truckName"))); // Ordered by truck name for combo typically

        TypedQuery<TruckMaster> typedQuery = entityManager.createQuery(cq);
        
        List<TruckMaster> results = typedQuery.getResultList();
        List<my.maleva.api.module.fleet.dto.TruckMasterDto> dtos = results.stream().map(mapper::toDto).collect(java.util.stream.Collectors.toList());

        for (int i = 0; i < results.size(); i++) {
            TruckMaster t = results.get(i);
            my.maleva.api.module.fleet.dto.TruckMasterDto dto = dtos.get(i);
            if (t.getAccountRefid() != null) {
                accountsGroupMasterRepository.findByIdAndCompanyRefId(t.getAccountRefid(), companyId)
                        .ifPresent(ag -> dto.setAccountCode(ag.getAccountCode()));
            }
        }

        return dtos;
    }


    @Override
    public List<TruckDriverContactDto> getFleetDrivers(Integer companyRefId) {
        if (companyRefId == null) {
            throw new IllegalArgumentException("Company ID is required");
        }

        Map<Integer, DriverMaster> byTruck = new HashMap<>();
        for (DriverMaster driver : driverMasterRepository.findByCompanyRefIdAndTruckRefIdIsNotNull(companyRefId)) {
            byTruck.putIfAbsent(driver.getTruckRefId(), driver);
        }

        return repository.findOrderableTrucks(companyRefId).stream()
                .map(truck -> {
                    DriverMaster driver = byTruck.get(truck.getId());
                    TruckSizeClass size = TruckSizeClass.fromTruckType(truck.getTruckType());
                    return TruckDriverContactDto.builder()
                            .truckId(truck.getId())
                            .truckName(truck.getTruckName() == null ? null : truck.getTruckName().trim())
                            .truckNumber(truck.getTruckNumber() == null ? null : truck.getTruckNumber().trim())
                            .truckType(truck.getTruckType())
                            .sizeClass(size == null ? null : size.getCode())
                            .truckStatus(truck.getTruckStatus() == null ? "ACTIVE" : truck.getTruckStatus())
                            .workshopUntil(truck.getWorkshopUntil())
                            .driverRefId(driver == null ? null : driver.getId())
                            .driverName(driver == null ? null : driver.getDriverName())
                            .driverMobileNo(driver == null ? null : driver.getMobileNo())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ------------------------------------------------- driver of a truck

    /**
     * Fills the driver on a whole page of trucks with one query.
     *
     * <p>The pairing is stored on the driver, so it is read from there; nothing
     * is written to TruckMaster. If two drivers point at the same truck - which
     * the database does not prevent - the first by id wins here, and the next
     * assign on that truck tidies it.
     */
    private List<TruckMasterDto> withDrivers(Integer companyRefId, List<TruckMasterDto> trucks) {
        if (companyRefId == null || trucks == null || trucks.isEmpty()) {
            return trucks;
        }
        Map<Integer, DriverMaster> byTruck = new HashMap<>();
        for (DriverMaster driver : driverMasterRepository.findByCompanyRefIdAndTruckRefIdIsNotNull(companyRefId)) {
            byTruck.putIfAbsent(driver.getTruckRefId(), driver);
        }
        for (TruckMasterDto truck : trucks) {
            DriverMaster driver = byTruck.get(truck.getId());
            if (driver != null) {
                truck.setDriverRefId(driver.getId());
                truck.setDriverName(driver.getDriverName());
            }
        }
        return trucks;
    }

    /** The same, for one truck. */
    private TruckMasterDto withDriver(TruckMasterDto truck) {
        if (truck == null || truck.getId() == null) {
            return truck;
        }
        driverMasterRepository
                .findByCompanyRefIdAndTruckRefId(truck.getCompanyRefId(), truck.getId())
                .stream()
                .findFirst()
                .ifPresent(driver -> {
                    truck.setDriverRefId(driver.getId());
                    truck.setDriverName(driver.getDriverName());
                });
        return truck;
    }

    @Override
    @Transactional
    public TruckMasterDto updateStatus(Integer truckId, String truckStatus, java.time.LocalDate workshopUntil) {
        TruckMaster truck = repository.findById(truckId)
                .orElseThrow(() -> new IllegalArgumentException("Truck not found: " + truckId));

        // Rejects anything that is not one of the three, so a typo cannot leave a
        // truck in a state the calendar does not understand.
        TruckStatus status = TruckStatus.fromCode(truckStatus);

        truck.setTruckStatus(status.name());
        truck.setWorkshopUntil(status == TruckStatus.WORKSHOP ? workshopUntil : null);
        truck.setModifiedDate(LocalDateTime.now());
        TruckMaster saved = repository.save(truck);

        logger.info("Truck {} is now {}{}", truckId, status.name(),
                status == TruckStatus.WORKSHOP && workshopUntil != null ? " until " + workshopUntil : "");
        return withDriver(mapper.toDto(saved));
    }

    @Override
    @Transactional
    public TruckMasterDto assignDriver(Integer truckId, Integer driverRefId) {
        TruckMaster truck = repository.findById(truckId)
                .orElseThrow(() -> new IllegalArgumentException("Truck not found: " + truckId));

        Integer wanted = driverRefId == null || driverRefId == 0 ? null : driverRefId;

        // One truck, one default driver: release anyone else still holding it.
        for (DriverMaster holder : driverMasterRepository
                .findByCompanyRefIdAndTruckRefId(truck.getCompanyRefId(), truckId)) {
            if (wanted == null || !holder.getId().equals(wanted)) {
                holder.setTruckRefId(null);
                holder.setModifiedDate(LocalDateTime.now());
                driverMasterRepository.save(holder);
                logger.info("Released truck {} from driver {}", truckId, holder.getId());
            }
        }

        if (wanted != null) {
            DriverMaster driver = driverMasterRepository.findById(wanted)
                    .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + wanted));
            if (!driver.getCompanyRefId().equals(truck.getCompanyRefId())) {
                throw new IllegalArgumentException("That driver belongs to another company.");
            }
            // Moving a driver from another truck is allowed and is the normal
            // way to swap: their row simply re-points.
            driver.setTruckRefId(truckId);
            driver.setModifiedDate(LocalDateTime.now());
            driverMasterRepository.save(driver);
            logger.info("Truck {} is now driven by driver {}", truckId, wanted);
        }

        return withDriver(mapper.toDto(truck));
    }
}
