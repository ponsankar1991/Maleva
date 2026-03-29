package my.maleva.api.module.fleet.service;

import my.maleva.api.module.fleet.dto.LeviEntryDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.fleet.mapper.LeviEntryMapper;
import my.maleva.api.module.fleet.entity.LeviEntry;
import my.maleva.api.module.fleet.repository.LeviEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeviEntryService {

    private final LeviEntryRepository repository;
    private final LeviEntryMapper mapper;

    public LeviEntryService(LeviEntryRepository repository, LeviEntryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<LeviEntryDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public LeviEntryDto getById(Integer id) {
        LeviEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("LeviEntry not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public LeviEntryDto create(LeviEntryDto dto) {
        LocalDateTime now = LocalDateTime.now();
        LeviEntry ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        LeviEntry saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public LeviEntryDto update(Integer id, LeviEntryDto dto) {
        LeviEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("LeviEntry not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        LeviEntry saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        LeviEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("LeviEntry not found: " + id));
        repository.delete(ent);
    }
}
