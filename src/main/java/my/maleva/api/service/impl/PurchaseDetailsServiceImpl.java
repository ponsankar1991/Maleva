package my.maleva.api.service.impl;

import my.maleva.api.dto.PurchaseDetailsDto;
import my.maleva.api.mapper.PurchaseDetailsMapper;
import my.maleva.api.model.PurchaseDetails;
import my.maleva.api.repo.PurchaseDetailsRepository;
import my.maleva.api.service.PurchaseDetailsService;
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
 * Service implementation for PurchaseDetails
 * Implements line item operations for purchase orders
 */
@Service
public class PurchaseDetailsServiceImpl implements PurchaseDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseDetailsServiceImpl.class);

    @Autowired
    private PurchaseDetailsRepository purchaseDetailsRepository;

    @Autowired
    private PurchaseDetailsMapper mapper;

    @Override
    public List<PurchaseDetailsDto> getByPurchaseMaster(Integer purchaseMasterRefId) {
        logger.info("Fetching all PurchaseDetails for purchase master: {}", purchaseMasterRefId);
        return purchaseDetailsRepository.findByPurchaseMasterRefId(purchaseMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PurchaseDetailsDto> getById(Integer id) {
        logger.info("Fetching PurchaseDetails by ID: {}", id);
        return purchaseDetailsRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public PurchaseDetailsDto create(PurchaseDetailsDto dto) {
        logger.info("Creating new PurchaseDetails for purchase master: {}", dto.getPurchaseMasterRefId());

        PurchaseDetails entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());

        PurchaseDetails saved = purchaseDetailsRepository.save(entity);
        logger.info("PurchaseDetails created successfully with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public List<PurchaseDetailsDto> createBatch(List<PurchaseDetailsDto> dtos) {
        logger.info("Creating batch of {} PurchaseDetails records", dtos.size());

        List<PurchaseDetails> entities = dtos.stream()
                .map(mapper::toEntity)
                .peek(entity -> {
                    entity.setCreatedDate(LocalDateTime.now());
                    entity.setModifiedDate(LocalDateTime.now());
                })
                .collect(Collectors.toList());

        List<PurchaseDetails> saved = purchaseDetailsRepository.saveAll(entities);
        logger.info("Batch of {} PurchaseDetails records created successfully", saved.size());

        return saved.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PurchaseDetailsDto update(Integer id, PurchaseDetailsDto dto) {
        logger.info("Updating PurchaseDetails with ID: {}", id);
        PurchaseDetails entity = purchaseDetailsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseDetails not found with ID: " + id));

        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());

        PurchaseDetails updated = purchaseDetailsRepository.save(entity);
        logger.info("PurchaseDetails updated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting PurchaseDetails with ID: {}", id);
        if (!purchaseDetailsRepository.existsById(id)) {
            logger.warn("PurchaseDetails not found with ID: {}", id);
            return false;
        }
        purchaseDetailsRepository.deleteById(id);
        logger.info("PurchaseDetails deleted successfully with ID: {}", id);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteByPurchaseMaster(Integer purchaseMasterRefId) {
        logger.info("Deleting all PurchaseDetails for purchase master: {}", purchaseMasterRefId);
        purchaseDetailsRepository.deleteByPurchaseMasterRefId(purchaseMasterRefId);
        logger.info("All PurchaseDetails deleted for purchase master: {}", purchaseMasterRefId);
        return true;
    }

    @Override
    public List<PurchaseDetailsDto> getByProduct(Integer productMasterRefId) {
        logger.info("Fetching PurchaseDetails for product: {}", productMasterRefId);
        return purchaseDetailsRepository.findByProductMasterRefId(productMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countByPurchaseMaster(Integer purchaseMasterRefId) {
        logger.info("Counting PurchaseDetails for purchase master: {}", purchaseMasterRefId);
        return purchaseDetailsRepository.countByPurchaseMasterRefId(purchaseMasterRefId);
    }

    @Override
    public Double calculateTotalAmount(Integer purchaseMasterRefId) {
        logger.info("Calculating total amount for purchase master: {}", purchaseMasterRefId);
        List<PurchaseDetails> details = purchaseDetailsRepository.findByPurchaseMasterRefId(purchaseMasterRefId);
        return details.stream()
                .map(PurchaseDetails::getAmount)
                .reduce(0.0, Double::sum);
    }

    @Override
    public Double calculateTotalTax(Integer purchaseMasterRefId) {
        logger.info("Calculating total tax for purchase master: {}", purchaseMasterRefId);
        List<PurchaseDetails> details = purchaseDetailsRepository.findByPurchaseMasterRefId(purchaseMasterRefId);
        return details.stream()
                .map(PurchaseDetails::getTaxAmount)
                .reduce(0.0, Double::sum);
    }

    @Override
    public Double calculateTotalDiscount(Integer purchaseMasterRefId) {
        logger.info("Calculating total discount for purchase master: {}", purchaseMasterRefId);
        List<PurchaseDetails> details = purchaseDetailsRepository.findByPurchaseMasterRefId(purchaseMasterRefId);
        return details.stream()
                .map(PurchaseDetails::getDiscAmount)
                .reduce(0.0, Double::sum);
    }
}

