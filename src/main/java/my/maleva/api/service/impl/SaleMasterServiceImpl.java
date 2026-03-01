package my.maleva.api.service.impl;

import my.maleva.api.dto.SaleMasterDto;
import my.maleva.api.mapper.SaleMasterMapper;
import my.maleva.api.model.SaleMaster;
import my.maleva.api.repo.SaleMasterRepository;
import my.maleva.api.service.SaleMasterService;
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
 * SaleMasterServiceImpl
 * Service implementation for SaleMaster
 * Incorporates business logic from SP_SaleMaster stored procedure
 */
@Service
public class SaleMasterServiceImpl implements SaleMasterService {

    private static final Logger logger = LoggerFactory.getLogger(SaleMasterServiceImpl.class);

    @Autowired
    private SaleMasterRepository saleMasterRepository;

    @Autowired
    private SaleMasterMapper mapper;

    @Override
    public List<SaleMasterDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all SaleMaster records for company: {}", companyRefId);
        return saleMasterRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleMasterDto> getByCompanyIdAndStatus(Integer companyRefId, Integer active) {
        logger.info("Fetching SaleMaster records for company: {} and status: {}", companyRefId, active);
        return saleMasterRepository.findByCompanyRefIdAndActive(companyRefId, active)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleMasterDto> getById(Integer id) {
        logger.info("Fetching SaleMaster by ID: {}", id);
        return saleMasterRepository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SaleMasterDto create(SaleMasterDto dto) {
        logger.info("Creating new SaleMaster for company: {}", dto.getCompanyRefId());

        // Validate business logic
        validateSaleData(dto);

        // Check C Number uniqueness
        if (saleMasterRepository.existsByCompanyRefIdAndCNumber(dto.getCompanyRefId(), dto.getCNumber())) {
            throw new RuntimeException("C Number already exists: " + dto.getCNumber());
        }

        // Calculate totals (from SP logic)
        calculateSaleTotals(dto);

        SaleMaster entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());

        // Set default values
        if (entity.getActive() == null) entity.setActive(0);
        if (entity.getCoinage() == null) entity.setCoinage(0.0);
        if (entity.getBoardingAmount() == null) entity.setBoardingAmount(0.0);
        if (entity.getBoardingAmount1() == null) entity.setBoardingAmount1(0.0);
        if (entity.getPortCharges() == null) entity.setPortCharges(0.0);
        if (entity.getSealAmount() == null) entity.setSealAmount(0.0);
        if (entity.getBreakSealAmount() == null) entity.setBreakSealAmount(0.0);
        if (entity.getSealAmount2() == null) entity.setSealAmount2(0.0);
        if (entity.getBreakSealAmount2() == null) entity.setBreakSealAmount2(0.0);
        if (entity.getSealAmount3() == null) entity.setSealAmount3(0.0);
        if (entity.getBreakSealAmount3() == null) entity.setBreakSealAmount3(0.0);
        if (entity.getCurrencyValue() == null) entity.setCurrencyValue(0.0);

        SaleMaster saved = saleMasterRepository.save(entity);
        logger.info("SaleMaster created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SaleMasterDto update(Integer id, SaleMasterDto dto) {
        logger.info("Updating SaleMaster with ID: {}", id);
        SaleMaster entity = saleMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleMaster not found with ID: " + id));

        // Validate and recalculate
        validateSaleData(dto);
        calculateSaleTotals(dto);

        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        SaleMaster updated = saleMasterRepository.save(entity);
        logger.info("SaleMaster updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SaleMaster with ID: {}", id);
        if (saleMasterRepository.existsById(id)) {
            saleMasterRepository.deleteById(id);
            logger.info("SaleMaster deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public List<SaleMasterDto> getByCustomerRefId(Integer customerRefId) {
        logger.info("Fetching SaleMaster records by customer ID: {}", customerRefId);
        return saleMasterRepository.findByCustomerRefId(customerRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleMasterDto> getByCompanyAndCustomer(Integer companyRefId, Integer customerRefId) {
        logger.info("Fetching SaleMaster records for company: {} and customer: {}", companyRefId, customerRefId);
        return saleMasterRepository.findByCompanyRefIdAndCustomerRefId(companyRefId, customerRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleMasterDto> getByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Fetching SaleMaster by C Number: {}", cNumber);
        return saleMasterRepository.findByCompanyRefIdAndCNumber(companyRefId, cNumber)
                .map(mapper::toDto);
    }

    @Override
    public List<SaleMasterDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching SaleMaster records by date range for company: {}", companyRefId);
        return saleMasterRepository.findByDateRange(companyRefId, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleMasterDto> getByDateAndStatus(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate, Integer active) {
        logger.info("Fetching SaleMaster records by date and status for company: {}", companyRefId);
        return saleMasterRepository.findByDateAndStatus(companyRefId, startDate, endDate, active)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleMasterDto> getByEmployeeId(Integer employeeRefId) {
        logger.info("Fetching SaleMaster records by employee ID: {}", employeeRefId);
        return saleMasterRepository.findByEmployeeRefId(employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleMasterDto> getByCompanyAndEmployee(Integer companyRefId, Integer employeeRefId) {
        logger.info("Fetching SaleMaster records for company: {} and employee: {}", companyRefId, employeeRefId);
        return saleMasterRepository.findByCompanyRefIdAndEmployeeRefId(companyRefId, employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleMasterDto> getByUserId(Integer userRefId) {
        logger.info("Fetching SaleMaster records by user ID: {}", userRefId);
        return saleMasterRepository.findByUserRefId(userRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleMasterDto> getByCompanyAndBillType(Integer companyRefId, String billType) {
        logger.info("Fetching SaleMaster records for company: {} and bill type: {}", companyRefId, billType);
        return saleMasterRepository.findByCompanyRefIdAndBillType(companyRefId, billType)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleMasterDto> getByCompanyAndSaleType(Integer companyRefId, String saleType) {
        logger.info("Fetching SaleMaster records for company: {} and sale type: {}", companyRefId, saleType);
        return saleMasterRepository.findByCompanyRefIdAndSaleType(companyRefId, saleType)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleMasterDto> getByJobMasterRefId(Integer jobMasterRefId) {
        logger.info("Fetching SaleMaster records by job master: {}", jobMasterRefId);
        return saleMasterRepository.findByJobMasterRefId(jobMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleMasterDto> getByAgentMasterRefId(Integer agentMasterRefId) {
        logger.info("Fetching SaleMaster records by agent master: {}", agentMasterRefId);
        return saleMasterRepository.findByAgentMasterRefId(agentMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleMasterDto> getByDriverRefid(Integer driverRefid) {
        logger.info("Fetching SaleMaster records by driver: {}", driverRefid);
        return saleMasterRepository.findByDriverRefid(driverRefid)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCompanyId(Integer companyRefId) {
        logger.info("Counting SaleMaster records for company: {}", companyRefId);
        return saleMasterRepository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countByCompanyIdAndStatus(Integer companyRefId, Integer active) {
        logger.info("Counting SaleMaster records for company: {} and status: {}", companyRefId, active);
        return saleMasterRepository.countByCompanyRefIdAndActive(companyRefId, active);
    }

    @Override
    public long countByCustomerRefId(Integer customerRefId) {
        logger.info("Counting SaleMaster records by customer: {}", customerRefId);
        return saleMasterRepository.countByCustomerRefId(customerRefId);
    }

    @Override
    public boolean existsByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Checking if C Number exists: {}", cNumber);
        return saleMasterRepository.existsByCompanyRefIdAndCNumber(companyRefId, cNumber);
    }

    @Override
    @Transactional
    public SaleMasterDto activate(Integer id) {
        logger.info("Activating SaleMaster with ID: {}", id);
        SaleMaster entity = saleMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleMaster not found with ID: " + id));
        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        SaleMaster updated = saleMasterRepository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public SaleMasterDto deactivate(Integer id) {
        logger.info("Deactivating SaleMaster with ID: {}", id);
        SaleMaster entity = saleMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleMaster not found with ID: " + id));
        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        SaleMaster updated = saleMasterRepository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public void processSaleMasterBatch(List<SaleMasterDto> saleList, Integer companyId) {
        logger.info("Processing batch of {} sales for company: {}", saleList.size(), companyId);
        for (SaleMasterDto dto : saleList) {
            dto.setCompanyRefId(companyId);
            // Validate and create each sale
            validateSaleData(dto);
            calculateSaleTotals(dto);
            create(dto);
        }
        logger.info("Batch processing completed for {} sales", saleList.size());
    }

    @Override
    public SaleMasterDto calculateSaleTotals(SaleMasterDto dto) {
        logger.info("Calculating sale totals for sale: {}", dto.getCNumber());
        // Implement calculation logic from SP_SaleMaster
        // This calculates GrossAmount, TaxAmount, DiscountAmount, etc.
        if (dto.getGrossAmount() == null) dto.setGrossAmount(0.0);
        if (dto.getTaxAmount() == null) dto.setTaxAmount(0.0);
        if (dto.getDiscountAmount() == null) dto.setDiscountAmount(0.0);
        if (dto.getPlusAmount() == null) dto.setPlusAmount(0.0);
        if (dto.getMinusAmount() == null) dto.setMinusAmount(0.0);

        // Calculate NetAmount = GrossAmount + TaxAmount - DiscountAmount + PlusAmount - MinusAmount + Coinage
        Double coinage = dto.getCoinage() != null ? dto.getCoinage() : 0.0;
        Double netAmount = dto.getGrossAmount() + dto.getTaxAmount() - dto.getDiscountAmount()
                          + dto.getPlusAmount() - dto.getMinusAmount() + coinage;

        dto.setAmount(netAmount);
        return dto;
    }

    @Override
    public void validateSaleData(SaleMasterDto dto) {
        logger.info("Validating sale data for C Number: {}", dto.getCNumber());
        if (dto.getCompanyRefId() == null) throw new RuntimeException("Company Reference ID is required");
        if (dto.getCustomerRefId() == null) throw new RuntimeException("Customer Reference ID is required");
        if (dto.getJobMasterRefId() == null) throw new RuntimeException("Job Master Reference ID is required");
        if (dto.getSaleDate() == null) throw new RuntimeException("Sale Date is required");
        if (dto.getBillType() == null || dto.getBillType().isEmpty()) throw new RuntimeException("Bill Type is required");
        if (dto.getSaleType() == null || dto.getSaleType().isEmpty()) throw new RuntimeException("Sale Type is required");
        if (dto.getCNumber() == null) throw new RuntimeException("C Number is required");
    }
}

