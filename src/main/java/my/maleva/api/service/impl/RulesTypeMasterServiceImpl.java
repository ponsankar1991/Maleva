package my.maleva.api.service.impl;

import my.maleva.api.dto.RulesTypeMasterDto;
import my.maleva.api.mapper.RulesTypeMasterMapper;
import my.maleva.api.model.RulesTypeMaster;
import my.maleva.api.repo.RulesTypeMasterRepository;
import my.maleva.api.service.RulesTypeMasterService;
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
 * RulesTypeMasterServiceImpl
 * Service implementation for RulesTypeMaster
 * Implements business logic for rule type management
 */
@Service
public class RulesTypeMasterServiceImpl implements RulesTypeMasterService {

    private static final Logger logger = LoggerFactory.getLogger(RulesTypeMasterServiceImpl.class);

    @Autowired
    private RulesTypeMasterRepository rulesTypeMasterRepository;

    @Autowired
    private RulesTypeMasterMapper mapper;

    @Override
    public List<RulesTypeMasterDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all RulesTypeMaster records for company: {}", companyRefId);
        return rulesTypeMasterRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RulesTypeMasterDto> getActiveByCompanyId(Integer companyRefId) {
        logger.info("Fetching active RulesTypeMaster records for company: {}", companyRefId);
        return rulesTypeMasterRepository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RulesTypeMasterDto> getById(Integer id) {
        logger.info("Fetching RulesTypeMaster by ID: {}", id);
        return rulesTypeMasterRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public RulesTypeMasterDto create(RulesTypeMasterDto dto) {
        logger.info("Creating new RulesTypeMaster for company: {}", dto.getCompanyRefId());
        if (rulesTypeMasterRepository.existsByCompanyRefIdAndRuleTypeCode(dto.getCompanyRefId(), dto.getRuleTypeCode())) {
            throw new RuntimeException("Rule Type Code already exists: " + dto.getRuleTypeCode());
        }
        RulesTypeMaster entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());
        if (entity.getActive() == null) {
            entity.setActive(0);
        }
        RulesTypeMaster saved = rulesTypeMasterRepository.save(entity);
        logger.info("RulesTypeMaster created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public RulesTypeMasterDto update(Integer id, RulesTypeMasterDto dto) {
        logger.info("Updating RulesTypeMaster with ID: {}", id);
        RulesTypeMaster entity = rulesTypeMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RulesTypeMaster not found with ID: " + id));
        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        RulesTypeMaster updated = rulesTypeMasterRepository.save(entity);
        logger.info("RulesTypeMaster updated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting RulesTypeMaster with ID: {}", id);
        if (rulesTypeMasterRepository.existsById(id)) {
            rulesTypeMasterRepository.deleteById(id);
            logger.info("RulesTypeMaster deleted with ID: {}", id);
            return true;
        }
        logger.warn("RulesTypeMaster not found with ID: {}", id);
        return false;
    }

    @Override
    public Optional<RulesTypeMasterDto> getByRuleTypeCode(Integer companyRefId, String ruleTypeCode) {
        logger.info("Fetching RulesTypeMaster by rule type code: {}", ruleTypeCode);
        return rulesTypeMasterRepository.findByCompanyRefIdAndRuleTypeCode(companyRefId, ruleTypeCode)
                .map(mapper::toDto);
    }

    @Override
    public Optional<RulesTypeMasterDto> getByRuleTypeName(Integer companyRefId, String ruleTypeName) {
        logger.info("Fetching RulesTypeMaster by rule type name: {}", ruleTypeName);
        return rulesTypeMasterRepository.findByCompanyRefIdAndRuleTypeName(companyRefId, ruleTypeName)
                .map(mapper::toDto);
    }

    @Override
    public boolean existsByRuleTypeCode(Integer companyRefId, String ruleTypeCode) {
        logger.info("Checking if rule type code exists: {}", ruleTypeCode);
        return rulesTypeMasterRepository.existsByCompanyRefIdAndRuleTypeCode(companyRefId, ruleTypeCode);
    }

    @Override
    public long countByCompanyId(Integer companyRefId) {
        logger.info("Counting RulesTypeMaster records for company: {}", companyRefId);
        return rulesTypeMasterRepository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompanyId(Integer companyRefId) {
        logger.info("Counting active RulesTypeMaster records for company: {}", companyRefId);
        return rulesTypeMasterRepository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    @Transactional
    public RulesTypeMasterDto activate(Integer id) {
        logger.info("Activating RulesTypeMaster with ID: {}", id);
        RulesTypeMaster entity = rulesTypeMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RulesTypeMaster not found with ID: " + id));
        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        RulesTypeMaster updated = rulesTypeMasterRepository.save(entity);
        logger.info("RulesTypeMaster activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public RulesTypeMasterDto deactivate(Integer id) {
        logger.info("Deactivating RulesTypeMaster with ID: {}", id);
        RulesTypeMaster entity = rulesTypeMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RulesTypeMaster not found with ID: " + id));
        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        RulesTypeMaster updated = rulesTypeMasterRepository.save(entity);
        logger.info("RulesTypeMaster deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }
}

