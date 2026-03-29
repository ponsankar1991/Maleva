package my.maleva.api.module.fleet.service;

import my.maleva.api.module.fleet.dto.LicenseMasterDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.fleet.mapper.LicenseMasterMapper;
import my.maleva.api.module.fleet.entity.LicenseMaster;
import my.maleva.api.module.fleet.repository.LicenseMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LicenseMasterService {

    private final LicenseMasterRepository repository;
    private final LicenseMasterMapper mapper;

    public LicenseMasterService(LicenseMasterRepository repository, LicenseMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<LicenseMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public LicenseMasterDto getById(Integer id) {
        LicenseMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("LicenseMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public LicenseMasterDto create(LicenseMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        LicenseMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        LicenseMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public LicenseMasterDto update(Integer id, LicenseMasterDto dto) {
        LicenseMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("LicenseMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        LicenseMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        LicenseMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("LicenseMaster not found: " + id));
        repository.delete(ent);
    }
}
