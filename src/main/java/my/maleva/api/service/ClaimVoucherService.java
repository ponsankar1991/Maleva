package my.maleva.api.service;

import my.maleva.api.dto.ClaimVoucherDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.ClaimVoucherMapper;
import my.maleva.api.model.ClaimVoucher;
import my.maleva.api.repo.ClaimVoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimVoucherService {

    private final ClaimVoucherRepository repository;
    private final ClaimVoucherMapper mapper;

    public ClaimVoucherService(ClaimVoucherRepository repository, ClaimVoucherMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ClaimVoucherDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public ClaimVoucherDto getById(Integer id) {
        ClaimVoucher ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ClaimVoucher not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public ClaimVoucherDto create(ClaimVoucherDto dto) {
        LocalDateTime now = LocalDateTime.now();
        ClaimVoucher ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        ClaimVoucher saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public ClaimVoucherDto update(Integer id, ClaimVoucherDto dto) {
        ClaimVoucher ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ClaimVoucher not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        ClaimVoucher saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        ClaimVoucher ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ClaimVoucher not found: " + id));
        repository.delete(ent);
    }
}
