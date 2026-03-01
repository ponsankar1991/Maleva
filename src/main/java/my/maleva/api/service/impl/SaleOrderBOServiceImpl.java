package my.maleva.api.service.impl;

import my.maleva.api.dto.SaleOrderBODto;
import my.maleva.api.mapper.SaleOrderBOMapper;
import my.maleva.api.model.SaleOrderBO;
import my.maleva.api.repo.SaleOrderBORepository;
import my.maleva.api.service.SaleOrderBOService;
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
 * SaleOrderBOServiceImpl - Implementation for SaleOrderBO service
 */
@Service
public class SaleOrderBOServiceImpl implements SaleOrderBOService {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderBOServiceImpl.class);

    @Autowired
    private SaleOrderBORepository repository;

    @Autowired
    private SaleOrderBOMapper mapper;

    @Override
    public List<SaleOrderBODto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleOrderBO for master: {}", saleOrderMasterRefId);
        return repository.findBySaleOrderMasterRefId(saleOrderMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleOrderBODto> getByBoTypeId(Integer boTypeId) {
        logger.info("Fetching SaleOrderBO for BOTypeId: {}", boTypeId);
        return repository.findByBoTypeId(boTypeId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleOrderBODto> getByStatus(Integer status) {
        logger.info("Fetching SaleOrderBO for status: {}", status);
        return repository.findByStatus(status)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleOrderBODto> getById(Integer id) {
        logger.info("Fetching SaleOrderBO by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SaleOrderBODto create(SaleOrderBODto dto) {
        logger.info("Creating new SaleOrderBO");
        SaleOrderBO entity = mapper.toEntity(dto);
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(LocalDateTime.now());
        }
        SaleOrderBO saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SaleOrderBODto update(Integer id, SaleOrderBODto dto) {
        logger.info("Updating SaleOrderBO with ID: {}", id);
        SaleOrderBO entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleOrderBO not found: " + id));
        mapper.updateEntityFromDto(dto, entity);
        SaleOrderBO updated = repository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SaleOrderBO with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Counting SaleOrderBO for master: {}", saleOrderMasterRefId);
        return repository.countBySaleOrderMasterRefId(saleOrderMasterRefId);
    }

    @Override
    @Transactional
    public void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Deleting all SaleOrderBO for master: {}", saleOrderMasterRefId);
        repository.deleteBySaleOrderMasterRefId(saleOrderMasterRefId);
    }
}

