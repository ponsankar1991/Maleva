package my.maleva.api.module.pettycash.service;

import my.maleva.api.module.pettycash.dto.PettyCashMasterDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.pettycash.mapper.PettyCashMasterMapper;
import my.maleva.api.module.pettycash.entity.PettyCashMaster;
import my.maleva.api.module.pettycash.repository.PettyCashMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PettyCashMasterService {

    private final PettyCashMasterRepository repository;
    private final PettyCashMasterMapper mapper;

    public PettyCashMasterService(PettyCashMasterRepository repository, PettyCashMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PettyCashMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public PettyCashMasterDto getById(Integer id) {
        PettyCashMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PettyCashMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public PettyCashMasterDto create(PettyCashMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        PettyCashMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        PettyCashMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public PettyCashMasterDto update(Integer id, PettyCashMasterDto dto) {
        PettyCashMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PettyCashMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        PettyCashMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        PettyCashMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PettyCashMaster not found: " + id));
        repository.delete(ent);
    }
}
