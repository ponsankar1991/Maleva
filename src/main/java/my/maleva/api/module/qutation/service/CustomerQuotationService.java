package my.maleva.api.module.qutation.service;

import my.maleva.api.module.qutation.dto.CustomerQuotationDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.qutation.mapper.CustomerQuotationMapper;
import my.maleva.api.module.qutation.entity.CustomerQuotation;
import my.maleva.api.module.qutation.repository.CustomerQuotationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerQuotationService {

    private final CustomerQuotationRepository repository;
    private final CustomerQuotationMapper mapper;

    public CustomerQuotationService(CustomerQuotationRepository repository, CustomerQuotationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CustomerQuotationDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CustomerQuotationDto getById(Integer id) {
        CustomerQuotation ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotation not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public CustomerQuotationDto create(CustomerQuotationDto dto) {
        LocalDateTime now = LocalDateTime.now();
        CustomerQuotation ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        CustomerQuotation saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public CustomerQuotationDto update(Integer id, CustomerQuotationDto dto) {
        CustomerQuotation ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotation not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        CustomerQuotation saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        CustomerQuotation ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotation not found: " + id));
        repository.delete(ent);
    }
}
