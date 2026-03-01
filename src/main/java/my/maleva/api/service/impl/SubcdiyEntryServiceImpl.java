package my.maleva.api.service.impl;

import my.maleva.api.dto.SubcdiyEntryDto;
import my.maleva.api.mapper.SubcdiyEntryMapper;
import my.maleva.api.model.SubcdiyEntry;
import my.maleva.api.repo.SubcdiyEntryRepository;
import my.maleva.api.service.SubcdiyEntryService;
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
 * SubcdiyEntryServiceImpl - Implementation for SubcdiyEntry service
 * Handles subsidy entry processing with comprehensive validation and audit trail
 */
@Service
public class SubcdiyEntryServiceImpl implements SubcdiyEntryService {

    private static final Logger logger = LoggerFactory.getLogger(SubcdiyEntryServiceImpl.class);

    @Autowired
    private SubcdiyEntryRepository repository;

    @Autowired
    private SubcdiyEntryMapper mapper;

    @Override
    public List<SubcdiyEntryDto> getByActive(Integer active) {
        logger.info("Fetching SubcdiyEntry for active status: {}", active);
        return repository.findByActive(active)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubcdiyEntryDto> getByEntryDate(LocalDate entryDate) {
        logger.info("Fetching SubcdiyEntry for entry date: {}", entryDate);
        return repository.findByEntryDate(entryDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubcdiyEntryDto> getByEntryDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching SubcdiyEntry for date range: {} to {}", startDate, endDate);
        return repository.findByEntryDateGreaterThanEqualAndEntryDateLessThanEqual(startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubcdiyEntryDto> getByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        logger.info("Fetching SubcdiyEntry for amount range: {} to {}", minAmount, maxAmount);
        return repository.findByAmountGreaterThanEqualAndAmountLessThanEqual(minAmount, maxAmount)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubcdiyEntryDto> getByCreatedDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching SubcdiyEntry for created date range: {} to {}", startDate, endDate);
        return repository.findByCreatedDateGreaterThanEqualAndCreatedDateLessThanEqual(startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SubcdiyEntryDto> getById(Integer id) {
        logger.info("Fetching SubcdiyEntry by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SubcdiyEntryDto create(SubcdiyEntryDto dto) {
        logger.info("Creating new SubcdiyEntry");
        validateSubcdiyEntryData(dto);
        SubcdiyEntry entity = mapper.toEntity(dto);

        // Set default values
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(now);
        }
        if (entity.getModifiedDate() == null) {
            entity.setModifiedDate(now);
        }
        if (entity.getActive() == null) {
            entity.setActive(0);
        }

        SubcdiyEntry saved = repository.save(entity);
        logger.info("SubcdiyEntry created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SubcdiyEntryDto update(Integer id, SubcdiyEntryDto dto) {
        logger.info("Updating SubcdiyEntry with ID: {}", id);
        validateSubcdiyEntryData(dto);

        SubcdiyEntry entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubcdiyEntry not found: " + id));

        // Preserve created date and update modified date
        LocalDateTime now = LocalDateTime.now();
        entity.setModifiedDate(now);

        mapper.updateEntityFromDto(dto, entity);
        SubcdiyEntry updated = repository.save(entity);
        logger.info("SubcdiyEntry updated with ID: {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SubcdiyEntry with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("SubcdiyEntry deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long countByActive(Integer active) {
        logger.info("Counting SubcdiyEntry for active status: {}", active);
        return repository.countByActive(active);
    }

    @Override
    public boolean existsByEntryDate(LocalDate entryDate) {
        logger.info("Checking if SubcdiyEntry exists for date: {}", entryDate);
        return repository.existsByEntryDate(entryDate);
    }

    @Override
    public void validateSubcdiyEntryData(SubcdiyEntryDto dto) {
        if (dto.getActive() == null) {
            throw new RuntimeException("Active status is required");
        }
    }

    @Override
    @Transactional
    public SubcdiyEntryDto activateEntry(Integer id) {
        logger.info("Activating SubcdiyEntry with ID: {}", id);
        SubcdiyEntry entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubcdiyEntry not found: " + id));

        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        SubcdiyEntry updated = repository.save(entity);

        logger.info("SubcdiyEntry activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public SubcdiyEntryDto deactivateEntry(Integer id) {
        logger.info("Deactivating SubcdiyEntry with ID: {}", id);
        SubcdiyEntry entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubcdiyEntry not found: " + id));

        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        SubcdiyEntry updated = repository.save(entity);

        logger.info("SubcdiyEntry deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    public List<SubcdiyEntryDto> getAllActive() {
        logger.info("Fetching all active SubcdiyEntry records");
        return getByActive(1);
    }
}

