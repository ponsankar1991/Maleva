package my.maleva.api.module.invoice.service.impl;

import my.maleva.api.module.invoice.dto.SaleMasterReferenceDto;
import my.maleva.api.module.invoice.mapper.SaleMasterReferenceMapper;
import my.maleva.api.module.invoice.entity.SaleMasterReference;
import my.maleva.api.module.invoice.repository.SaleMasterReferenceRepository;
import my.maleva.api.module.invoice.service.SaleMasterReferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SaleMasterReferenceServiceImpl - Implementation for SaleMasterReference service
 */
@Service
public class SaleMasterReferenceServiceImpl implements SaleMasterReferenceService {

    private static final Logger logger = LoggerFactory.getLogger(SaleMasterReferenceServiceImpl.class);

    @Autowired
    private SaleMasterReferenceRepository repository;

    @Autowired
    private SaleMasterReferenceMapper mapper;

    @Override
    public List<SaleMasterReferenceDto> getBySaleMasterRefId(Integer saleMasterRefId) {
        logger.info("Fetching SaleMasterReference for SaleMaster: {}", saleMasterRefId);
        return repository.findBySaleMasterRefId(saleMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleMasterReferenceDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleMasterReference for SaleOrderMaster: {}", saleOrderMasterRefId);
        return repository.findBySaleOrderMasterRefId(saleOrderMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleMasterReferenceDto> getById(Integer id) {
        logger.info("Fetching SaleMasterReference by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SaleMasterReferenceDto create(SaleMasterReferenceDto dto) {
        logger.info("Creating new SaleMasterReference");
        SaleMasterReference entity = mapper.toEntity(dto);
        SaleMasterReference saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SaleMasterReferenceDto update(Integer id, SaleMasterReferenceDto dto) {
        logger.info("Updating SaleMasterReference with ID: {}", id);
        SaleMasterReference entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleMasterReference not found: " + id));
        mapper.updateEntityFromDto(dto, entity);
        SaleMasterReference updated = repository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SaleMasterReference with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public long countBySaleMasterRefId(Integer saleMasterRefId) {
        logger.info("Counting SaleMasterReference for SaleMaster: {}", saleMasterRefId);
        return repository.countBySaleMasterRefId(saleMasterRefId);
    }

    @Override
    @Transactional
    public void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Deleting all SaleMasterReference for SaleOrderMaster: {}", saleOrderMasterRefId);
        repository.deleteBySaleOrderMasterRefId(saleOrderMasterRefId);
    }
}

