package my.maleva.api.module.prealert.service.impl;

import my.maleva.api.module.prealert.dto.PreAlertMasterDto;
import my.maleva.api.module.prealert.entity.PreAlertMaster;
import my.maleva.api.module.prealert.repository.PreAlertMasterRepository;
import my.maleva.api.module.prealert.repository.PreAlertRepository;
import my.maleva.api.module.prealert.service.PreAlertMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PreAlertMaster Service Implementation
 * Handles business logic for PreAlertMaster operations and delegates writes to SP_PreAlert.
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
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<PreAlertMasterDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all PreAlertMaster records for company: {}", companyRefId);
        return preAlertMasterRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getActiveByCompanyId(Integer companyRefId) {
        logger.info("Fetching active PreAlertMaster records for company: {}", companyRefId);
        return preAlertMasterRepository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PreAlertMasterDto> getById(Integer id) {
        logger.info("Fetching PreAlertMaster by ID: {}", id);
        return preAlertMasterRepository.findById(id).map(this::toDto);
    }

    @Override
    public boolean delete(Integer id) {
        logger.info("Deleting PreAlertMaster with ID: {}", id);

        if (!preAlertMasterRepository.existsById(id)) {
            logger.warn("PreAlertMaster not found with ID: {}", id);
            return false;
        }

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
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getByJobTypeId(Integer jobTypeMasterRefId) {
        logger.info("Fetching PreAlertMaster records for job type: {}", jobTypeMasterRefId);
        return preAlertMasterRepository.findByJobTypeMasterRefId(jobTypeMasterRefId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getByPort(String port) {
        logger.info("Fetching PreAlertMaster records for port: {}", port);
        return preAlertMasterRepository.findByPort(port)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getByVessel(String vessel) {
        logger.info("Fetching PreAlertMaster records for vessel: {}", vessel);
        return preAlertMasterRepository.findByVessel(vessel)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getByDateRange(Integer companyId, LocalDate fromDate, LocalDate toDate) {
        logger.info("Fetching PreAlertMaster records for date range: {} to {}", fromDate, toDate);
        return preAlertMasterRepository.findByDateRange(companyId, fromDate, toDate)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PreAlertMasterDto> getByCNumber(Integer cNumber, Integer companyRefId) {
        logger.info("Fetching PreAlertMaster by CNumber: {}", cNumber);
        return preAlertMasterRepository.findByCNumberAndCompanyRefId(cNumber, companyRefId)
                .map(this::toDto);
    }

    @Override
    public Optional<PreAlertMasterDto> getByCNumberDisplay(String cNumberDisplay) {
        logger.info("Fetching PreAlertMaster by CNumberDisplay: {}", cNumberDisplay);
        return preAlertMasterRepository.findByCNumberDisplay(cNumberDisplay)
                .map(this::toDto);
    }

    @Override
    public Long countActiveRecords(Integer companyRefId) {
        logger.info("Counting active PreAlertMaster records for company: {}", companyRefId);
        return preAlertMasterRepository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    public PreAlertMasterDto activate(Integer id) {
        logger.info("Activating PreAlertMaster with ID: {}", id);

        PreAlertMaster entity = preAlertMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreAlertMaster not found with ID: " + id));

        entity.setActive(1);
        PreAlertMaster updated = preAlertMasterRepository.save(entity);

        logger.info("PreAlertMaster activated successfully with ID: {}", id);
        return toDto(updated);
    }

    @Override
    public PreAlertMasterDto deactivate(Integer id) {
        logger.info("Deactivating PreAlertMaster with ID: {}", id);

        PreAlertMaster entity = preAlertMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreAlertMaster not found with ID: " + id));

        entity.setActive(0);
        PreAlertMaster updated = preAlertMasterRepository.save(entity);

        logger.info("PreAlertMaster deactivated successfully with ID: {}", id);
        return toDto(updated);
    }

    @Override
    public void executePreAlertStoredProcedure(String masterJson, Integer companyId) {
        logger.info("Executing SP_PreAlert stored procedure for company: {}", companyId);

        try {
            String sql = "EXEC SP_PreAlert @master = ?, @ComId = ?";
            jdbcTemplate.update(sql, masterJson, companyId);
            logger.info("SP_PreAlert executed successfully for company: {}", companyId);
        } catch (Exception e) {
            logger.error("Error executing SP_PreAlert for company: {}", companyId, e);
            throw new RuntimeException("Error executing SP_PreAlert: " + e.getMessage(), e);
        }
    }

    private PreAlertMasterDto toDto(PreAlertMaster entity) {
        return PreAlertMasterDto.builder()
                .id(entity.getId())
                .companyRefId(entity.getCompanyRefId())
                .customerMasterRefId(entity.getCustomerMasterRefId())
                .jobTypeMasterRefId(entity.getJobTypeMasterRefId())
                .fromDate(entity.getFromDate())
                .toDate(entity.getToDate())
                .port(entity.getPort())
                .vessel(entity.getVessel())
                .oeta(entity.getOeta())
                .leta(entity.getLeta())
                .alleta(entity.getAlleta())
                .none(entity.getNone())
                .chkPort(entity.getChkPort())
                .chkVessel(entity.getChkVessel())
                .chkPickupDate(entity.getChkPickupDate())
                .chkConsolidated(entity.getChkConsolidated())
                .chkDeliveryDone(entity.getChkDeliveryDone())
                .active(entity.getActive())
                .createdDate(entity.getCreatedDate())
                .modifiedDate(entity.getModifiedDate())
                .CNumber(entity.getCNumber())
                .CNumberDisplay(entity.getCNumberDisplay())
                .entryDate(entity.getEntryDate())
                .saleOrderMasterRefId(entity.getSaleOrderMasterRefId())
                .build();
    }
}
