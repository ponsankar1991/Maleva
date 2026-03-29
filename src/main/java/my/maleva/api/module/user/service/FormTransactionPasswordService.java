package my.maleva.api.module.user.service;

import my.maleva.api.module.user.dto.FormTransactionPasswordDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.user.mapper.FormTransactionPasswordMapper;
import my.maleva.api.module.user.entity.FormTransactionPassword;
import my.maleva.api.module.user.repository.FormTransactionPasswordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FormTransactionPasswordService {

    private final FormTransactionPasswordRepository repository;
    private final FormTransactionPasswordMapper mapper;

    public FormTransactionPasswordService(FormTransactionPasswordRepository repository, FormTransactionPasswordMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<FormTransactionPasswordDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public FormTransactionPasswordDto getById(Integer id) {
        FormTransactionPassword ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("FormTransactionPassword not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public FormTransactionPasswordDto create(FormTransactionPasswordDto dto) {
        LocalDateTime now = LocalDateTime.now();
        FormTransactionPassword ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        FormTransactionPassword saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public FormTransactionPasswordDto update(Integer id, FormTransactionPasswordDto dto) {
        FormTransactionPassword ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("FormTransactionPassword not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        FormTransactionPassword saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        FormTransactionPassword ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("FormTransactionPassword not found: " + id));
        repository.delete(ent);
    }
}
