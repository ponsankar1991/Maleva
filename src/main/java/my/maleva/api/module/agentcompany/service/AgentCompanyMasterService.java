package my.maleva.api.module.agentcompany.service;

import my.maleva.api.module.agentcompany.dto.AgentCompanyMasterDTO;
import my.maleva.api.module.agentcompany.entity.AgentCompanyMaster;
import my.maleva.api.module.agentcompany.repository.AgentCompanyMasterRepository;
import my.maleva.api.module.agentcompany.mapper.AgentCompanyMasterMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for AgentCompanyMaster business logic.
 * Implements CRUD operations and stored procedure business logic.
 */
@Service
public class AgentCompanyMasterService {

    private final AgentCompanyMasterRepository repository;
    private final AgentCompanyMasterMapper mapper;

    public AgentCompanyMasterService(
            AgentCompanyMasterRepository repository,
            AgentCompanyMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Retrieve all agent companies (active != 2).
     * @return List of AgentCompanyMasterDTO
     */
    public List<AgentCompanyMasterDTO> getAllAgentCompanies() {
        return repository.findByActiveNot(2).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all agent companies by CompanyRefId (active != 2).
     * @param companyRefId The company reference ID
     * @return List of AgentCompanyMasterDTO
     */
    public List<AgentCompanyMasterDTO> getAgentCompaniesByCompanyRefId(Integer companyRefId) {
        return repository.findByCompanyRefIdAndActiveNot(companyRefId, 2).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a single agent company by ID.
     * @param id The agent company ID
     * @return AgentCompanyMasterDTO
     */
    public AgentCompanyMasterDTO getAgentCompanyById(Long id) {
        AgentCompanyMaster entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agent Company not found: " + id));
        return mapper.toDto(entity);
    }

    /**
     * Create a new agent company.
     * Implements business logic from SP_AgentCompany stored procedure.
     * @param dto The DTO containing agent company data
     * @return Created AgentCompanyMasterDTO
     */
    @Transactional
    public AgentCompanyMasterDTO createAgentCompany(AgentCompanyMasterDTO dto) {
        // Validate input
        if (dto.getCompanyRefId() == null || dto.getCompanyRefId() <= 0) {
            throw new InvalidRequestException("CompanyRefId must be a valid positive integer");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new InvalidRequestException("Name is required");
        }

        // Business Logic from SP_AgentCompany:
        // Check if agent company with same CompanyRefId and Name already exists with Active=1
        List<AgentCompanyMaster> existing = repository.findByCompanyRefIdAndNameAndActive(
                dto.getCompanyRefId(),
                dto.getName().trim(),
                1
        );

        AgentCompanyMaster entity;
        if (existing.isEmpty()) {
            // Insert new record
            entity = mapper.toEntity(dto);
            entity.setCreatedDate(LocalDateTime.now());
            entity.setModifiedDate(LocalDateTime.now());
            entity.setDFlag(dto.getDFlag() != null ? dto.getDFlag() : 0);
            entity.setActive(dto.getActive() != null ? dto.getActive() : 1);
        } else {
            // If exists with Active=1, use the existing ID but update the record
            entity = existing.get(0);
            mapper.updateEntityFromDto(dto, entity);
            entity.setModifiedDate(LocalDateTime.now());
        }

        AgentCompanyMaster saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    /**
     * Bulk upsert agent companies.
     * Implements the core logic of SP_AgentCompany stored procedure.
     * For each record: if exists with CompanyRefId + Name + Active=1, update; otherwise insert.
     * @param companyRefId The company reference ID
     * @param dtos List of DTOs to upsert
     * @return List of saved DTOs
     */
    @Transactional
    public List<AgentCompanyMasterDTO> upsertAgentCompanies(Integer companyRefId, List<AgentCompanyMasterDTO> dtos) {
        if (companyRefId == null || companyRefId <= 0) {
            throw new InvalidRequestException("CompanyRefId must be a valid positive integer");
        }
        if (dtos == null || dtos.isEmpty()) {
            throw new InvalidRequestException("Agent companies list cannot be empty");
        }

        LocalDateTime now = LocalDateTime.now();
        List<AgentCompanyMaster> result = dtos.stream().map(dto -> {
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                throw new InvalidRequestException("Name is required for all records");
            }

            // Implement SP_AgentCompany logic:
            // If Id=0, check if exists by CompanyRefId+Name+Active=1
            List<AgentCompanyMaster> existing = repository.findByCompanyRefIdAndNameAndActive(
                    companyRefId,
                    dto.getName().trim(),
                    1
            );

            AgentCompanyMaster entity;
            if (existing.isEmpty()) {
                // Insert new record
                entity = new AgentCompanyMaster();
                entity.setCompanyRefId(companyRefId);
                entity.setName(dto.getName().trim());
                entity.setDFlag(dto.getDFlag() != null ? dto.getDFlag() : 0);
                entity.setActive(dto.getActive() != null ? dto.getActive() : 1);
                entity.setCreatedDate(now);
                entity.setModifiedDate(now);
                entity.setModifiedBy(System.getProperty("user.name", "SYSTEM"));
            } else {
                // Update existing record
                entity = existing.get(0);
                entity.setName(dto.getName().trim());
                entity.setDFlag(dto.getDFlag() != null ? dto.getDFlag() : entity.getDFlag());
                entity.setActive(dto.getActive() != null ? dto.getActive() : entity.getActive());
                entity.setModifiedDate(now);
                entity.setModifiedBy(System.getProperty("user.name", "SYSTEM"));
            }

            return repository.save(entity);
        }).collect(Collectors.toList());

        return result.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    /**
     * Update an existing agent company.
     * @param id The agent company ID
     * @param dto The DTO with updated data
     * @return Updated AgentCompanyMasterDTO
     */
    @Transactional
    public AgentCompanyMasterDTO updateAgentCompany(Long id, AgentCompanyMasterDTO dto) {
        AgentCompanyMaster entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agent Company not found: " + id));

        mapper.updateEntityFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        AgentCompanyMaster saved = repository.save(entity);

        return mapper.toDto(saved);
    }

    /**
     * Delete an agent company by ID (soft delete via Active flag).
     * @param id The agent company ID
     */
    @Transactional
    public void deleteAgentCompany(Long id) {
        AgentCompanyMaster entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agent Company not found: " + id));

        // Soft delete: set Active = 2 (or 0)
        entity.setActive(2);
        entity.setModifiedDate(LocalDateTime.now());
        repository.save(entity);
    }

    /**
     * Search agent companies by company reference ID and optional filters.
     * Filters: Active != 2
     * @param companyRefId The company reference ID
     * @return List of matching DTOs
     */
    public List<AgentCompanyMasterDTO> searchByCompanyRefId(Integer companyRefId) {
        return getAgentCompaniesByCompanyRefId(companyRefId);
    }

    /**
     * Custom exception for entity not found
     */
    public static class EntityNotFoundException extends RuntimeException {
        public EntityNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Custom exception for invalid request
     */
    public static class InvalidRequestException extends RuntimeException {
        public InvalidRequestException(String message) {
            super(message);
        }
    }
}

