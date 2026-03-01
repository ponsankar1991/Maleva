package my.maleva.api.service.impl;

import my.maleva.api.dto.StockInDto;
import my.maleva.api.mapper.StockInMapper;
import my.maleva.api.model.StockIn;
import my.maleva.api.repo.StockInRepository;
import my.maleva.api.service.StockInService;
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
 * StockInServiceImpl - Implementation for StockIn service
 * Incorporates SP_StockIn stored procedure business logic
 * Handles stock inbound processing with automatic sequence number generation
 */
@Service
public class StockInServiceImpl implements StockInService {

    private static final Logger logger = LoggerFactory.getLogger(StockInServiceImpl.class);

    @Autowired
    private StockInRepository repository;

    @Autowired
    private StockInMapper mapper;

    @Override
    public List<StockInDto> getByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching StockIn for company: {}", companyRefId);
        return repository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockInDto> getByUserRefId(Integer userRefId) {
        logger.info("Fetching StockIn for user: {}", userRefId);
        return repository.findByUserRefId(userRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockInDto> getByEmployeeRefId(Integer employeeRefId) {
        logger.info("Fetching StockIn for employee: {}", employeeRefId);
        return repository.findByEmployeeRefId(employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockInDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Fetching StockIn for sale order master: {}", saleOrderMasterRefId);
        return repository.findBySaleOrderMasterRefId(saleOrderMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockInDto> getByPortMasterRefId(Integer portMasterRefId) {
        logger.info("Fetching StockIn for port master: {}", portMasterRefId);
        return repository.findByPortMasterRefId(portMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockInDto> getByStatus(Integer status) {
        logger.info("Fetching StockIn for status: {}", status);
        return repository.findByStatus(status)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockInDto> getByCompanyAndStatus(Integer companyRefId, Integer status) {
        logger.info("Fetching StockIn for company: {} and status: {}", companyRefId, status);
        return repository.findByCompanyRefIdAndStatus(companyRefId, status)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockInDto> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching StockIn for date range: {} to {}", startDate, endDate);
        return repository.findByStockDateGreaterThanEqualAndStockDateLessThanEqual(startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockInDto> getByCompanyAndDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching StockIn for company: {} and date range: {} to {}", companyRefId, startDate, endDate);
        return repository.findByCompanyRefIdAndStockDateGreaterThanEqualAndStockDateLessThanEqual(companyRefId, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StockInDto> getById(Integer id) {
        logger.info("Fetching StockIn by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public Optional<StockInDto> getByCNumber(Integer cNumber, Integer companyRefId) {
        logger.info("Fetching StockIn by C Number: {} for company: {}", cNumber, companyRefId);
        return repository.findByCNumberAndCompanyRefId(cNumber, companyRefId).map(mapper::toDto);
    }

    @Override
    public Optional<StockInDto> getByBarcode(String barcode) {
        logger.info("Fetching StockIn by barcode: {}", barcode);
        return repository.findByBarcode(barcode).map(mapper::toDto);
    }

    @Override
    @Transactional
    public StockInDto create(StockInDto dto) {
        logger.info("Creating new StockIn");
        validateStockInData(dto);
        StockIn entity = mapper.toEntity(dto);

        // Set default values as per SP_StockIn logic
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(now);
        }
        if (entity.getModifiedDate() == null) {
            entity.setModifiedDate(now);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy("SYSTEM");
        }
        if (entity.getModifiedBy() == null) {
            entity.setModifiedBy("SYSTEM");
        }

        StockIn saved = repository.save(entity);
        logger.info("StockIn created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public StockInDto update(Integer id, StockInDto dto) {
        logger.info("Updating StockIn with ID: {}", id);
        validateStockInData(dto);

        StockIn entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("StockIn not found: " + id));

        // Preserve created info and update modified date as per SP logic
        LocalDateTime now = LocalDateTime.now();
        entity.setModifiedDate(now);
        entity.setModifiedBy("SYSTEM");

        mapper.updateEntityFromDto(dto, entity);
        StockIn updated = repository.save(entity);
        logger.info("StockIn updated with ID: {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting StockIn with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("StockIn deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long countByCompanyRefId(Integer companyRefId) {
        logger.info("Counting StockIn for company: {}", companyRefId);
        return repository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countByCompanyAndStatus(Integer companyRefId, Integer status) {
        logger.info("Counting StockIn for company: {} and status: {}", companyRefId, status);
        return repository.countByCompanyRefIdAndStatus(companyRefId, status);
    }

    @Override
    public void validateStockInData(StockInDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new RuntimeException("Company Reference ID is required");
        }
        if (dto.getStockDate() == null) {
            throw new RuntimeException("Stock Date is required");
        }
        if (dto.getCNumberDisplay() == null || dto.getCNumberDisplay().trim().isEmpty()) {
            throw new RuntimeException("C Number Display is required");
        }
        if (dto.getCNumber() == null) {
            throw new RuntimeException("C Number is required");
        }
        if (dto.getNumberOfPackages() == null) {
            throw new RuntimeException("Number of Packages is required");
        }
        if (dto.getPortMasterRefId() == null) {
            throw new RuntimeException("Port Master Reference ID is required");
        }
    }

    @Override
    @Transactional
    public StockInDto processStockIn(StockInDto dto) {
        logger.info("Processing StockIn - incorporating SP_StockIn logic");

        // SP_StockIn logic: Delete existing records for same SaleOrderMasterRefId, then INSERT or UPDATE
        if (dto.getSaleOrderMasterRefId() != null) {
            logger.info("Deleting existing StockIn records for SaleOrderMasterRefId: {}", dto.getSaleOrderMasterRefId());
            deleteAllBySaleOrderMasterRefId(dto.getSaleOrderMasterRefId());
        }

        if (dto.getId() == null || dto.getId() == 0) {
            // New record - INSERT with sequence number generation
            logger.info("Processing INSERT operation with sequence number generation");
            return create(dto);
        } else {
            // Existing record - UPDATE
            logger.info("Processing UPDATE operation for ID: {}", dto.getId());
            return update(dto.getId(), dto);
        }
    }

    @Override
    @Transactional
    public void deleteAllBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Deleting all StockIn records for SaleOrderMasterRefId: {}", saleOrderMasterRefId);
        repository.deleteBySaleOrderMasterRefId(saleOrderMasterRefId);
    }
}

