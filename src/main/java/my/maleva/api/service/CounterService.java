package my.maleva.api.service;

import my.maleva.api.dto.CounterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.CounterMapper;
import my.maleva.api.model.Counter;
import my.maleva.api.repo.CounterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CounterService {

    private final CounterRepository repository;
    private final CounterMapper mapper;

    public CounterService(CounterRepository repository, CounterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CounterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CounterDto getById(Integer id) {
        Counter ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Counter not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public CounterDto create(CounterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        Counter ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        Counter saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public CounterDto update(Integer id, CounterDto dto) {
        Counter ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Counter not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        Counter saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        Counter ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Counter not found: " + id));
        repository.delete(ent);
    }
}
