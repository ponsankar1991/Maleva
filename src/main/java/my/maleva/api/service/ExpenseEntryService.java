package my.maleva.api.service;

import my.maleva.api.dto.ExpenseEntryDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.ExpenseEntryMapper;
import my.maleva.api.model.ExpenseEntry;
import my.maleva.api.repo.ExpenseEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseEntryService {

    private final ExpenseEntryRepository repository;
    private final ExpenseEntryMapper mapper;

    public ExpenseEntryService(ExpenseEntryRepository repository, ExpenseEntryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ExpenseEntryDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public ExpenseEntryDto getById(Integer id) {
        ExpenseEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ExpenseEntry not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public ExpenseEntryDto create(ExpenseEntryDto dto) {
        LocalDateTime now = LocalDateTime.now();
        ExpenseEntry ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        ExpenseEntry saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public ExpenseEntryDto update(Integer id, ExpenseEntryDto dto) {
        ExpenseEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ExpenseEntry not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        ExpenseEntry saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        ExpenseEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ExpenseEntry not found: " + id));
        repository.delete(ent);
    }
}
