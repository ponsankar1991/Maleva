package my.maleva.api.service.impl;

import my.maleva.api.dto.RTIDetailsDto;
import my.maleva.api.mapper.RTIDetailsMapper;
import my.maleva.api.model.RTIDetails;
import my.maleva.api.repo.RTIDetailsRepository;
import my.maleva.api.service.RTIDetailsService;
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
 * RTIDetailsServiceImpl
 * Service implementation for RTIDetails
 */
@Service
public class RTIDetailsServiceImpl implements RTIDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(RTIDetailsServiceImpl.class);

    @Autowired
    private RTIDetailsRepository rtiDetailsRepository;

    @Autowired
    private RTIDetailsMapper mapper;

    @Override
    public List<RTIDetailsDto> getByRtiMasterId(Integer rtiMasterRefId) {
        logger.info("Fetching RTIDetails for RTIMaster: {}", rtiMasterRefId);
        return rtiDetailsRepository.findByRtiMasterRefId(rtiMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RTIDetailsDto> getById(Integer id) {
        logger.info("Fetching RTIDetails by ID: {}", id);
        return rtiDetailsRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public RTIDetailsDto create(RTIDetailsDto dto) {
        logger.info("Creating new RTIDetails for RTIMaster: {}", dto.getRtiMasterRefId());
        RTIDetails entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());
        if (entity.getSalary() == null) {
            entity.setSalary(0.0);
        }
        if (entity.getPwdType() == null) {
            entity.setPwdType(0);
        }
        RTIDetails saved = rtiDetailsRepository.save(entity);
        logger.info("RTIDetails created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public RTIDetailsDto update(Integer id, RTIDetailsDto dto) {
        logger.info("Updating RTIDetails with ID: {}", id);
        RTIDetails entity = rtiDetailsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RTIDetails not found with ID: " + id));
        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        RTIDetails updated = rtiDetailsRepository.save(entity);
        logger.info("RTIDetails updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting RTIDetails with ID: {}", id);
        if (rtiDetailsRepository.existsById(id)) {
            rtiDetailsRepository.deleteById(id);
            logger.info("RTIDetails deleted with ID: {}", id);
            return true;
        }
        logger.warn("RTIDetails not found with ID: {}", id);
        return false;
    }

    @Override
    public List<RTIDetailsDto> getBySaleOrderMasterId(Integer saleOrderMasterRefId) {
        logger.info("Fetching RTIDetails by sale order master: {}", saleOrderMasterRefId);
        return rtiDetailsRepository.findBySaleOrderMasterRefId(saleOrderMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countByRtiMasterId(Integer rtiMasterRefId) {
        logger.info("Counting RTIDetails for RTIMaster: {}", rtiMasterRefId);
        return rtiDetailsRepository.countByRtiMasterRefId(rtiMasterRefId);
    }

    @Override
    @Transactional
    public void deleteByRtiMasterId(Integer rtiMasterRefId) {
        logger.info("Deleting all RTIDetails for RTIMaster: {}", rtiMasterRefId);
        rtiDetailsRepository.deleteByRtiMasterRefId(rtiMasterRefId);
    }
}

