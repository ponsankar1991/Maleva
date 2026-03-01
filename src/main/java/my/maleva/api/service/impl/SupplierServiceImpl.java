package my.maleva.api.service.impl;

import my.maleva.api.dto.SupplierDto;
import my.maleva.api.mapper.SupplierMapper;
import my.maleva.api.model.Supplier;
import my.maleva.api.repo.SupplierRepository;
import my.maleva.api.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SupplierServiceImpl - Implementation for Supplier service
 * Handles comprehensive supplier/vendor management with audit trail
 */
@Service
public class SupplierServiceImpl implements SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierServiceImpl.class);

    @Autowired
    private SupplierRepository repository;

    @Autowired
    private SupplierMapper mapper;

    @Override
    public List<SupplierDto> getByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching Supplier for company: {}", companyRefId);
        return repository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SupplierDto> getBySupplierName(String supplierName) {
        logger.info("Fetching Supplier by name: {}", supplierName);
        return repository.findBySupplierName(supplierName).map(mapper::toDto);
    }

    @Override
    public Optional<SupplierDto> getByCNumber(Integer cNumber, Integer companyRefId) {
        logger.info("Fetching Supplier by C Number: {} for company: {}", cNumber, companyRefId);
        return repository.findByCNumberAndCompanyRefId(cNumber, companyRefId).map(mapper::toDto);
    }

    @Override
    public List<SupplierDto> getActiveByCompany(Integer companyRefId) {
        logger.info("Fetching active Supplier for company: {}", companyRefId);
        return repository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierDto> getBySupplierType(String supplierType) {
        logger.info("Fetching Supplier for type: {}", supplierType);
        return repository.findBySupplierType(supplierType)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierDto> getByCountry(String country) {
        logger.info("Fetching Supplier for country: {}", country);
        return repository.findByCountry(country)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierDto> getByCity(String city) {
        logger.info("Fetching Supplier for city: {}", city);
        return repository.findByCity(city)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SupplierDto> getByEmail(String email) {
        logger.info("Fetching Supplier by email: {}", email);
        return repository.findByEmail(email).map(mapper::toDto);
    }

    @Override
    public Optional<SupplierDto> getByGstNo(String gstNo) {
        logger.info("Fetching Supplier by GST No: {}", gstNo);
        return repository.findByGstNo(gstNo).map(mapper::toDto);
    }

    @Override
    public Optional<SupplierDto> getById(Integer id) {
        logger.info("Fetching Supplier by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SupplierDto create(SupplierDto dto) {
        logger.info("Creating new Supplier");
        validateSupplierData(dto);
        Supplier entity = mapper.toEntity(dto);

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
        if (entity.getCNumber() == null) {
            entity.setCNumber(1);
        }
        if (entity.getOpeningBalance() == null) {
            entity.setOpeningBalance(BigDecimal.ZERO);
        }
        if (entity.getSupplierType() == null) {
            entity.setSupplierType("VENDOR");
        }
        if (entity.getAccountRefid() == null) {
            entity.setAccountRefid(1);
        }

        Supplier saved = repository.save(entity);
        logger.info("Supplier created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SupplierDto update(Integer id, SupplierDto dto) {
        logger.info("Updating Supplier with ID: {}", id);
        validateSupplierData(dto);

        Supplier entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));

        // Preserve created date and update modified date
        LocalDateTime now = LocalDateTime.now();
        entity.setModifiedDate(now);
        entity.setModifiedBy("SYSTEM");

        mapper.updateEntityFromDto(dto, entity);
        Supplier updated = repository.save(entity);
        logger.info("Supplier updated with ID: {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting Supplier with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("Supplier deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long countByCompanyRefId(Integer companyRefId) {
        logger.info("Counting Supplier for company: {}", companyRefId);
        return repository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompany(Integer companyRefId) {
        logger.info("Counting active Supplier for company: {}", companyRefId);
        return repository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    public void validateSupplierData(SupplierDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new RuntimeException("Company Reference ID is required");
        }
        if (dto.getSupplierName() == null || dto.getSupplierName().trim().isEmpty()) {
            throw new RuntimeException("Supplier Name is required");
        }
        if (dto.getCNumberDisplay() == null || dto.getCNumberDisplay().trim().isEmpty()) {
            throw new RuntimeException("C Number Display is required");
        }
        if (dto.getCNumber() == null) {
            throw new RuntimeException("C Number is required");
        }
        if (dto.getSymbolRefid() == null) {
            throw new RuntimeException("Symbol Reference ID is required");
        }
        if (dto.getPaymentTermsRefid() == null) {
            throw new RuntimeException("Payment Terms Reference ID is required");
        }
        if (dto.getSupplierType() == null || dto.getSupplierType().trim().isEmpty()) {
            throw new RuntimeException("Supplier Type is required");
        }
        if (dto.getAccountRefid() == null) {
            throw new RuntimeException("Account Reference ID is required");
        }
    }

    @Override
    @Transactional
    public SupplierDto activateSupplier(Integer id) {
        logger.info("Activating Supplier with ID: {}", id);
        Supplier entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));

        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        Supplier updated = repository.save(entity);

        logger.info("Supplier activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public SupplierDto deactivateSupplier(Integer id) {
        logger.info("Deactivating Supplier with ID: {}", id);
        Supplier entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));

        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        Supplier updated = repository.save(entity);

        logger.info("Supplier deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    public boolean existsBySupplierName(String supplierName) {
        logger.info("Checking if Supplier exists with name: {}", supplierName);
        return repository.existsBySupplierName(supplierName);
    }

    /**
     * Process Supplier using SP_Supplier logic
     * Supports both INSERT (id = 0) and UPDATE (id > 0)
     */
    @Transactional
    public SupplierDto processSupplierBatch(SupplierDto dto) {
        logger.info("Processing Supplier with SP_Supplier logic");

        if (dto.getId() == null || dto.getId() == 0) {
            // New record - INSERT with default values
            logger.info("Processing INSERT operation");
            return create(dto);
        } else {
            // Existing record - UPDATE with modified date
            logger.info("Processing UPDATE operation for ID: {}", dto.getId());
            return update(dto.getId(), dto);
        }
    }
}


