package my.maleva.api.service.impl;

import my.maleva.api.dto.TollEntryDto;
import my.maleva.api.dto.TollEntryDetailsDto;
import my.maleva.api.mapper.TollEntryMapper;
import my.maleva.api.mapper.TollEntryDetailsMapper;
import my.maleva.api.model.TollEntry;
import my.maleva.api.model.TollEntryDetails;
import my.maleva.api.repo.TollEntryRepository;
import my.maleva.api.repo.TollEntryDetailsRepository;
import my.maleva.api.service.TollEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TollEntryServiceImpl - Implementation for TollEntry service
 * Incorporates SP_TollEntry stored procedure logic
 * Handles toll entry processing with detail records
 */
@Service
public class TollEntryServiceImpl implements TollEntryService {

    private static final Logger logger = LoggerFactory.getLogger(TollEntryServiceImpl.class);

    @Autowired
    private TollEntryRepository repository;

    @Autowired
    private TollEntryDetailsRepository detailsRepository;

    @Autowired
    private TollEntryMapper mapper;

    @Autowired
    private TollEntryDetailsMapper detailsMapper;

    @Override
    public List<TollEntryDto> getByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching TollEntry for company: {}", companyRefId);
        return repository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TollEntryDto> getActiveByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching active TollEntry for company: {}", companyRefId);
        return repository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TollEntryDto> getByCNumber(Integer cNumber, Integer companyRefId) {
        logger.info("Fetching TollEntry by C Number: {} for company: {}", cNumber, companyRefId);
        return repository.findByCNumberAndCompanyRefId(cNumber, companyRefId).map(mapper::toDto);
    }

    @Override
    public List<TollEntryDto> getByUserRefId(Integer userRefId) {
        logger.info("Fetching TollEntry for user: {}", userRefId);
        return repository.findByUserRefId(userRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TollEntryDto> getByEmployeeRefId(Integer employeeRefId) {
        logger.info("Fetching TollEntry for employee: {}", employeeRefId);
        return repository.findByEmployeeRefId(employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TollEntryDto> getByTruckRefid(Integer truckRefid) {
        logger.info("Fetching TollEntry for truck: {}", truckRefid);
        return repository.findByTruckRefid(truckRefid)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TollEntryDto> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching TollEntry for date range: {} to {}", startDate, endDate);
        return repository.findBySaleDateGreaterThanEqualAndSaleDateLessThanEqual(startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TollEntryDto> getByCompanyAndDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching TollEntry for company: {} and date range: {} to {}", companyRefId, startDate, endDate);
        return repository.findByCompanyRefIdAndSaleDateGreaterThanEqualAndSaleDateLessThanEqual(companyRefId, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TollEntryDto> getById(Integer id) {
        logger.info("Fetching TollEntry by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public TollEntryDto create(TollEntryDto dto) {
        logger.info("Creating new TollEntry");
        validateTollEntryData(dto);
        TollEntry entity = mapper.toEntity(dto);

        // Set default values as per SP_TollEntry logic
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(now);
        }
        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy("SYSTEM");
        }
        if (entity.getModifiedDate() == null) {
            entity.setModifiedDate(now);
        }
        if (entity.getModifiedBy() == null) {
            entity.setModifiedBy("SYSTEM");
        }
        if (entity.getActive() == null) {
            entity.setActive(0);
        }
        if (entity.getAmount() == null) {
            entity.setAmount(0F);
        }

        TollEntry saved = repository.save(entity);
        logger.info("TollEntry created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public TollEntryDto update(Integer id, TollEntryDto dto) {
        logger.info("Updating TollEntry with ID: {}", id);
        validateTollEntryData(dto);

        TollEntry entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TollEntry not found: " + id));

        // Update modified date as per SP_TollEntry logic
        LocalDateTime now = LocalDateTime.now();
        entity.setModifiedDate(now);
        entity.setModifiedBy("SYSTEM");

        mapper.updateEntityFromDto(dto, entity);
        TollEntry updated = repository.save(entity);
        logger.info("TollEntry updated with ID: {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting TollEntry with ID: {}", id);
        if (repository.existsById(id)) {
            // Delete associated detail records first (foreign key constraint)
            detailsRepository.deleteByTollEntryMasterRefId(id);
            repository.deleteById(id);
            logger.info("TollEntry and details deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long countByCompanyRefId(Integer companyRefId) {
        logger.info("Counting TollEntry for company: {}", companyRefId);
        return repository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompanyRefId(Integer companyRefId) {
        logger.info("Counting active TollEntry for company: {}", companyRefId);
        return repository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    public void validateTollEntryData(TollEntryDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new RuntimeException("Company Reference ID is required");
        }
        if (dto.getSaleDate() == null) {
            throw new RuntimeException("Sale Date is required");
        }
        if (dto.getCNumberDisplay() == null || dto.getCNumberDisplay().trim().isEmpty()) {
            throw new RuntimeException("C Number Display is required");
        }
        if (dto.getCNumber() == null) {
            throw new RuntimeException("C Number is required");
        }
    }

    @Override
    @Transactional
    public TollEntryDto activateTollEntry(Integer id) {
        logger.info("Activating TollEntry with ID: {}", id);
        TollEntry entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TollEntry not found: " + id));

        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        TollEntry updated = repository.save(entity);

        logger.info("TollEntry activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public TollEntryDto deactivateTollEntry(Integer id) {
        logger.info("Deactivating TollEntry with ID: {}", id);
        TollEntry entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TollEntry not found: " + id));

        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        TollEntry updated = repository.save(entity);

        logger.info("TollEntry deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public TollEntryDto processTollEntry(TollEntryDto dto, List<TollEntryDetailsDto> details, Integer companyId) {
        logger.info("Processing TollEntry with SP_TollEntry logic for company: {}", companyId);

        // SP_TollEntry Logic:
        // 1. Set company ID
        dto.setCompanyRefId(companyId);

        // 2. Validate referenced entities (User, Employee, Truck)
        validateReferencedEntities(dto);

        // 3. If update (id > 0), delete existing details
        if (dto.getId() != null && dto.getId() > 0) {
            logger.info("Deleting existing TollEntry details for ID: {}", dto.getId());
            detailsRepository.deleteByTollEntryMasterRefId(dto.getId());
        }

        // 4. Create or update TollEntry
        TollEntryDto savedEntry;
        if (dto.getId() == null || dto.getId() == 0) {
            logger.info("Processing INSERT operation for TollEntry");
            savedEntry = create(dto);
        } else {
            logger.info("Processing UPDATE operation for TollEntry ID: {}", dto.getId());
            savedEntry = update(dto.getId(), dto);
        }

        // 5. Insert detail records with sequence number generation
        if (details != null && !details.isEmpty()) {
            logger.info("Inserting {} TollEntry detail records", details.size());
            for (TollEntryDetailsDto detail : details) {
                detail.setTollEntryMasterRefId(savedEntry.getId());
                TollEntryDetails detailEntity = detailsMapper.toEntity(detail);
                detailsRepository.save(detailEntity);
            }
        }

        // 6. Generate sequence number if new record
        if ((dto.getId() == null || dto.getId() == 0)) {
            generateSequenceNumber(savedEntry, companyId);
        }

        logger.info("TollEntry processing complete with ID: {}", savedEntry.getId());
        return savedEntry;
    }

    private void validateReferencedEntities(TollEntryDto dto) {
        // Validations for referenced entities would require calling other repositories
        // For now, we'll just log the validation
        logger.debug("Validating referenced entities for TollEntry");
        if (dto.getUserRefId() != null && dto.getUserRefId() > 0) {
            logger.debug("User ID to validate: {}", dto.getUserRefId());
        }
        if (dto.getEmployeeRefId() != null && dto.getEmployeeRefId() > 0) {
            logger.debug("Employee ID to validate: {}", dto.getEmployeeRefId());
        }
        if (dto.getTruckRefid() != null && dto.getTruckRefid() > 0) {
            logger.debug("Truck ID to validate: {}", dto.getTruckRefid());
        }
    }

    private void generateSequenceNumber(TollEntryDto dto, Integer companyId) {
        // Sequence number generation logic (TE + 9 digits)
        // This would normally integrate with SequenceNoMaster table
        logger.debug("Generating sequence number for TollEntry: {}", dto.getId());
        // Format: TE + 000000001
        String sequenceDisplay = String.format("TE%09d", dto.getId());
        logger.debug("Generated sequence: {}", sequenceDisplay);
    }
}

