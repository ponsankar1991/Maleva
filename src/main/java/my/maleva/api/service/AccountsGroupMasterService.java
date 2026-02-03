package my.maleva.api.service;

import my.maleva.api.dto.AccountsGroupMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.AccountsGroupMasterMapper;
import my.maleva.api.model.AccountsGroupMaster;
import my.maleva.api.repo.AccountsGroupMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountsGroupMasterService {

    private final AccountsGroupMasterRepository repository;
    private final AccountsGroupMasterMapper mapper;

    public AccountsGroupMasterService(AccountsGroupMasterRepository repository, AccountsGroupMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<AccountsGroupMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public AccountsGroupMasterDto getById(Integer id) {
        AccountsGroupMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("AccountsGroupMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public AccountsGroupMasterDto create(AccountsGroupMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        AccountsGroupMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        AccountsGroupMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public AccountsGroupMasterDto update(Integer id, AccountsGroupMasterDto dto) {
        AccountsGroupMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("AccountsGroupMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        AccountsGroupMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        AccountsGroupMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("AccountsGroupMaster not found: " + id));
        repository.delete(ent);
    }
}
