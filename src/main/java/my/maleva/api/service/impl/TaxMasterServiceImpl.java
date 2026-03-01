package my.maleva.api.service.impl;

import my.maleva.api.dto.TaxMasterDto;
import my.maleva.api.mapper.TaxMasterMapper;
import my.maleva.api.model.TaxMaster;
import my.maleva.api.repo.TaxMasterRepository;
import my.maleva.api.service.TaxMasterService;
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
 * TaxMasterServiceImpl - Implementation for TaxMaster service
 * Incorporates SP_TaxMaster stored procedure logic
 * Handles tax configuration with batch processing
 */
@Service
public class TaxMasterServiceImpl implements TaxMasterService {

    private static final Logger logger = LoggerFactory.getLogger(TaxMasterServiceImpl.class);

    @Autowired
    private TaxMasterRepository repository;

    @Autowired
    private TaxMasterMapper mapper;

    @Override
    public List<TaxMasterDto> getByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching TaxMaster for company: {}", companyRefId);
        return repository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaxMasterDto> getActiveByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching active TaxMaster for company: {}", companyRefId);
        return repository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TaxMasterDto> getByCode(String code, Integer companyRefId) {
        logger.info("Fetching TaxMaster by code: {} for company: {}", code, companyRefId);
        return repository.findByCodeAndCompanyRefId(code, companyRefId).map(mapper::toDto);
    }

    @Override
    public Optional<TaxMasterDto> getByDescription(String description, Integer companyRefId) {
        logger.info("Fetching TaxMaster by description: {} for company: {}", description, companyRefId);
        return repository.findByDescriptionAndCompanyRefIdAndActive(description, companyRefId, 1).map(mapper::toDto);
    }

    @Override
    public List<TaxMasterDto> getByTaxIO(Integer taxIO) {
        logger.info("Fetching TaxMaster for tax IO: {}", taxIO);
        return repository.findByTaxIO(taxIO)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaxMasterDto> getByCompanyAndTaxIO(Integer companyRefId, Integer taxIO) {
        logger.info("Fetching TaxMaster for company: {} and tax IO: {}", companyRefId, taxIO);
        return repository.findByCompanyRefIdAndTaxIO(companyRefId, taxIO)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TaxMasterDto> getById(Integer id) {
        logger.info("Fetching TaxMaster by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public TaxMasterDto create(TaxMasterDto dto) {
        logger.info("Creating new TaxMaster");
        validateTaxMasterData(dto);
        TaxMaster entity = mapper.toEntity(dto);

        // Set default values as per table schema
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(now);
        }
        if (entity.getModifiedDate() == null) {
            entity.setModifiedDate(now);
        }
        if (entity.getModifiedBy() == null) {
            entity.setModifiedBy("SYSTEM");
        }
        if (entity.getActive() == null) {
            entity.setActive(1);
        }
        if (entity.getTax() == null) {
            entity.setTax(0F);
        }
        if (entity.getTaxIO() == null) {
            entity.setTaxIO(0);
        }

        TaxMaster saved = repository.save(entity);
        logger.info("TaxMaster created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public TaxMasterDto update(Integer id, TaxMasterDto dto) {
        logger.info("Updating TaxMaster with ID: {}", id);
        validateTaxMasterData(dto);

        TaxMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TaxMaster not found: " + id));

        // Preserve created date and update modified date
        LocalDateTime now = LocalDateTime.now();
        entity.setModifiedDate(now);
        entity.setModifiedBy("SYSTEM");

        mapper.updateEntityFromDto(dto, entity);
        TaxMaster updated = repository.save(entity);
        logger.info("TaxMaster updated with ID: {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting TaxMaster with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("TaxMaster deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long countByCompanyRefId(Integer companyRefId) {
        logger.info("Counting TaxMaster for company: {}", companyRefId);
        return repository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompanyRefId(Integer companyRefId) {
        logger.info("Counting active TaxMaster for company: {}", companyRefId);
        return repository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    public void validateTaxMasterData(TaxMasterDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new RuntimeException("Company Reference ID is required");
        }
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw new RuntimeException("Tax Code is required");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Description is required");
        }
        if (dto.getTax() == null) {
            throw new RuntimeException("Tax rate is required");
        }
        if (dto.getTaxIO() == null) {
            throw new RuntimeException("Tax IO is required");
        }
    }

    @Override
    @Transactional
    public TaxMasterDto activateTax(Integer id) {
        logger.info("Activating TaxMaster with ID: {}", id);
        TaxMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TaxMaster not found: " + id));

        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        TaxMaster updated = repository.save(entity);

        logger.info("TaxMaster activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public TaxMasterDto deactivateTax(Integer id) {
        logger.info("Deactivating TaxMaster with ID: {}", id);
        TaxMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TaxMaster not found: " + id));

        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        TaxMaster updated = repository.save(entity);

        logger.info("TaxMaster deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    public boolean existsByCode(String code, Integer companyRefId) {
        logger.info("Checking if TaxMaster exists with code: {} for company: {}", code, companyRefId);
        return repository.existsByCodeAndCompanyRefId(code, companyRefId);
    }

    @Override
    @Transactional
    public TaxMasterDto processTaxMaster(TaxMasterDto dto, Integer companyId, Integer checkFlag) {
        logger.info("Processing TaxMaster with SP_TaxMaster logic for company: {} with check flag: {}", companyId, checkFlag);

        // Set company ID
        dto.setCompanyRefId(companyId);

        // SP_TaxMaster logic: If Check flag = 1, check if tax exists by description
        if (checkFlag != null && checkFlag == 1) {
            logger.info("Check flag enabled - checking if tax with description exists");
            Optional<TaxMaster> existing = repository.findByDescriptionAndCompanyRefIdAndActive(
                    dto.getDescription(), companyId, 1);

            if (existing.isPresent()) {
                logger.info("Tax with description already exists, updating existing record ID: {}", existing.get().getId());
                return update(existing.get().getId(), dto);
            }
        }

        // Standard insert/update logic
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

