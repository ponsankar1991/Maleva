package my.maleva.api.service;

import my.maleva.api.dto.AutoPassEntryDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.AutoPassEntryMapper;
import my.maleva.api.model.AutoPassEntry;
import my.maleva.api.repo.AutoPassEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AutoPassEntryService {

    private final AutoPassEntryRepository repository;
    private final AutoPassEntryMapper mapper;

    public AutoPassEntryService(AutoPassEntryRepository repository, AutoPassEntryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<AutoPassEntryDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public AutoPassEntryDto getById(Integer id) {
        AutoPassEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("AutoPassEntry not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public AutoPassEntryDto create(AutoPassEntryDto dto) {
        LocalDateTime now = LocalDateTime.now();
        AutoPassEntry ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        AutoPassEntry saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public AutoPassEntryDto update(Integer id, AutoPassEntryDto dto) {
        AutoPassEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("AutoPassEntry not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        AutoPassEntry saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        AutoPassEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("AutoPassEntry not found: " + id));
        repository.delete(ent);
    }
}
