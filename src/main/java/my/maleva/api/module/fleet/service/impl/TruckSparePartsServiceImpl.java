package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.module.fleet.dto.TruckSparePartsDto;
import my.maleva.api.module.fleet.mapper.TruckSparePartsMapper;
import my.maleva.api.module.fleet.entity.TruckSpareParts;
import my.maleva.api.module.fleet.repository.TruckSparePartsRepository;
import my.maleva.api.module.fleet.service.TruckSparePartsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TruckSparePartsServiceImpl - Implementation for TruckSpareParts service
 * Incorporates SP_TruckSpareParts stored procedure logic
 * Handles spare parts tracking for trucks
 */
@Service
public class TruckSparePartsServiceImpl implements TruckSparePartsService {

    private static final Logger logger = LoggerFactory.getLogger(TruckSparePartsServiceImpl.class);

    @Autowired
    private TruckSparePartsRepository repository;

    @Autowired
    private TruckSparePartsMapper mapper;

    @Override
    public List<TruckSparePartsDto> getByComid(Integer comid) {
        logger.info("Fetching TruckSpareParts for company: {}", comid);
        return repository.findByComid(comid)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TruckSparePartsDto> getByTruckName(String truckName) {
        logger.info("Fetching TruckSpareParts for truck: {}", truckName);
        return repository.findByTruckName(truckName)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TruckSparePartsDto> getByTruckNameAndComid(String truckName, Integer comid) {
        logger.info("Fetching TruckSpareParts for truck: {} and company: {}", truckName, comid);
        return repository.findByTruckNameAndComid(truckName, comid)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TruckSparePartsDto> getByDriverName(String driverName) {
        logger.info("Fetching TruckSpareParts for driver: {}", driverName);
        return repository.findByDriverName(driverName)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TruckSparePartsDto> getByDriverNameAndComid(String driverName, Integer comid) {
        logger.info("Fetching TruckSpareParts for driver: {} and company: {}", driverName, comid);
        return repository.findByDriverNameAndComid(driverName, comid)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TruckSparePartsDto> getBySpareParts(String spareParts) {
        logger.info("Fetching TruckSpareParts for spare parts: {}", spareParts);
        return repository.findBySpareParts(spareParts)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TruckSparePartsDto> getByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching TruckSpareParts for date range: {} to {}", startDate, endDate);
        return repository.findByEntryDateGreaterThanEqualAndEntryDateLessThanEqual(startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TruckSparePartsDto> getByComidAndDateRange(Integer comid, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching TruckSpareParts for company: {} and date range: {} to {}", comid, startDate, endDate);
        return repository.findByComidAndEntryDateGreaterThanEqualAndEntryDateLessThanEqual(comid, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TruckSparePartsDto> getById(Integer id) {
        logger.info("Fetching TruckSpareParts by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public TruckSparePartsDto create(TruckSparePartsDto dto) {
        logger.info("Creating new TruckSpareParts");
        validateTruckSparePartsData(dto);
        TruckSpareParts entity = mapper.toEntity(dto);

        // Set default values as per SP_TruckSpareParts logic
        LocalDateTime now = LocalDateTime.now();
        if (entity.getModifiedDate() == null) {
            entity.setModifiedDate(now);
        }
        if (entity.getModifiedBy() == null) {
            entity.setModifiedBy("SYSTEM");
        }
        if (entity.getEntryDate() == null) {
            entity.setEntryDate(LocalDate.now());
        }

        TruckSpareParts saved = repository.save(entity);
        logger.info("TruckSpareParts created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public TruckSparePartsDto update(Integer id, TruckSparePartsDto dto) {
        logger.info("Updating TruckSpareParts with ID: {}", id);
        validateTruckSparePartsData(dto);

        TruckSpareParts entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TruckSpareParts not found: " + id));

        // Update modified date as per SP_TruckSpareParts logic
        LocalDateTime now = LocalDateTime.now();
        entity.setModifiedDate(now);

        mapper.updateEntityFromDto(dto, entity);
        TruckSpareParts updated = repository.save(entity);
        logger.info("TruckSpareParts updated with ID: {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting TruckSpareParts with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("TruckSpareParts deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long countByComid(Integer comid) {
        logger.info("Counting TruckSpareParts for company: {}", comid);
        return repository.countByComid(comid);
    }

    @Override
    public long countByTruckNameAndComid(String truckName, Integer comid) {
        logger.info("Counting TruckSpareParts for truck: {} and company: {}", truckName, comid);
        return repository.countByTruckNameAndComid(truckName, comid);
    }

    @Override
    public void validateTruckSparePartsData(TruckSparePartsDto dto) {
        if (dto.getTruckName() == null || dto.getTruckName().trim().isEmpty()) {
            throw new RuntimeException("Truck Name is required");
        }
        if (dto.getDriverName() == null || dto.getDriverName().trim().isEmpty()) {
            throw new RuntimeException("Driver Name is required");
        }
        if (dto.getSpareParts() == null || dto.getSpareParts().trim().isEmpty()) {
            throw new RuntimeException("Spare Parts is required");
        }
        if (dto.getAmount() == null || dto.getAmount().signum() <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }
    }

    @Override
    @Transactional
    public TruckSparePartsDto processTruckSpareParts(TruckSparePartsDto dto, Integer comid) {
        logger.info("Processing TruckSpareParts with SP_TruckSpareParts logic for company: {}", comid);

        // SP_TruckSpareParts Logic:
        // 1. Set company ID
        dto.setComid(comid);

        // 2. Set entry date if null
        if (dto.getEntryDate() == null) {
            dto.setEntryDate(LocalDate.now());
        }

        // 3. Create or update based on ID
        if (dto.getId() == null || dto.getId() == 0) {
            logger.info("Processing INSERT operation");
            return create(dto);
        } else {
            logger.info("Processing UPDATE operation for ID: {}", dto.getId());
            return update(dto.getId(), dto);
        }
    }
}

