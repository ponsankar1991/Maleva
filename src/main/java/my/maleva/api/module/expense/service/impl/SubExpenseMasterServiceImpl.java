package my.maleva.api.module.expense.service.impl;

import my.maleva.api.module.expense.dto.SubExpenseMasterDto;
import my.maleva.api.module.expense.mapper.SubExpenseMasterMapper;
import my.maleva.api.module.expense.entity.SubExpenseMaster;
import my.maleva.api.module.expense.repository.SubExpenseMasterRepository;
import my.maleva.api.module.expense.service.SubExpenseMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SubExpenseMasterServiceImpl - Implementation for SubExpenseMaster service
 * Incorporates SP_SubExpense stored procedure logic
 * Handles sub-expense master with account integration
 */
@Service
public class SubExpenseMasterServiceImpl implements SubExpenseMasterService {

    private static final Logger logger = LoggerFactory.getLogger(SubExpenseMasterServiceImpl.class);

    @Autowired
    private SubExpenseMasterRepository repository;

    @Autowired
    private SubExpenseMasterMapper mapper;

    @Override
    public List<SubExpenseMasterDto> getByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching SubExpenseMaster for company: {}", companyRefId);
        return repository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubExpenseMasterDto> getByExpenseMasterRefId(Integer expenseMasterRefId) {
        logger.info("Fetching SubExpenseMaster for expense master: {}", expenseMasterRefId);
        return repository.findByExpenseMasterRefId(expenseMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubExpenseMasterDto> getByCompanyAndExpenseMaster(Integer companyRefId, Integer expenseMasterRefId) {
        logger.info("Fetching SubExpenseMaster for company: {} and expense master: {}", companyRefId, expenseMasterRefId);
        return repository.findByCompanyRefIdAndExpenseMasterRefId(companyRefId, expenseMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubExpenseMasterDto> getActiveByCompany(Integer companyRefId) {
        logger.info("Fetching active SubExpenseMaster for company: {}", companyRefId);
        return repository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubExpenseMasterDto> getByAccountRefid(Integer accountRefid) {
        logger.info("Fetching SubExpenseMaster for account: {}", accountRefid);
        return repository.findByAccountRefid(accountRefid)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubExpenseMasterDto> getByGlAccountRefId(Integer glAccountRefId) {
        logger.info("Fetching SubExpenseMaster for GL account: {}", glAccountRefId);
        return repository.findByGlAccountRefId(glAccountRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SubExpenseMasterDto> getByDescriptionAndCompany(String description, Integer companyRefId) {
        logger.info("Fetching SubExpenseMaster by description: {} for company: {}", description, companyRefId);
        return repository.findByDescriptionAndCompanyRefId(description, companyRefId).map(mapper::toDto);
    }

    @Override
    public Optional<SubExpenseMasterDto> getById(Integer id) {
        logger.info("Fetching SubExpenseMaster by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SubExpenseMasterDto create(SubExpenseMasterDto dto) {
        logger.info("Creating new SubExpenseMaster");
        validateSubExpenseMasterData(dto);
        SubExpenseMaster entity = mapper.toEntity(dto);

        // Set default values as per table schema
        if (entity.getActive() == null) {
            entity.setActive(1);
        }
        if (entity.getDueAmount() == null) {
            entity.setDueAmount(0F);
        }
        if (entity.getAccountRefid() == null) {
            entity.setAccountRefid(1);
        }

        SubExpenseMaster saved = repository.save(entity);
        logger.info("SubExpenseMaster created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SubExpenseMasterDto update(Integer id, SubExpenseMasterDto dto) {
        logger.info("Updating SubExpenseMaster with ID: {}", id);
        validateSubExpenseMasterData(dto);

        SubExpenseMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubExpenseMaster not found: " + id));

        mapper.updateEntityFromDto(dto, entity);
        SubExpenseMaster updated = repository.save(entity);
        logger.info("SubExpenseMaster updated with ID: {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SubExpenseMaster with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("SubExpenseMaster deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long countByCompanyRefId(Integer companyRefId) {
        logger.info("Counting SubExpenseMaster for company: {}", companyRefId);
        return repository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompany(Integer companyRefId) {
        logger.info("Counting active SubExpenseMaster for company: {}", companyRefId);
        return repository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    public void validateSubExpenseMasterData(SubExpenseMasterDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new RuntimeException("Company Reference ID is required");
        }
        if (dto.getExpenseMasterRefId() == null) {
            throw new RuntimeException("Expense Master Reference ID is required");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Description is required");
        }
        if (dto.getDueAmount() == null) {
            throw new RuntimeException("Due Amount is required");
        }
        if (dto.getAccountRefid() == null) {
            throw new RuntimeException("Account Reference ID is required");
        }
    }

    @Override
    @Transactional
    public SubExpenseMasterDto activateSubExpense(Integer id) {
        logger.info("Activating SubExpenseMaster with ID: {}", id);
        SubExpenseMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubExpenseMaster not found: " + id));

        entity.setActive(1);
        SubExpenseMaster updated = repository.save(entity);

        logger.info("SubExpenseMaster activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public SubExpenseMasterDto deactivateSubExpense(Integer id) {
        logger.info("Deactivating SubExpenseMaster with ID: {}", id);
        SubExpenseMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubExpenseMaster not found: " + id));

        entity.setActive(0);
        SubExpenseMaster updated = repository.save(entity);

        logger.info("SubExpenseMaster deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public SubExpenseMasterDto processSubExpense(SubExpenseMasterDto dto, Integer companyId) {
        logger.info("Processing SubExpenseMaster with SP_SubExpense logic for company: {}", companyId);

        // SP_SubExpense logic: Set company ID and process
        dto.setCompanyRefId(companyId);

        if (dto.getId() == null || dto.getId() == 0) {
            // New record - INSERT
            logger.info("Processing INSERT operation");
            return create(dto);
        } else {
            // Existing record - UPDATE
            logger.info("Processing UPDATE operation for ID: {}", dto.getId());
            return update(dto.getId(), dto);
        }
    }
}

