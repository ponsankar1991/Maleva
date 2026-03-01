package my.maleva.api.service.impl;

import my.maleva.api.dto.SummonDto;
import my.maleva.api.mapper.SummonMapper;
import my.maleva.api.model.Summon;
import my.maleva.api.repo.SummonRepository;
import my.maleva.api.service.SummonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SummonServiceImpl - Implementation for Summon service
 * Incorporates SP_Summon stored procedure logic for summon processing
 */
@Service
public class SummonServiceImpl implements SummonService {

    private static final Logger logger = LoggerFactory.getLogger(SummonServiceImpl.class);

    @Autowired
    private SummonRepository repository;

    @Autowired
    private SummonMapper mapper;

    @Override
    public List<SummonDto> getByTruckName(String truckName) {
        logger.info("Fetching Summon for truck: {}", truckName);
        return repository.findByTruckName(truckName)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SummonDto> getByDriverName(String driverName) {
        logger.info("Fetching Summon for driver: {}", driverName);
        return repository.findByDriverName(driverName)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SummonDto> getByComid(Integer comid) {
        logger.info("Fetching Summon for company: {}", comid);
        return repository.findByComid(comid)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SummonDto> getByEntryDate(LocalDate entryDate) {
        logger.info("Fetching Summon for entry date: {}", entryDate);
        return repository.findByEntryDate(entryDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SummonDto> getByEntryDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching Summon for date range: {} to {}", startDate, endDate);
        return repository.findByEntryDateGreaterThanEqualAndEntryDateLessThanEqual(startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SummonDto> getByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        logger.info("Fetching Summon for amount range: {} to {}", minAmount, maxAmount);
        return repository.findByAmountGreaterThanEqualAndAmountLessThanEqual(minAmount, maxAmount)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SummonDto> getByCountry(String country) {
        logger.info("Fetching Summon for country: {}", country);
        return repository.findByCountry(country)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SummonDto> getById(Integer id) {
        logger.info("Fetching Summon by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public Optional<SummonDto> getByTruckAndDriver(String truckName, String driverName) {
        logger.info("Fetching Summon by truck: {} and driver: {}", truckName, driverName);
        return repository.findByTruckNameAndDriverName(truckName, driverName).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SummonDto create(SummonDto dto) {
        logger.info("Creating new Summon");
        validateSummonData(dto);
        Summon entity = mapper.toEntity(dto);

        // Set default values as per SP_Summon logic
        if (entity.getModifiedDate() == null) {
            entity.setModifiedDate(LocalDateTime.now());
        }
        if (entity.getModifiedBy() == null) {
            entity.setModifiedBy("sa");
        }

        Summon saved = repository.save(entity);
        logger.info("Summon created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SummonDto update(Integer id, SummonDto dto) {
        logger.info("Updating Summon with ID: {}", id);
        validateSummonData(dto);

        Summon entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Summon not found: " + id));

        // Update modified date as per SP logic
        entity.setModifiedDate(LocalDateTime.now());

        mapper.updateEntityFromDto(dto, entity);
        Summon updated = repository.save(entity);
        logger.info("Summon updated with ID: {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting Summon with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("Summon deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long countByComid(Integer comid) {
        logger.info("Counting Summon for company: {}", comid);
        return repository.countByComid(comid);
    }

    @Override
    public long countByCountry(String country) {
        logger.info("Counting Summon for country: {}", country);
        return repository.countByCountry(country);
    }

    @Override
    public void validateSummonData(SummonDto dto) {
        if (dto.getTruckName() == null || dto.getTruckName().trim().isEmpty()) {
            throw new RuntimeException("Truck Name is required");
        }
        if (dto.getDriverName() == null || dto.getDriverName().trim().isEmpty()) {
            throw new RuntimeException("Driver Name is required");
        }
        if (dto.getSummon() == null || dto.getSummon().trim().isEmpty()) {
            throw new RuntimeException("Summon is required");
        }
        if (dto.getAmount() == null) {
            throw new RuntimeException("Amount is required");
        }
    }

    @Override
    @Transactional
    public SummonDto processSummon(SummonDto dto, Integer comid) {
        logger.info("Processing Summon - incorporating SP_Summon logic with company: {}", comid);

        // Set company ID as per SP_Summon logic
        dto.setComid(comid);
        dto.setModifiedDate(LocalDateTime.now());
        dto.setModifiedBy("sa");

        if (dto.getId() == null || dto.getId() == 0) {
            // New record - INSERT
            logger.info("Processing INSERT operation");
            return create(dto);
        } else {
            // Existing record - UPDATE
            logger.info("Processing UPDATE operation for ID: {}", dto.getId());
            return update(dto.getId(), dto);
        }
    }
}

