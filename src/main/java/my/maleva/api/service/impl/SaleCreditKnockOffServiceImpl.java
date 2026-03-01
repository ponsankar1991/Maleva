package my.maleva.api.service.impl;

import my.maleva.api.dto.SaleCreditKnockOffDto;
import my.maleva.api.mapper.SaleCreditKnockOffMapper;
import my.maleva.api.model.SaleCreditKnockOff;
import my.maleva.api.repo.SaleCreditKnockOffRepository;
import my.maleva.api.service.SaleCreditKnockOffService;
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
 * SaleCreditKnockOffServiceImpl
 * Service implementation for SaleCreditKnockOff
 * Implements business logic for sale credit knock-off management
 */
@Service
public class SaleCreditKnockOffServiceImpl implements SaleCreditKnockOffService {

    private static final Logger logger = LoggerFactory.getLogger(SaleCreditKnockOffServiceImpl.class);

    @Autowired
    private SaleCreditKnockOffRepository saleCreditKnockOffRepository;

    @Autowired
    private SaleCreditKnockOffMapper mapper;

    @Override
    public List<SaleCreditKnockOffDto> getBySaleCreditMasterRefId(Integer saleCreditMasterRefId) {
        logger.info("Fetching SaleCreditKnockOff records by Sale Credit Master Reference ID: {}", saleCreditMasterRefId);
        return saleCreditKnockOffRepository.findBySaleCreditMasterRefId(saleCreditMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleCreditKnockOffDto> getById(Integer id) {
        logger.info("Fetching SaleCreditKnockOff by ID: {}", id);
        return saleCreditKnockOffRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public SaleCreditKnockOffDto create(SaleCreditKnockOffDto dto) {
        logger.info("Creating new SaleCreditKnockOff for Sale Credit Master: {}", dto.getSaleCreditMasterRefId());

        // Set default values if null
        if (dto.getCurrencyValue() == null) {
            dto.setCurrencyValue(0.0);
        }
        if (dto.getActualAmount() == null) {
            dto.setActualAmount(0.0);
        }

        SaleCreditKnockOff entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());

        SaleCreditKnockOff saved = saleCreditKnockOffRepository.save(entity);
        logger.info("SaleCreditKnockOff created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SaleCreditKnockOffDto update(Integer id, SaleCreditKnockOffDto dto) {
        logger.info("Updating SaleCreditKnockOff with ID: {}", id);
        SaleCreditKnockOff entity = saleCreditKnockOffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleCreditKnockOff not found with ID: " + id));

        mapper.updateEntityFromDto(dto, entity);
        SaleCreditKnockOff updated = saleCreditKnockOffRepository.save(entity);
        logger.info("SaleCreditKnockOff updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SaleCreditKnockOff with ID: {}", id);
        if (saleCreditKnockOffRepository.existsById(id)) {
            saleCreditKnockOffRepository.deleteById(id);
            logger.info("SaleCreditKnockOff deleted with ID: {}", id);
            return true;
        }
        logger.warn("SaleCreditKnockOff not found with ID: {}", id);
        return false;
    }

    @Override
    public List<SaleCreditKnockOffDto> getByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching SaleCreditKnockOff records by company ID: {}", companyRefId);
        return saleCreditKnockOffRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleCreditKnockOffDto> getBySaleMasterRefId(Integer saleMasterRefId) {
        logger.info("Fetching SaleCreditKnockOff records by Sale Master Reference ID: {}", saleMasterRefId);
        return saleCreditKnockOffRepository.findBySaleMasterRefId(saleMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleCreditKnockOffDto> getByCustomerOpenRefId(Integer customerOpenRefId) {
        logger.info("Fetching SaleCreditKnockOff records by customer ID: {}", customerOpenRefId);
        return saleCreditKnockOffRepository.findByCustomerOpenRefId(customerOpenRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countBySaleCreditMasterRefId(Integer saleCreditMasterRefId) {
        logger.info("Counting SaleCreditKnockOff records for Sale Credit Master: {}", saleCreditMasterRefId);
        return saleCreditKnockOffRepository.countBySaleCreditMasterRefId(saleCreditMasterRefId);
    }

    @Override
    public List<SaleCreditKnockOffDto> getByCompanyAndSaleCreditMaster(Integer companyRefId, Integer saleCreditMasterRefId) {
        logger.info("Fetching SaleCreditKnockOff records for company: {} and Sale Credit Master: {}", companyRefId, saleCreditMasterRefId);
        return saleCreditKnockOffRepository.findByCompanyRefIdAndSaleCreditMasterRefId(companyRefId, saleCreditMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllBySaleCreditMasterRefId(Integer saleCreditMasterRefId) {
        logger.info("Deleting all SaleCreditKnockOff records for Sale Credit Master: {}", saleCreditMasterRefId);
        List<SaleCreditKnockOff> knockOffs = saleCreditKnockOffRepository.findBySaleCreditMasterRefId(saleCreditMasterRefId);
        saleCreditKnockOffRepository.deleteAll(knockOffs);
        logger.info("Deleted {} SaleCreditKnockOff records", knockOffs.size());
    }
}

