package my.maleva.api.service;

import my.maleva.api.dto.GLAccountsDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.GLAccountsMapper;
import my.maleva.api.model.GLAccounts;
import my.maleva.api.repo.GLAccountsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GLAccountsService {

    private final GLAccountsRepository repository;
    private final GLAccountsMapper mapper;

    public GLAccountsService(GLAccountsRepository repository, GLAccountsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<GLAccountsDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public GLAccountsDto getById(UUID id) {
        GLAccounts ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("GLAccounts not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public GLAccountsDto create(GLAccountsDto dto) {
        GLAccounts ent = mapper.toEntity(dto);
        GLAccounts saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public GLAccountsDto update(UUID id, GLAccountsDto dto) {
        GLAccounts ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("GLAccounts not found: " + id));
        mapper.updateFromDto(dto, ent);
        GLAccounts saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        GLAccounts ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("GLAccounts not found: " + id));
        repository.delete(ent);
    }
}
