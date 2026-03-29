package my.maleva.api.module.fleet.service;

import my.maleva.api.module.fleet.dto.FuelEntryDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.fleet.mapper.FuelEntryMapper;
import my.maleva.api.module.fleet.entity.FuelEntry;
import my.maleva.api.module.fleet.repository.FuelEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FuelEntryService {

    private final FuelEntryRepository repository;
    private final FuelEntryMapper mapper;

    public FuelEntryService(FuelEntryRepository repository, FuelEntryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<FuelEntryDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public FuelEntryDto getById(Integer id) {
        FuelEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("FuelEntry not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public FuelEntryDto create(FuelEntryDto dto) {
        LocalDateTime now = LocalDateTime.now();
        FuelEntry ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        FuelEntry saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public FuelEntryDto update(Integer id, FuelEntryDto dto) {
        FuelEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("FuelEntry not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        FuelEntry saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        FuelEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("FuelEntry not found: " + id));
        repository.delete(ent);
    }
}
