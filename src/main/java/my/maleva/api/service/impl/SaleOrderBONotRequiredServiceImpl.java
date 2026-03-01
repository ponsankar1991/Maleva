package my.maleva.api.service.impl;

import my.maleva.api.dto.SaleOrderBONotRequiredDto;
import my.maleva.api.mapper.SaleOrderBONotRequiredMapper;
import my.maleva.api.model.SaleOrderBONotRequired;
import my.maleva.api.repo.SaleOrderBONotRequiredRepository;
import my.maleva.api.service.SaleOrderBONotRequiredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SaleOrderBONotRequiredServiceImpl - Implementation for SaleOrderBONotRequired service
 */
@Service
public class SaleOrderBONotRequiredServiceImpl implements SaleOrderBONotRequiredService {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderBONotRequiredServiceImpl.class);

    @Autowired
    private SaleOrderBONotRequiredRepository repository;

    @Autowired
    private SaleOrderBONotRequiredMapper mapper;

    @Override
    public List<SaleOrderBONotRequiredDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleOrderBONotRequired for master: {}", saleOrderMasterRefId);
        return repository.findBySaleOrderMasterRefId(saleOrderMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleOrderBONotRequiredDto> getByBoTypeId(Integer boTypeId) {
        logger.info("Fetching SaleOrderBONotRequired for BOTypeId: {}", boTypeId);
        return repository.findByBoTypeId(boTypeId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleOrderBONotRequiredDto> getById(Integer id) {
        logger.info("Fetching SaleOrderBONotRequired by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SaleOrderBONotRequiredDto create(SaleOrderBONotRequiredDto dto) {
        logger.info("Creating new SaleOrderBONotRequired");
        SaleOrderBONotRequired entity = mapper.toEntity(dto);
        SaleOrderBONotRequired saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SaleOrderBONotRequiredDto update(Integer id, SaleOrderBONotRequiredDto dto) {
        logger.info("Updating SaleOrderBONotRequired with ID: {}", id);
        SaleOrderBONotRequired entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleOrderBONotRequired not found: " + id));
        mapper.updateEntityFromDto(dto, entity);
        SaleOrderBONotRequired updated = repository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SaleOrderBONotRequired with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Counting SaleOrderBONotRequired for master: {}", saleOrderMasterRefId);
        return repository.countBySaleOrderMasterRefId(saleOrderMasterRefId);
    }

    @Override
    @Transactional
    public void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Deleting all SaleOrderBONotRequired for master: {}", saleOrderMasterRefId);
        repository.deleteBySaleOrderMasterRefId(saleOrderMasterRefId);
    }
}

