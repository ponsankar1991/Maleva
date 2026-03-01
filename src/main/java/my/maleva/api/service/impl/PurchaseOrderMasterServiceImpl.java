package my.maleva.api.service.impl;

import my.maleva.api.dto.PurchaseOrderMasterDto;
import my.maleva.api.mapper.PurchaseOrderMasterMapper;
import my.maleva.api.model.PurchaseOrderMaster;
import my.maleva.api.repo.PurchaseOrderMasterRepository;
import my.maleva.api.service.PurchaseOrderMasterService;
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
 * PurchaseOrderMasterServiceImpl
 * Service implementation for PurchaseOrderMaster
 * Implements SP_PurchaseOrderMaster stored procedure logic
 */
@Service
public class PurchaseOrderMasterServiceImpl implements PurchaseOrderMasterService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderMasterServiceImpl.class);

    @Autowired
    private PurchaseOrderMasterRepository purchaseOrderMasterRepository;

    @Autowired
    private PurchaseOrderMasterMapper mapper;

    @Override
    public List<PurchaseOrderMasterDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all PurchaseOrderMaster records for company: {}", companyRefId);
        return purchaseOrderMasterRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderMasterDto> getActiveByCompanyId(Integer companyRefId) {
        logger.info("Fetching active PurchaseOrderMaster records for company: {}", companyRefId);
        return purchaseOrderMasterRepository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PurchaseOrderMasterDto> getById(Integer id) {
        logger.info("Fetching PurchaseOrderMaster by ID: {}", id);
        return purchaseOrderMasterRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public PurchaseOrderMasterDto create(PurchaseOrderMasterDto dto) {
        logger.info("Creating new PurchaseOrderMaster for company: {}", dto.getCompanyRefId());
        PurchaseOrderMaster entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());
        if (entity.getActive() == null) {
            entity.setActive(0);
        }
        if (entity.getPStatus() == null) {
            entity.setPStatus(0);
        }
        PurchaseOrderMaster saved = purchaseOrderMasterRepository.save(entity);
        logger.info("PurchaseOrderMaster created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderMasterDto update(Integer id, PurchaseOrderMasterDto dto) {
        logger.info("Updating PurchaseOrderMaster with ID: {}", id);
        PurchaseOrderMaster entity = purchaseOrderMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseOrderMaster not found with ID: " + id));
        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        PurchaseOrderMaster updated = purchaseOrderMasterRepository.save(entity);
        logger.info("PurchaseOrderMaster updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting PurchaseOrderMaster with ID: {}", id);
        if (purchaseOrderMasterRepository.existsById(id)) {
            purchaseOrderMasterRepository.deleteById(id);
            logger.info("PurchaseOrderMaster deleted with ID: {}", id);
            return true;
        }
        logger.warn("PurchaseOrderMaster not found with ID: {}", id);
        return false;
    }

    @Override
    public Optional<PurchaseOrderMasterDto> getByInvoiceNo(Integer companyRefId, String invoiceNo) {
        logger.info("Fetching PurchaseOrderMaster by invoice number: {}", invoiceNo);
        return purchaseOrderMasterRepository.findByCompanyRefIdAndInvoiceNo(companyRefId, invoiceNo)
                .map(mapper::toDto);
    }

    @Override
    public List<PurchaseOrderMasterDto> getBySupplier(Integer companyRefId, Integer supplierRefId) {
        logger.info("Fetching PurchaseOrderMaster for supplier: {}", supplierRefId);
        return purchaseOrderMasterRepository.findByCompanyRefIdAndSupplierRefId(companyRefId, supplierRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderMasterDto> getBySaleType(Integer companyRefId, String saleType) {
        logger.info("Fetching PurchaseOrderMaster by sale type: {}", saleType);
        return purchaseOrderMasterRepository.findByCompanyRefIdAndSaleType(companyRefId, saleType)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderMasterDto> getByDateRange(Integer companyRefId, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching PurchaseOrderMaster between dates: {} to {}", startDate, endDate);
        return purchaseOrderMasterRepository.findByCompanyRefIdAndSaleDateBetween(companyRefId, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderMasterDto> getByEmployee(Integer companyRefId, Integer employeeRefId) {
        logger.info("Fetching PurchaseOrderMaster for employee: {}", employeeRefId);
        return purchaseOrderMasterRepository.findByCompanyRefIdAndEmployeeRefId(companyRefId, employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PurchaseOrderMasterDto> getByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Fetching PurchaseOrderMaster by CNumber: {}", cNumber);
        return purchaseOrderMasterRepository.findByCompanyRefIdAndCNumber(companyRefId, cNumber)
                .map(mapper::toDto);
    }

    @Override
    public boolean existsByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Checking if CNumber exists: {}", cNumber);
        return purchaseOrderMasterRepository.existsByCompanyRefIdAndCNumber(companyRefId, cNumber);
    }

    @Override
    public long countByCompanyId(Integer companyRefId) {
        logger.info("Counting PurchaseOrderMaster records for company: {}", companyRefId);
        return purchaseOrderMasterRepository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompanyId(Integer companyRefId) {
        logger.info("Counting active PurchaseOrderMaster records for company: {}", companyRefId);
        return purchaseOrderMasterRepository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    @Transactional
    public PurchaseOrderMasterDto activate(Integer id) {
        logger.info("Activating PurchaseOrderMaster with ID: {}", id);
        PurchaseOrderMaster entity = purchaseOrderMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseOrderMaster not found with ID: " + id));
        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        PurchaseOrderMaster updated = purchaseOrderMasterRepository.save(entity);
        logger.info("PurchaseOrderMaster activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public PurchaseOrderMasterDto deactivate(Integer id) {
        logger.info("Deactivating PurchaseOrderMaster with ID: {}", id);
        PurchaseOrderMaster entity = purchaseOrderMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseOrderMaster not found with ID: " + id));
        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        PurchaseOrderMaster updated = purchaseOrderMasterRepository.save(entity);
        logger.info("PurchaseOrderMaster deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    public String generateCNumberDisplay(Integer cNumber) {
        logger.info("Generating CNumberDisplay for CNumber: {}", cNumber);
        // Format: PO + 9 digit zero-padded number (e.g., PO000000001)
        return String.format("PO%09d", cNumber);
    }
}

