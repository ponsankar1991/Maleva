package my.maleva.api.module.saleorder.service.impl;

import my.maleva.api.module.saleorder.dto.SaleOrderForwardingDto;
import my.maleva.api.module.saleorder.mapper.SaleOrderForwardingMapper;
import my.maleva.api.module.saleorder.entity.SaleOrderForwarding;
import my.maleva.api.module.saleorder.repository.SaleOrderForwardingRepository;
import my.maleva.api.module.saleorder.service.SaleOrderForwardingService;
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
 * SaleOrderForwardingServiceImpl - Implementation for SaleOrderForwarding service
 * Incorporates SP_SaleOrderMaster business logic for forwarding operations
 */
@Service
public class SaleOrderForwardingServiceImpl implements SaleOrderForwardingService {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderForwardingServiceImpl.class);

    @Autowired
    private SaleOrderForwardingRepository repository;

    @Autowired
    private SaleOrderForwardingMapper mapper;

    @Override
    public List<SaleOrderForwardingDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleOrderForwarding for master: {}", saleOrderMasterRefId);
        return repository.findBySaleOrderMasterRefId(saleOrderMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleOrderForwardingDto> getById(Integer id) {
        logger.info("Fetching SaleOrderForwarding by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SaleOrderForwardingDto create(SaleOrderForwardingDto dto) {
        logger.info("Creating new SaleOrderForwarding");
        validateForwardingData(dto);
        SaleOrderForwarding entity = mapper.toEntity(dto);
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(LocalDateTime.now());
        }
        SaleOrderForwarding saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SaleOrderForwardingDto update(Integer id, SaleOrderForwardingDto dto) {
        logger.info("Updating SaleOrderForwarding with ID: {}", id);
        validateForwardingData(dto);
        SaleOrderForwarding entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleOrderForwarding not found: " + id));
        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        SaleOrderForwarding updated = repository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SaleOrderForwarding with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Counting SaleOrderForwarding for master: {}", saleOrderMasterRefId);
        return repository.countBySaleOrderMasterRefId(saleOrderMasterRefId);
    }

    @Override
    @Transactional
    public void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Deleting all SaleOrderForwarding for master: {}", saleOrderMasterRefId);
        repository.deleteAllBySaleOrderMasterRefId(saleOrderMasterRefId);
    }

    @Override
    public void validateForwardingData(SaleOrderForwardingDto dto) {
        if (dto.getSaleOrderMasterRefId() == null) {
            throw new RuntimeException("Sale Order Master Reference ID is required");
        }
        if (dto.getRowNumber() == null) {
            throw new RuntimeException("Row Number is required");
        }
    }
}

