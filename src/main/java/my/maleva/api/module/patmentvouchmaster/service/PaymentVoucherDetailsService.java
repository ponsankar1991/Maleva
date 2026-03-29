package my.maleva.api.module.patmentvouchmaster.service;

import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherDetailsDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.patmentvouchmaster.mapper.PaymentVoucherDetailsMapper;
import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherDetails;
import my.maleva.api.module.patmentvouchmaster.repository.PaymentVoucherDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentVoucherDetailsService {

    private final PaymentVoucherDetailsRepository repository;
    private final PaymentVoucherDetailsMapper mapper;

    public PaymentVoucherDetailsService(PaymentVoucherDetailsRepository repository, PaymentVoucherDetailsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PaymentVoucherDetailsDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public PaymentVoucherDetailsDto getById(Integer id) {
        PaymentVoucherDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucherDetails not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public PaymentVoucherDetailsDto create(PaymentVoucherDetailsDto dto) {
        LocalDateTime now = LocalDateTime.now();
        PaymentVoucherDetails ent = mapper.toEntity(dto);
        if (ent.getCreatedDate() == null) ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        PaymentVoucherDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentVoucherDetailsDto update(Integer id, PaymentVoucherDetailsDto dto) {
        PaymentVoucherDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucherDetails not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        PaymentVoucherDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        PaymentVoucherDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucherDetails not found: " + id));
        repository.delete(ent);
    }
}
