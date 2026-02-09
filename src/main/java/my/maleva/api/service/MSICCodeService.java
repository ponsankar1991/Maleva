package my.maleva.api.service;

import my.maleva.api.dto.MSICCodeDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.MSICCodeMapper;
import my.maleva.api.model.MSICCode;
import my.maleva.api.repo.MSICCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MSICCodeService {

    private final MSICCodeRepository repository;
    private final MSICCodeMapper mapper;

    public MSICCodeService(MSICCodeRepository repository, MSICCodeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<MSICCodeDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public MSICCodeDto getById(Integer id) {
        MSICCode ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MSICCode not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public MSICCodeDto create(MSICCodeDto dto) {
        MSICCode ent = mapper.toEntity(dto);
        MSICCode saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public MSICCodeDto update(Integer id, MSICCodeDto dto) {
        MSICCode ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MSICCode not found: " + id));
        mapper.updateFromDto(dto, ent);
        MSICCode saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        MSICCode ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MSICCode not found: " + id));
        repository.delete(ent);
    }
}
