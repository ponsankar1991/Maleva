package my.maleva.api.service;

import my.maleva.api.dto.MENUMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.MENUMasterMapper;
import my.maleva.api.model.MENUMaster;
import my.maleva.api.repo.MENUMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MENUMasterService {

    private final MENUMasterRepository repository;
    private final MENUMasterMapper mapper;

    public MENUMasterService(MENUMasterRepository repository, MENUMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<MENUMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public MENUMasterDto getById(Integer id) {
        MENUMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MENUMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public MENUMasterDto create(MENUMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        MENUMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        MENUMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public MENUMasterDto update(Integer id, MENUMasterDto dto) {
        MENUMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MENUMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        MENUMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        MENUMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MENUMaster not found: " + id));
        repository.delete(ent);
    }
}
