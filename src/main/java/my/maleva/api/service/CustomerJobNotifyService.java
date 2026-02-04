package my.maleva.api.service;

import my.maleva.api.dto.CustomerJobNotifyDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.CustomerJobNotifyMapper;
import my.maleva.api.model.CustomerJobNotify;
import my.maleva.api.repo.CustomerJobNotifyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerJobNotifyService {

    private final CustomerJobNotifyRepository repository;
    private final CustomerJobNotifyMapper mapper;

    public CustomerJobNotifyService(CustomerJobNotifyRepository repository, CustomerJobNotifyMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CustomerJobNotifyDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CustomerJobNotifyDto getById(Integer id) {
        CustomerJobNotify ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerJobNotify not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public CustomerJobNotifyDto create(CustomerJobNotifyDto dto) {
        CustomerJobNotify ent = mapper.toEntity(dto);
        CustomerJobNotify saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public CustomerJobNotifyDto update(Integer id, CustomerJobNotifyDto dto) {
        CustomerJobNotify ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerJobNotify not found: " + id));
        mapper.updateFromDto(dto, ent);
        CustomerJobNotify saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        CustomerJobNotify ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerJobNotify not found: " + id));
        repository.delete(ent);
    }
}
