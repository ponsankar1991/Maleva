package my.maleva.api.module.patmentvouchmaster.service;

import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherMasterDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.patmentvouchmaster.mapper.PaymentVoucherMasterMapper;
import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherMaster;
import my.maleva.api.module.patmentvouchmaster.repository.PaymentVoucherMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentVoucherMasterService {

    private final PaymentVoucherMasterRepository repository;
    private final PaymentVoucherMasterMapper mapper;

    public PaymentVoucherMasterService(PaymentVoucherMasterRepository repository, PaymentVoucherMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PaymentVoucherMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public PaymentVoucherMasterDto getById(Integer id) {
        PaymentVoucherMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucherMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public PaymentVoucherMasterDto create(PaymentVoucherMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        PaymentVoucherMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        PaymentVoucherMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentVoucherMasterDto update(Integer id, PaymentVoucherMasterDto dto) {
        PaymentVoucherMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucherMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        PaymentVoucherMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        PaymentVoucherMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucherMaster not found: " + id));
        repository.delete(ent);
    }
}
