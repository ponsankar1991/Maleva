package my.maleva.api.service.impl;

import my.maleva.api.dto.RTIMasterDto;
import my.maleva.api.mapper.RTIMasterMapper;
import my.maleva.api.model.RTIMaster;
import my.maleva.api.repo.RTIMasterRepository;
import my.maleva.api.service.RTIMasterService;
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
 * RTIMasterServiceImpl
 * Service implementation for RTIMaster
 * Implements SP_RTIMaster stored procedure logic
 */
@Service
public class RTIMasterServiceImpl implements RTIMasterService {

    private static final Logger logger = LoggerFactory.getLogger(RTIMasterServiceImpl.class);

    @Autowired
    private RTIMasterRepository rtiMasterRepository;

    @Autowired
    private RTIMasterMapper mapper;

    @Override
    public List<RTIMasterDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all RTIMaster records for company: {}", companyRefId);
        return rtiMasterRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RTIMasterDto> getActiveByCompanyId(Integer companyRefId) {
        logger.info("Fetching active RTIMaster records for company: {}", companyRefId);
        return rtiMasterRepository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RTIMasterDto> getById(Integer id) {
        logger.info("Fetching RTIMaster by ID: {}", id);
        return rtiMasterRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public RTIMasterDto create(RTIMasterDto dto) {
        logger.info("Creating new RTIMaster for company: {}", dto.getCompanyRefId());
        RTIMaster entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());
        if (entity.getActive() == null) {
            entity.setActive(0);
        }
        if (entity.getSleeping() == null) {
            entity.setSleeping(0);
        }
        RTIMaster saved = rtiMasterRepository.save(entity);
        logger.info("RTIMaster created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public RTIMasterDto update(Integer id, RTIMasterDto dto) {
        logger.info("Updating RTIMaster with ID: {}", id);
        RTIMaster entity = rtiMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RTIMaster not found with ID: " + id));
        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        RTIMaster updated = rtiMasterRepository.save(entity);
        logger.info("RTIMaster updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting RTIMaster with ID: {}", id);
        if (rtiMasterRepository.existsById(id)) {
            rtiMasterRepository.deleteById(id);
            logger.info("RTIMaster deleted with ID: {}", id);
            return true;
        }
        logger.warn("RTIMaster not found with ID: {}", id);
        return false;
    }

    @Override
    public Optional<RTIMasterDto> getByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Fetching RTIMaster by CNumber: {}", cNumber);
        return rtiMasterRepository.findByCompanyRefIdAndCNumber(companyRefId, cNumber)
                .map(mapper::toDto);
    }

    @Override
    public List<RTIMasterDto> getByEmployee(Integer companyRefId, Integer employeeRefId) {
        logger.info("Fetching RTIMaster for employee: {}", employeeRefId);
        return rtiMasterRepository.findByCompanyRefIdAndEmployeeRefId(companyRefId, employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RTIMasterDto> getByAgent(Integer companyRefId, Integer agentMasterRefId) {
        logger.info("Fetching RTIMaster for agent: {}", agentMasterRefId);
        return rtiMasterRepository.findByCompanyRefIdAndAgentMasterRefId(companyRefId, agentMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RTIMasterDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching RTIMaster between dates: {} to {}", startDate, endDate);
        return rtiMasterRepository.findByCompanyRefIdAndSaleDateBetween(companyRefId, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RTIMasterDto> getByCNumberDisplay(String cNumberDisplay) {
        logger.info("Fetching RTIMaster by CNumberDisplay: {}", cNumberDisplay);
        return rtiMasterRepository.findByCNumberDisplay(cNumberDisplay)
                .map(mapper::toDto);
    }

    @Override
    public List<RTIMasterDto> getSleepingRecords(Integer companyRefId) {
        logger.info("Fetching sleeping RTIMaster records for company: {}", companyRefId);
        return rtiMasterRepository.findByCompanyRefIdAndSleeping(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RTIMasterDto> getByTruck(Integer companyRefId, Integer truckRefId) {
        logger.info("Fetching RTIMaster for truck: {}", truckRefId);
        return rtiMasterRepository.findByCompanyRefIdAndTruckRefId(companyRefId, truckRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Checking if CNumber exists: {}", cNumber);
        return rtiMasterRepository.existsByCompanyRefIdAndCNumber(companyRefId, cNumber);
    }

    @Override
    public long countByCompanyId(Integer companyRefId) {
        logger.info("Counting RTIMaster records for company: {}", companyRefId);
        return rtiMasterRepository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompanyId(Integer companyRefId) {
        logger.info("Counting active RTIMaster records for company: {}", companyRefId);
        return rtiMasterRepository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    @Transactional
    public RTIMasterDto activate(Integer id) {
        logger.info("Activating RTIMaster with ID: {}", id);
        RTIMaster entity = rtiMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RTIMaster not found with ID: " + id));
        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        RTIMaster updated = rtiMasterRepository.save(entity);
        logger.info("RTIMaster activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public RTIMasterDto deactivate(Integer id) {
        logger.info("Deactivating RTIMaster with ID: {}", id);
        RTIMaster entity = rtiMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RTIMaster not found with ID: " + id));
        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        RTIMaster updated = rtiMasterRepository.save(entity);
        logger.info("RTIMaster deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    public String generateCNumberDisplay(Integer cNumber) {
        logger.info("Generating CNumberDisplay for CNumber: {}", cNumber);
        // Format: RTI + 9 digit zero-padded number (e.g., RTI000000001)
        return String.format("RTI%09d", cNumber);
    }
}

