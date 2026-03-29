package my.maleva.api.module.purchase.service.impl;

import my.maleva.api.module.purchase.dto.PurchaseMasterDto;
import my.maleva.api.module.purchase.mapper.PurchaseMasterMapper;
import my.maleva.api.module.purchase.entity.PurchaseMaster;
import my.maleva.api.module.purchase.repository.PurchaseMasterRepository;
import my.maleva.api.module.purchase.service.PurchaseMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for PurchaseMaster
 * Implements SP_PurchaseMaster stored procedure logic
 */
@Service
public class PurchaseMasterServiceImpl implements PurchaseMasterService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseMasterServiceImpl.class);

    @Autowired
    private PurchaseMasterRepository purchaseMasterRepository;

    @Autowired
    private PurchaseMasterMapper mapper;

    @Override
    public List<PurchaseMasterDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all PurchaseMaster records for company: {}", companyRefId);
        return purchaseMasterRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseMasterDto> getActiveByCompanyId(Integer companyRefId) {
        logger.info("Fetching active PurchaseMaster records for company: {}", companyRefId);
        return purchaseMasterRepository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PurchaseMasterDto> getById(Integer id) {
        logger.info("Fetching PurchaseMaster by ID: {}", id);
        return purchaseMasterRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public PurchaseMasterDto create(PurchaseMasterDto dto) {
        logger.info("Creating new PurchaseMaster for company: {}", dto.getCompanyRefId());

        PurchaseMaster entity = mapper.toEntity(dto);
        entity.setActive(1);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());

        // Generate next CNumber based on SP_PurchaseMaster logic
        Integer nextCNumber = getNextCNumber(dto.getCompanyRefId());
        entity.setCNumber(nextCNumber);
        String cNumberDisplay = generateCNumberDisplay(nextCNumber);
        entity.setCNumberDisplay(cNumberDisplay);

        PurchaseMaster saved = purchaseMasterRepository.save(entity);
        logger.info("PurchaseMaster created successfully with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public PurchaseMasterDto update(Integer id, PurchaseMasterDto dto) {
        logger.info("Updating PurchaseMaster with ID: {}", id);
        PurchaseMaster entity = purchaseMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseMaster not found with ID: " + id));

        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());

        PurchaseMaster updated = purchaseMasterRepository.save(entity);
        logger.info("PurchaseMaster updated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting PurchaseMaster with ID: {}", id);
        if (!purchaseMasterRepository.existsById(id)) {
            logger.warn("PurchaseMaster not found with ID: {}", id);
            return false;
        }
        purchaseMasterRepository.deleteById(id);
        logger.info("PurchaseMaster deleted successfully with ID: {}", id);
        return true;
    }

    @Override
    public Optional<PurchaseMasterDto> getByInvoiceNo(Integer companyRefId, String invoiceNo) {
        logger.info("Fetching PurchaseMaster by invoice number: {}", invoiceNo);
        return purchaseMasterRepository.findByCompanyRefIdAndInvoiceNo(companyRefId, invoiceNo)
                .map(mapper::toDto);
    }

    @Override
    public List<PurchaseMasterDto> getBySupplier(Integer companyRefId, Integer supplierRefId) {
        logger.info("Fetching PurchaseMaster for supplier: {}", supplierRefId);
        return purchaseMasterRepository.findByCompanyRefIdAndSupplierRefId(companyRefId, supplierRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseMasterDto> getBySaleType(Integer companyRefId, String saleType) {
        logger.info("Fetching PurchaseMaster by sale type: {}", saleType);
        return purchaseMasterRepository.findByCompanyRefIdAndSaleType(companyRefId, saleType)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseMasterDto> getByDateRange(Integer companyRefId, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching PurchaseMaster between dates: {} to {}", startDate, endDate);
        return purchaseMasterRepository.findByCompanyRefIdAndSaleDateBetween(companyRefId, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseMasterDto> getByEmployee(Integer companyRefId, Integer employeeRefId) {
        logger.info("Fetching PurchaseMaster for employee: {}", employeeRefId);
        return purchaseMasterRepository.findByCompanyRefIdAndEmployeeRefId(companyRefId, employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Checking if CNumber exists: {}", cNumber);
        return purchaseMasterRepository.existsByCompanyRefIdAndCNumber(companyRefId, cNumber);
    }

    @Override
    public Optional<PurchaseMasterDto> getByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Fetching PurchaseMaster by CNumber: {}", cNumber);
        return purchaseMasterRepository.findByCompanyRefIdAndCNumber(companyRefId, cNumber)
                .map(mapper::toDto);
    }

    @Override
    public Optional<PurchaseMasterDto> getByPurchaseOrderRef(Integer purchaseOrderMasterRefId) {
        logger.info("Fetching PurchaseMaster by purchase order reference: {}", purchaseOrderMasterRefId);
        return purchaseMasterRepository.findByPurchaseOrderMasterRefId(purchaseOrderMasterRefId)
                .map(mapper::toDto);
    }

    @Override
    public long countByCompanyId(Integer companyRefId) {
        logger.info("Counting PurchaseMaster records for company: {}", companyRefId);
        return purchaseMasterRepository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompanyId(Integer companyRefId) {
        logger.info("Counting active PurchaseMaster records for company: {}", companyRefId);
        return purchaseMasterRepository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    @Transactional
    public PurchaseMasterDto activate(Integer id) {
        logger.info("Activating PurchaseMaster with ID: {}", id);
        PurchaseMaster entity = purchaseMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseMaster not found with ID: " + id));
        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        PurchaseMaster updated = purchaseMasterRepository.save(entity);
        logger.info("PurchaseMaster activated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public PurchaseMasterDto deactivate(Integer id) {
        logger.info("Deactivating PurchaseMaster with ID: {}", id);
        PurchaseMaster entity = purchaseMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseMaster not found with ID: " + id));
        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        PurchaseMaster updated = purchaseMasterRepository.save(entity);
        logger.info("PurchaseMaster deactivated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    public Integer getNextCNumber(Integer companyRefId) {
        logger.info("Generating next CNumber for company: {}", companyRefId);
        // This logic is based on SP_PurchaseMaster stored procedure
        // It retrieves the maximum CNumber from SequenceNoMaster for 'PurchaseMaster'
        // and increments it by 1
        List<PurchaseMaster> purchases = purchaseMasterRepository.findByCompanyRefId(companyRefId);
        if (purchases.isEmpty()) {
            return 1;
        }
        Integer maxCNumber = purchases.stream()
                .map(PurchaseMaster::getCNumber)
                .max(Integer::compareTo)
                .orElse(0);
        return maxCNumber + 1;
    }

    @Override
    public String generateCNumberDisplay(Integer cNumber) {
        logger.info("Generating CNumberDisplay for CNumber: {}", cNumber);
        // Format: PM + 9 digit zero-padded number (e.g., PM000000001)
        return String.format("PM%09d", cNumber);
    }
}

