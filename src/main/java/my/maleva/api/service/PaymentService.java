package my.maleva.api.service;

import my.maleva.api.dto.PaymentDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.PaymentMapper;
import my.maleva.api.model.Payment;
import my.maleva.api.repo.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;

    public PaymentService(PaymentRepository repository, PaymentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PaymentDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public PaymentDto getById(Integer id) {
        Payment ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public PaymentDto create(PaymentDto dto) {
        LocalDateTime now = LocalDateTime.now();
        Payment ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        Payment saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentDto update(Integer id, PaymentDto dto) {
        Payment ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        Payment saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        Payment ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));
        repository.delete(ent);
    }
}
