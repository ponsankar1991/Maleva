package my.maleva.api.module.qutation.service;

import my.maleva.api.module.qutation.dto.CustomerQuotationGCDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.qutation.mapper.CustomerQuotationGCMapper;
import my.maleva.api.module.qutation.entity.CustomerQuotationGC;
import my.maleva.api.module.qutation.repository.CustomerQuotationGCRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerQuotationGCService {

    private final CustomerQuotationGCRepository repository;
    private final CustomerQuotationGCMapper mapper;

    public CustomerQuotationGCService(CustomerQuotationGCRepository repository, CustomerQuotationGCMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CustomerQuotationGCDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CustomerQuotationGCDto getById(Integer id) {
        CustomerQuotationGC ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotationGC not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public CustomerQuotationGCDto create(CustomerQuotationGCDto dto) {
        LocalDateTime now = LocalDateTime.now();
        CustomerQuotationGC ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        CustomerQuotationGC saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public CustomerQuotationGCDto update(Integer id, CustomerQuotationGCDto dto) {
        CustomerQuotationGC ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotationGC not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        CustomerQuotationGC saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        CustomerQuotationGC ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotationGC not found: " + id));
        repository.delete(ent);
    }
}
