package my.maleva.api.service;

import my.maleva.api.dto.EngineHoursDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.EngineHoursMapper;
import my.maleva.api.model.EngineHours;
import my.maleva.api.repo.EngineHoursRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EngineHoursService {

    private final EngineHoursRepository repository;
    private final EngineHoursMapper mapper;

    public EngineHoursService(EngineHoursRepository repository, EngineHoursMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<EngineHoursDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public EngineHoursDto getById(Integer id) {
        EngineHours ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("EngineHours not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public EngineHoursDto create(EngineHoursDto dto) {
        LocalDateTime now = LocalDateTime.now();
        EngineHours ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        EngineHours saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public EngineHoursDto update(Integer id, EngineHoursDto dto) {
        EngineHours ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("EngineHours not found: " + id));
        mapper.updateFromDto(dto, ent);
        EngineHours saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        EngineHours ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("EngineHours not found: " + id));
        repository.delete(ent);
    }
}
