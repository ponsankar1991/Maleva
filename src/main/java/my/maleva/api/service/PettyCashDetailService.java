package my.maleva.api.service;

import my.maleva.api.dto.PettyCashDetailDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.PettyCashDetailMapper;
import my.maleva.api.model.PettyCashDetail;
import my.maleva.api.repo.PettyCashDetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PettyCashDetailService {

    private final PettyCashDetailRepository repository;
    private final PettyCashDetailMapper mapper;

    public PettyCashDetailService(PettyCashDetailRepository repository, PettyCashDetailMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PettyCashDetailDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public PettyCashDetailDto getById(Integer id) {
        PettyCashDetail ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PettyCashDetail not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public PettyCashDetailDto create(PettyCashDetailDto dto) {
        LocalDateTime now = LocalDateTime.now();
        PettyCashDetail ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        PettyCashDetail saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public PettyCashDetailDto update(Integer id, PettyCashDetailDto dto) {
        PettyCashDetail ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PettyCashDetail not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        PettyCashDetail saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        PettyCashDetail ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PettyCashDetail not found: " + id));
        repository.delete(ent);
    }
}
