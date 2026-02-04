package my.maleva.api.service;

import my.maleva.api.dto.CustomerQuotationMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.CustomerQuotationMasterMapper;
import my.maleva.api.model.CustomerQuotationMaster;
import my.maleva.api.repo.CustomerQuotationMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerQuotationMasterService {

    private final CustomerQuotationMasterRepository repository;
    private final CustomerQuotationMasterMapper mapper;

    public CustomerQuotationMasterService(CustomerQuotationMasterRepository repository, CustomerQuotationMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CustomerQuotationMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CustomerQuotationMasterDto getById(Integer id) {
        CustomerQuotationMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotationMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public CustomerQuotationMasterDto create(CustomerQuotationMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        CustomerQuotationMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        CustomerQuotationMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public CustomerQuotationMasterDto update(Integer id, CustomerQuotationMasterDto dto) {
        CustomerQuotationMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotationMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        CustomerQuotationMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        CustomerQuotationMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotationMaster not found: " + id));
        repository.delete(ent);
    }
}
