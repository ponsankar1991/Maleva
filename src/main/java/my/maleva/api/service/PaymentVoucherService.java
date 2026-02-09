package my.maleva.api.service;

import my.maleva.api.dto.PaymentVoucherDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.PaymentVoucherMapper;
import my.maleva.api.model.PaymentVoucher;
import my.maleva.api.repo.PaymentVoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentVoucherService {

    private final PaymentVoucherRepository repository;
    private final PaymentVoucherMapper mapper;

    public PaymentVoucherService(PaymentVoucherRepository repository, PaymentVoucherMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PaymentVoucherDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public PaymentVoucherDto getById(Integer id) {
        PaymentVoucher ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucher not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public PaymentVoucherDto create(PaymentVoucherDto dto) {
        LocalDateTime now = LocalDateTime.now();
        PaymentVoucher ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        PaymentVoucher saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentVoucherDto update(Integer id, PaymentVoucherDto dto) {
        PaymentVoucher ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucher not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        PaymentVoucher saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        PaymentVoucher ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucher not found: " + id));
        repository.delete(ent);
    }
}
