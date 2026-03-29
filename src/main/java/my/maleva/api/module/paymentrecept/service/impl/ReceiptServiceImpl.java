package my.maleva.api.module.paymentrecept.service.impl;

import my.maleva.api.module.paymentrecept.dto.ReceiptDto;
import my.maleva.api.module.paymentrecept.mapper.ReceiptMapper;
import my.maleva.api.module.paymentrecept.entity.Receipt;
import my.maleva.api.module.paymentrecept.repository.ReceiptRepository;
import my.maleva.api.module.paymentrecept.service.ReceiptService;
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
 * ReceiptServiceImpl
 * Service implementation for Receipt
 * Implements SP_Receipt stored procedure logic
 */
@Service
public class ReceiptServiceImpl implements ReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptServiceImpl.class);

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private ReceiptMapper mapper;

    @Override
    public List<ReceiptDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all Receipt records for company: {}", companyRefId);
        return receiptRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReceiptDto> getById(Integer id) {
        logger.info("Fetching Receipt by ID: {}", id);
        return receiptRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public ReceiptDto create(ReceiptDto dto) {
        logger.info("Creating new Receipt for company: {}", dto.getCompanyRefId());
        Receipt entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());
        if (entity.getPvStatus() == null) {
            entity.setPvStatus(0);
        }
        Receipt saved = receiptRepository.save(entity);
        logger.info("Receipt created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public ReceiptDto update(Integer id, ReceiptDto dto) {
        logger.info("Updating Receipt with ID: {}", id);
        Receipt entity = receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found with ID: " + id));
        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        Receipt updated = receiptRepository.save(entity);
        logger.info("Receipt updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting Receipt with ID: {}", id);
        if (receiptRepository.existsById(id)) {
            receiptRepository.deleteById(id);
            logger.info("Receipt deleted with ID: {}", id);
            return true;
        }
        logger.warn("Receipt not found with ID: {}", id);
        return false;
    }

    @Override
    public List<ReceiptDto> getByCustomer(Integer companyRefId, Integer customerRefId) {
        logger.info("Fetching Receipt for customer: {}", customerRefId);
        return receiptRepository.findByCompanyRefIdAndCustomerRefId(companyRefId, customerRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReceiptDto> getByBank(Integer companyRefId, Integer bankRefId) {
        logger.info("Fetching Receipt for bank: {}", bankRefId);
        return receiptRepository.findByCompanyRefIdAndBankRefId(companyRefId, bankRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReceiptDto> getByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Fetching Receipt by CNumber: {}", cNumber);
        return receiptRepository.findByCompanyRefIdAndCNumber(companyRefId, cNumber)
                .map(mapper::toDto);
    }

    @Override
    public List<ReceiptDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching Receipt between dates: {} to {}", startDate, endDate);
        return receiptRepository.findByCompanyRefIdAndReceiptDateBetween(companyRefId, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReceiptDto> getByRefNumber(Integer companyRefId, String refNumber) {
        logger.info("Fetching Receipt by reference number: {}", refNumber);
        return receiptRepository.findByCompanyRefIdAndRefNumber(companyRefId, refNumber)
                .map(mapper::toDto);
    }

    @Override
    public Optional<ReceiptDto> getByCNumberDisplay(String cNumberDisplay) {
        logger.info("Fetching Receipt by CNumberDisplay: {}", cNumberDisplay);
        return receiptRepository.findByCNumberDisplay(cNumberDisplay)
                .map(mapper::toDto);
    }

    @Override
    public List<ReceiptDto> getByPvStatus(Integer companyRefId, Integer pvStatus) {
        logger.info("Fetching Receipt by PV Status: {}", pvStatus);
        return receiptRepository.findByCompanyRefIdAndPvStatus(companyRefId, pvStatus)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Checking if CNumber exists: {}", cNumber);
        return receiptRepository.existsByCompanyRefIdAndCNumber(companyRefId, cNumber);
    }

    @Override
    public long countByCompanyId(Integer companyRefId) {
        logger.info("Counting Receipt records for company: {}", companyRefId);
        return receiptRepository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countByPvStatus(Integer companyRefId, Integer pvStatus) {
        logger.info("Counting Receipt by PV Status for company: {}", companyRefId);
        return receiptRepository.countByCompanyRefIdAndPvStatus(companyRefId, pvStatus);
    }

    @Override
    public String generateCNumberDisplay(Integer cNumber) {
        logger.info("Generating CNumberDisplay for CNumber: {}", cNumber);
        // Format: RC + 9 digit zero-padded number (e.g., RC000000001)
        return String.format("RC%09d", cNumber);
    }

    @Override
    @Transactional
    public ReceiptDto changeStatus(Integer id, Integer pvStatus) {
        logger.info("Changing Receipt status to: {}", pvStatus);
        Receipt entity = receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found with ID: " + id));
        entity.setPvStatus(pvStatus);
        entity.setModifiedDate(LocalDateTime.now());
        Receipt updated = receiptRepository.save(entity);
        logger.info("Receipt status changed with ID: {}", id);
        return mapper.toDto(updated);
    }
}

