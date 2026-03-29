package my.maleva.api.module.paymentrecept.service.impl;

import my.maleva.api.module.paymentrecept.dto.ReceiptDetailsDto;
import my.maleva.api.module.paymentrecept.mapper.ReceiptDetailsMapper;
import my.maleva.api.module.paymentrecept.entity.ReceiptDetails;
import my.maleva.api.module.paymentrecept.repository.ReceiptDetailsRepository;
import my.maleva.api.module.paymentrecept.service.ReceiptDetailsService;
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
 * ReceiptDetailsServiceImpl
 * Service implementation for ReceiptDetails
 */
@Service
public class ReceiptDetailsServiceImpl implements ReceiptDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptDetailsServiceImpl.class);

    @Autowired
    private ReceiptDetailsRepository receiptDetailsRepository;

    @Autowired
    private ReceiptDetailsMapper mapper;

    @Override
    public List<ReceiptDetailsDto> getByReceiptId(Integer receiptRefId) {
        logger.info("Fetching ReceiptDetails for Receipt: {}", receiptRefId);
        return receiptDetailsRepository.findByReceiptRefId(receiptRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReceiptDetailsDto> getById(Integer id) {
        logger.info("Fetching ReceiptDetails by ID: {}", id);
        return receiptDetailsRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public ReceiptDetailsDto create(ReceiptDetailsDto dto) {
        logger.info("Creating new ReceiptDetails for Receipt: {}", dto.getReceiptRefId());
        ReceiptDetails entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        ReceiptDetails saved = receiptDetailsRepository.save(entity);
        logger.info("ReceiptDetails created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public ReceiptDetailsDto update(Integer id, ReceiptDetailsDto dto) {
        logger.info("Updating ReceiptDetails with ID: {}", id);
        ReceiptDetails entity = receiptDetailsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReceiptDetails not found with ID: " + id));
        mapper.updateEntityFromDto(dto, entity);
        ReceiptDetails updated = receiptDetailsRepository.save(entity);
        logger.info("ReceiptDetails updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting ReceiptDetails with ID: {}", id);
        if (receiptDetailsRepository.existsById(id)) {
            receiptDetailsRepository.deleteById(id);
            logger.info("ReceiptDetails deleted with ID: {}", id);
            return true;
        }
        logger.warn("ReceiptDetails not found with ID: {}", id);
        return false;
    }

    @Override
    public List<ReceiptDetailsDto> getBySaleMasterId(Integer saleMasterRefId) {
        logger.info("Fetching ReceiptDetails by sale master: {}", saleMasterRefId);
        return receiptDetailsRepository.findBySaleMasterRefId(saleMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReceiptDetailsDto> getByCustomerOpenId(Integer customerOpenRefId) {
        logger.info("Fetching ReceiptDetails by customer open: {}", customerOpenRefId);
        return receiptDetailsRepository.findByCustomerOpenRefId(customerOpenRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countByReceiptId(Integer receiptRefId) {
        logger.info("Counting ReceiptDetails for Receipt: {}", receiptRefId);
        return receiptDetailsRepository.countByReceiptRefId(receiptRefId);
    }

    @Override
    @Transactional
    public void deleteByReceiptId(Integer receiptRefId) {
        logger.info("Deleting all ReceiptDetails for Receipt: {}", receiptRefId);
        receiptDetailsRepository.deleteByReceiptRefId(receiptRefId);
    }
}

