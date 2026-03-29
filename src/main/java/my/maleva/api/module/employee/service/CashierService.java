package my.maleva.api.module.employee.service;

import my.maleva.api.module.employee.dto.CashierDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.employee.mapper.CashierMapper;
import my.maleva.api.module.employee.entity.Cashier;
import my.maleva.api.module.employee.repository.CashierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashierService {

    private final CashierRepository repository;
    private final CashierMapper mapper;

    public CashierService(CashierRepository repository, CashierMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CashierDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CashierDto getById(Integer id) {
        Cashier ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cashier not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public CashierDto create(CashierDto dto) {
        LocalDateTime now = LocalDateTime.now();
        Cashier ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        Cashier saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public CashierDto update(Integer id, CashierDto dto) {
        Cashier ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cashier not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        Cashier saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        Cashier ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cashier not found: " + id));
        repository.delete(ent);
    }
}
