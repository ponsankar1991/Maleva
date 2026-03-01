package my.maleva.api.service.impl;

import my.maleva.api.dto.SaleCreditDetailsDto;
import my.maleva.api.mapper.SaleCreditDetailsMapper;
import my.maleva.api.model.SaleCreditDetails;
import my.maleva.api.repo.SaleCreditDetailsRepository;
import my.maleva.api.service.SaleCreditDetailsService;
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
 * SaleCreditDetailsServiceImpl
 * Service implementation for SaleCreditDetails
 * Implements business logic for sale credit details management
 */
@Service
public class SaleCreditDetailsServiceImpl implements SaleCreditDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(SaleCreditDetailsServiceImpl.class);

    @Autowired
    private SaleCreditDetailsRepository saleCreditDetailsRepository;

    @Autowired
    private SaleCreditDetailsMapper mapper;

    @Override
    public List<SaleCreditDetailsDto> getBySaleCreditMasterRefId(Integer saleCreditMasterRefId) {
        logger.info("Fetching SaleCreditDetails by Sale Credit Master Reference ID: {}", saleCreditMasterRefId);
        return saleCreditDetailsRepository.findBySaleCreditMasterRefId(saleCreditMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleCreditDetailsDto> getById(Integer id) {
        logger.info("Fetching SaleCreditDetails by ID: {}", id);
        return saleCreditDetailsRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public SaleCreditDetailsDto create(SaleCreditDetailsDto dto) {
        logger.info("Creating new SaleCreditDetails for Sale Credit Master: {}", dto.getSaleCreditMasterRefId());

        // Set default values if null
        if (dto.getCurrencyValue() == null) {
            dto.setCurrencyValue(0.0);
        }
        if (dto.getActualAmount() == null) {
            dto.setActualAmount(0.0);
        }

        SaleCreditDetails entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());

        SaleCreditDetails saved = saleCreditDetailsRepository.save(entity);
        logger.info("SaleCreditDetails created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SaleCreditDetailsDto update(Integer id, SaleCreditDetailsDto dto) {
        logger.info("Updating SaleCreditDetails with ID: {}", id);
        SaleCreditDetails entity = saleCreditDetailsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleCreditDetails not found with ID: " + id));

        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        SaleCreditDetails updated = saleCreditDetailsRepository.save(entity);
        logger.info("SaleCreditDetails updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SaleCreditDetails with ID: {}", id);
        if (saleCreditDetailsRepository.existsById(id)) {
            saleCreditDetailsRepository.deleteById(id);
            logger.info("SaleCreditDetails deleted with ID: {}", id);
            return true;
        }
        logger.warn("SaleCreditDetails not found with ID: {}", id);
        return false;
    }

    @Override
    public List<SaleCreditDetailsDto> getByItemMasterRefId(Integer itemMasterRefId) {
        logger.info("Fetching SaleCreditDetails by Item Master Reference ID: {}", itemMasterRefId);
        return saleCreditDetailsRepository.findByItemMasterRefId(itemMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countBySaleCreditMasterRefId(Integer saleCreditMasterRefId) {
        logger.info("Counting SaleCreditDetails for Sale Credit Master: {}", saleCreditMasterRefId);
        return saleCreditDetailsRepository.countBySaleCreditMasterRefId(saleCreditMasterRefId);
    }

    @Override
    @Transactional
    public void deleteAllBySaleCreditMasterRefId(Integer saleCreditMasterRefId) {
        logger.info("Deleting all SaleCreditDetails for Sale Credit Master: {}", saleCreditMasterRefId);
        List<SaleCreditDetails> details = saleCreditDetailsRepository.findBySaleCreditMasterRefId(saleCreditMasterRefId);
        saleCreditDetailsRepository.deleteAll(details);
        logger.info("Deleted {} SaleCreditDetails records", details.size());
    }
}

