package my.maleva.api.module.prealert.service.impl;

import my.maleva.api.module.prealert.dto.PreAlertMasterDto;
import my.maleva.api.module.prealert.mapper.PreAlertMasterMapper;
import my.maleva.api.module.prealert.entity.PreAlertMaster;
import my.maleva.api.module.prealert.repository.PreAlertMasterRepository;
import my.maleva.api.module.prealert.repository.PreAlertRepository;
import my.maleva.api.module.prealert.service.PreAlertMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PreAlertMaster Service Implementation
 * Handles business logic for PreAlertMaster operations
 * Incorporates SP_PreAlert stored procedure logic
 */
@Service
@Transactional
public class PreAlertMasterServiceImpl implements PreAlertMasterService {

    private static final Logger logger = LoggerFactory.getLogger(PreAlertMasterServiceImpl.class);

    @Autowired
    private PreAlertMasterRepository preAlertMasterRepository;

    @Autowired
    private PreAlertRepository preAlertRepository;

    @Autowired
    private PreAlertMasterMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<PreAlertMasterDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all PreAlertMaster records for company: {}", companyRefId);
        return preAlertMasterRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getActiveByCompanyId(Integer companyRefId) {
        logger.info("Fetching active PreAlertMaster records for company: {}", companyRefId);
        return preAlertMasterRepository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PreAlertMasterDto> getById(Integer id) {
        logger.info("Fetching PreAlertMaster by ID: {}", id);
        return preAlertMasterRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public PreAlertMasterDto create(PreAlertMasterDto dto) {
        logger.info("Creating new PreAlertMaster for company: {}", dto.getCompanyRefId());

        PreAlertMaster entity = mapper.toEntity(dto);
        entity.setActive(1);

        PreAlertMaster saved = preAlertMasterRepository.save(entity);
        logger.info("PreAlertMaster created successfully with ID: {}", saved.getId());

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public PreAlertMasterDto update(Integer id, PreAlertMasterDto dto) {
        logger.info("Updating PreAlertMaster with ID: {}", id);

        PreAlertMaster entity = preAlertMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreAlertMaster not found with ID: " + id));

        mapper.updateEntityFromDto(dto, entity);

        PreAlertMaster updated = preAlertMasterRepository.save(entity);
        logger.info("PreAlertMaster updated successfully with ID: {}", id);

        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting PreAlertMaster with ID: {}", id);

        if (!preAlertMasterRepository.existsById(id)) {
            logger.warn("PreAlertMaster not found with ID: {}", id);
            return false;
        }

        // Delete associated PreAlert records first
        preAlertRepository.deleteByPreAlertMasterRefId(id);

        preAlertMasterRepository.deleteById(id);
        logger.info("PreAlertMaster deleted successfully with ID: {}", id);

        return true;
    }

    @Override
    public List<PreAlertMasterDto> getByCustomerId(Integer customerMasterRefId) {
        logger.info("Fetching PreAlertMaster records for customer: {}", customerMasterRefId);
        return preAlertMasterRepository.findByCustomerMasterRefId(customerMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getByJobTypeId(Integer jobTypeMasterRefId) {
        logger.info("Fetching PreAlertMaster records for job type: {}", jobTypeMasterRefId);
        return preAlertMasterRepository.findByJobTypeMasterRefId(jobTypeMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getByPort(String port) {
        logger.info("Fetching PreAlertMaster records for port: {}", port);
        return preAlertMasterRepository.findByPort(port)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getByVessel(String vessel) {
        logger.info("Fetching PreAlertMaster records for vessel: {}", vessel);
        return preAlertMasterRepository.findByVessel(vessel)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getByDateRange(Integer companyId, LocalDate fromDate, LocalDate toDate) {
        logger.info("Fetching PreAlertMaster records for date range: {} to {}", fromDate, toDate);
        return preAlertMasterRepository.findByDateRange(companyId, fromDate, toDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PreAlertMasterDto> getByCNumber(Integer cNumber, Integer companyRefId) {
        logger.info("Fetching PreAlertMaster by CNumber: {}", cNumber);
        return preAlertMasterRepository.findByCNumberAndCompanyRefId(cNumber, companyRefId)
                .map(mapper::toDto);
    }

    @Override
    public Optional<PreAlertMasterDto> getByCNumberDisplay(String cNumberDisplay) {
        logger.info("Fetching PreAlertMaster by CNumberDisplay: {}", cNumberDisplay);
        return preAlertMasterRepository.findByCNumberDisplay(cNumberDisplay)
                .map(mapper::toDto);
    }

    @Override
    public Long countActiveRecords(Integer companyRefId) {
        logger.info("Counting active PreAlertMaster records for company: {}", companyRefId);
        return preAlertMasterRepository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    @Transactional
    public PreAlertMasterDto activate(Integer id) {
        logger.info("Activating PreAlertMaster with ID: {}", id);

        PreAlertMaster entity = preAlertMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreAlertMaster not found with ID: " + id));

        entity.setActive(1);
        PreAlertMaster updated = preAlertMasterRepository.save(entity);

        logger.info("PreAlertMaster activated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public PreAlertMasterDto deactivate(Integer id) {
        logger.info("Deactivating PreAlertMaster with ID: {}", id);

        PreAlertMaster entity = preAlertMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreAlertMaster not found with ID: " + id));

        entity.setActive(0);
        PreAlertMaster updated = preAlertMasterRepository.save(entity);

        logger.info("PreAlertMaster deactivated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }

    /**
     * Execute SP_PreAlert stored procedure for bulk PreAlert operations
     *
     * This method incorporates the business logic from the SP_PreAlert stored procedure:
     * - Accepts master PreAlertMaster data and child PreAlert details in JSON format
     * - Handles insert/update of PreAlertMaster records
     * - Processes PreAlert detail records from JSON array
     * - Manages sequence number generation (CNumber and CNumberDisplay)
     * - Handles transaction management with rollback on error
     *
     * @param masterJson JSON containing PreAlertMaster and PreAlert details
     * @param companyId Company Reference ID
     */
    @Override
    @Transactional
    public void executePreAlertStoredProcedure(String masterJson, Integer companyId) {
        logger.info("Executing SP_PreAlert stored procedure for company: {}", companyId);

        try {
            // Call the stored procedure using JdbcTemplate
            String sql = "EXEC SP_PreAlert @master = ?, @ComId = ?";

            jdbcTemplate.update(sql, masterJson, companyId);

            logger.info("SP_PreAlert executed successfully for company: {}", companyId);
        } catch (Exception e) {
            logger.error("Error executing SP_PreAlert for company: {}", companyId, e);
            throw new RuntimeException("Error executing SP_PreAlert: " + e.getMessage(), e);
        }
    }
}

