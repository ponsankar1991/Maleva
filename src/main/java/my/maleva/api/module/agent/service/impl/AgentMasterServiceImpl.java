package my.maleva.api.module.agent.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.agent.dto.AgentMasterCreateRequest;
import my.maleva.api.module.agent.dto.AgentMasterDto;
import my.maleva.api.module.agent.dto.AgentMasterUpdateRequest;
import my.maleva.api.module.agent.entity.AgentMaster;
import my.maleva.api.module.agent.mapper.AgentMasterMapper;
import my.maleva.api.module.agent.repository.AgentMasterRepository;
import my.maleva.api.module.agent.service.AgentMasterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentMasterServiceImpl implements AgentMasterService {

    private final AgentMasterRepository repository;
    private final AgentMasterMapper mapper;

    @Override
    @Transactional
    public AgentMasterDto createAgent(AgentMasterCreateRequest request) {
        log.info("Creating new AgentMaster with location code: {}", request.locationCode());
        
        if (repository.existsByLocationCodeIgnoreCase(request.locationCode())) {
            log.warn("Agent creation failed: Duplicate LocationCode '{}'", request.locationCode());
            throw new InvalidRequestException("Agent with this Location Code already exists.");
        }

        AgentMaster entity = mapper.toEntity(request);
        entity = repository.save(entity);
        
        log.info("Successfully created AgentMaster with ID: {}", entity.getId());
        return mapper.toDto(entity);
    }

    @Override
    @Transactional
    public AgentMasterDto updateAgent(Integer id, AgentMasterUpdateRequest request) {
        log.info("Updating AgentMaster with ID: {}", id);
        
        if (!id.equals(request.id())) {
            throw new IllegalArgumentException("Path ID and Request ID do not match.");
        }
        
        AgentMaster entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AgentMaster not found with id: " + id));

        if (repository.existsByLocationCodeIgnoreCaseAndIdNot(request.locationCode(), id)) {
            log.warn("Agent update failed: Duplicate LocationCode '{}' for ID: {}", request.locationCode(), id);
            throw new InvalidRequestException("Another Agent with this Location Code already exists.");
        }

        mapper.updateEntity(entity, request);
        entity = repository.save(entity);
        
        log.info("Successfully updated AgentMaster with ID: {}", entity.getId());
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public AgentMasterDto getAgentById(Integer id) {
        log.debug("Fetching AgentMaster with ID: {}", id);
        
        AgentMaster entity = repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Active AgentMaster not found with id: " + id));
                
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentMasterDto> getAllAgents(Pageable pageable, String search, Integer companyRefId, Boolean active) {
        log.debug("Fetching all AgentMasters with search criteria");
        
        Specification<AgentMaster> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("agentName")), searchPattern),
                    cb.like(cb.lower(root.get("locationCode")), searchPattern)
                ));
            }
            
            if (companyRefId != null) {
                predicates.add(cb.equal(root.get("companyRefId"), companyRefId));
            }
            
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<AgentMaster> page = repository.findAll(spec, pageable);
        return page.map(mapper::toDto);
    }

    @Override
    @Transactional
    public void softDeleteAgent(Integer id) {
        log.info("Soft deleting AgentMaster with ID: {}", id);
        
        AgentMaster entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AgentMaster not found with id: " + id));
                
        entity.setActive(false);
        repository.save(entity);
        
        log.info("Successfully soft deleted AgentMaster with ID: {}", id);
    }
}
