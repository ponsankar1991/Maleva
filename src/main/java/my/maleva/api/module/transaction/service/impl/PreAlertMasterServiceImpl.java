package my.maleva.api.module.transaction.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.transaction.dto.PreAlertMasterDto;
import my.maleva.api.module.transaction.entity.PreAlertMaster;
import my.maleva.api.module.transaction.repository.PreAlertMasterRepository;
import my.maleva.api.module.transaction.repository.PreAlertRepository;
import my.maleva.api.module.transaction.service.PreAlertMasterService;
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
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PreAlertMasterServiceImpl implements PreAlertMasterService {

    private final PreAlertMasterRepository preAlertMasterRepository;
    private final PreAlertRepository preAlertRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<PreAlertMasterDto> getAllByCompanyId(Integer companyRefId) {
        log.info("Fetching all PreAlertMaster records for company: {}", companyRefId);
        return preAlertMasterRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getActiveByCompanyId(Integer companyRefId) {
        log.info("Fetching active PreAlertMaster records for company: {}", companyRefId);
        return preAlertMasterRepository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PreAlertMasterDto> getById(Integer id) {
        log.info("Fetching PreAlertMaster by ID: {}", id);
        return preAlertMasterRepository.findById(id).map(this::toDto);
    }

    @Override
    public boolean delete(Integer id) {
        log.info("Deleting PreAlertMaster with ID: {}", id);

        if (!preAlertMasterRepository.existsById(id)) {
            log.warn("PreAlertMaster not found with ID: {}", id);
            return false;
        }

        preAlertRepository.deleteByPreAlertMasterRefId(id);
        preAlertMasterRepository.deleteById(id);
        log.info("PreAlertMaster deleted successfully with ID: {}", id);
        return true;
    }

    @Override
    public List<PreAlertMasterDto> getByCustomerId(Integer customerMasterRefId) {
        log.info("Fetching PreAlertMaster records for customer: {}", customerMasterRefId);
        return preAlertMasterRepository.findByCustomerMasterRefId(customerMasterRefId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getByJobTypeId(Integer jobTypeMasterRefId) {
        log.info("Fetching PreAlertMaster records for job type: {}", jobTypeMasterRefId);
        return preAlertMasterRepository.findByJobTypeMasterRefId(jobTypeMasterRefId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getByPort(String port) {
        log.info("Fetching PreAlertMaster records for port: {}", port);
        return preAlertMasterRepository.findByPort(port)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getByVessel(String vessel) {
        log.info("Fetching PreAlertMaster records for vessel: {}", vessel);
        return preAlertMasterRepository.findByVessel(vessel)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertMasterDto> getByDateRange(Integer companyId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching PreAlertMaster records for date range: {} to {}", fromDate, toDate);
        return preAlertMasterRepository.findByDateRange(companyId, fromDate, toDate)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PreAlertMasterDto> getByCNumber(Integer cNumber, Integer companyRefId) {
        log.info("Fetching PreAlertMaster by CNumber: {}", cNumber);
        return preAlertMasterRepository.findByCNumberAndCompanyRefId(cNumber, companyRefId)
                .map(this::toDto);
    }

    @Override
    public Optional<PreAlertMasterDto> getByCNumberDisplay(String cNumberDisplay) {
        log.info("Fetching PreAlertMaster by CNumberDisplay: {}", cNumberDisplay);
        return preAlertMasterRepository.findByCNumberDisplay(cNumberDisplay)
                .map(this::toDto);
    }

    @Override
    public Long countActiveRecords(Integer companyRefId) {
        log.info("Counting active PreAlertMaster records for company: {}", companyRefId);
        return preAlertMasterRepository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    public PreAlertMasterDto activate(Integer id) {
        log.info("Activating PreAlertMaster with ID: {}", id);

        PreAlertMaster entity = preAlertMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreAlertMaster not found with ID: " + id));

        entity.setActive(1);
        PreAlertMaster updated = preAlertMasterRepository.save(entity);

        log.info("PreAlertMaster activated successfully with ID: {}", id);
        return toDto(updated);
    }

    @Override
    public PreAlertMasterDto deactivate(Integer id) {
        log.info("Deactivating PreAlertMaster with ID: {}", id);

        PreAlertMaster entity = preAlertMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreAlertMaster not found with ID: " + id));

        entity.setActive(0);
        PreAlertMaster updated = preAlertMasterRepository.save(entity);

        log.info("PreAlertMaster deactivated successfully with ID: {}", id);
        return toDto(updated);
    }

    @Override
    public void executePreAlertStoredProcedure(String masterJson, Integer companyId) {
        log.info("Executing SP_PreAlert stored procedure for company: {}", companyId);

        try {
            String sql = "EXEC SP_PreAlert @master = ?, @ComId = ?";
            jdbcTemplate.update(sql, masterJson, companyId);
            log.info("SP_PreAlert executed successfully for company: {}", companyId);
        } catch (Exception e) {
            log.error("Error executing SP_PreAlert for company: {}", companyId, e);
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
                .cNumber(entity.getCNumber())
                .cNumberDisplay(entity.getCNumberDisplay())
                .entryDate(entity.getEntryDate())
                .saleOrderMasterRefId(entity.getSaleOrderMasterRefId())
                .build();
    }
}
