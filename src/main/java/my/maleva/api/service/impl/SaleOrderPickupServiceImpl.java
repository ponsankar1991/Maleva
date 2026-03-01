package my.maleva.api.service.impl;

import my.maleva.api.dto.SaleOrderPickupDto;
import my.maleva.api.mapper.SaleOrderPickupMapper;
import my.maleva.api.model.SaleOrderPickup;
import my.maleva.api.repo.SaleOrderPickupRepository;
import my.maleva.api.service.SaleOrderPickupService;
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
 * SaleOrderPickupServiceImpl - Implementation for SaleOrderPickup service
 */
@Service
public class SaleOrderPickupServiceImpl implements SaleOrderPickupService {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderPickupServiceImpl.class);

    @Autowired
    private SaleOrderPickupRepository repository;

    @Autowired
    private SaleOrderPickupMapper mapper;

    @Override
    public List<SaleOrderPickupDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleOrderPickup for master: {}", saleOrderMasterRefId);
        return repository.findBySaleOrderMasterRefId(saleOrderMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleOrderPickupDto> getById(Integer id) {
        logger.info("Fetching SaleOrderPickup by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SaleOrderPickupDto create(SaleOrderPickupDto dto) {
        logger.info("Creating new SaleOrderPickup");
        validatePickupData(dto);
        SaleOrderPickup entity = mapper.toEntity(dto);
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(LocalDateTime.now());
        }
        SaleOrderPickup saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SaleOrderPickupDto update(Integer id, SaleOrderPickupDto dto) {
        logger.info("Updating SaleOrderPickup with ID: {}", id);
        validatePickupData(dto);
        SaleOrderPickup entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleOrderPickup not found: " + id));
        mapper.updateEntityFromDto(dto, entity);
        SaleOrderPickup updated = repository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SaleOrderPickup with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Counting SaleOrderPickup for master: {}", saleOrderMasterRefId);
        return repository.countBySaleOrderMasterRefId(saleOrderMasterRefId);
    }

    @Override
    @Transactional
    public void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Deleting all SaleOrderPickup for master: {}", saleOrderMasterRefId);
        repository.deleteBySaleOrderMasterRefId(saleOrderMasterRefId);
    }

    @Override
    public void validatePickupData(SaleOrderPickupDto dto) {
        if (dto.getSaleOrderMasterRefId() == null) {
            throw new RuntimeException("Sale Order Master Reference ID is required");
        }
    }
}

