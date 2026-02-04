package my.maleva.api.service;

import my.maleva.api.dto.BankMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.BankMasterMapper;
import my.maleva.api.model.BankMaster;
import my.maleva.api.repo.BankMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BankMasterService {

    private final BankMasterRepository repository;
    private final BankMasterMapper mapper;

    public BankMasterService(BankMasterRepository repository, BankMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<BankMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public BankMasterDto getById(Integer id) {
        BankMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BankMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public BankMasterDto create(BankMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        BankMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        BankMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public BankMasterDto update(Integer id, BankMasterDto dto) {
        BankMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BankMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        BankMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        BankMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BankMaster not found: " + id));
        repository.delete(ent);
    }
}
