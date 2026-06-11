package my.maleva.api.module.master.service;

import my.maleva.api.module.master.dto.AddressMasterDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.master.mapper.AddressMasterMapper;
import my.maleva.api.module.master.entity.AddressMaster;
import my.maleva.api.module.master.repository.AddressMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressMasterService {

    private final AddressMasterRepository repository;
    private final AddressMasterMapper mapper;

    public AddressMasterService(AddressMasterRepository repository, AddressMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Cacheable(value = "addresses", key = "'all'")
    public List<AddressMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Cacheable(value = "addresses", key = "'id_' + #id")
    public AddressMasterDto getById(Integer id) {
        AddressMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("AddressMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    @CacheEvict(value = "addresses", allEntries = true)
    public AddressMasterDto create(AddressMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        AddressMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        AddressMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    @CacheEvict(value = "addresses", allEntries = true)
    public AddressMasterDto update(Integer id, AddressMasterDto dto) {
        AddressMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("AddressMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        AddressMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    @CacheEvict(value = "addresses", allEntries = true)
    public void delete(Integer id) {
        AddressMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("AddressMaster not found: " + id));
        repository.delete(ent);
    }

    /**
     * Search addresses by company ID and keyword (name contains)
     * Returns only active addresses (active != 2)
     * Results are ordered by name
     * Equivalent to legacy .NET SelectAddress method
     *
     * @param companyRefId the company ID
     * @param keyword the search keyword (can be null or empty)
     * @return list of matching active addresses ordered by name
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "addresses", key = "'search_' + #companyRefId + '_' + (#keyword != null ? #keyword : 'ALL')")
    public List<AddressMasterDto> searchAddresses(Integer companyRefId, String keyword) {
        List<AddressMaster> result;

        // If keyword is empty or null, get all active addresses for company
        if (keyword == null || keyword.trim().isEmpty()) {
            result = repository.findActiveByCompanyId(companyRefId);
        } else {
            // Otherwise search by keyword
            result = repository.findByCompanyAndKeyword(companyRefId, keyword.trim());
        }

        // Map to DTOs and return ordered by name
        return result.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all active addresses for a company
     * Returns only active addresses (active != 2)
     * Results are ordered by name
     *
     * @param companyRefId the company ID
     * @return list of active addresses ordered by name
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "addresses", key = "'active_company_' + #companyRefId")
    public List<AddressMasterDto> getActiveAddressesByCompany(Integer companyRefId) {
        return repository.findActiveByCompanyId(companyRefId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
