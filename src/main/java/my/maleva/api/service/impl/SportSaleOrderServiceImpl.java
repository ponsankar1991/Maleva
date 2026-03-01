package my.maleva.api.service.impl;

import my.maleva.api.dto.SportSaleOrderDto;
import my.maleva.api.mapper.SportSaleOrderMapper;
import my.maleva.api.model.SportSaleOrder;
import my.maleva.api.repo.SportSaleOrderRepository;
import my.maleva.api.service.SportSaleOrderService;
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
 * SportSaleOrderServiceImpl - Implementation for SportSaleOrder service
 * Incorporates SP_SoptSaleorder stored procedure business logic
 * Handles sport-related sales order creation, update, and retrieval with comprehensive validation
 */
@Service
public class SportSaleOrderServiceImpl implements SportSaleOrderService {

    private static final Logger logger = LoggerFactory.getLogger(SportSaleOrderServiceImpl.class);

    @Autowired
    private SportSaleOrderRepository repository;

    @Autowired
    private SportSaleOrderMapper mapper;

    @Override
    public List<SportSaleOrderDto> getByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching SportSaleOrder for company: {}", companyRefId);
        return repository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SportSaleOrderDto> getActiveByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching active SportSaleOrder for company: {}", companyRefId);
        return repository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SportSaleOrderDto> getByCustomerRefId(Integer customerRefId) {
        logger.info("Fetching SportSaleOrder for customer: {}", customerRefId);
        return repository.findByCustomerRefId(customerRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SportSaleOrderDto> getByCompanyAndCustomer(Integer companyRefId, Integer customerRefId) {
        logger.info("Fetching SportSaleOrder for company: {} and customer: {}", companyRefId, customerRefId);
        return repository.findByCompanyRefIdAndCustomerRefId(companyRefId, customerRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SportSaleOrderDto> getByJobMasterRefId(Integer jobMasterRefId) {
        logger.info("Fetching SportSaleOrder for job master: {}", jobMasterRefId);
        return repository.findByJobMasterRefId(jobMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SportSaleOrderDto> getByEmployeeRefId(Integer employeeRefId) {
        logger.info("Fetching SportSaleOrder for employee: {}", employeeRefId);
        return repository.findByEmployeeRefId(employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SportSaleOrderDto> getByStatus(Integer jStatus) {
        logger.info("Fetching SportSaleOrder for status: {}", jStatus);
        return repository.findByJStatus(jStatus)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SportSaleOrderDto> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching SportSaleOrder for date range: {} to {}", startDate, endDate);
        return repository.findByCreatedDateGreaterThanEqualAndCreatedDateLessThanEqual(startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SportSaleOrderDto> getById(Integer id) {
        logger.info("Fetching SportSaleOrder by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public Optional<SportSaleOrderDto> getByAwbNo(String awbNo) {
        logger.info("Fetching SportSaleOrder by AWB No: {}", awbNo);
        return repository.findByAwbNo(awbNo).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SportSaleOrderDto create(SportSaleOrderDto dto) {
        logger.info("Creating new SportSaleOrder");
        validateSportSaleOrderData(dto);
        SportSaleOrder entity = mapper.toEntity(dto);

        // Set default values as per SP_SoptSaleorder logic
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

        SportSaleOrder saved = repository.save(entity);
        logger.info("SportSaleOrder created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SportSaleOrderDto update(Integer id, SportSaleOrderDto dto) {
        logger.info("Updating SportSaleOrder with ID: {}", id);
        validateSportSaleOrderData(dto);

        SportSaleOrder entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SportSaleOrder not found: " + id));

        // Preserve created date and update modified date as per SP logic
        LocalDateTime now = LocalDateTime.now();
        entity.setModifiedDate(now);

        mapper.updateEntityFromDto(dto, entity);
        SportSaleOrder updated = repository.save(entity);
        logger.info("SportSaleOrder updated with ID: {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SportSaleOrder with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("SportSaleOrder deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long countByCompanyRefId(Integer companyRefId) {
        logger.info("Counting SportSaleOrder for company: {}", companyRefId);
        return repository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompanyRefId(Integer companyRefId) {
        logger.info("Counting active SportSaleOrder for company: {}", companyRefId);
        return repository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    public void validateSportSaleOrderData(SportSaleOrderDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new RuntimeException("Company Reference ID is required");
        }
        if (dto.getJobMasterRefId() == null) {
            throw new RuntimeException("Job Master Reference ID is required");
        }
    }

    @Override
    @Transactional
    public SportSaleOrderDto processSportSaleOrder(SportSaleOrderDto dto) {
        logger.info("Processing SportSaleOrder - incorporating SP_SoptSaleorder logic");

        // SP_SoptSaleorder logic: Insert or Update based on ID
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

