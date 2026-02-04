package my.maleva.api.service;

import my.maleva.api.dto.BillsOrderMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.BillsOrderMasterMapper;
import my.maleva.api.model.BillsOrderMaster;
import my.maleva.api.repo.BillsOrderMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillsOrderMasterService {

    private final BillsOrderMasterRepository repository;
    private final BillsOrderMasterMapper mapper;

    public BillsOrderMasterService(BillsOrderMasterRepository repository, BillsOrderMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<BillsOrderMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public BillsOrderMasterDto getById(Integer id) {
        BillsOrderMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BillsOrderMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public BillsOrderMasterDto create(BillsOrderMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        BillsOrderMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        BillsOrderMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public BillsOrderMasterDto update(Integer id, BillsOrderMasterDto dto) {
        BillsOrderMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BillsOrderMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        BillsOrderMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        BillsOrderMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BillsOrderMaster not found: " + id));
        repository.delete(ent);
    }
}
