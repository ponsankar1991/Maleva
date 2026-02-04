package my.maleva.api.service;

import my.maleva.api.dto.ForwardingSalaryDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.ForwardingSalaryMapper;
import my.maleva.api.model.ForwardingSalary;
import my.maleva.api.repo.ForwardingSalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForwardingSalaryService {

    private final ForwardingSalaryRepository repository;
    private final ForwardingSalaryMapper mapper;

    public ForwardingSalaryService(ForwardingSalaryRepository repository, ForwardingSalaryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ForwardingSalaryDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public ForwardingSalaryDto getById(Integer id) {
        ForwardingSalary ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ForwardingSalary not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public ForwardingSalaryDto create(ForwardingSalaryDto dto) {
        ForwardingSalary ent = mapper.toEntity(dto);
        ForwardingSalary saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public ForwardingSalaryDto update(Integer id, ForwardingSalaryDto dto) {
        ForwardingSalary ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ForwardingSalary not found: " + id));
        mapper.updateFromDto(dto, ent);
        ForwardingSalary saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        ForwardingSalary ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ForwardingSalary not found: " + id));
        repository.delete(ent);
    }
}
