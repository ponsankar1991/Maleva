package my.maleva.api.service.impl;

import my.maleva.api.dto.SaleDetailsDto;
import my.maleva.api.mapper.SaleDetailsMapper;
import my.maleva.api.model.SaleDetails;
import my.maleva.api.repo.SaleDetailsRepository;
import my.maleva.api.service.SaleDetailsService;
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
 * SaleDetailsServiceImpl
 * Service implementation for SaleDetails
 */
@Service
public class SaleDetailsServiceImpl implements SaleDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(SaleDetailsServiceImpl.class);

    @Autowired
    private SaleDetailsRepository saleDetailsRepository;

    @Autowired
    private SaleDetailsMapper mapper;

    @Override
    public List<SaleDetailsDto> getBySaleMasterRefId(Integer saleMasterRefId) {
        logger.info("Fetching SaleDetails by Sale Master Reference ID: {}", saleMasterRefId);
        return saleDetailsRepository.findBySaleMasterRefId(saleMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleDetailsDto> getById(Integer id) {
        logger.info("Fetching SaleDetails by ID: {}", id);
        return saleDetailsRepository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SaleDetailsDto create(SaleDetailsDto dto) {
        logger.info("Creating new SaleDetails for Sale Master: {}", dto.getSaleMasterRefId());

        validateLineItemData(dto);
        calculateLineAmount(dto);

        SaleDetails entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());

        // Set default values
        if (entity.getCurrencyValue() == null) entity.setCurrencyValue(0.0);
        if (entity.getActualAmount() == null) entity.setActualAmount(0.0);

        SaleDetails saved = saleDetailsRepository.save(entity);
        logger.info("SaleDetails created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SaleDetailsDto update(Integer id, SaleDetailsDto dto) {
        logger.info("Updating SaleDetails with ID: {}", id);
        SaleDetails entity = saleDetailsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleDetails not found with ID: " + id));

        validateLineItemData(dto);
        calculateLineAmount(dto);

        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        SaleDetails updated = saleDetailsRepository.save(entity);
        logger.info("SaleDetails updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SaleDetails with ID: {}", id);
        if (saleDetailsRepository.existsById(id)) {
            saleDetailsRepository.deleteById(id);
            logger.info("SaleDetails deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public List<SaleDetailsDto> getByItemMasterRefId(Integer itemMasterRefId) {
        logger.info("Fetching SaleDetails by Item Master Reference ID: {}", itemMasterRefId);
        return saleDetailsRepository.findByItemMasterRefId(itemMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countBySaleMasterRefId(Integer saleMasterRefId) {
        logger.info("Counting SaleDetails for Sale Master: {}", saleMasterRefId);
        return saleDetailsRepository.countBySaleMasterRefId(saleMasterRefId);
    }

    @Override
    @Transactional
    public void deleteAllBySaleMasterRefId(Integer saleMasterRefId) {
        logger.info("Deleting all SaleDetails for Sale Master: {}", saleMasterRefId);
        List<SaleDetails> details = saleDetailsRepository.findBySaleMasterRefId(saleMasterRefId);
        saleDetailsRepository.deleteAll(details);
        logger.info("Deleted {} SaleDetails records", details.size());
    }

    @Override
    public SaleDetailsDto calculateLineAmount(SaleDetailsDto dto) {
        logger.info("Calculating line amount for item: {}", dto.getItemMasterRefId());
        // Amount = (ItemQty * SalesRate) - DiscAmount + TaxAmount
        Double quantity = dto.getItemQty() != null ? dto.getItemQty() : 0.0;
        Double salesRate = dto.getSalesRate() != null ? dto.getSalesRate() : 0.0;
        Double discAmount = dto.getDiscAmount() != null ? dto.getDiscAmount() : 0.0;
        Double taxAmount = dto.getTaxAmount() != null ? dto.getTaxAmount() : 0.0;

        Double amount = (quantity * salesRate) - discAmount + taxAmount;
        dto.setAmount(amount);
        return dto;
    }

    @Override
    public void validateLineItemData(SaleDetailsDto dto) {
        logger.info("Validating line item data for item: {}", dto.getItemMasterRefId());
        if (dto.getSaleMasterRefId() == null) throw new RuntimeException("Sale Master Reference ID is required");
        if (dto.getItemMasterRefId() == null) throw new RuntimeException("Item Master Reference ID is required");
        if (dto.getItemQty() == null || dto.getItemQty() < 0) throw new RuntimeException("Item Quantity must be valid");
        if (dto.getSalesRate() == null || dto.getSalesRate() < 0) throw new RuntimeException("Sales Rate must be valid");
    }
}

