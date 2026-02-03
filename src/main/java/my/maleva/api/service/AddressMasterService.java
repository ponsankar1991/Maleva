package my.maleva.api.service;

import my.maleva.api.dto.AddressMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.AddressMasterMapper;
import my.maleva.api.model.AddressMaster;
import my.maleva.api.repo.AddressMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressMasterService {

    private final AddressMasterRepository repository;
    private final AddressMasterMapper mapper;

    public AddressMasterService(AddressMasterRepository repository, AddressMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<AddressMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public AddressMasterDto getById(Integer id) {
        AddressMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("AddressMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public AddressMasterDto create(AddressMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        AddressMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        AddressMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public AddressMasterDto update(Integer id, AddressMasterDto dto) {
        AddressMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("AddressMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        AddressMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        AddressMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("AddressMaster not found: " + id));
        repository.delete(ent);
    }
}
