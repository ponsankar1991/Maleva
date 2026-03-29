package my.maleva.api.module.company.service;

import my.maleva.api.module.company.dto.AgentDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.company.mapper.AgentMapper;
import my.maleva.api.module.company.entity.Agent;
import my.maleva.api.module.company.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgentService {

    private final AgentRepository repository;
    private final AgentMapper mapper;

    public AgentService(AgentRepository repository, AgentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<AgentDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public AgentDto getById(Integer id) {
        Agent ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Agent not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public AgentDto create(AgentDto dto) {
        LocalDateTime now = LocalDateTime.now();
        Agent ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        Agent saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public AgentDto update(Integer id, AgentDto dto) {
        Agent ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Agent not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        Agent saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        Agent ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Agent not found: " + id));
        repository.delete(ent);
    }

    /**
     * Select all agents for a company with optional filtering by AgentCompanyRefId.
     * Equivalent to the .NET SelectAgentAll method.
     *
     * SQL Equivalent:
     * SELECT S.*, A.Name as SName, Ag.AccountCode
     * FROM Agent S
     * INNER JOIN AgentCompanyMaster A ON S.AgentCompanyRefId = A.Id
     * INNER JOIN AccountsGroupMaster Ag ON Ag.Id = S.AccountRefId
     * WHERE S.CompanyRefId = :companyRefId AND S.Active != 2
     * [AND S.AgentCompanyRefId = :jobId IF jobId != 0]
     * ORDER BY S.AgentName ASC
     *
     * @param companyRefId The company reference ID (required, > 0)
     * @param jobId The agent company reference ID for filtering (optional, 0 means no filter)
     * @return List of AgentDtos sorted by agentName, filtered by Active != 2
     */
    public List<AgentDto> selectAgentAll(Integer companyRefId, Integer jobId) {
        List<Agent> agents;

        // Apply conditional filtering based on jobId
        if (jobId != null && jobId != 0) {
            // Filter by both companyRefId and agentCompanyRefId (jobId)
            agents = repository.findByCompanyRefIdAndAgentCompanyRefIdActiveNot2(companyRefId, jobId);
        } else {
            // Filter by companyRefId only
            agents = repository.findByCompanyRefIdActiveNot2(companyRefId);
        }
        // Map entities to DTOs and return (already sorted in query)
        return agents.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
