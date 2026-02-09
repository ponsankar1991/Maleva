package my.maleva.api.service;

import my.maleva.api.dto.PaymentDetailsDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.PaymentDetailsMapper;
import my.maleva.api.model.PaymentDetails;
import my.maleva.api.repo.PaymentDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentDetailsService {

    private final PaymentDetailsRepository repository;
    private final PaymentDetailsMapper mapper;

    public PaymentDetailsService(PaymentDetailsRepository repository, PaymentDetailsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PaymentDetailsDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public PaymentDetailsDto getById(Integer id) {
        PaymentDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentDetails not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public PaymentDetailsDto create(PaymentDetailsDto dto) {
        PaymentDetails ent = mapper.toEntity(dto);
        if (ent.getCreatedDate() == null) ent.setCreatedDate(LocalDateTime.now());
        PaymentDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentDetailsDto update(Integer id, PaymentDetailsDto dto) {
        PaymentDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentDetails not found: " + id));
        mapper.updateFromDto(dto, ent);
        PaymentDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        PaymentDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentDetails not found: " + id));
        repository.delete(ent);
    }
}
