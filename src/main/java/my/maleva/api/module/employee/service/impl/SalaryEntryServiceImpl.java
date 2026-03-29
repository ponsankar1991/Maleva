package my.maleva.api.module.employee.service.impl;

import my.maleva.api.module.employee.dto.SalaryEntryDto;
import my.maleva.api.module.employee.mapper.SalaryEntryMapper;
import my.maleva.api.module.employee.entity.SalaryEntry;
import my.maleva.api.module.employee.repository.SalaryEntryRepository;
import my.maleva.api.module.employee.service.SalaryEntryService;
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
 * SalaryEntryServiceImpl
 * Service implementation for SalaryEntry
 * Implements business logic for salary entry management
 */
@Service
public class SalaryEntryServiceImpl implements SalaryEntryService {

    private static final Logger logger = LoggerFactory.getLogger(SalaryEntryServiceImpl.class);

    @Autowired
    private SalaryEntryRepository salaryEntryRepository;

    @Autowired
    private SalaryEntryMapper mapper;

    @Override
    public List<SalaryEntryDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all SalaryEntry records for company: {}", companyRefId);
        return salaryEntryRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalaryEntryDto> getActiveByCompanyId(Integer companyRefId) {
        logger.info("Fetching active SalaryEntry records for company: {}", companyRefId);
        return salaryEntryRepository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SalaryEntryDto> getById(Integer id) {
        logger.info("Fetching SalaryEntry by ID: {}", id);
        return salaryEntryRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public SalaryEntryDto create(SalaryEntryDto dto) {
        logger.info("Creating new SalaryEntry for company: {}", dto.getCompanyRefId());

        // Validate business logic
        if (salaryEntryRepository.existsByCompanyRefIdAndCNumber(dto.getCompanyRefId(), dto.getCNumber())) {
            throw new RuntimeException("C Number already exists: " + dto.getCNumber());
        }

        if (dto.getRefNumber() != null &&
            salaryEntryRepository.existsByCompanyRefIdAndRefNumber(dto.getCompanyRefId(), dto.getRefNumber())) {
            throw new RuntimeException("Reference Number already exists: " + dto.getRefNumber());
        }

        SalaryEntry entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());
        if (entity.getActive() == null) {
            entity.setActive(0);
        }
        if (entity.getAmount() == null) {
            entity.setAmount(0.0);
        }
        if (entity.getPvStatus() == null) {
            entity.setPvStatus(0);
        }

        SalaryEntry saved = salaryEntryRepository.save(entity);
        logger.info("SalaryEntry created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SalaryEntryDto update(Integer id, SalaryEntryDto dto) {
        logger.info("Updating SalaryEntry with ID: {}", id);
        SalaryEntry entity = salaryEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalaryEntry not found with ID: " + id));

        // Validate business logic if reference number is being changed
        if (dto.getRefNumber() != null &&
            !dto.getRefNumber().equals(entity.getRefNumber()) &&
            salaryEntryRepository.existsByCompanyRefIdAndRefNumber(dto.getCompanyRefId(), dto.getRefNumber())) {
            throw new RuntimeException("Reference Number already exists: " + dto.getRefNumber());
        }

        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        SalaryEntry updated = salaryEntryRepository.save(entity);
        logger.info("SalaryEntry updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SalaryEntry with ID: {}", id);
        if (salaryEntryRepository.existsById(id)) {
            salaryEntryRepository.deleteById(id);
            logger.info("SalaryEntry deleted with ID: {}", id);
            return true;
        }
        logger.warn("SalaryEntry not found with ID: {}", id);
        return false;
    }

    @Override
    public List<SalaryEntryDto> getByEmployeeId(Integer employeeRefId) {
        logger.info("Fetching SalaryEntry records by employee ID: {}", employeeRefId);
        return salaryEntryRepository.findByEmployeeRefId(employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalaryEntryDto> getByCompanyAndEmployee(Integer companyRefId, Integer employeeRefId) {
        logger.info("Fetching SalaryEntry records for company: {} and employee: {}", companyRefId, employeeRefId);
        return salaryEntryRepository.findByCompanyRefIdAndEmployeeRefId(companyRefId, employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalaryEntryDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching SalaryEntry records by date range for company: {}", companyRefId);
        return salaryEntryRepository.findByDateRange(companyRefId, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SalaryEntryDto> getByRefNumber(Integer companyRefId, String refNumber) {
        logger.info("Fetching SalaryEntry by reference number: {}", refNumber);
        return salaryEntryRepository.findByCompanyRefIdAndRefNumber(companyRefId, refNumber)
                .map(mapper::toDto);
    }

    @Override
    public List<SalaryEntryDto> getByBankId(Integer bankRefId) {
        logger.info("Fetching SalaryEntry records by bank ID: {}", bankRefId);
        return salaryEntryRepository.findByBankRefId(bankRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalaryEntryDto> getByPvStatus(Integer companyRefId, Integer pvStatus) {
        logger.info("Fetching SalaryEntry records by PV Status: {}", pvStatus);
        return salaryEntryRepository.findByCompanyRefIdAndPvStatus(companyRefId, pvStatus)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCompanyId(Integer companyRefId) {
        logger.info("Counting SalaryEntry records for company: {}", companyRefId);
        return salaryEntryRepository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompanyId(Integer companyRefId) {
        logger.info("Counting active SalaryEntry records for company: {}", companyRefId);
        return salaryEntryRepository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    public long countByEmployeeId(Integer employeeRefId) {
        logger.info("Counting SalaryEntry records by employee: {}", employeeRefId);
        return salaryEntryRepository.countByEmployeeRefId(employeeRefId);
    }

    @Override
    public boolean existsByRefNumber(Integer companyRefId, String refNumber) {
        logger.info("Checking if reference number exists: {}", refNumber);
        return salaryEntryRepository.existsByCompanyRefIdAndRefNumber(companyRefId, refNumber);
    }

    @Override
    public boolean existsByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Checking if C Number exists: {}", cNumber);
        return salaryEntryRepository.existsByCompanyRefIdAndCNumber(companyRefId, cNumber);
    }

    @Override
    public Optional<SalaryEntryDto> getByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Fetching SalaryEntry by C Number: {}", cNumber);
        return salaryEntryRepository.findByCompanyRefIdAndCNumber(companyRefId, cNumber)
                .map(mapper::toDto);
    }

    @Override
    public List<SalaryEntryDto> getByDateAndStatus(Integer companyRefId, LocalDateTime startDate,
                                                   LocalDateTime endDate, Integer pvStatus) {
        logger.info("Fetching SalaryEntry records by date and status for company: {}", companyRefId);
        return salaryEntryRepository.findByDateAndStatus(companyRefId, startDate, endDate, pvStatus)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SalaryEntryDto activate(Integer id) {
        logger.info("Activating SalaryEntry with ID: {}", id);
        SalaryEntry entity = salaryEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalaryEntry not found with ID: " + id));
        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        SalaryEntry updated = salaryEntryRepository.save(entity);
        logger.info("SalaryEntry activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public SalaryEntryDto deactivate(Integer id) {
        logger.info("Deactivating SalaryEntry with ID: {}", id);
        SalaryEntry entity = salaryEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalaryEntry not found with ID: " + id));
        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        SalaryEntry updated = salaryEntryRepository.save(entity);
        logger.info("SalaryEntry deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public SalaryEntryDto updatePvStatus(Integer id, Integer pvStatus) {
        logger.info("Updating PV Status for SalaryEntry with ID: {}", id);
        SalaryEntry entity = salaryEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalaryEntry not found with ID: " + id));
        entity.setPvStatus(pvStatus);
        entity.setModifiedDate(LocalDateTime.now());
        SalaryEntry updated = salaryEntryRepository.save(entity);
        logger.info("SalaryEntry PV Status updated with ID: {}", id);
        return mapper.toDto(updated);
    }
}

