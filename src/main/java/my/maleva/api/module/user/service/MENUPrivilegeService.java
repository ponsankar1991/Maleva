package my.maleva.api.module.user.service;

import my.maleva.api.module.user.dto.MENUPrivilegeDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.user.mapper.MENUPrivilegeMapper;
import my.maleva.api.module.user.entity.MENUPrivilege;
import my.maleva.api.module.user.repository.MENUPrivilegeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MENUPrivilegeService {

    private final MENUPrivilegeRepository repository;
    private final MENUPrivilegeMapper mapper;

    public MENUPrivilegeService(MENUPrivilegeRepository repository, MENUPrivilegeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<MENUPrivilegeDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public MENUPrivilegeDto getById(Integer id) {
        MENUPrivilege ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MENUPrivilege not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public MENUPrivilegeDto create(MENUPrivilegeDto dto) {
        LocalDateTime now = LocalDateTime.now();
        MENUPrivilege ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        MENUPrivilege saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public MENUPrivilegeDto update(Integer id, MENUPrivilegeDto dto) {
        MENUPrivilege ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MENUPrivilege not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        MENUPrivilege saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        MENUPrivilege ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MENUPrivilege not found: " + id));
        repository.delete(ent);
    }
}
