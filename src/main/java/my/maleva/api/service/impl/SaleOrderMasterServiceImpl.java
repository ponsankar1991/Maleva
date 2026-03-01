package my.maleva.api.service.impl;

import my.maleva.api.dto.SaleOrderMasterDto;
import my.maleva.api.mapper.SaleOrderMasterMapper;
import my.maleva.api.model.SaleOrderMaster;
import my.maleva.api.repo.SaleOrderMasterRepository;
import my.maleva.api.service.SaleOrderMasterService;
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
 * SaleOrderMasterServiceImpl - Implementation for SaleOrderMaster service
 * Incorporates SP_SaleOrderMaster business logic
 */
@Service
public class SaleOrderMasterServiceImpl implements SaleOrderMasterService {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderMasterServiceImpl.class);

    @Autowired
    private SaleOrderMasterRepository repository;

    @Autowired
    private SaleOrderMasterMapper mapper;

    @Override
    public List<SaleOrderMasterDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all SaleOrderMaster records for company: {}", companyRefId);
        return repository.findByCompanyRefId(companyRefId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SaleOrderMasterDto> getByCompanyIdAndStatus(Integer companyRefId, Integer active) {
        logger.info("Fetching SaleOrderMaster for company: {} and status: {}", companyRefId, active);
        return repository.findByCompanyRefIdAndActive(companyRefId, active).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<SaleOrderMasterDto> getById(Integer id) {
        logger.info("Fetching SaleOrderMaster by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SaleOrderMasterDto create(SaleOrderMasterDto dto) {
        logger.info("Creating SaleOrderMaster for company: {}", dto.getCompanyRefId());
        validateOrderData(dto);
        if (repository.existsByCompanyRefIdAndCNumber(dto.getCompanyRefId(), dto.getCNumber())) {
            throw new RuntimeException("C Number already exists: " + dto.getCNumber());
        }
        calculateOrderTotals(dto);
        SaleOrderMaster entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());
        if (entity.getActive() == null) entity.setActive(0);
        initializeDefaults(entity);
        SaleOrderMaster saved = repository.save(entity);
        logger.info("SaleOrderMaster created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SaleOrderMasterDto update(Integer id, SaleOrderMasterDto dto) {
        logger.info("Updating SaleOrderMaster with ID: {}", id);
        SaleOrderMaster entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        validateOrderData(dto);
        calculateOrderTotals(dto);
        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        SaleOrderMaster updated = repository.save(entity);
        logger.info("SaleOrderMaster updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SaleOrderMaster with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<SaleOrderMasterDto> getByCustomerRefId(Integer customerRefId) {
        return repository.findByCustomerRefId(customerRefId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SaleOrderMasterDto> getByCompanyAndCustomer(Integer companyRefId, Integer customerRefId) {
        return repository.findByCompanyRefIdAndCustomerRefId(companyRefId, customerRefId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<SaleOrderMasterDto> getByCNumber(Integer companyRefId, Integer cNumber) {
        return repository.findByCompanyRefIdAndCNumber(companyRefId, cNumber).map(mapper::toDto);
    }

    @Override
    public List<SaleOrderMasterDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findByDateRange(companyRefId, startDate, endDate).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SaleOrderMasterDto> getByEmployeeId(Integer employeeRefId) {
        return repository.findByEmployeeRefId(employeeRefId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SaleOrderMasterDto> getByCompanyAndEmployee(Integer companyRefId, Integer employeeRefId) {
        return repository.findByCompanyRefIdAndEmployeeRefId(companyRefId, employeeRefId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SaleOrderMasterDto> getByUserId(Integer userRefId) {
        return repository.findByUserRefId(userRefId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SaleOrderMasterDto> getByJobMasterRefId(Integer jobMasterRefId) {
        return repository.findByJobMasterRefId(jobMasterRefId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SaleOrderMasterDto> getByAgentMasterRefId(Integer agentMasterRefId) {
        return repository.findByAgentMasterRefId(agentMasterRefId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SaleOrderMasterDto> getByDriverRefid(Integer driverRefid) {
        return repository.findByDriverRefid(driverRefid).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public long countByCompanyId(Integer companyRefId) {
        return repository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countByCompanyIdAndStatus(Integer companyRefId, Integer active) {
        return repository.countByCompanyRefIdAndActive(companyRefId, active);
    }

    @Override
    public boolean existsByCNumber(Integer companyRefId, Integer cNumber) {
        return repository.existsByCompanyRefIdAndCNumber(companyRefId, cNumber);
    }

    @Override
    @Transactional
    public SaleOrderMasterDto activate(Integer id) {
        SaleOrderMaster entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public SaleOrderMasterDto deactivate(Integer id) {
        SaleOrderMaster entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void processSaleOrderBatch(List<SaleOrderMasterDto> orderList, Integer companyId) {
        logger.info("Processing batch of {} orders for company: {}", orderList.size(), companyId);
        for (SaleOrderMasterDto dto : orderList) {
            dto.setCompanyRefId(companyId);
            create(dto);
        }
    }

    @Override
    public SaleOrderMasterDto calculateOrderTotals(SaleOrderMasterDto dto) {
        if (dto.getGrossAmount() == null) dto.setGrossAmount(0.0);
        if (dto.getTaxAmount() == null) dto.setTaxAmount(0.0);
        if (dto.getDiscountAmount() == null) dto.setDiscountAmount(0.0);
        if (dto.getPlusAmount() == null) dto.setPlusAmount(0.0);
        if (dto.getMinusAmount() == null) dto.setMinusAmount(0.0);

        Double coinage = dto.getCoinage() != null ? dto.getCoinage() : 0.0;
        Double netAmount = dto.getGrossAmount() + dto.getTaxAmount() - dto.getDiscountAmount()
                          + dto.getPlusAmount() - dto.getMinusAmount() + coinage;
        dto.setAmount(netAmount);
        return dto;
    }

    @Override
    public void validateOrderData(SaleOrderMasterDto dto) {
        if (dto.getCompanyRefId() == null) throw new RuntimeException("Company ID required");
        if (dto.getCustomerRefId() == null) throw new RuntimeException("Customer ID required");
        if (dto.getJobMasterRefId() == null) throw new RuntimeException("Job Master ID required");
        if (dto.getSaleDate() == null) throw new RuntimeException("Sale Date required");
        if (dto.getBillType() == null || dto.getBillType().isEmpty()) throw new RuntimeException("Bill Type required");
        if (dto.getSaleType() == null || dto.getSaleType().isEmpty()) throw new RuntimeException("Sale Type required");
        if (dto.getCNumber() == null) throw new RuntimeException("C Number required");
    }

    private void initializeDefaults(SaleOrderMaster entity) {
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
    }
}

