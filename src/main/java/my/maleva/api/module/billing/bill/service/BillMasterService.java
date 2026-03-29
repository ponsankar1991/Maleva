package my.maleva.api.module.billing.bill.service;

import my.maleva.api.module.billing.bill.dto.BillMasterDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.billing.bill.mapper.BillMasterMapper;
import my.maleva.api.module.billing.bill.entity.BillMaster;
import my.maleva.api.module.billing.bill.repository.BillMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillMasterService {

    private final BillMasterRepository repository;
    private final BillMasterMapper mapper;

    public BillMasterService(BillMasterRepository repository, BillMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<BillMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public BillMasterDto getById(Integer id) {
        BillMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BillMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public BillMasterDto create(BillMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        BillMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        BillMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public BillMasterDto update(Integer id, BillMasterDto dto) {
        BillMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BillMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        BillMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        BillMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BillMaster not found: " + id));
        repository.delete(ent);
    }
}
