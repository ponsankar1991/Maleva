package my.maleva.api.service;

import my.maleva.api.dto.BillsOrderDetailsDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.BillsOrderDetailsMapper;
import my.maleva.api.model.BillsOrderDetails;
import my.maleva.api.repo.BillsOrderDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillsOrderDetailsService {

    private final BillsOrderDetailsRepository repository;
    private final BillsOrderDetailsMapper mapper;

    public BillsOrderDetailsService(BillsOrderDetailsRepository repository, BillsOrderDetailsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<BillsOrderDetailsDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public BillsOrderDetailsDto getById(Integer id) {
        BillsOrderDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BillsOrderDetails not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public BillsOrderDetailsDto create(BillsOrderDetailsDto dto) {
        LocalDateTime now = LocalDateTime.now();
        BillsOrderDetails ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        BillsOrderDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public BillsOrderDetailsDto update(Integer id, BillsOrderDetailsDto dto) {
        BillsOrderDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BillsOrderDetails not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        BillsOrderDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        BillsOrderDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BillsOrderDetails not found: " + id));
        repository.delete(ent);
    }
}
