package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.module.fleet.dto.SpeedReportDto;
import my.maleva.api.module.fleet.mapper.SpeedReportMapper;
import my.maleva.api.module.fleet.entity.SpeedReport;
import my.maleva.api.module.fleet.repository.SpeedReportRepository;
import my.maleva.api.module.fleet.service.SpeedReportService;
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
 * SpeedReportServiceImpl - Implementation for SpeedReport service
 * Handles speed violation report tracking and management
 */
@Service
public class SpeedReportServiceImpl implements SpeedReportService {

    private static final Logger logger = LoggerFactory.getLogger(SpeedReportServiceImpl.class);

    @Autowired
    private SpeedReportRepository repository;

    @Autowired
    private SpeedReportMapper mapper;

    @Override
    public List<SpeedReportDto> getByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching SpeedReport for company: {}", companyRefId);
        return repository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpeedReportDto> getByTruckRefId(Integer truckRefId) {
        logger.info("Fetching SpeedReport for truck: {}", truckRefId);
        return repository.findByTruckRefId(truckRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpeedReportDto> getByCompanyAndTruck(Integer companyRefId, Integer truckRefId) {
        logger.info("Fetching SpeedReport for company: {} and truck: {}", companyRefId, truckRefId);
        return repository.findByCompanyRefIdAndTruckRefId(companyRefId, truckRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpeedReportDto> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Fetching SpeedReport for time range: {} to {}", startTime, endTime);
        return repository.findByTimeGreaterThanEqualAndTimeLessThanEqual(startTime, endTime)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpeedReportDto> getByCompanyAndTimeRange(Integer companyRefId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Fetching SpeedReport for company: {} and time range: {} to {}", companyRefId, startTime, endTime);
        return repository.findByCompanyRefIdAndTimeGreaterThanEqualAndTimeLessThanEqual(companyRefId, startTime, endTime)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SpeedReportDto> getById(Integer id) {
        logger.info("Fetching SpeedReport by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SpeedReportDto create(SpeedReportDto dto) {
        logger.info("Creating new SpeedReport");
        validateSpeedReportData(dto);
        SpeedReport entity = mapper.toEntity(dto);
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(LocalDateTime.now());
        }
        SpeedReport saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SpeedReportDto update(Integer id, SpeedReportDto dto) {
        logger.info("Updating SpeedReport with ID: {}", id);
        validateSpeedReportData(dto);
        SpeedReport entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SpeedReport not found: " + id));
        mapper.updateEntityFromDto(dto, entity);
        SpeedReport updated = repository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SpeedReport with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public long countByCompanyRefId(Integer companyRefId) {
        logger.info("Counting SpeedReport for company: {}", companyRefId);
        return repository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countByTruckRefId(Integer truckRefId) {
        logger.info("Counting SpeedReport for truck: {}", truckRefId);
        return repository.countByTruckRefId(truckRefId);
    }

    @Override
    public void validateSpeedReportData(SpeedReportDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new RuntimeException("Company Reference ID is required");
        }
        if (dto.getTruckRefId() == null) {
            throw new RuntimeException("Truck Reference ID is required");
        }
    }
}

