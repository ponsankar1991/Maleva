package my.maleva.api.service.impl;

import my.maleva.api.dto.PreAlertDto;
import my.maleva.api.mapper.PreAlertMapper;
import my.maleva.api.model.PreAlert;
import my.maleva.api.repo.PreAlertRepository;
import my.maleva.api.service.PreAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PreAlert Service Implementation
 * Handles business logic for PreAlert detail records
 */
@Service
@Transactional
public class PreAlertServiceImpl implements PreAlertService {

    private static final Logger logger = LoggerFactory.getLogger(PreAlertServiceImpl.class);

    @Autowired
    private PreAlertRepository preAlertRepository;

    @Autowired
    private PreAlertMapper mapper;

    @Override
    public List<PreAlertDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all PreAlert records for company: {}", companyRefId);
        return preAlertRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertDto> getActiveByCompanyId(Integer companyRefId) {
        logger.info("Fetching active PreAlert records for company: {}", companyRefId);
        return preAlertRepository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PreAlertDto> getById(Integer id) {
        logger.info("Fetching PreAlert by ID: {}", id);
        return preAlertRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public PreAlertDto create(PreAlertDto dto) {
        logger.info("Creating new PreAlert for company: {}", dto.getCompanyRefId());

        PreAlert entity = mapper.toEntity(dto);
        entity.setActive(1);

        PreAlert saved = preAlertRepository.save(entity);
        logger.info("PreAlert created successfully with ID: {}", saved.getId());

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public PreAlertDto update(Integer id, PreAlertDto dto) {
        logger.info("Updating PreAlert with ID: {}", id);

        PreAlert entity = preAlertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreAlert not found with ID: " + id));

        mapper.updateEntityFromDto(dto, entity);

        PreAlert updated = preAlertRepository.save(entity);
        logger.info("PreAlert updated successfully with ID: {}", id);

        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting PreAlert with ID: {}", id);

        if (!preAlertRepository.existsById(id)) {
            logger.warn("PreAlert not found with ID: {}", id);
            return false;
        }

        preAlertRepository.deleteById(id);
        logger.info("PreAlert deleted successfully with ID: {}", id);

        return true;
    }

    @Override
    public List<PreAlertDto> getByPreAlertMasterId(Integer preAlertMasterRefId) {
        logger.info("Fetching PreAlert records for master: {}", preAlertMasterRefId);
        return preAlertRepository.findByPreAlertMasterRefId(preAlertMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertDto> getByCustomerId(Integer customerMasterRefId) {
        logger.info("Fetching PreAlert records for customer: {}", customerMasterRefId);
        return preAlertRepository.findByCustomerMasterRefId(customerMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertDto> getByEmployeeId(Integer employeeMasterRefId) {
        logger.info("Fetching PreAlert records for employee: {}", employeeMasterRefId);
        return preAlertRepository.findByEmployeeMasterRefId(employeeMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertDto> getByJobTypeId(Integer jobTypeMasterRefId) {
        logger.info("Fetching PreAlert records for job type: {}", jobTypeMasterRefId);
        return preAlertRepository.findByJobTypeMasterRefId(jobTypeMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertDto> getByJobStatusId(Integer jobStatusMasterRefId) {
        logger.info("Fetching PreAlert records for job status: {}", jobStatusMasterRefId);
        return preAlertRepository.findByJobStatusMasterRefId(jobStatusMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertDto> getByBoardingOfficerId(Integer boardingOfficerRefId) {
        logger.info("Fetching PreAlert records for boarding officer: {}", boardingOfficerRefId);
        return preAlertRepository.findByBoardingOfficerRefId(boardingOfficerRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertDto> getByVessel(String vessel) {
        logger.info("Fetching PreAlert records for vessel: {}", vessel);
        return preAlertRepository.findByVessel(vessel)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertDto> getByPort(String port) {
        logger.info("Fetching PreAlert records for port: {}", port);
        return preAlertRepository.findByPort(port)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreAlertDto> getByJobNo(String jobNo) {
        logger.info("Fetching PreAlert records for job number: {}", jobNo);
        return preAlertRepository.findByJobNo(jobNo)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByPreAlertMasterId(Integer preAlertMasterRefId) {
        logger.info("Deleting all PreAlert records for master: {}", preAlertMasterRefId);
        preAlertRepository.deleteByPreAlertMasterRefId(preAlertMasterRefId);
    }

    @Override
    public Long countByPreAlertMasterId(Integer preAlertMasterRefId) {
        logger.info("Counting PreAlert records for master: {}", preAlertMasterRefId);
        return preAlertRepository.countByPreAlertMasterRefId(preAlertMasterRefId);
    }

    @Override
    @Transactional
    public PreAlertDto activate(Integer id) {
        logger.info("Activating PreAlert with ID: {}", id);

        PreAlert entity = preAlertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreAlert not found with ID: " + id));

        entity.setActive(1);
        PreAlert updated = preAlertRepository.save(entity);

        logger.info("PreAlert activated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public PreAlertDto deactivate(Integer id) {
        logger.info("Deactivating PreAlert with ID: {}", id);

        PreAlert entity = preAlertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreAlert not found with ID: " + id));

        entity.setActive(0);
        PreAlert updated = preAlertRepository.save(entity);

        logger.info("PreAlert deactivated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }
}

