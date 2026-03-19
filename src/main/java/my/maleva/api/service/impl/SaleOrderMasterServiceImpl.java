package my.maleva.api.service.impl;

import my.maleva.api.dto.SaleOrderDTO;
import my.maleva.api.dto.SaleOrderFilterDTO;
import my.maleva.api.dto.SaleOrderMasterDto;
import my.maleva.api.dto.SaleF5View;
import my.maleva.api.dto.SaleMasterViewModel;
import my.maleva.api.dto.SaleDetailsViewModel;
import my.maleva.api.mapper.SaleOrderMasterMapper;
import my.maleva.api.mapper.SaleOrderDetailsMapper;
import my.maleva.api.mapper.SaleF5ViewMapper;
import my.maleva.api.mapper.QueryResultMapper;
import my.maleva.api.model.*;
import my.maleva.api.repo.*;
import my.maleva.api.service.SaleOrderMasterService;
import my.maleva.api.specification.SaleOrderSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    private SaleOrderDetailsRepository saleOrderDetailsRepository;
    @Autowired
    private SaleOrderPickupRepository saleOrderPickupRepository;
    @Autowired
    private SaleOrderDeliveryRepository saleOrderDeliveryRepository;
    @Autowired
    private SaleOrderForwardingRepository saleOrderForwardingRepository;

    @Autowired
    private SaleOrderMasterMapper mapper;
    @Autowired
    private SaleOrderDetailsMapper saleOrderDetailsMapper;
    @Autowired
    private my.maleva.api.mapper.SaleF5ViewMapper saleF5ViewMapper;
    @Autowired
    private my.maleva.api.mapper.QueryResultMapper queryResultMapper;
    @Autowired
    private my.maleva.api.service.helper.SaleOrderFilterHelper filterHelper;
    @Autowired
    private SequenceNoMasterRepository sequenceNoMasterRepository;

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


    @Transactional
    public SaleOrderMasterDto save(SaleOrderDTO dto) {
        logger.info("SaleOrderMaster save operation initiated. CompanyId: {}, CustomerId: {}, CNumber: {}",
                   dto.getCompanyRefId(), dto.getCustomerRefId(), dto.getCNumber());

        try {
            // Step 1: Validate critical fields
            validateDtoForSave(dto);

            // Step 2: Determine if INSERT or UPDATE based on ID
            boolean isInsert = dto.getId() == null || dto.getId() == 0;
            logger.info("Operation type: {}", isInsert ? "INSERT" : "UPDATE");

            // Step 3: Prepare entity data
            SaleOrderMaster entity = isInsert
                ? createNewEntity(dto)
                : updateExistingEntity(dto);

            // Step 4: Save master record
            SaleOrderMaster savedEntity = repository.saveAndFlush(entity);
            Integer masterId = savedEntity.getId();
            logger.info("SaleOrderMaster {} with ID: {}",
                       isInsert ? "CREATED" : "UPDATED", masterId);

            // Step 5: Manage child records
            if (isInsert) {
                saveAllChildRecords(dto, masterId);
            } else {
                deleteAndRecreateChildRecords(dto, masterId);
            }

            logger.info("SaleOrderMaster {} completed successfully. ID: {}",
                       isInsert ? "CREATE" : "UPDATE", masterId);
            return mapper.toDto(savedEntity);

        } catch (Exception e) {
            logger.error("Error in SaleOrderMaster save operation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save SaleOrderMaster: " + e.getMessage());
        }
    }

    /**
     * Validate DTO before save operation
     */
    private void validateDtoForSave(SaleOrderDTO dto) {
        if (dto.getCompanyRefId() == null || dto.getCompanyRefId() <= 0) {
            throw new RuntimeException("Company Reference ID is required and must be positive");
        }
        if (dto.getCustomerRefId() == null || dto.getCustomerRefId() <= 0) {
            throw new RuntimeException("Customer Reference ID is required and must be positive");
        }

        // NOTE: CNumber can be 0 on INSERT (will be auto-generated from SequenceNoMaster)
        // CNumber is optional on INSERT, required on UPDATE
        boolean isInsert = dto.getId() == null || dto.getId() == 0;
        if (!isInsert && (dto.getCNumber() == null || dto.getCNumber() <= 0)) {
            throw new RuntimeException("C Number is required for UPDATE and must be positive");
        }

        // Check for duplicate cNumber only on INSERT if cNumber is provided
        if (isInsert && dto.getCNumber() != null && dto.getCNumber() > 0 &&
            repository.existsByCompanyRefIdAndCNumber(dto.getCompanyRefId(), dto.getCNumber())) {
            throw new RuntimeException("C Number " + dto.getCNumber() +
                                     " already exists for Company " + dto.getCompanyRefId());
        }

        logger.debug("DTO validation passed");
    }

    /**
     * Create new SaleOrderMaster entity for INSERT operation
     */
    private SaleOrderMaster createNewEntity(SaleOrderDTO dto) {
        logger.debug("Creating new SaleOrderMaster entity");

        SaleOrderMaster entity = mapper.toEntity(dto);
        entity.setId(null);  // Critical: Set ID to NULL for auto-generation

        // CRITICAL FIX: Handle CNumber generation when it's 0
        if (entity.getCNumber() == null || entity.getCNumber() == 0) {
            logger.debug("CNumber is 0 or null. Generating from SequenceNoMaster...");
            Integer generatedCNumber = generateCNumber(entity.getCompanyRefId(), entity.getBillType());
            entity.setCNumber(generatedCNumber);

            // Generate CNumberDisplay: BillType + formatted sequence number (9 digits with leading zeros)
            String cNumberDisplay = entity.getBillType() + String.format("%09d", generatedCNumber);
            entity.setCNumberDisplay(cNumberDisplay);

            logger.info("Generated CNumber: {}, CNumberDisplay: {}", generatedCNumber, cNumberDisplay);
        } else if (entity.getCNumberDisplay() == null && dto.getCNumberDisplay() != null) {
            // If CNumber is provided but CNumberDisplay is missing, set from DTO
            entity.setCNumberDisplay(dto.getCNumberDisplay());
            logger.debug("Set cNumberDisplay from DTO: {}", dto.getCNumberDisplay());
        }

        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedDate(now);
        entity.setModifiedDate(now);

        // IMPORTANT: Set Created_By to database username (suser_name() in SQL Server)
        // In JPA, we let the database trigger handle this via DEFAULT constraint
        // Or we can set it to "SYSTEM" as a fallback
        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy("SYSTEM");  // Will be overridden by DB trigger if exists
            logger.debug("Set CreatedBy to SYSTEM (DB will use suser_name() if trigger exists)");
        }

        // IMPORTANT: Set Modified_By to database username (suser_name() in SQL Server)
        if (entity.getModifiedBy() == null) {
            entity.setModifiedBy("SYSTEM");  // Will be overridden by DB trigger if exists
            logger.debug("Set ModifiedBy to SYSTEM (DB will use suser_name() if trigger exists)");
        }

        // IMPORTANT: Always set Active to 1 for new records
        entity.setActive(1);
        logger.debug("Set Active to 1 for new record");

        sanitizeEntity(entity);
        initializeNumericDefaults(entity);

        // Final validation before returning
        if (entity.getCNumber() == null) {
            throw new RuntimeException("CRITICAL: CNumber must not be null before insert!");
        }

        logger.debug("New SaleOrderMaster entity created successfully with cNumber: {} and Active: {}",
                   entity.getCNumber(), entity.getActive());
        return entity;
    }

    /**
     * Update existing SaleOrderMaster entity for UPDATE operation
     */
    private SaleOrderMaster updateExistingEntity(SaleOrderDTO dto) {
        logger.debug("Updating existing SaleOrderMaster with ID: {}", dto.getId());

        SaleOrderMaster entity = repository.findById(dto.getId())
            .orElseThrow(() -> new RuntimeException(
                "SaleOrderMaster not found with ID: " + dto.getId()));

        // Map updates from DTO to entity
        mapper.updateEntityFromDto(dto, entity);

        // CRITICAL FIX: Ensure cNumber is set and never null
        if (dto.getCNumber() != null) {
            entity.setCNumber(dto.getCNumber());
            logger.debug("Updated cNumber to: {}", dto.getCNumber());
        }

        // CRITICAL FIX: Ensure cNumberDisplay is updated
        if (dto.getCNumberDisplay() != null) {
            entity.setCNumberDisplay(dto.getCNumberDisplay());
        }

        entity.setModifiedDate(LocalDateTime.now());

        // IMPORTANT: Ensure Modified_By is set and never null
        if (entity.getModifiedBy() == null) {
            entity.setModifiedBy("SYSTEM");
            logger.debug("Set ModifiedBy to SYSTEM for UPDATE operation");
        }

        sanitizeEntity(entity);
        initializeNumericDefaults(entity);

        // Final validation before returning
        if (entity.getCNumber() == null) {
            throw new RuntimeException("CRITICAL: CNumber must not be null before update!");
        }

        logger.debug("SaleOrderMaster entity updated successfully for ID: {} with cNumber: {}", dto.getId(), entity.getCNumber());
        return entity;
    }

    /**
     * Sanitize entity by converting empty strings to null
     */
    private void sanitizeEntity(SaleOrderMaster entity) {
        if (entity == null) return;

        if (entity.getSaleType() != null && entity.getSaleType().trim().isEmpty()) {
            entity.setSaleType(null);
        }
        if (entity.getBillType() != null && entity.getBillType().trim().isEmpty()) {
            entity.setBillType(null);
        }

        logger.debug("Entity sanitization completed");
    }

    /**
     * Initialize all numeric fields with proper defaults
     * Ensures NO NULL values are inserted into database columns that don't allow nulls
     */
    private void initializeNumericDefaults(SaleOrderMaster entity) {
        // Main Amount fields
        entity.setCoinage(entity.getCoinage() != null ? entity.getCoinage() : 0.0);
        entity.setGrossAmount(entity.getGrossAmount() != null ? entity.getGrossAmount() : 0.0);
        entity.setTaxAmount(entity.getTaxAmount() != null ? entity.getTaxAmount() : 0.0);
        entity.setDiscountAmount(entity.getDiscountAmount() != null ? entity.getDiscountAmount() : 0.0);
        entity.setPlusAmount(entity.getPlusAmount() != null ? entity.getPlusAmount() : 0.0);
        entity.setMinusAmount(entity.getMinusAmount() != null ? entity.getMinusAmount() : 0.0);
        entity.setAmount(entity.getAmount() != null ? entity.getAmount() : 0.0);
        entity.setCurrencyValue(entity.getCurrencyValue() != null ? entity.getCurrencyValue() : 0.0);
        entity.setActualNetAmount(entity.getActualNetAmount() != null ? entity.getActualNetAmount() : 0.0);

        // Boarding Amount fields
        entity.setBoardingAmount(entity.getBoardingAmount() != null ? entity.getBoardingAmount() : 0.0);
        entity.setBoardingAmount1(entity.getBoardingAmount1() != null ? entity.getBoardingAmount1() : 0.0);

        // L-prefixed Boarding Amount fields (Loading)
        entity.setLBoardingAmount(entity.getLBoardingAmount() != null ? entity.getLBoardingAmount() : 0.0);
        entity.setLBoardingAmount1(entity.getLBoardingAmount1() != null ? entity.getLBoardingAmount1() : 0.0);

        // O-prefixed Boarding Amount fields (Other/Origin)
        entity.setOBoardingAmount(entity.getOBoardingAmount() != null ? entity.getOBoardingAmount() : 0.0);
        entity.setOBoardingAmount1(entity.getOBoardingAmount1() != null ? entity.getOBoardingAmount1() : 0.0);

        // Port Charges fields
        entity.setPortCharges(entity.getPortCharges() != null ? entity.getPortCharges() : 0.0);

        // L-prefixed Port Charges fields
        entity.setLPortCharges(entity.getLPortCharges() != null ? entity.getLPortCharges() : 0.0);

        // O-prefixed Port Charges fields
        entity.setOPortCharges(entity.getOPortCharges() != null ? entity.getOPortCharges() : 0.0);

        // Seal amount fields
        entity.setSealAmount(entity.getSealAmount() != null ? entity.getSealAmount() : 0.0);
        entity.setBreakSealAmount(entity.getBreakSealAmount() != null ? entity.getBreakSealAmount() : 0.0);
        entity.setSealAmount2(entity.getSealAmount2() != null ? entity.getSealAmount2() : 0.0);
        entity.setBreakSealAmount2(entity.getBreakSealAmount2() != null ? entity.getBreakSealAmount2() : 0.0);
        entity.setSealAmount3(entity.getSealAmount3() != null ? entity.getSealAmount3() : 0.0);
        entity.setBreakSealAmount3(entity.getBreakSealAmount3() != null ? entity.getBreakSealAmount3() : 0.0);

        // String defaults
        if (entity.getBillType() == null || entity.getBillType().isEmpty()) {
            entity.setBillType("STANDARD");
        }
        if (entity.getSaleType() == null || entity.getSaleType().isEmpty()) {
            entity.setSaleType("STANDARD");
        }

        logger.debug("All numeric defaults initialized - no NULL amount fields");
    }

    /**
     * Save all child records for new SaleOrderMaster (INSERT case)
     */
    private void saveAllChildRecords(SaleOrderDTO dto, Integer masterId) {
        logger.debug("Saving child records for MasterId: {}", masterId);

        saveSaleDetails(dto, masterId);
        savePickupDetails(dto, masterId);
        saveDeliveryDetails(dto, masterId);
        saveForwardingDetails(dto, masterId);

        logger.debug("All child records saved successfully");
    }

    /**
     * Delete and recreate all child records (UPDATE case)
     */
    private void deleteAndRecreateChildRecords(SaleOrderDTO dto, Integer masterId) {
        logger.debug("Deleting existing child records for MasterId: {}", masterId);

        saleOrderDetailsRepository.deleteAllBySaleOrderMasterRefId(masterId);
        saleOrderPickupRepository.deleteAllBySaleOrderMasterRefId(masterId);
        saleOrderDeliveryRepository.deleteAllBySaleOrderMasterRefId(masterId);
        saleOrderForwardingRepository.deleteAllBySaleOrderMasterRefId(masterId);

        logger.debug("Child records deleted. Saving new records...");
        saveAllChildRecords(dto, masterId);
    }

    /**
     * Save Sale Details child records
     */
    private void saveSaleDetails(SaleOrderDTO dto, Integer masterId) {
        if (dto.getSaleOrderDetails() == null || dto.getSaleOrderDetails().isEmpty()) {
            logger.debug("No SaleOrderDetails to save");
            return;
        }

        List<SaleOrderDetails> saleOrderDetails = saleOrderDetailsMapper.toEntityList(dto.getSaleOrderDetails());
        LocalDateTime now = LocalDateTime.now();
        saleOrderDetails.forEach(detail -> {
            // IMPORTANT: Set id to null for auto-generation (id=0 in JSON becomes null in Java)
            if (detail.getId() == null || detail.getId() == 0) {
                detail.setId(null);
            }
            // CRITICAL: Set SaleOrderMasterRefId (master reference)
            detail.setSaleOrderMasterRefId(masterId);

            // CRITICAL: Set Created_Date and Modified_Date for child records
            detail.setCreatedDate(now);
            detail.setModifiedDate(now);
        });
        saleOrderDetailsRepository.saveAll(saleOrderDetails);

        logger.info("Saved {} SaleOrderDetails records for MasterId: {}",
                   saleOrderDetails.size(), masterId);
    }

    /**
     * Save Pickup Details child records
     */
    private void savePickupDetails(SaleOrderDTO dto, Integer masterId) {
        if (dto.getPickupDetails() == null || dto.getPickupDetails().isEmpty()) {
            logger.debug("No PickupDetails to save");
            return;
        }

        List<SaleOrderPickup> pickupList = mapper.toSaleOrderPickupentity(dto.getPickupDetails());
        LocalDateTime now = LocalDateTime.now();
        pickupList.forEach(pickup -> {
            // IMPORTANT: Set id to null for auto-generation (id=0 in JSON becomes null in Java)
            if (pickup.getId() == null || pickup.getId() == 0) {
                pickup.setId(null);
            }
            pickup.setSaleOrderMasterRefId(masterId);
            // CRITICAL: Set Created_Date and Modified_Date for child records
            pickup.setCreatedDate(now);

        });
        saleOrderPickupRepository.saveAll(pickupList);

        logger.info("Saved {} PickupDetails records for MasterId: {}",
                   pickupList.size(), masterId);
    }

    /**
     * Save Delivery Details child records
     */
    private void saveDeliveryDetails(SaleOrderDTO dto, Integer masterId) {
        if (dto.getDeliveryDetails() == null || dto.getDeliveryDetails().isEmpty()) {
            logger.debug("No DeliveryDetails to save");
            return;
        }

        List<SaleOrderDelivery> deliveryList = mapper.toSaleOrderDeliveryentity(dto.getDeliveryDetails());
        LocalDateTime now = LocalDateTime.now();
        deliveryList.forEach(delivery -> {
            // IMPORTANT: Set id to null for auto-generation (id=0 in JSON becomes null in Java)
            if (delivery.getId() == null || delivery.getId() == 0) {
                delivery.setId(null);
            }
            delivery.setSaleOrderMasterRefId(masterId);
            // CRITICAL: Set Created_Date and Modified_Date for child records
            delivery.setCreatedDate(now);

        });
        saleOrderDeliveryRepository.saveAll(deliveryList);

        logger.info("Saved {} DeliveryDetails records for MasterId: {}",
                   deliveryList.size(), masterId);
    }

    /**
     * Save Forwarding Details child records
     */
    private void saveForwardingDetails(SaleOrderDTO dto, Integer masterId) {
        if (dto.getForwardingDetails() == null || dto.getForwardingDetails().isEmpty()) {
            logger.debug("No ForwardingDetails to save");
            return;
        }

        List<SaleOrderForwarding> forwardingList = mapper.toSaleOrderForwardingentity(dto.getForwardingDetails());
        LocalDateTime now = LocalDateTime.now();
        forwardingList.forEach(forwarding -> {
            // IMPORTANT: Set id to null for auto-generation (id=0 in JSON becomes null in Java)
            if (forwarding.getId() == null || forwarding.getId() == 0) {
                forwarding.setId(null);
            }
            forwarding.setSaleOrderMasterRefId(masterId);
            // CRITICAL: Set Created_Date and Modified_Date for child records
            forwarding.setCreatedDate(now);
            forwarding.setModifiedDate(now);
        });
        saleOrderForwardingRepository.saveAll(forwardingList);

        logger.info("Saved {} ForwardingDetails records for MasterId: {}",
                   forwardingList.size(), masterId);
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
        // JobMasterRefId can be null - will be set to 0 in initializeDefaults
        if (dto.getSaleDate() == null) throw new RuntimeException("Sale Date required");
        // BillType and SaleType can be empty - will be set to defaults in initializeDefaults
        if (dto.getCNumber() == null) throw new RuntimeException("C Number required");
    }

    private void initializeDefaults(SaleOrderMaster entity) {
        // Critical field - CNumber must never be null
        if (entity.getCNumber() == null) {
            throw new RuntimeException("CNumber cannot be null - this is a critical field");
        }

        // Numeric defaults - all amount/charge fields
        if (entity.getCoinage() == null) entity.setCoinage(0.0);
        if (entity.getBoardingAmount() == null) entity.setBoardingAmount(0.0);
        if (entity.getBoardingAmount1() == null) entity.setBoardingAmount1(0.0);

        // L-prefixed Boarding Amount fields
        if (entity.getLBoardingAmount() == null) entity.setLBoardingAmount(0.0);
        if (entity.getLBoardingAmount1() == null) entity.setLBoardingAmount1(0.0);

        // O-prefixed Boarding Amount fields
        if (entity.getOBoardingAmount() == null) entity.setOBoardingAmount(0.0);
        if (entity.getOBoardingAmount1() == null) entity.setOBoardingAmount1(0.0);

        if (entity.getPortCharges() == null) entity.setPortCharges(0.0);

        // L-prefixed Port Charges
        if (entity.getLPortCharges() == null) entity.setLPortCharges(0.0);

        // O-prefixed Port Charges
        if (entity.getOPortCharges() == null) entity.setOPortCharges(0.0);

        if (entity.getSealAmount() == null) entity.setSealAmount(0.0);
        if (entity.getBreakSealAmount() == null) entity.setBreakSealAmount(0.0);
        if (entity.getSealAmount2() == null) entity.setSealAmount2(0.0);
        if (entity.getBreakSealAmount2() == null) entity.setBreakSealAmount2(0.0);
        if (entity.getSealAmount3() == null) entity.setSealAmount3(0.0);
        if (entity.getBreakSealAmount3() == null) entity.setBreakSealAmount3(0.0);
        if (entity.getGrossAmount() == null) entity.setGrossAmount(0.0);
        if (entity.getTaxAmount() == null) entity.setTaxAmount(0.0);
        if (entity.getDiscountAmount() == null) entity.setDiscountAmount(0.0);
        if (entity.getPlusAmount() == null) entity.setPlusAmount(0.0);
        if (entity.getMinusAmount() == null) entity.setMinusAmount(0.0);
        if (entity.getAmount() == null) entity.setAmount(0.0);
        if (entity.getCurrencyValue() == null) entity.setCurrencyValue(0.0);
        if (entity.getActualNetAmount() == null) entity.setActualNetAmount(0.0);

        // String defaults - required fields that cannot be null


        // Integer defaults
        if (entity.getActive() == null) entity.setActive(0);
        if (entity.getJobMasterRefId() == null) entity.setJobMasterRefId(0);
    }



    /**
     * Generate CNumber from SequenceNoMaster table
     * Implements the stored procedure logic for auto-generation:
     * 1. Get the next sequence number for the company and bill type
     * 2. Update the sequence in SequenceNoMaster table
     * 3. Return the generated sequence number
     *
     * @param companyRefId the company reference ID
     * @param billType the bill type (used as part of sequence name)
     * @return the generated CNumber (sequence number)
     */
    private Integer generateCNumber(Integer companyRefId, String billType) {
        logger.info("Generating CNumber for CompanyRefId: {}, BillType: {}", companyRefId, billType);

        try {
            // Build sequence name: "SaleOrderMaster" + BillType
            String sequenceName = "SaleOrderMaster" + billType;

            // Step 1: Get the maximum sequence number from SequenceNoMaster
            Integer maxSequenceNo = sequenceNoMasterRepository.findMaxSequenceNoByCompanyAndName(
                    companyRefId, sequenceName);

            logger.debug("Current max sequence number: {}", maxSequenceNo);

            Integer nextCNumber;

            // Step 2: If no sequence exists, start with 1 and create new record
            if (maxSequenceNo == null || maxSequenceNo == 0) {
                nextCNumber = 1;
                logger.debug("No existing sequence. Starting with CNumber = 1");

                // Create new SequenceNoMaster record
                SequenceNoMaster newSequence = SequenceNoMaster.builder()
                        .companyRefId(companyRefId)
                        .sequenceName(sequenceName)
                        .sequenceNo(nextCNumber)
                        .sequenceDate(LocalDateTime.now())
                        .build();

                sequenceNoMasterRepository.save(newSequence);
                logger.info("Created new sequence record. SequenceName: {}, NextCNumber: {}",
                           sequenceName, nextCNumber);
            } else {
                // Step 3: If sequence exists, increment by 1 and update record
                nextCNumber = maxSequenceNo + 1;
                logger.debug("Incrementing sequence. NextCNumber: {}", nextCNumber);

                // Update existing SequenceNoMaster record
                sequenceNoMasterRepository.findByCompanyRefIdAndSequenceName(
                        companyRefId, sequenceName)
                        .ifPresent(sequence -> {
                            sequence.setSequenceNo(nextCNumber);
                            sequence.setSequenceDate(LocalDateTime.now());
                            sequenceNoMasterRepository.save(sequence);
                            logger.info("Updated sequence record. SequenceName: {}, NewSequenceNo: {}",
                                       sequenceName, nextCNumber);
                        });
            }

            logger.info("Generated CNumber: {} for SequenceName: {}", nextCNumber, sequenceName);
            return nextCNumber;

        } catch (Exception e) {
            logger.error("Error generating CNumber for CompanyRefId: {}, BillType: {}, Error: {}",
                        companyRefId, billType, e.getMessage(), e);
            throw new RuntimeException("Failed to generate CNumber: " + e.getMessage());
        }
    }

    @Override
    public SaleF5View selectSaleOrder(SaleOrderFilterDTO filter) {
        logger.info("========== SelectSaleOrder API Started ==========");
        
        long startTime = System.currentTimeMillis();

        try {
            // ===== STEP 1: VALIDATE FILTER PARAMETERS =====
            logger.debug("Step 1: Validating filter parameters");
            filterHelper.validateFilter(filter);
            filterHelper.logFilterDetails(filter);

            // ===== STEP 2: BUILD DYNAMIC SPECIFICATION =====
            logger.debug("Step 2: Building dynamic specification from filter");
            Specification<SaleOrderMaster> specification = 
                    buildFilterSpecification(filter);

            // ===== STEP 3: FETCH FILTERED ORDER IDS =====
            logger.debug("Step 3: Fetching filtered order IDs");
            List<Integer> filteredOrderIds = getFilteredOrderIds(specification);
            logger.info("Found {} SaleOrderMaster records matching filter criteria", 
                    filteredOrderIds.size());

            // Early return if no matching records
            if (filteredOrderIds.isEmpty()) {
                logger.warn("No SaleOrderMaster records found matching filter criteria");
                return buildEmptySaleF5ViewResponse();
            }

            // ===== STEP 4: FETCH AND MAP SALE MASTER DATA =====
            logger.debug("Step 4: Fetching sale master data with joins");
            List<SaleMasterViewModel> saleMasterList = 
                    fetchAndMapSaleMasterData(filter.getComid(), filteredOrderIds);
            logger.info("Fetched and mapped {} SaleMaster records", saleMasterList.size());

            // ===== STEP 5: FETCH AND MAP SALE DETAILS DATA =====
            logger.debug("Step 5: Fetching sale details data with joins");
            List<SaleDetailsViewModel> saleDetailsList = 
                    fetchAndMapSaleDetailsData(filter.getComid(), filteredOrderIds);
            logger.info("Fetched and mapped {} SaleDetails records", saleDetailsList.size());

            // ===== STEP 6: BUILD FINAL RESPONSE =====
            logger.debug("Step 6: Building final SaleF5View response");
            SaleF5View response = buildSaleF5ViewResponse(
                    saleMasterList, 
                    saleDetailsList
            );

            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("SelectSaleOrder completed successfully in {} ms", executionTime);
            logger.info("========== SelectSaleOrder API Completed ==========");

            return response;

        } catch (Exception ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("ERROR in SelectSaleOrder after {} ms - Company: {}, Error: {}", 
                    executionTime, filter != null ? filter.getComid() : "NULL", 
                    ex.getMessage(), ex);
            logger.error("========== SelectSaleOrder API FAILED ==========");
            throw new RuntimeException("SelectSaleOrder failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Validate that company ID is present and valid
     * 
     * @param comid the company ID to validate
     * @throws RuntimeException if company ID is invalid
     */
    private void validateCompanyId(Integer comid) {
        if (comid == null || comid == 0) {
            logger.error("Invalid Company ID: {}", comid);
            throw new RuntimeException("Company ID is required and must be greater than 0");
        }
        logger.debug("Company ID validation passed: {}", comid);
    }

    /**
     * Build dynamic specification from filter parameters
     * Encapsulates all filter logic for SQL WHERE clause construction
     *
     * @param filter the SaleOrderFilterDTO with all filter parameters
     * @return Specification for JPA query building
     */
    private Specification<SaleOrderMaster> buildFilterSpecification(SaleOrderFilterDTO filter) {
        logger.debug("Building filter specification from parameters");

        return SaleOrderSpecification.buildFilter(
                filter.getComid(),
                filter.getId(),
                filter.getJId(),
                filter.getEmployeeid(),
                filter.getDashboardStatus(),
                filter.getStatusList(),
                filter.getStatusid(),
                filter.getCompletestatusnotshow(),
                filter.getRemarks(),
                filter.getOffvesselname(),
                filter.getLoadingvesselname(),
                filter.getSearch(),
                filter.getInvoice(),
                filter.getEta(),
                filter.getEtaType(),
                filter.getFromdate(),
                filter.getTodate(),
                filter.getPickup(),
                filter.getInvoicecheck()
        );
    }

    /**
     * Get list of filtered order IDs matching the specification
     * This helps avoid loading all data and then filtering in memory
     *
     * @param specification the JPA Specification to apply
     * @return List of order IDs matching the specification
     */
    private List<Integer> getFilteredOrderIds(
            Specification<SaleOrderMaster> specification) {
        logger.debug("Fetching filtered order IDs from database");

        List<Integer> orderIds = repository.findAll(specification)
                .stream()
                .map(SaleOrderMaster::getId)
                .collect(Collectors.toList());

        logger.debug("Retrieved {} order IDs from specification", orderIds.size());
        return orderIds;
    }

    /**
     * Fetch raw data from database and map to SaleMasterViewModel
     * OPTIMIZED: Filters at database level using IN clause
     * 
     * Performs:
     * 1. Native query execution with complex joins and ID filter
     * 2. Object array mapping to structured ViewModels
     * 3. Sorting by DETA (formatted date) then BillDate
     * 
     * @param companyId the company reference ID
     * @param filteredOrderIds list of order IDs to filter by
     * @return List of mapped SaleMasterViewModel objects, sorted
     */
    private List<SaleMasterViewModel> fetchAndMapSaleMasterData(
            Integer companyId, 
            List<Integer> filteredOrderIds) {

        logger.debug("Fetching SaleMaster raw data from database with joins and order ID filter");

        // OPTIMIZED: Pass orderIds to native query for database-level filtering
        // This fetches only 3-10 records instead of 100K+
        List<Object[]> rawData = repository.findSaleMasterRawDataWithJoinsByOrderIds(
                companyId, 
                filteredOrderIds);
        logger.debug("Retrieved {} raw SaleMaster rows from database (already filtered)", 
                rawData.size());

        // Map Object arrays to structured ViewModels
        List<SaleMasterViewModel> allRecords = 
                queryResultMapper.mapSaleMasterRows(rawData);
        logger.debug("Mapped {} SaleMaster records from raw data", allRecords.size());

        // Sort by DETA (formatted date) then BillDate
        // Note: Data is already filtered at DB level, no need for stream filter
        List<SaleMasterViewModel> sorted = allRecords.stream()
                .sorted(
                        Comparator
                                .comparing((SaleMasterViewModel s) ->
                                        s.getDeta() != null && !s.getDeta().isEmpty() 
                                                ? s.getDeta() 
                                                : "01/01/1900"
                                )
                                .thenComparing(vm -> vm.getBillDate() != null ? vm.getBillDate() : "")
                )
                .collect(Collectors.toList());

        logger.debug("After sorting: {} SaleMaster records", sorted.size());

        return sorted;
    }

    /**
     * Fetch raw data from database and map to SaleDetailsViewModel
     * OPTIMIZED: Filters at database level using IN clause
     * 
     * Performs:
     * 1. Native query execution with complex joins and ID filter
     * 2. Object array mapping to structured ViewModels
     * 
     * @param companyId the company reference ID
     * @param filteredOrderIds list of order IDs to filter by
     * @return List of mapped SaleDetailsViewModel objects
     */
    private List<SaleDetailsViewModel> fetchAndMapSaleDetailsData(
            Integer companyId, 
            List<Integer> filteredOrderIds) {

        logger.debug("Fetching SaleDetails raw data from database with joins and order ID filter");

        // OPTIMIZED: Pass orderIds to native query for database-level filtering
        // This fetches only 10-50 records instead of 200K+
        List<Object[]> rawData = repository.findSaleDetailsRawDataWithJoinsByOrderIds(
                companyId, 
                filteredOrderIds);
        logger.debug("Retrieved {} raw SaleDetails rows from database (already filtered)", 
                rawData.size());

        // Map Object arrays to structured ViewModels
        List<SaleDetailsViewModel> allRecords = 
                queryResultMapper.mapSaleDetailsRows(rawData);
        logger.debug("Mapped {} SaleDetails records from raw data", allRecords.size());

        // No additional filtering needed - data is already filtered at DB level
        logger.debug("Returning {} SaleDetails records", allRecords.size());

        return allRecords;
    }

    /**
     * Build final response using MapStruct mapper
     * Creates SaleF5View object combining sale master and details
     * 
     * @param saleMasterList the list of SaleMasterViewModel objects
     * @param saleDetailsList the list of SaleDetailsViewModel objects
     * @return constructed SaleF5View response object
     */
    private SaleF5View buildSaleF5ViewResponse(
            List<SaleMasterViewModel> saleMasterList,
            List<SaleDetailsViewModel> saleDetailsList) {

        logger.debug("Building SaleF5View response object");

        SaleF5View response = saleF5ViewMapper.createSaleF5View(
                saleMasterList,
                saleDetailsList
        );

        logger.debug("SaleF5View response built successfully");
        return response;
    }

    /**
     * Build an empty SaleF5View response when no records are found
     * Ensures response structure consistency across all scenarios
     * 
     * @return empty SaleF5View object
     */
    private SaleF5View buildEmptySaleF5ViewResponse() {
        logger.debug("Building empty SaleF5View response");
        
        SaleF5View response = saleF5ViewMapper.createSaleF5View(
                new ArrayList<>(),
                new ArrayList<>()
        );
        
        logger.debug("Empty SaleF5View response built");
        return response;
    }

    

}



