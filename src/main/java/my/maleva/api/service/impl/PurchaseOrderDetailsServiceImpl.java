package my.maleva.api.service.impl;

import my.maleva.api.dto.PurchaseOrderDetailsDto;
import my.maleva.api.mapper.PurchaseOrderDetailsMapper;
import my.maleva.api.model.PurchaseOrderDetails;
import my.maleva.api.repo.PurchaseOrderDetailsRepository;
import my.maleva.api.service.PurchaseOrderDetailsService;
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
 * PurchaseOrderDetailsServiceImpl
 * Service implementation for PurchaseOrderDetails
 */
@Service
public class PurchaseOrderDetailsServiceImpl implements PurchaseOrderDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderDetailsServiceImpl.class);

    @Autowired
    private PurchaseOrderDetailsRepository purchaseOrderDetailsRepository;

    @Autowired
    private PurchaseOrderDetailsMapper mapper;

    @Override
    public List<PurchaseOrderDetailsDto> getByPurchaseOrderMasterId(Integer purchaseOrderMasterRefId) {
        logger.info("Fetching PurchaseOrderDetails for PurchaseOrderMaster: {}", purchaseOrderMasterRefId);
        return purchaseOrderDetailsRepository.findByPurchaseOrderMasterRefId(purchaseOrderMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PurchaseOrderDetailsDto> getById(Integer id) {
        logger.info("Fetching PurchaseOrderDetails by ID: {}", id);
        return purchaseOrderDetailsRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailsDto create(PurchaseOrderDetailsDto dto) {
        logger.info("Creating new PurchaseOrderDetails for PurchaseOrderMaster: {}", dto.getPurchaseOrderMasterRefId());
        PurchaseOrderDetails entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());
        PurchaseOrderDetails saved = purchaseOrderDetailsRepository.save(entity);
        logger.info("PurchaseOrderDetails created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailsDto update(Integer id, PurchaseOrderDetailsDto dto) {
        logger.info("Updating PurchaseOrderDetails with ID: {}", id);
        PurchaseOrderDetails entity = purchaseOrderDetailsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseOrderDetails not found with ID: " + id));
        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        PurchaseOrderDetails updated = purchaseOrderDetailsRepository.save(entity);
        logger.info("PurchaseOrderDetails updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting PurchaseOrderDetails with ID: {}", id);
        if (purchaseOrderDetailsRepository.existsById(id)) {
            purchaseOrderDetailsRepository.deleteById(id);
            logger.info("PurchaseOrderDetails deleted with ID: {}", id);
            return true;
        }
        logger.warn("PurchaseOrderDetails not found with ID: {}", id);
        return false;
    }

    @Override
    public List<PurchaseOrderDetailsDto> getByProductMasterId(Integer productMasterRefId) {
        logger.info("Fetching PurchaseOrderDetails by product: {}", productMasterRefId);
        return purchaseOrderDetailsRepository.findByProductMasterRefId(productMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countByPurchaseOrderMasterId(Integer purchaseOrderMasterRefId) {
        logger.info("Counting PurchaseOrderDetails for PurchaseOrderMaster: {}", purchaseOrderMasterRefId);
        return purchaseOrderDetailsRepository.countByPurchaseOrderMasterRefId(purchaseOrderMasterRefId);
    }

    @Override
    @Transactional
    public void deleteByPurchaseOrderMasterId(Integer purchaseOrderMasterRefId) {
        logger.info("Deleting all PurchaseOrderDetails for PurchaseOrderMaster: {}", purchaseOrderMasterRefId);
        purchaseOrderDetailsRepository.deleteByPurchaseOrderMasterRefId(purchaseOrderMasterRefId);
    }
}

