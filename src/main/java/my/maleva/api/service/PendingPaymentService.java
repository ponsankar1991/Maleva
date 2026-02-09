package my.maleva.api.service;

import my.maleva.api.dto.PendingPaymentDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.PendingPaymentMapper;
import my.maleva.api.model.PendingPayment;
import my.maleva.api.repo.PendingPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PendingPaymentService {

    private final PendingPaymentRepository repository;
    private final PendingPaymentMapper mapper;

    public PendingPaymentService(PendingPaymentRepository repository, PendingPaymentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PendingPaymentDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public PendingPaymentDto getById(Integer id) {
        PendingPayment ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PendingPayment not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public PendingPaymentDto create(PendingPaymentDto dto) {
        PendingPayment ent = mapper.toEntity(dto);
        if (ent.getCreatedDate() == null) ent.setCreatedDate(LocalDateTime.now());
        PendingPayment saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public PendingPaymentDto update(Integer id, PendingPaymentDto dto) {
        PendingPayment ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PendingPayment not found: " + id));
        mapper.updateFromDto(dto, ent);
        PendingPayment saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        PendingPayment ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PendingPayment not found: " + id));
        repository.delete(ent);
    }
}
