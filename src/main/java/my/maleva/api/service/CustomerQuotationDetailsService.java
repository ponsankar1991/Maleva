package my.maleva.api.service;

import my.maleva.api.dto.CustomerQuotationDetailsDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.CustomerQuotationDetailsMapper;
import my.maleva.api.model.CustomerQuotationDetails;
import my.maleva.api.repo.CustomerQuotationDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerQuotationDetailsService {

    private final CustomerQuotationDetailsRepository repository;
    private final CustomerQuotationDetailsMapper mapper;

    public CustomerQuotationDetailsService(CustomerQuotationDetailsRepository repository, CustomerQuotationDetailsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CustomerQuotationDetailsDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CustomerQuotationDetailsDto getById(Integer id) {
        CustomerQuotationDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotationDetails not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public CustomerQuotationDetailsDto create(CustomerQuotationDetailsDto dto) {
        LocalDateTime now = LocalDateTime.now();
        CustomerQuotationDetails ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        CustomerQuotationDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public CustomerQuotationDetailsDto update(Integer id, CustomerQuotationDetailsDto dto) {
        CustomerQuotationDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotationDetails not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        CustomerQuotationDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        CustomerQuotationDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotationDetails not found: " + id));
        repository.delete(ent);
    }
}
