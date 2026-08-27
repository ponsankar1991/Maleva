package my.maleva.api.module.rti.service.impl;

import my.maleva.api.module.rti.dto.RTIDetailsDto;
import my.maleva.api.module.rti.mapper.RTIDetailsMapper;
import my.maleva.api.module.rti.entity.RTIDetails;
import my.maleva.api.module.rti.repository.RTIDetailsRepository;
import my.maleva.api.module.rti.service.RTIDetailsService;
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

    @Override
    public List<java.util.Map<String, Object>> getRtiStatusBySaleOrderIds(List<Integer> saleOrderIds) {
        if (saleOrderIds == null || saleOrderIds.isEmpty()) {
            return List.of();
        }

        List<Integer> validIds = saleOrderIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (validIds.isEmpty()) {
            return List.of();
        }

        // Rows come newest RTI first; keep only the latest RTI per sale order.
        java.util.Map<Integer, java.util.Map<String, Object>> latestBySaleOrder = new java.util.LinkedHashMap<>();
        for (Object[] row : rtiDetailsRepository.findRtiStatusBySaleOrderIds(validIds)) {
            Integer saleOrderId = (Integer) row[0];
            latestBySaleOrder.computeIfAbsent(saleOrderId, key -> {
                java.util.Map<String, Object> status = new java.util.LinkedHashMap<>();
                status.put("saleOrderMasterRefId", key);
                status.put("rtiMasterRefId", row[1]);
                status.put("rtiNo", row[2]);
                return status;
            });
        }

        return new java.util.ArrayList<>(latestBySaleOrder.values());
    }
}

