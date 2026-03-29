package my.maleva.api.module.paymentrecept.service;

import my.maleva.api.module.paymentrecept.dto.PaymentReceiptInfoDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.paymentrecept.mapper.PaymentReceiptInfoMapper;
import my.maleva.api.module.paymentrecept.entity.PaymentReceiptInfo;
import my.maleva.api.module.paymentrecept.repository.PaymentReceiptInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentReceiptInfoService {

    private final PaymentReceiptInfoRepository repository;
    private final PaymentReceiptInfoMapper mapper;

    public PaymentReceiptInfoService(PaymentReceiptInfoRepository repository, PaymentReceiptInfoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PaymentReceiptInfoDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public PaymentReceiptInfoDto getById(Integer id) {
        PaymentReceiptInfo ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentReceiptInfo not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public PaymentReceiptInfoDto create(PaymentReceiptInfoDto dto) {
        PaymentReceiptInfo ent = mapper.toEntity(dto);
        ent.setCreatedDate(LocalDateTime.now());
        PaymentReceiptInfo saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentReceiptInfoDto update(Integer id, PaymentReceiptInfoDto dto) {
        PaymentReceiptInfo ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentReceiptInfo not found: " + id));
        mapper.updateFromDto(dto, ent);
        PaymentReceiptInfo saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        PaymentReceiptInfo ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentReceiptInfo not found: " + id));
        repository.delete(ent);
    }
}
