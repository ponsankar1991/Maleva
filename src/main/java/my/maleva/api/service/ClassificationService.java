package my.maleva.api.service;

import my.maleva.api.dto.ClassificationDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.ClassificationMapper;
import my.maleva.api.model.Classification;
import my.maleva.api.repo.ClassificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassificationService {

    private final ClassificationRepository repository;
    private final ClassificationMapper mapper;

    public ClassificationService(ClassificationRepository repository, ClassificationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ClassificationDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public ClassificationDto getById(Integer id) {
        Classification ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Classification not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public ClassificationDto create(ClassificationDto dto) {
        Classification ent = mapper.toEntity(dto);
        Classification saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public ClassificationDto update(Integer id, ClassificationDto dto) {
        Classification ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Classification not found: " + id));
        mapper.updateFromDto(dto, ent);
        Classification saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        Classification ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Classification not found: " + id));
        repository.delete(ent);
    }
}
