package my.maleva.api.module.salecreditmaster.service.impl;

import my.maleva.api.module.salecreditmaster.dto.SaleCreditMasterDto;
import my.maleva.api.module.salecreditmaster.mapper.SaleCreditMasterMapper;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditMaster;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditMasterRepository;
import my.maleva.api.module.salecreditmaster.service.SaleCreditMasterService;
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
 * SaleCreditMasterServiceImpl
 * Service implementation for SaleCreditMaster
 * Implements business logic for sale credit management
 */
@Service
public class SaleCreditMasterServiceImpl implements SaleCreditMasterService {

    private static final Logger logger = LoggerFactory.getLogger(SaleCreditMasterServiceImpl.class);

    @Autowired
    private SaleCreditMasterRepository saleCreditMasterRepository;

    @Autowired
    private SaleCreditMasterMapper mapper;

    @Override
    public List<SaleCreditMasterDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all SaleCreditMaster records for company: {}", companyRefId);
        return saleCreditMasterRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleCreditMasterDto> getByCompanyIdAndStatus(Integer companyRefId, Integer cStatus) {
        logger.info("Fetching SaleCreditMaster records for company: {} and status: {}", companyRefId, cStatus);
        return saleCreditMasterRepository.findByCompanyRefIdAndCStatus(companyRefId, cStatus)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleCreditMasterDto> getById(Integer id) {
        logger.info("Fetching SaleCreditMaster by ID: {}", id);
        return saleCreditMasterRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public SaleCreditMasterDto create(SaleCreditMasterDto dto) {
        logger.info("Creating new SaleCreditMaster for company: {}", dto.getCompanyRefId());

        // Validate business logic - Check if C Number already exists
        if (saleCreditMasterRepository.existsByCompanyRefIdAndCNumber(dto.getCompanyRefId(), dto.getCNumber())) {
            throw new RuntimeException("C Number already exists: " + dto.getCNumber());
        }

        // Check if reference number is provided and already exists
        if (dto.getRefNumber() != null &&
            saleCreditMasterRepository.existsByCompanyRefIdAndRefNumber(dto.getCompanyRefId(), dto.getRefNumber())) {
            throw new RuntimeException("Reference Number already exists: " + dto.getRefNumber());
        }

        SaleCreditMaster entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());

        // Set default values if null
        if (entity.getCStatus() == null) {
            entity.setCStatus(0);
        }
        if (entity.getCurrencyValue() == null) {
            entity.setCurrencyValue(0.0);
        }
        if (entity.getActualAmount() == null) {
            entity.setActualAmount(0.0);
        }
        if (entity.getCoinage() == null) {
            entity.setCoinage(0.0);
        }
        if (entity.getGrossAmount() == null) {
            entity.setGrossAmount(0.0);
        }
        if (entity.getTaxAmount() == null) {
            entity.setTaxAmount(0.0);
        }
        if (entity.getDiscountAmount() == null) {
            entity.setDiscountAmount(0.0);
        }
        if (entity.getPlusAmount() == null) {
            entity.setPlusAmount(0.0);
        }
        if (entity.getMinusAmount() == null) {
            entity.setMinusAmount(0.0);
        }

        SaleCreditMaster saved = saleCreditMasterRepository.save(entity);
        logger.info("SaleCreditMaster created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SaleCreditMasterDto update(Integer id, SaleCreditMasterDto dto) {
        logger.info("Updating SaleCreditMaster with ID: {}", id);
        SaleCreditMaster entity = saleCreditMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleCreditMaster not found with ID: " + id));

        // Validate business logic if reference number is being changed
        if (dto.getRefNumber() != null &&
            !dto.getRefNumber().equals(entity.getRefNumber()) &&
            saleCreditMasterRepository.existsByCompanyRefIdAndRefNumber(dto.getCompanyRefId(), dto.getRefNumber())) {
            throw new RuntimeException("Reference Number already exists: " + dto.getRefNumber());
        }

        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        SaleCreditMaster updated = saleCreditMasterRepository.save(entity);
        logger.info("SaleCreditMaster updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SaleCreditMaster with ID: {}", id);
        if (saleCreditMasterRepository.existsById(id)) {
            saleCreditMasterRepository.deleteById(id);
            logger.info("SaleCreditMaster deleted with ID: {}", id);
            return true;
        }
        logger.warn("SaleCreditMaster not found with ID: {}", id);
        return false;
    }

    @Override
    public List<SaleCreditMasterDto> getByCustomerRefId(Integer customerRefId) {
        logger.info("Fetching SaleCreditMaster records by customer ID: {}", customerRefId);
        return saleCreditMasterRepository.findByCustomerRefId(customerRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleCreditMasterDto> getByCompanyAndCustomer(Integer companyRefId, Integer customerRefId) {
        logger.info("Fetching SaleCreditMaster records for company: {} and customer: {}", companyRefId, customerRefId);
        return saleCreditMasterRepository.findByCompanyRefIdAndCustomerRefId(companyRefId, customerRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleCreditMasterDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching SaleCreditMaster records by date range for company: {}", companyRefId);
        return saleCreditMasterRepository.findByDateRange(companyRefId, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleCreditMasterDto> getByRefNumber(Integer companyRefId, String refNumber) {
        logger.info("Fetching SaleCreditMaster by reference number: {}", refNumber);
        return saleCreditMasterRepository.findByCompanyRefIdAndRefNumber(companyRefId, refNumber)
                .map(mapper::toDto);
    }

    @Override
    public Optional<SaleCreditMasterDto> getByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Fetching SaleCreditMaster by C Number: {}", cNumber);
        return saleCreditMasterRepository.findByCompanyRefIdAndCNumber(companyRefId, cNumber)
                .map(mapper::toDto);
    }

    @Override
    public List<SaleCreditMasterDto> getByEmployeeId(Integer employeeRefId) {
        logger.info("Fetching SaleCreditMaster records by employee ID: {}", employeeRefId);
        return saleCreditMasterRepository.findByEmployeeRefId(employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleCreditMasterDto> getByCompanyAndEmployee(Integer companyRefId, Integer employeeRefId) {
        logger.info("Fetching SaleCreditMaster records for company: {} and employee: {}", companyRefId, employeeRefId);
        return saleCreditMasterRepository.findByCompanyRefIdAndEmployeeRefId(companyRefId, employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleCreditMasterDto> getByUserId(Integer userRefId) {
        logger.info("Fetching SaleCreditMaster records by user ID: {}", userRefId);
        return saleCreditMasterRepository.findByUserRefId(userRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleCreditMasterDto> getBySaleMasterRefId(Integer saleMasterRefId) {
        logger.info("Fetching SaleCreditMaster records by Sale Master Reference ID: {}", saleMasterRefId);
        return saleCreditMasterRepository.findBySaleMasterRefId(saleMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCompanyId(Integer companyRefId) {
        logger.info("Counting SaleCreditMaster records for company: {}", companyRefId);
        return saleCreditMasterRepository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countByCompanyIdAndStatus(Integer companyRefId, Integer cStatus) {
        logger.info("Counting SaleCreditMaster records for company: {} and status: {}", companyRefId, cStatus);
        return saleCreditMasterRepository.countByCompanyRefIdAndCStatus(companyRefId, cStatus);
    }

    @Override
    public long countByCustomerRefId(Integer customerRefId) {
        logger.info("Counting SaleCreditMaster records by customer: {}", customerRefId);
        return saleCreditMasterRepository.countByCustomerRefId(customerRefId);
    }

    @Override
    public boolean existsByRefNumber(Integer companyRefId, String refNumber) {
        logger.info("Checking if reference number exists: {}", refNumber);
        return saleCreditMasterRepository.existsByCompanyRefIdAndRefNumber(companyRefId, refNumber);
    }

    @Override
    public boolean existsByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Checking if C Number exists: {}", cNumber);
        return saleCreditMasterRepository.existsByCompanyRefIdAndCNumber(companyRefId, cNumber);
    }

    @Override
    public List<SaleCreditMasterDto> getByDateAndStatus(Integer companyRefId, LocalDateTime startDate,
                                                         LocalDateTime endDate, Integer cStatus) {
        logger.info("Fetching SaleCreditMaster records by date and status for company: {}", companyRefId);
        return saleCreditMasterRepository.findByDateAndStatus(companyRefId, startDate, endDate, cStatus)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SaleCreditMasterDto changeStatus(Integer id, Integer newStatus) {
        logger.info("Changing status for SaleCreditMaster with ID: {} to {}", id, newStatus);
        SaleCreditMaster entity = saleCreditMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleCreditMaster not found with ID: " + id));
        entity.setCStatus(newStatus);
        entity.setModifiedDate(LocalDateTime.now());
        SaleCreditMaster updated = saleCreditMasterRepository.save(entity);
        logger.info("SaleCreditMaster status changed with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public SaleCreditMasterDto activate(Integer id) {
        logger.info("Activating SaleCreditMaster with ID: {}", id);
        return changeStatus(id, 1);
    }

    @Override
    @Transactional
    public SaleCreditMasterDto deactivate(Integer id) {
        logger.info("Deactivating SaleCreditMaster with ID: {}", id);
        return changeStatus(id, 0);
    }
}

