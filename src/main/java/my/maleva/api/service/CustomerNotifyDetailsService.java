package my.maleva.api.service;

import my.maleva.api.dto.CustomerNotifyDetailsDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.CustomerNotifyDetailsMapper;
import my.maleva.api.model.CustomerNotifyDetails;
import my.maleva.api.repo.CustomerNotifyDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerNotifyDetailsService {

    private final CustomerNotifyDetailsRepository repository;
    private final CustomerNotifyDetailsMapper mapper;

    public CustomerNotifyDetailsService(CustomerNotifyDetailsRepository repository, CustomerNotifyDetailsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CustomerNotifyDetailsDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CustomerNotifyDetailsDto getById(Integer id) {
        CustomerNotifyDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerNotifyDetails not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public CustomerNotifyDetailsDto create(CustomerNotifyDetailsDto dto) {
        LocalDateTime now = LocalDateTime.now();
        CustomerNotifyDetails ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        CustomerNotifyDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public CustomerNotifyDetailsDto update(Integer id, CustomerNotifyDetailsDto dto) {
        CustomerNotifyDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerNotifyDetails not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        CustomerNotifyDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        CustomerNotifyDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerNotifyDetails not found: " + id));
        repository.delete(ent);
    }
}
