package my.maleva.api.service;

import my.maleva.api.dto.PaymentTermsMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.PaymentTermsMasterMapper;
import my.maleva.api.model.PaymentTermsMaster;
import my.maleva.api.repo.PaymentTermsMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentTermsMasterService {

    private final PaymentTermsMasterRepository repository;
    private final PaymentTermsMasterMapper mapper;

    public PaymentTermsMasterService(PaymentTermsMasterRepository repository, PaymentTermsMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PaymentTermsMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public PaymentTermsMasterDto getById(Integer id) {
        PaymentTermsMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentTermsMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public PaymentTermsMasterDto create(PaymentTermsMasterDto dto) {
        PaymentTermsMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(LocalDateTime.now());
        ent.setModifiedDate(LocalDateTime.now());
        PaymentTermsMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentTermsMasterDto update(Integer id, PaymentTermsMasterDto dto) {
        PaymentTermsMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentTermsMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        PaymentTermsMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        PaymentTermsMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentTermsMaster not found: " + id));
        repository.delete(ent);
    }
}
