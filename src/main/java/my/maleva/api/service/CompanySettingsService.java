package my.maleva.api.service;

import my.maleva.api.dto.CompanySettingsDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.CompanySettingsMapper;
import my.maleva.api.model.CompanySettings;
import my.maleva.api.repo.CompanySettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanySettingsService {

    private final CompanySettingsRepository repository;
    private final CompanySettingsMapper mapper;

    public CompanySettingsService(CompanySettingsRepository repository, CompanySettingsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CompanySettingsDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CompanySettingsDto getById(Integer id) {
        CompanySettings ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CompanySettings not found: " + id));
        return mapper.toDto(ent);
    }

    public CompanySettingsDto getByCompanyRefId(Integer companyRefId) {
        CompanySettings ent = repository.findByCompanyRefId(companyRefId).orElseThrow(() -> new EntityNotFoundException("CompanySettings not found for company: " + companyRefId));
        return mapper.toDto(ent);
    }

    @Transactional
    public CompanySettingsDto create(CompanySettingsDto dto) {
        LocalDateTime now = LocalDateTime.now();
        CompanySettings ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        CompanySettings saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public CompanySettingsDto update(Integer id, CompanySettingsDto dto) {
        CompanySettings ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CompanySettings not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        CompanySettings saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        CompanySettings ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CompanySettings not found: " + id));
        repository.delete(ent);
    }
}
