package my.maleva.api.service;

import my.maleva.api.dto.DoMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.DoMasterMapper;
import my.maleva.api.model.DoMaster;
import my.maleva.api.repo.DoMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoMasterService {

    private final DoMasterRepository repository;
    private final DoMasterMapper mapper;

    public DoMasterService(DoMasterRepository repository, DoMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<DoMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public DoMasterDto getById(Integer id) {
        DoMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("DoMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public DoMasterDto create(DoMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        DoMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        DoMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public DoMasterDto update(Integer id, DoMasterDto dto) {
        DoMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("DoMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        DoMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        DoMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("DoMaster not found: " + id));
        repository.delete(ent);
    }
}
