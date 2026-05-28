package my.maleva.api.module.master.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.master.dto.SymbolMasterDto;
import my.maleva.api.module.master.mapper.SymbolMasterMapper;
import my.maleva.api.module.master.entity.SymbolMaster;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.master.service.SymbolMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SymbolMasterServiceImpl - Implementation for SymbolMaster service
 * Handles currency symbol management with validation and audit trail
 */
@Service
public class SymbolMasterServiceImpl implements SymbolMasterService {

    private static final Logger logger = LoggerFactory.getLogger(SymbolMasterServiceImpl.class);

    @Autowired
    private SymbolMasterRepository repository;

    @Autowired
    private SymbolMasterMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<SymbolMasterDto> getByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching SymbolMaster for company: {}", companyRefId);
        return repository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SymbolMasterDto> getActiveByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching active SymbolMaster for company: {}", companyRefId);
        return repository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SymbolMasterDto> selectSymbol(Integer companyRefId) {
        logger.info("Selecting SymbolMaster for company: {} (Active != 2)", companyRefId);
        List<SymbolMaster> entities = repository.findByCompanyRefIdAndActiveNot(companyRefId, 2);

        if (logger.isDebugEnabled() && !entities.isEmpty()) {
            SymbolMaster first = entities.get(0);
            logger.debug("Query result - First entity: sName='{}', cName='{}', id={}",
                    first.getSName(), first.getCName(), first.getId());
        }

        List<SymbolMasterDto> result = entities.stream()
                .map(entity -> {
                    SymbolMasterDto dto = mapper.toDto(entity);
                    logger.debug("Mapped DTO - id={}, sName='{}', cName='{}'",
                            dto.getId(), dto.getSName(), dto.getCName());
                    return dto;
                })
                .collect(Collectors.toList());

        return result;
    }

    @Override
    public Optional<SymbolMasterDto> getBySName(String sName, Integer companyRefId) {
        logger.info("Fetching SymbolMaster by name: {} for company: {}", sName, companyRefId);
        return repository.findBySNameAndCompanyRefId(sName, companyRefId).map(mapper::toDto);
    }

    @Override
    public Optional<SymbolMasterDto> getByCName(String cName) {
        logger.info("Fetching SymbolMaster by currency name: {}", cName);
        return repository.findByCName(cName).map(mapper::toDto);
    }

    @Override
    public List<SymbolMasterDto> getByDFlag(Integer dFlag) {
        logger.info("Fetching SymbolMaster for display flag: {}", dFlag);
        return repository.findByDFlag(dFlag)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SymbolMasterDto> getByQneId(Integer qneId) {
        logger.info("Fetching SymbolMaster for QNE ID: {}", qneId);
        return repository.findByQneId(qneId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SymbolMasterDto> getById(Integer id) {
        logger.info("Fetching SymbolMaster by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SymbolMasterDto create(SymbolMasterDto dto) {
        logger.info("Creating new SymbolMaster");
        validateSymbolMasterData(dto);
        SymbolMaster entity = mapper.toEntity(dto);

        // Set default values as per table schema
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(now);
        }
        if (entity.getModifiedDate() == null) {
            entity.setModifiedDate(now);
        }
        if (entity.getModifiedBy() == null) {
            entity.setModifiedBy("SYSTEM");
        }
        if (entity.getActive() == null) {
            entity.setActive(1);
        }
        if (entity.getDFlag() == null) {
            entity.setDFlag(0);
        }
        if (entity.getQneId() == null) {
            entity.setQneId(0);
        }

        SymbolMaster saved = repository.save(entity);
        logger.info("SymbolMaster created with ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SymbolMasterDto update(Integer id, SymbolMasterDto dto) {
        logger.info("Updating SymbolMaster with ID: {}", id);
        validateSymbolMasterData(dto);

        SymbolMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SymbolMaster not found: " + id));

        // Preserve created date and update modified date
        LocalDateTime now = LocalDateTime.now();
        entity.setModifiedDate(now);
        entity.setModifiedBy("SYSTEM");

        mapper.updateEntityFromDto(dto, entity);
        SymbolMaster updated = repository.save(entity);
        logger.info("SymbolMaster updated with ID: {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting SymbolMaster with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("SymbolMaster deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long countByCompanyRefId(Integer companyRefId) {
        logger.info("Counting SymbolMaster for company: {}", companyRefId);
        return repository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompanyRefId(Integer companyRefId) {
        logger.info("Counting active SymbolMaster for company: {}", companyRefId);
        return repository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    public void validateSymbolMasterData(SymbolMasterDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new RuntimeException("Company Reference ID is required");
        }
        if (dto.getSName() == null || dto.getSName().trim().isEmpty()) {
            throw new RuntimeException("Symbol Name is required");
        }
        if (dto.getDFlag() == null) {
            throw new RuntimeException("Display Flag is required");
        }
        if (dto.getActive() == null) {
            throw new RuntimeException("Active status is required");
        }
    }

    @Override
    @Transactional
    public SymbolMasterDto activateSymbol(Integer id) {
        logger.info("Activating SymbolMaster with ID: {}", id);
        SymbolMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SymbolMaster not found: " + id));

        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        SymbolMaster updated = repository.save(entity);

        logger.info("SymbolMaster activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public SymbolMasterDto deactivateSymbol(Integer id) {
        logger.info("Deactivating SymbolMaster with ID: {}", id);
        SymbolMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SymbolMaster not found: " + id));

        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        SymbolMaster updated = repository.save(entity);

        logger.info("SymbolMaster deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    public boolean existsBySName(String sName, Integer companyRefId) {
        logger.info("Checking if SymbolMaster exists with name: {} for company: {}", sName, companyRefId);
        return repository.existsBySNameAndCompanyRefId(sName, companyRefId);
    }

    /**
     * Process Symbol using SP_Symbol logic
     * Incorporates stored procedure logic with batch processing
     *
     * @param dto - SymbolMasterDto with symbol data
     * @param companyId - Company ID for the symbol
     * @param checkFlag - If 1, checks if symbol already exists before insert
     * @return Processed SymbolMasterDto
     */
    @Transactional
    public SymbolMasterDto processSymbol(SymbolMasterDto dto, Integer companyId, Integer checkFlag) {
        logger.info("Processing Symbol with SP_Symbol logic for company: {} with check flag: {}", companyId, checkFlag);

        try {
            String detailsJson = objectMapper.writeValueAsString(List.of(dto));

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("SP_Symbol");

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("details", detailsJson);
            inParams.put("Comid", companyId);
            inParams.put("Check", checkFlag);

            Map<String, Object> out = jdbcCall.execute(inParams);

            List<Map<String, Object>> resultList = (List<Map<String, Object>>) out.get("#result-set-1");
            if (resultList != null && !resultList.isEmpty()) {
                Map<String, Object> row = resultList.get(0);
                Integer resultStatus = (Integer) row.get("Result");
                if (resultStatus != null && resultStatus == 1) {
                    Integer newId = (Integer) row.get("Id");
                    return getById(newId).orElseThrow(() -> new RuntimeException("Failed to retrieve symbol after SP execution"));
                } else {
                    String msg = (String) row.get("Msg");
                    throw new RuntimeException("Stored procedure execution failed: " + msg);
                }
            }
            throw new RuntimeException("No result set returned from stored procedure");

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing DTO to JSON", e);
        }
    }
}
