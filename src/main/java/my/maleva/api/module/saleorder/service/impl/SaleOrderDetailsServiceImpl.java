package my.maleva.api.module.saleorder.service.impl;

import my.maleva.api.module.saleorder.dto.SaleOrderDetailsDto;
import my.maleva.api.module.saleorder.mapper.SaleOrderDetailsMapper;
import my.maleva.api.module.saleorder.entity.SaleOrderDetails;
import my.maleva.api.module.saleorder.repository.SaleOrderDetailsRepository;
import my.maleva.api.module.saleorder.service.SaleOrderDetailsService;
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
 * SaleOrderDetailsServiceImpl - Implementation for SaleOrderDetails service
 */
@Service
public class SaleOrderDetailsServiceImpl implements SaleOrderDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderDetailsServiceImpl.class);

    @Autowired
    private SaleOrderDetailsRepository repository;

    @Autowired
    private SaleOrderDetailsMapper mapper;

    @Override
    public List<SaleOrderDetailsDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleOrderDetails for master: {}", saleOrderMasterRefId);
        return repository.findBySaleOrderMasterRefId(saleOrderMasterRefId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<SaleOrderDetailsDto> getById(Integer id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SaleOrderDetailsDto create(SaleOrderDetailsDto dto) {
        logger.info("Creating SaleOrderDetails");
        validateLineItemData(dto);
        calculateLineAmount(dto);
        SaleOrderDetails entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());
        if (entity.getCurrencyValue() == null) entity.setCurrencyValue(0.0);
        if (entity.getActualAmount() == null) entity.setActualAmount(0.0);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public SaleOrderDetailsDto update(Integer id, SaleOrderDetailsDto dto) {
        logger.info("Updating SaleOrderDetails with ID: {}", id);
        SaleOrderDetails entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        validateLineItemData(dto);
        calculateLineAmount(dto);
        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<SaleOrderDetailsDto> getByItemMasterRefId(Integer itemMasterRefId) {
        return repository.findByItemMasterRefId(itemMasterRefId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        return repository.countBySaleOrderMasterRefId(saleOrderMasterRefId);
    }

    @Override
    @Transactional
    public void deleteAllBySaleOrderMasterRefId(Integer saleOrderMasterRefId) {
        logger.info("Deleting all details for order: {}", saleOrderMasterRefId);
        List<SaleOrderDetails> details = repository.findBySaleOrderMasterRefId(saleOrderMasterRefId);
        repository.deleteAll(details);
    }

    @Override
    public SaleOrderDetailsDto calculateLineAmount(SaleOrderDetailsDto dto) {
        Double qty = dto.getItemQty() != null ? dto.getItemQty() : 0.0;
        Double rate = dto.getSalesRate() != null ? dto.getSalesRate() : 0.0;
        Double disc = dto.getDiscAmount() != null ? dto.getDiscAmount() : 0.0;
        Double tax = dto.getTaxAmount() != null ? dto.getTaxAmount() : 0.0;
        Double amount = (qty * rate) - disc + tax;
        dto.setAmount(amount);
        return dto;
    }

    @Override
    public void validateLineItemData(SaleOrderDetailsDto dto) {
        if (dto.getSaleOrderMasterRefId() == null) throw new RuntimeException("Order Master ID required");
        if (dto.getItemMasterRefId() == null) throw new RuntimeException("Item Master ID required");
        if (dto.getItemQty() == null || dto.getItemQty() < 0) throw new RuntimeException("Quantity invalid");
        if (dto.getSalesRate() == null || dto.getSalesRate() < 0) throw new RuntimeException("Rate invalid");
    }
}

