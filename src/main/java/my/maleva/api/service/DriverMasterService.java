package my.maleva.api.service;

import my.maleva.api.dto.DriverMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.DriverMasterMapper;
import my.maleva.api.model.DriverMaster;
import my.maleva.api.repo.DriverMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverMasterService {

    private final DriverMasterRepository repository;
    private final DriverMasterMapper mapper;

    public DriverMasterService(DriverMasterRepository repository, DriverMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<DriverMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public DriverMasterDto getById(Integer id) {
        DriverMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("DriverMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public DriverMasterDto create(DriverMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        DriverMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        DriverMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public DriverMasterDto update(Integer id, DriverMasterDto dto) {
        DriverMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("DriverMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        DriverMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        DriverMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("DriverMaster not found: " + id));
        repository.delete(ent);
    }
}
