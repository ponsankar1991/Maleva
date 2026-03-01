package my.maleva.api.service.impl;

import my.maleva.api.dto.SaleOrderDeliveryDto;
import my.maleva.api.mapper.SaleOrderDeliveryMapper;
import my.maleva.api.model.SaleOrderDelivery;
import my.maleva.api.repo.SaleOrderDeliveryRepository;
import my.maleva.api.service.SaleOrderDeliveryService;
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
 * SaleOrderDeliveryServiceImpl - Implementation for SaleOrderDelivery service
 */
@Service
public class SaleOrderDeliveryServiceImpl implements SaleOrderDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderDeliveryServiceImpl.class);

    @Autowired
    private SaleOrderDeliveryRepository repository;

    @Autowired
    private SaleOrderDeliveryMapper mapper;

    @Override
    public List<SaleOrderDeliveryDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleOrderDelivery for master: {}", saleOrderMasterRefId);
        return repository.findBySaleOrderMasterRefId(saleOrderMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleOrderDeliveryDto> getById(Integer id) {
        logger.info("Fetching SaleOrderDelivery by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SaleOrderDeliveryDto create(SaleOrderDeliveryDto dto) {
        logger.info("Creating new SaleOrderDelivery");
        validateDeliveryData(dto);
        SaleOrderDelivery entity = mapper.toEntity(dto);
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(LocalDateTime.now());
        }
        SaleOrderDelivery saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SaleOrderDeliveryDto update(Integer id, SaleOrderDeliveryDto dto) {
        logger.info("Updating SaleOrderDelivery with ID: {}", id);
        validateDeliveryData(dto);
        SaleOrderDelivery entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleOrderDelivery not found: " + id));
        mapper.updateEntityFromDto(dto, entity);
        SaleOrderDelivery updated = repository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SaleOrderDelivery with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Counting SaleOrderDelivery for master: {}", saleOrderMasterRefId);
        return repository.countBySaleOrderMasterRefId(saleOrderMasterRefId);
    }

    @Override
    @Transactional
    public void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Deleting all SaleOrderDelivery for master: {}", saleOrderMasterRefId);
        repository.deleteBySaleOrderMasterRefId(saleOrderMasterRefId);
    }

    @Override
    public void validateDeliveryData(SaleOrderDeliveryDto dto) {
        if (dto.getSaleOrderMasterRefId() == null) {
            throw new RuntimeException("Sale Order Master Reference ID is required");
        }
        if (dto.getDeliveryAddress() == null || dto.getDeliveryAddress().trim().isEmpty()) {
            throw new RuntimeException("Delivery Address is required");
        }
    }
}

