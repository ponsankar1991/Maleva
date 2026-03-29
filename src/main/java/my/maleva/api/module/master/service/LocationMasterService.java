package my.maleva.api.module.master.service;

import my.maleva.api.module.master.dto.LocationMasterDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.master.mapper.LocationMasterMapper;
import my.maleva.api.module.master.entity.LocationMaster;
import my.maleva.api.module.master.repository.LocationMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationMasterService {

    private final LocationMasterRepository repository;
    private final LocationMasterMapper mapper;

    public LocationMasterService(LocationMasterRepository repository, LocationMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<LocationMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public LocationMasterDto getById(Integer id) {
        LocationMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("LocationMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public LocationMasterDto create(LocationMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        LocationMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        LocationMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public LocationMasterDto update(Integer id, LocationMasterDto dto) {
        LocationMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("LocationMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        LocationMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        LocationMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("LocationMaster not found: " + id));
        repository.delete(ent);
    }
}
