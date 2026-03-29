package my.maleva.api.module.master.service;

import my.maleva.api.module.master.dto.MSICCodeDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.master.mapper.MSICCodeMapper;
import my.maleva.api.module.master.entity.MSICCode;
import my.maleva.api.module.master.repository.MSICCodeRepository;
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
