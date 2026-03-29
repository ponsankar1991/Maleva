package my.maleva.api.module.vessalplanning.service.impl;

import my.maleva.api.module.vessalplanning.dto.VesselPlanningMasterDto;
import my.maleva.api.module.vessalplanning.dto.VesselPlanningDetailsDto;
import my.maleva.api.module.vessalplanning.mapper.VesselPlanningMasterMapper;
import my.maleva.api.module.vessalplanning.mapper.VesselPlanningDetailsMapper;
import my.maleva.api.module.vessalplanning.entity.VesselPlanningMaster;
import my.maleva.api.module.vessalplanning.entity.VesselPlanningDetails;
import my.maleva.api.module.vessalplanning.repository.VesselPlanningMasterRepository;
import my.maleva.api.module.vessalplanning.repository.VesselPlanningDetailsRepository;
import my.maleva.api.module.vessalplanning.service.VesselPlanningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * VesselPlanningServiceImpl - Implementation for VesselPlanning service
 * Incorporates SP_VESSELPLANINGMaster stored procedure logic
 * Handles vessel planning processing with detail records
 */
@Service
public class VesselPlanningServiceImpl implements VesselPlanningService {

    private static final Logger logger = LoggerFactory.getLogger(VesselPlanningServiceImpl.class);

    @Autowired
    private VesselPlanningMasterRepository repository;

    @Autowired
    private VesselPlanningDetailsRepository detailsRepository;

    @Autowired
    private VesselPlanningMasterMapper mapper;

    @Autowired
    private VesselPlanningDetailsMapper detailsMapper;

    @Override
    public List<VesselPlanningMasterDto> getByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching VesselPlanning for company: {}", companyRefId);
        return repository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VesselPlanningMasterDto> getActiveByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching active VesselPlanning for company: {}", companyRefId);
        return repository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<VesselPlanningMasterDto> getByCNumber(Integer cNumber, Integer companyRefId) {
        logger.info("Fetching VesselPlanning by C Number: {} for company: {}", cNumber, companyRefId);
        return repository.findByCNumberAndCompanyRefId(cNumber, companyRefId).map(mapper::toDto);
    }

    @Override
    public List<VesselPlanningMasterDto> getByUserRefId(Integer userRefId) {
        logger.info("Fetching VesselPlanning for user: {}", userRefId);
        return repository.findByUserRefId(userRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VesselPlanningMasterDto> getByEmployeeRefId(Integer employeeRefId) {
        logger.info("Fetching VesselPlanning for employee: {}", employeeRefId);
        return repository.findByEmployeeRefId(employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VesselPlanningMasterDto> getByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching VesselPlanning for date range: {} to {}", startDate, endDate);
        return repository.findByFDateGreaterThanEqualAndTDateLessThanEqual(startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VesselPlanningMasterDto> getByCompanyAndDateRange(Integer companyRefId, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching VesselPlanning for company: {} and date range: {} to {}", companyRefId, startDate, endDate);
        return repository.findByCompanyRefIdAndFDateGreaterThanEqualAndTDateLessThanEqual(companyRefId, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<VesselPlanningMasterDto> getById(Integer id) {
        logger.info("Fetching VesselPlanning by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public VesselPlanningMasterDto create(VesselPlanningMasterDto dto) {
        logger.info("Creating new VesselPlanning");
        validateVesselPlanningData(dto);
        VesselPlanningMaster entity = mapper.toEntity(dto);

        // Set default values as per SP_VESSELPLANINGMaster logic
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

        VesselPlanningMaster saved = repository.save(entity);
        logger.info("VesselPlanning created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public VesselPlanningMasterDto update(Integer id, VesselPlanningMasterDto dto) {
        logger.info("Updating VesselPlanning with ID: {}", id);
        validateVesselPlanningData(dto);

        VesselPlanningMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("VesselPlanning not found: " + id));

        // Update modified date as per SP_VESSELPLANINGMaster logic
        LocalDateTime now = LocalDateTime.now();
        entity.setModifiedDate(now);
        entity.setModifiedBy("SYSTEM");

        mapper.updateEntityFromDto(dto, entity);
        VesselPlanningMaster updated = repository.save(entity);
        logger.info("VesselPlanning updated with ID: {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting VesselPlanning with ID: {}", id);
        if (repository.existsById(id)) {
            // Delete associated detail records first (foreign key constraint)
            detailsRepository.deleteByVesselPlanningMasterRefId(id);
            repository.deleteById(id);
            logger.info("VesselPlanning and details deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long countByCompanyRefId(Integer companyRefId) {
        logger.info("Counting VesselPlanning for company: {}", companyRefId);
        return repository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompanyRefId(Integer companyRefId) {
        logger.info("Counting active VesselPlanning for company: {}", companyRefId);
        return repository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    public void validateVesselPlanningData(VesselPlanningMasterDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new RuntimeException("Company Reference ID is required");
        }
        if (dto.getSaleDate() == null) {
            throw new RuntimeException("Sale Date is required");
        }
        if (dto.getFDate() == null) {
            throw new RuntimeException("From Date is required");
        }
        if (dto.getTDate() == null) {
            throw new RuntimeException("To Date is required");
        }
        if (dto.getCNumberDisplay() == null || dto.getCNumberDisplay().trim().isEmpty()) {
            throw new RuntimeException("C Number Display is required");
        }
    }

    @Override
    @Transactional
    public VesselPlanningMasterDto activateVesselPlanning(Integer id) {
        logger.info("Activating VesselPlanning with ID: {}", id);
        VesselPlanningMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("VesselPlanning not found: " + id));

        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        VesselPlanningMaster updated = repository.save(entity);

        logger.info("VesselPlanning activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public VesselPlanningMasterDto deactivateVesselPlanning(Integer id) {
        logger.info("Deactivating VesselPlanning with ID: {}", id);
        VesselPlanningMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("VesselPlanning not found: " + id));

        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        VesselPlanningMaster updated = repository.save(entity);

        logger.info("VesselPlanning deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public VesselPlanningMasterDto processVesselPlanning(VesselPlanningMasterDto dto, List<VesselPlanningDetailsDto> details, Integer companyId) {
        logger.info("Processing VesselPlanning with SP_VESSELPLANINGMaster logic for company: {}", companyId);

        // SP_VESSELPLANINGMaster Logic:
        // 1. Set company ID
        dto.setCompanyRefId(companyId);

        // 2. Validate referenced entities (User, Employee)
        validateReferencedEntities(dto);

        // 3. If update (id > 0), delete existing details
        if (dto.getId() != null && dto.getId() > 0) {
            logger.info("Deleting existing VesselPlanning details for ID: {}", dto.getId());
            detailsRepository.deleteByVesselPlanningMasterRefId(dto.getId());
        }

        // 4. Create or update VesselPlanningMaster
        VesselPlanningMasterDto savedEntry;
        if (dto.getId() == null || dto.getId() == 0) {
            logger.info("Processing INSERT operation for VesselPlanning");
            savedEntry = create(dto);
        } else {
            logger.info("Processing UPDATE operation for VesselPlanning ID: {}", dto.getId());
            savedEntry = update(dto.getId(), dto);
        }

        // 5. Insert detail records
        if (details != null && !details.isEmpty()) {
            logger.info("Inserting {} VesselPlanning detail records", details.size());
            for (VesselPlanningDetailsDto detail : details) {
                detail.setVesselPlanningMasterRefId(savedEntry.getId());
                LocalDateTime now = LocalDateTime.now();
                detail.setCreatedDate(now);
                detail.setModifiedDate(now);
                VesselPlanningDetails detailEntity = detailsMapper.toEntity(detail);
                detailsRepository.save(detailEntity);
            }
        }

        // 6. Generate sequence number if new record
        if (dto.getId() == null || dto.getId() == 0) {
            generateSequenceNumber(savedEntry, companyId);
        }

        logger.info("VesselPlanning processing complete with ID: {}", savedEntry.getId());
        return savedEntry;
    }

    private void validateReferencedEntities(VesselPlanningMasterDto dto) {
        logger.debug("Validating referenced entities for VesselPlanning");
        if (dto.getUserRefId() != null && dto.getUserRefId() > 0) {
            logger.debug("User ID to validate: {}", dto.getUserRefId());
        }
        if (dto.getEmployeeRefId() != null && dto.getEmployeeRefId() > 0) {
            logger.debug("Employee ID to validate: {}", dto.getEmployeeRefId());
        }
    }

    private void generateSequenceNumber(VesselPlanningMasterDto dto, Integer companyId) {
        logger.debug("Generating sequence number for VesselPlanning: {}", dto.getId());
        // Format: VPL + 9 digits
        String sequenceDisplay = String.format("VPL%09d", dto.getId());
        logger.debug("Generated sequence: {}", sequenceDisplay);
    }
}

