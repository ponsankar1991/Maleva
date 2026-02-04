package my.maleva.api.service;

import my.maleva.api.dto.BillDetailsDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.BillDetailsMapper;
import my.maleva.api.model.BillDetails;
import my.maleva.api.repo.BillDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillDetailsService {

    private final BillDetailsRepository repository;
    private final BillDetailsMapper mapper;

    public BillDetailsService(BillDetailsRepository repository, BillDetailsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<BillDetailsDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public BillDetailsDto getById(Integer id) {
        BillDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BillDetails not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public BillDetailsDto create(BillDetailsDto dto) {
        LocalDateTime now = LocalDateTime.now();
        BillDetails ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        BillDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public BillDetailsDto update(Integer id, BillDetailsDto dto) {
        BillDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BillDetails not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        BillDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        BillDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BillDetails not found: " + id));
        repository.delete(ent);
    }
}
