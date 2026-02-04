package my.maleva.api.service;

import my.maleva.api.dto.CompanyDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.CompanyMapper;
import my.maleva.api.model.Company;
import my.maleva.api.repo.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    private final CompanyRepository repository;
    private final CompanyMapper mapper;

    public CompanyService(CompanyRepository repository, CompanyMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CompanyDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CompanyDto getById(Integer id) {
        Company ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Company not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public CompanyDto create(CompanyDto dto) {
        LocalDateTime now = LocalDateTime.now();
        Company ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        Company saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public CompanyDto update(Integer id, CompanyDto dto) {
        Company ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Company not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        Company saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        Company ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Company not found: " + id));
        repository.delete(ent);
    }
}
