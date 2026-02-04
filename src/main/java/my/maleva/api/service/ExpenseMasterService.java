package my.maleva.api.service;

import my.maleva.api.dto.ExpenseMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.ExpenseMasterMapper;
import my.maleva.api.model.ExpenseMaster;
import my.maleva.api.repo.ExpenseMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseMasterService {

    private final ExpenseMasterRepository repository;
    private final ExpenseMasterMapper mapper;

    public ExpenseMasterService(ExpenseMasterRepository repository, ExpenseMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ExpenseMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public ExpenseMasterDto getById(Integer id) {
        ExpenseMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ExpenseMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public ExpenseMasterDto create(ExpenseMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        ExpenseMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        ExpenseMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public ExpenseMasterDto update(Integer id, ExpenseMasterDto dto) {
        ExpenseMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ExpenseMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        ExpenseMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        ExpenseMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ExpenseMaster not found: " + id));
        repository.delete(ent);
    }
}
