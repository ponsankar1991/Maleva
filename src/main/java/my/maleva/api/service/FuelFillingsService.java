package my.maleva.api.service;

import my.maleva.api.dto.FuelFillingsDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.FuelFillingsMapper;
import my.maleva.api.model.FuelFillings;
import my.maleva.api.repo.FuelFillingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FuelFillingsService {

    private final FuelFillingsRepository repository;
    private final FuelFillingsMapper mapper;

    public FuelFillingsService(FuelFillingsRepository repository, FuelFillingsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<FuelFillingsDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public FuelFillingsDto getById(Integer id) {
        FuelFillings ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("FuelFillings not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public FuelFillingsDto create(FuelFillingsDto dto) {
        LocalDateTime now = LocalDateTime.now();
        FuelFillings ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        FuelFillings saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public FuelFillingsDto update(Integer id, FuelFillingsDto dto) {
        FuelFillings ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("FuelFillings not found: " + id));
        mapper.updateFromDto(dto, ent);
        FuelFillings saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        FuelFillings ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("FuelFillings not found: " + id));
        repository.delete(ent);
    }
}
